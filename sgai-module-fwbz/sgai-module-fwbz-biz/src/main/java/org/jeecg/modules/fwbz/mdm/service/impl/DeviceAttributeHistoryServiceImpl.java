package org.jeecg.modules.fwbz.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.jeecg.modules.fwbz.mdm.dto.DeviceAttributeHistoryQueryDto;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttributeHistory;
import org.jeecg.modules.fwbz.mdm.mapper.DeviceAttributeHistoryMapper;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeHistoryService;
import org.jeecg.modules.fwbz.main.service.IBusinessConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DeviceAttributeHistoryServiceImpl extends ServiceImpl<DeviceAttributeHistoryMapper, DeviceAttributeHistory> implements IDeviceAttributeHistoryService {

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
        if(attributes == null || attributes.isEmpty()){
            return;
        }
        // 获取需要保存的设备id
        List<Long> deviceIds = businessConfigService.getListByKey("device_attribute_history", Long.class);
        if(deviceIds == null || deviceIds.isEmpty()){
            return;
        }
        for (DeviceAttribute attribute : attributes) {
            if (!deviceIds.contains(attribute.getDeviceId())) {
                continue;
            }
            DeviceAttributeHistory history = new DeviceAttributeHistory();
            history.setAttributeId(attribute.getId());
            history.setDeviceId(attribute.getDeviceId());
            history.setCollectionTime(attribute.getGatherTime());
            history.setValue(attribute.getValue());
            save(history);
        }
    }

    @Override
    public void saveAttributeHistory(Collection<DeviceAttribute> attributes, LocalDateTime dataTime) {
        if (attributes == null || attributes.isEmpty() || dataTime == null) {
            return;
        }
        List<Long> attributeIds = attributes.stream()
                .map(DeviceAttribute::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (attributeIds.isEmpty()) {
            return;
        }
        // 查询同一时间槽位已存在的历史记录（有则更新，无则新增）
        Map<Long, DeviceAttributeHistory> existMap = list(new LambdaQueryWrapper<DeviceAttributeHistory>()
                        .eq(DeviceAttributeHistory::getCollectionTime, dataTime)
                        .in(DeviceAttributeHistory::getAttributeId, attributeIds))
                .stream()
                .collect(Collectors.toMap(DeviceAttributeHistory::getAttributeId, Function.identity(), (a, b) -> a));

        List<DeviceAttributeHistory> updateList = new ArrayList<>();
        List<DeviceAttributeHistory> insertList = new ArrayList<>();
        for (DeviceAttribute attribute : attributes) {
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
            saveBatch(insertList);
        }
    }
}
