package org.jeecg.modules.fwbz.collectionPitStatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.modules.fwbz.collectionPitStatistics.dto.CollectionPitStatisticsDto;
import org.jeecg.modules.fwbz.collectionPitStatistics.service.ICollectionPitStatisticsService;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 集水坑统计 Service 实现
 */
@Service
@Slf4j
@AllArgsConstructor
public class CollectionPitStatisticsServiceImpl implements ICollectionPitStatisticsService {

    /**
     * 集水坑设备类别id
     */
    private static final Long COLLECTION_PIT_CATEGORY_ID = 34L;

    /**
     * 液位告警属性名
     */
    private static final String LIQUID_LEVEL_ALARM_ATTRIBUTE = "报警液位信号";

    /**
     * 故障属性关键字（属性名同时包含"水泵"与"故障"，如"水泵1故障状态"、"水泵2故障报警"）
     */
    private static final String PUMP_FAULT_KEYWORD = "故障";
    private static final String PUMP_NAME_KEYWORD = "水泵";

    private final IDeviceService deviceService;
    private final IDeviceAttributeService deviceAttributeService;

    @Override
    public CollectionPitStatisticsDto statistics() {
        CollectionPitStatisticsDto dto = new CollectionPitStatisticsDto();

        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, COLLECTION_PIT_CATEGORY_ID));

        if (CollectionUtils.isEmpty(list)) {
            dto.setLiquidLevelAlarmCount(0L);
            dto.setFaultDeviceCount(0L);
            return dto;
        }

        //液位告警设备id集合
        Set<Long> liquidLevelAlarmDeviceIds = new HashSet<>();
        //故障设备id集合（水泵1故障状态/水泵2故障报警任一为1即故障，同一设备去重）
        Set<Long> faultDeviceIds = new HashSet<>();

        //分批次查询属性，防止in语句过大
        int batch = list.size() / 1000;
        for (int j = 0; j <= batch; j++) {
            ArrayList<Long> deviceIds = new ArrayList<>();
            for (int k = 0; k < 1000; k++) {
                int index = j * 1000 + k;
                if (index >= list.size()) {
                    break;
                }
                deviceIds.add(list.get(index).getId());
            }
            if (CollectionUtils.isEmpty(deviceIds)) {
                continue;
            }
            List<DeviceAttribute> attributes = deviceAttributeService.findByDeviceIds(deviceIds);
            for (DeviceAttribute attribute : attributes) {
                if (attribute.getDeviceId() == null || attribute.getValue() == null) {
                    continue;
                }
                //液位告警：报警液位信号为1
                if (LIQUID_LEVEL_ALARM_ATTRIBUTE.equals(attribute.getAttributeName())
                        && isSignalOn(attribute.getValue())) {
                    liquidLevelAlarmDeviceIds.add(attribute.getDeviceId());
                }
                //故障：水泵故障属性（水泵1故障状态、水泵2故障报警等）任一为1
                if (isPumpFaultAttribute(attribute.getAttributeName())
                        && isSignalOn(attribute.getValue())) {
                    faultDeviceIds.add(attribute.getDeviceId());
                }
            }
        }

        dto.setLiquidLevelAlarmCount((long) liquidLevelAlarmDeviceIds.size());
        dto.setFaultDeviceCount((long) faultDeviceIds.size());
        return dto;
    }

    /**
     * 判断属性是否为水泵故障类属性
     */
    private boolean isPumpFaultAttribute(String attributeName) {
        if (attributeName == null) {
            return false;
        }
        return attributeName.contains(PUMP_NAME_KEYWORD) && attributeName.contains(PUMP_FAULT_KEYWORD);
    }

    /**
     * 判断信号值是否为1（兼容"1"、"1.0"等格式）
     */
    private boolean isSignalOn(String value) {
        if (value == null) {
            return false;
        }
        String trim = value.trim();
        return "1".equals(trim) || "1.0".equals(trim) || "1.0000".equals(trim);
    }
}
