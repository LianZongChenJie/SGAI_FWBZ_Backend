package org.jeecg.modules.fwbz.coldSourceSystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.enums.PsErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableTagidInfo;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.TableColdSourceHistoryMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.TableTagidInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 冷源历史数据定时保存服务
 * <p>
 * 数据获取方式参考楼控 {@code BuildingControlServerService#realReadList}：
 * 通过 {@link ColdSourceServerService#connect()} 的 pSpace 客户端按测点ID(tagId) 调用
 * {@code realRead} 读取点位实时值（返回 PsResult&lt;PsData&gt;，成功判断 code 为 PSRET_OK）。
 * <p>
 * 定时规则：每整十分钟执行一次（如 08:00、08:10、08:20），
 * 启动时先对齐到下一个整十分钟，之后按固定 10 分钟间隔执行：
 * 1. 从 table_tagid_info 表查询 is_save='1' 的采集点ID(tagId)；
 * 2. 逐个调用 realRead 读取实时值；
 * 3. 组装后批量写入 table_cold_source_history 表（tag_id / value / value_type / data_time）。
 */
@Slf4j
@Service
public class SaveHisttoryService {

    /** 定时任务执行间隔（分钟），整十分钟一次 */
    private static final long SAVE_INTERVAL_MINUTES = 10L;

    /** 单次批量插入的条数 */
    private static final int BATCH_SIZE = 500;

    @Autowired
    private TableTagidInfoMapper tableTagidInfoMapper;

    @Autowired
    private TableColdSourceHistoryMapper tableColdSourceHistoryMapper;

    @Autowired
    private ColdSourceServerService coldSourceServerService;

    /** 历史数据保存定时任务：整十分钟执行一次 */
    private ScheduledExecutorService historyScheduler;

    @PostConstruct
    public void init() {
        historyScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cold-source-history-save");
            t.setDaemon(true);
            return t;
        });
        // 初始延迟对齐到下一个整十分钟（分钟能被 10 整除且秒/纳秒为 0），
        // 如 08:03 -> 08:10，08:10:30 -> 08:20；之后 scheduleAtFixedRate 每 10 分钟一次
        long initialDelay = nextAlignedDelayMillis();
        historyScheduler.scheduleAtFixedRate(this::saveHistory, initialDelay, SAVE_INTERVAL_MINUTES, TimeUnit.MINUTES);
        log.info("冷源历史数据定时保存任务已启动: 每 {} 分钟执行一次（整十分钟开始，初始延迟 {} ms）",
                SAVE_INTERVAL_MINUTES, initialDelay);
    }

    /**
     * 计算到下一个整十分钟的延迟毫秒数
     */
    private long nextAlignedDelayMillis() {
        LocalDateTime now = LocalDateTime.now();
        int slotMinute = (now.getMinute() / 10) * 10;
        LocalDateTime next = now.withMinute(slotMinute).withSecond(0).withNano(0)
                .plusMinutes(SAVE_INTERVAL_MINUTES);
        return Duration.between(now, next).toMillis();
    }

    /**
     * 定时保存冷源历史数据（整十分钟执行）：
     * 从 table_tagid_info 取 is_save='1' 的采集点，逐个 realRead 读取值后批量写入 table_cold_source_history。
     * 单个采集点读取失败仅记录告警，不影响其他采集点。
     */
    public void saveHistory() {
        // 对齐到当前整十分钟槽位（如 08:00:05 执行 -> data_time = 08:00:00）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dataTime = now.withMinute((now.getMinute() / 10) * 10).withSecond(0).withNano(0);
        // 1. 查询需要存储的采集点（is_save=1）
        List<TableTagidInfo> tagInfos = tableTagidInfoMapper.selectList(
                new LambdaQueryWrapper<TableTagidInfo>().eq(TableTagidInfo::getIsSave, "1"));
        if (tagInfos == null || tagInfos.isEmpty()) {
            log.warn("table_tagid_info 中没有 is_save=1 的采集点，跳过冷源历史数据保存");
            return;
        }
        log.info("冷源历史数据保存开始: 采集点数={}, 记录时间={}", tagInfos.size(), dataTime);

        // 2. 逐个读取实时值并组装历史记录
        List<TableColdSourceHistory> historyList = new ArrayList<>();
        for (TableTagidInfo info : tagInfos) {
            Long tagId = info.getTagId();
            if (tagId == null) {
                continue;
            }
            try {
                PsResult<PsData> result = coldSourceServerService.connect().realRead(tagId);
                if (!Objects.equals(result.getCode(), PsErrorCodeEnum.PSRET_OK)
                        || result.getData() == null || result.getData().isEmpty()) {
                    log.warn("冷源历史数据读取失败: tagId={}, code={}", tagId, result.getCode());
                    continue;
                }
                PsData data = result.getData().get(0);
                TableColdSourceHistory history = new TableColdSourceHistory();
                history.setTagId(tagId);
                history.setValue(convertValue(data.getValue()));
                history.setValueType(data.getDataType() == null ? null : data.getDataType().name());
                history.setDataTime(dataTime);
                historyList.add(history);
            } catch (Exception e) {
                log.warn("冷源历史数据读取异常: tagId={}", tagId, e);
            }
        }

        // 3. 批量写入 table_cold_source_history
        if (historyList.isEmpty()) {
            log.warn("冷源历史数据读取成功数为 0，无数据写入");
            return;
        }
        int saved = 0;
        for (int i = 0; i < historyList.size(); i += BATCH_SIZE) {
            List<TableColdSourceHistory> batch = historyList.subList(i,
                    Math.min(i + BATCH_SIZE, historyList.size()));
            for (TableColdSourceHistory history : batch) {
                tableColdSourceHistoryMapper.insert(history);
            }
            saved += batch.size();
        }
        log.info("冷源历史数据保存完成: 采集点={}, 读取成功={}, 写入={}",
                tagInfos.size(), historyList.size(), saved);
    }

    /**
     * 值转换：Boolean -> "1"/"0"，整数不带小数，BigDecimal 去尾零，其余 toString
     */
    private static String convertValue(Object v) {
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
