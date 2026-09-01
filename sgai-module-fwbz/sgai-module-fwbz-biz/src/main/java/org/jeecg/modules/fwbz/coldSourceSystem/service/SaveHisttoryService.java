package org.jeecg.modules.fwbz.coldSourceSystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsDataWithTagId;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsSubRealData;
import com.sunwayland.pspace.enums.PsDataTypeEnum;
import com.sunwayland.pspace.enums.PsErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.config.ColdSourceProperties;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableTagidInfo;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.TableColdSourceHistoryMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.TableTagidInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 冷源历史数据定时保存服务
 * <p>
 * 数据获取方式参考楼控 {@code BuildingControlServerService#realReadList}：
 * 通过 {@link ColdSourceServerService#connect()} 的 pSpace 客户端按测点ID(tagId) 调用
 * {@code realRead} 读取点位实时值（返回 PsResult&lt;PsData&gt;，成功判断 code 为 PSRET_OK）。
 * <p>
 * 定时规则：每 15 分钟整点执行一次（如 08:00:00、08:15:00、08:30:00），
 * 由 Spring 调度器按 cron 触发（项目已启用 @EnableScheduling）：
 * 1. 从 table_tagid_info 表查询 is_save='1' 的采集点ID(tagId)；
 * 2. 逐个调用 realRead 读取实时值；
 * 3. 组装后批量写入 table_cold_source_history 表（tag_id / value / value_type / data_time）。
 */
@Slf4j
@Service
public class SaveHisttoryService {

    /** 单次批量插入的条数 */
    private static final int BATCH_SIZE = 500;

    @Autowired
    private TableTagidInfoMapper tableTagidInfoMapper;

    @Autowired
    private TableColdSourceHistoryMapper tableColdSourceHistoryMapper;

    @Autowired
    private ColdSourceServerService coldSourceServerService;

    @Autowired
    private ColdSourceRealPushService coldSourceRealPushService;

    @Autowired
    private ColdSourceProperties coldSourceProperties;

    /**
     * 定时保存冷源历史数据（每 15 分钟整点执行，cron 秒=0 分钟=0/15）。
     * 从 table_tagid_info 取 is_save='1' 的采集点，逐个读取最新值后批量写入 table_cold_source_history。
     * 单个采集点读取失败仅记录告警，不影响其他采集点；整体异常由方法内兜底，
     * 不抛出到 Spring 调度器（避免影响后续周期）。
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void saveHistory() {
        // 对齐到当前整十五分钟槽位（如 08:00:05 执行 -> data_time = 08:00:00，08:16:59 -> 08:15:00）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dataTime = now.withMinute((now.getMinute() / 15) * 15).withSecond(0).withNano(0);
        log.info("冷源历史数据保存开始:记录时间={}", dataTime);
        // 1. 判断该槽位是否已有历史数据，已有则跳过本次存储（避免重复入库）
        try {
            Long exists = tableColdSourceHistoryMapper.selectCount(
                    new LambdaQueryWrapper<TableColdSourceHistory>().eq(TableColdSourceHistory::getDataTime, dataTime));
            if (exists != null && exists > 0) {
                log.info("冷源历史数据槽位 {} 已存在 {} 条数据，跳过本次保存", dataTime, exists);
                return;
            }
        } catch (Exception e) {
            log.error("冷源历史数据保存失败: 查询 table_cold_source_history 槽位 {} 异常", dataTime, e);
            return;
        }

        // 2. 查询需要存储的采集点（is_save=1）
        List<TableTagidInfo> tagInfos;
        try {
            tagInfos = tableTagidInfoMapper.selectList(
                    new LambdaQueryWrapper<TableTagidInfo>().eq(TableTagidInfo::getIsSave, "1"));
        } catch (Exception e) {
            log.error("冷源历史数据保存失败: 查询 table_tagid_info(is_save=1) 异常", e);
            return;
        }
        if (tagInfos == null || tagInfos.isEmpty()) {
            log.warn("table_tagid_info 中没有 is_save=1 的采集点，跳过冷源历史数据保存");
            return;
        }
        log.info("冷源历史数据保存开始: 采集点数={}, 记录时间={}", tagInfos.size(), dataTime);

        // 3. 逐个读取实时值并组装历史记录
        //    优先取 ColdSourceRealPushService 订阅缓存（mock 数据源/订阅回调维护的最新值，
        //    避免 mock 模式下 connect() 返回 null 无法读点），无缓存时回退 realRead 读点
        List<TableColdSourceHistory> historyList = new ArrayList<>();
        for (TableTagidInfo info : tagInfos) {
            Long tagId = info.getTagId();
            if (tagId == null) {
                continue;
            }
            try {
                TagValue tagValue = readLatestValue(tagId);
                if (tagValue == null) {
                    log.warn("冷源历史数据读取失败(无数据): tagId={}", tagId);
                    continue;
                }
                TableColdSourceHistory history = new TableColdSourceHistory();
                history.setTagId(tagId);
                history.setValue(convertValue(tagValue.value));
                history.setValueType(tagValue.dataType == null ? null : tagValue.dataType.name());
                history.setDataTime(dataTime);
                historyList.add(history);
            } catch (Exception e) {
                log.warn("冷源历史数据读取异常: tagId={}", tagId, e);
            }
        }

        // 4. 批量写入 table_cold_source_history
        if (historyList.isEmpty()) {
            log.warn("冷源历史数据读取成功数为 0，无数据写入");
            return;
        }
        int saved = 0;
        try {
            for (int i = 0; i < historyList.size(); i += BATCH_SIZE) {
                List<TableColdSourceHistory> batch = historyList.subList(i,
                        Math.min(i + BATCH_SIZE, historyList.size()));
                for (TableColdSourceHistory history : batch) {
                    tableColdSourceHistoryMapper.insert(history);
                }
                saved += batch.size();
            }
        } catch (Exception e) {
            log.error("冷源历史数据保存失败: 写入 table_cold_source_history 异常（已写入 {} 条）", saved, e);
            return;
        }
        log.info("冷源历史数据保存完成: 采集点={}, 读取成功={}, 写入={}",
                tagInfos.size(), historyList.size(), saved);
    }

    /**
     * 读取测点最新值：
     * <ol>
     *   <li>优先取订阅缓存 {@link ColdSourceRealPushService#getLatestRealData}（mock/真实环境均有）；</li>
     *   <li>缓存无值且非 mock 模式下，回退 {@code realRead} 读点；</li>
     *   <li>mock 模式下 connect() 返回 null、realRead 不可用，返回 null（读取失败）。</li>
     * </ol>
     *
     * @param tagId 测点ID
     * @return 测点值及数据类型；读取失败返回 null
     */
    private TagValue readLatestValue(Long tagId) {
        PsSubRealData cached = coldSourceRealPushService.getLatestRealData(tagId);
        if (cached != null && cached.getValue() != null) {
            return new TagValue(cached.getValue(), cached.getDataType());
        }
        if (!coldSourceProperties.isMock()) {
            PsResult<PsData> result = coldSourceServerService.connect().realRead(tagId);
            if (Objects.equals(result.getCode(), PsErrorCodeEnum.PSRET_OK)
                    && result.getData() != null && !result.getData().isEmpty()) {
                PsData data = result.getData().get(0);
                return new TagValue(data.getValue(), data.getDataType());
            }
            log.warn("冷源历史数据 realRead 读取失败: tagId={}, code={}", tagId, result.getCode());
        } else {
            log.warn("冷源历史数据无缓存且处于 mock 模式(connect 不可用), 无法读取: tagId={}", tagId);
        }
        return null;
    }
    /**
     * 批量读取点位真实值：
     * <ol>
     *   <li>优先取订阅缓存 {@link ColdSourceRealPushService#getLatestRealData}（mock/真实环境均有）；</li>
     *   <li>缓存无值且非 mock 模式下，回退 {@code realRead} 读点；</li>
     *   <li>mock 模式下 connect() 返回 null、realRead 不可用，返回 null（读取失败）。</li>
     * </ol>
     *
     * @param tagIds 测点ID
     * @return 测点值及数据类型；读取失败返回 null
     */
    public List<PsDataWithTagId> readLatestValue(List<Long> tagIds) {
        if (!coldSourceProperties.isMock()) {
            PsResult<PsDataWithTagId> result = coldSourceServerService.connect().realReadListV2(tagIds);
            if (Objects.equals(result.getCode(), PsErrorCodeEnum.PSRET_OK)
                    && result.getData() != null && !result.getData().isEmpty()) {
                log.info("冷源历史数据 realRead 读取成功: tagId={}, data={}", tagIds, result.getData());
                return result.getData();
            }
            log.warn("冷源历史数据 realRead 读取失败: tagId={}, code={}", tagIds, result.getCode());
        } else {
            log.warn("冷源历史数据无缓存且处于 mock 模式(connect 不可用), 无法读取: tagId={}", tagIds);
        }
        return null;
    }


    /** 读取到的测点值及数据类型 */
    private static class TagValue {
        private final Object value;
        private final PsDataTypeEnum dataType;

        TagValue(Object value, PsDataTypeEnum dataType) {
            this.value = value;
            this.dataType = dataType;
        }
    }

    /**
     * 值转换：Boolean -> "1"/"0"，整数不带小数，BigDecimal 去尾零，其余 toString
     */
    public static String convertValue(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean) {
            return (Boolean) v ? "1" : "0";
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).stripTrailingZeros().toPlainString();
        }
        if (v instanceof Long || v instanceof Integer || v instanceof Short || v instanceof Byte) {
            return v.toString();
        }
        if (v instanceof Double || v instanceof Float) {
            double d = ((Number) v).doubleValue();
            if (d == Math.rint(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        return v.toString();
    }
}
