package org.jeecg.modules.fwbz.coldSourceSystem.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunwayland.pspace.entity.PsDataWithTagId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceDevice;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceDeviceAttribute;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.ColdSourceDeviceAttributeMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.ColdSourceDeviceMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.service.SaveHisttoryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 冷源设备属性采集定时任务
 * <p>每 15 分钟执行一次：读取 cold_source_device_attribute 表所有属性(tagid)的真实值，
 * 对齐整十五分钟时间槽后：</p>
 * <ol>
 *   <li>更新属性表 value(采集值)、gather_time(采集时间)；</li>
 *   <li>按设备维度更新 cold_source_device 最后采集时间(last_collect_time)与在线状态(online_status)：
 *       有属性采集成功 -> 在线(1) 并刷新采集时间；全部失败 -> 离线(0)。</li>
 * </ol>
 * <p>整体异常由方法内兜底，不抛出到 Spring 调度器（避免影响后续周期），单个属性/设备更新失败仅记告警。</p>
 *
 * @author fwbz
 */
@Slf4j
@Component
@AllArgsConstructor
public class ColdSourceDeviceCollectJob {

    /** 在线状态: 在线 */
    private static final int ONLINE = 1;
    /** 在线状态: 离线 */
    private static final int OFFLINE = 0;

    private final ColdSourceDeviceAttributeMapper coldSourceDeviceAttributeMapper;
    private final ColdSourceDeviceMapper coldSourceDeviceMapper;
    private final SaveHisttoryService saveHisttoryService;

    /**
     * 每 15 分钟执行一次冷源设备属性采集（cron 秒=0 分钟=0/15）
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void collectDeviceAttribute() {
        // 对齐到当前整十五分钟槽位（如 08:00:05 执行 -> 采集时间=08:00:00，08:16:59 -> 08:15:00）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dataTime = now.withMinute((now.getMinute() / 15) * 15).withSecond(0).withNano(0);
        log.info("冷源设备属性采集开始: 采集时间={}", dataTime);
        try {
            // 1. 查询所有 tagid 非空的属性（按 sort_order, id 排序），并建立 tagid -> deviceId 映射
            List<ColdSourceDeviceAttribute> attributes = coldSourceDeviceAttributeMapper.selectList(
                    new LambdaQueryWrapper<ColdSourceDeviceAttribute>()
                            .isNotNull(ColdSourceDeviceAttribute::getTagid)
                            .orderByAsc(ColdSourceDeviceAttribute::getSortOrder)
                            .orderByAsc(ColdSourceDeviceAttribute::getId));
            if (attributes == null || attributes.isEmpty()) {
                log.info("冷源设备属性采集完成: cold_source_device_attribute 无 tagid 属性");
                return;
            }
            Map<Long, Long> tagIdToDeviceId = new HashMap<>(attributes.size());
            for (ColdSourceDeviceAttribute attr : attributes) {
                if (attr.getDeviceId() != null) {
                    tagIdToDeviceId.put(attr.getTagid(), attr.getDeviceId());
                }
            }

            // 2. 批量读取真实值（realReadListV2，返回带 tagid 的结果；mock 模式返回 null）
            List<Long> tagIds = new java.util.ArrayList<>(attributes.size());
            for (ColdSourceDeviceAttribute attr : attributes) {
                tagIds.add(attr.getTagid().longValue());
            }
            List<PsDataWithTagId> latestList = saveHisttoryService.readLatestValue(tagIds);
            if (latestList == null || latestList.isEmpty()) {
                log.warn("冷源设备属性采集: 批量读取无返回数据(可能 mock 模式或读取失败), 采集时间={}", dataTime);
                return;
            }

            // 3. 按 tagid 更新属性 value/gather_time，并按设备统计读取成功数
            Map<Long, Integer> deviceSuccess = new HashMap<>();
            int updateAttr = 0;
            for (PsDataWithTagId data : latestList) {
                if (data == null || data.getTagId() == null || data.getValue() == null) {
                    continue;
                }
                try {
                    ColdSourceDeviceAttribute update = new ColdSourceDeviceAttribute();
                    update.setTagid(data.getTagId());
                    update.setValue(SaveHisttoryService.convertValue(data.getValue()));
                    update.setGatherTime(dataTime);
                    coldSourceDeviceAttributeMapper.updateByTagId(update);
                    updateAttr++;
                    Long deviceId = tagIdToDeviceId.get(data.getTagId());
                    if (deviceId != null) {
                        deviceSuccess.merge(deviceId, 1, Integer::sum);
                    }
                } catch (Exception e) {
                    log.warn("冷源设备属性采集: 更新属性失败, tagid={}", data.getTagId(), e);
                }
            }
            log.info("冷源设备属性采集: 属性数={}, 读取成功={}, 更新成功={}", attributes.size(), latestList.size(), updateAttr);

            // 4. 更新设备最后采集时间与在线状态
            Set<Long> involvedDeviceIds = new HashSet<>(tagIdToDeviceId.values());
            int updateDevice = 0;
            for (Long deviceId : involvedDeviceIds) {
                try {
                    boolean success = deviceSuccess.getOrDefault(deviceId, 0) > 0;
                    ColdSourceDevice update = new ColdSourceDevice();
                    update.setId(deviceId);
                    update.setOnlineStatus(success ? ONLINE : OFFLINE);
                    if (success) {
                        update.setLastCollectTime(dataTime);
                    }
                    coldSourceDeviceMapper.updateById(update);
                    updateDevice++;
                } catch (Exception e) {
                    log.warn("冷源设备属性采集: 更新设备状态失败, deviceId={}", deviceId, e);
                }
            }
            log.info("冷源设备属性采集完成: 采集时间={}, 更新设备数={}", dataTime, updateDevice);
        } catch (Exception e) {
            log.error("冷源设备属性采集定时任务执行异常", e);
        }
    }
}
