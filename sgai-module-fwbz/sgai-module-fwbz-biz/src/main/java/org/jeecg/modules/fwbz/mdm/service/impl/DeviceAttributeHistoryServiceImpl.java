package org.jeecg.modules.fwbz.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.mdm.dto.DeviceAttributeHistoryQueryDto;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttributeHistory;
import org.jeecg.modules.fwbz.mdm.mapper.DeviceAttributeHistoryMapper;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeHistoryService;
import org.jeecg.modules.fwbz.main.service.IBusinessConfigService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DeviceAttributeHistoryServiceImpl extends ServiceImpl<DeviceAttributeHistoryMapper, DeviceAttributeHistory> implements IDeviceAttributeHistoryService {

    /** 单批处理条数：历史保存按批查询已存在记录并提交，避免大批量集合驻留内存导致 OOM */
    private static final int BATCH_SIZE = 500;

    private final IBusinessConfigService businessConfigService;
    @Override
    public List<DeviceAttributeHistory> listByAttributeId(DeviceAttributeHistoryQueryDto param) {
        if (param.getDeviceAttributeId() == null) {
            return Collections.emptyList();
        }
        return super.list(new LambdaQueryWrapper<DeviceAttributeHistory>()
                .eq(DeviceAttributeHistory::getAttributeId, param.getDeviceAttributeId())
                .between(DeviceAttributeHistory::getCollectionTime, param.getStartTime(), param.getEndTime())
                .orderByDesc(DeviceAttributeHistory::getCollectionTime)
        );
    }
    @Override
    public List<DeviceAttributeHistory> listByAttributeIds(DeviceAttributeHistoryQueryDto param) {
        if (param.getDeviceAttributeIds() == null) {
            return Collections.emptyList();
        }
        return super.list(new LambdaQueryWrapper<DeviceAttributeHistory>()
                .in(DeviceAttributeHistory::getAttributeId, param.getDeviceAttributeIds())
                .between(DeviceAttributeHistory::getCollectionTime, param.getStartTime(), param.getEndTime())
                .orderByDesc(DeviceAttributeHistory::getCollectionTime));
    }

    @Override
    public void saveAttributeHistory(Collection<DeviceAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        // 获取需要保存的设备id
        List<Long> deviceIds = businessConfigService.getListByKey("device_attribute_history", Long.class);
        if (deviceIds == null || deviceIds.isEmpty()) {
            return;
        }
        // 与 MQTT/楼控链路统一：采集时间对齐到整十五分钟槽位，按槽位分批 upsert（有则更新，无则新增）。
        // 原实现逐条 INSERT 且未对齐槽位，会导致：
        //  1) 同一属性同时存在"原始时间记录+槽位记录"，历史数据翻倍膨胀；
        //  2) 大批量逐条 INSERT 放大 DB 往返，且与 MQTT/楼控并发争写同一槽位时产生竞态冲突。
        Map<LocalDateTime, List<DeviceAttribute>> slotGroup = new LinkedHashMap<>();
        for (DeviceAttribute attribute : attributes) {
            if (attribute.getId() == null || attribute.getDeviceId() == null
                    || attribute.getGatherTime() == null
                    || !deviceIds.contains(attribute.getDeviceId())) {
                continue;
            }
            slotGroup.computeIfAbsent(alignTo15MinuteSlot(attribute.getGatherTime()), k -> new ArrayList<>()).add(attribute);
        }
        if (slotGroup.isEmpty()) {
            return;
        }
        for (Map.Entry<LocalDateTime, List<DeviceAttribute>> entry : slotGroup.entrySet()) {
            saveAttributeHistory(entry.getValue(), entry.getKey());
        }
    }

    /**
     * 将时间对齐到当前整十五分钟槽位：分钟向下取整到 15 的倍数，秒与纳秒清零
     * 如 08:00:05 -> 08:00:00，08:16:59 -> 08:15:00（与 MQTT/楼控对齐逻辑一致）
     *
     * @param time 原始时间
     * @return 对齐后的时间；入参为 null 时返回 null
     */
    private LocalDateTime alignTo15MinuteSlot(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        int slotMinute = (time.getMinute() / 15) * 15;
        return time.withMinute(slotMinute).withSecond(0).withNano(0);
    }

    @Override
    public void saveAttributeHistory(Collection<DeviceAttribute> attributes, LocalDateTime dataTime) {
        if (attributes == null || attributes.isEmpty() || dataTime == null) {
            return;
        }
        // 流式分批：逐批查询已存在记录并提交，避免全量集合驻留内存导致 OOM
        List<DeviceAttribute> list = attributes instanceof List
                ? (List<DeviceAttribute>) attributes
                : new ArrayList<>(attributes);
        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            saveAttributeHistoryBatch(list.subList(i, Math.min(i + BATCH_SIZE, list.size())), dataTime);
        }
    }

    /**
     * 单批历史保存：查询该批同一时间槽位已存在的历史记录（有则更新，无则新增）
     *
     * @param batch    本批待保存的属性集合（不超过 BATCH_SIZE）
     * @param dataTime 采集时间（已对齐到整十五分钟槽位）
     */
    private void saveAttributeHistoryBatch(List<DeviceAttribute> batch, LocalDateTime dataTime) {
        List<Long> attributeIds = batch.stream()
                .map(DeviceAttribute::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (attributeIds.isEmpty()) {
            return;
        }
        // 查询该批同一时间槽位已存在的历史记录（有则更新，无则新增）
        Map<Long, DeviceAttributeHistory> existMap = list(new LambdaQueryWrapper<DeviceAttributeHistory>()
                        .eq(DeviceAttributeHistory::getCollectionTime, dataTime)
                        .in(DeviceAttributeHistory::getAttributeId, attributeIds))
                .stream()
                .collect(Collectors.toMap(DeviceAttributeHistory::getAttributeId, Function.identity(), (a, b) -> a));

        List<DeviceAttributeHistory> updateList = new ArrayList<>();
        List<DeviceAttributeHistory> insertList = new ArrayList<>();
        for (DeviceAttribute attribute : batch) {
            if (attribute.getId() == null || attribute.getDeviceId() == null) {
                continue;
            }
            DeviceAttributeHistory exist = existMap.get(attribute.getId());
            if (exist != null) {
                exist.setDeviceId(attribute.getDeviceId());
                exist.setValue(attribute.getValue());
                updateList.add(exist);
            } else {
                DeviceAttributeHistory history = new DeviceAttributeHistory();
                history.setAttributeId(attribute.getId());
                history.setDeviceId(attribute.getDeviceId());
                history.setCollectionTime(dataTime);
                history.setValue(attribute.getValue());
                insertList.add(history);
            }
        }
        if (!updateList.isEmpty()) {
            updateBatchById(updateList);
        }
        if (!insertList.isEmpty()) {
            try {
                saveBatch(insertList);
            } catch (DuplicateKeyException e) {
                // 并发竞态：MQTT/楼控等链路已先插入同一 (attribute_id, collection_time) 槽位记录，
                // 降级为按槽位更新，避免批量归档中断或产生重复数据
                log.warn("设备属性历史并发插入冲突，降级为按槽位更新: 条数={}", insertList.size());
                for (DeviceAttributeHistory history : insertList) {
                    update(new LambdaUpdateWrapper<DeviceAttributeHistory>()
                            .eq(DeviceAttributeHistory::getAttributeId, history.getAttributeId())
                            .eq(DeviceAttributeHistory::getCollectionTime, history.getCollectionTime())
                            .set(DeviceAttributeHistory::getDeviceId, history.getDeviceId())
                            .set(DeviceAttributeHistory::getValue, history.getValue()));
                }
            }
        }
    }
}
