package org.jeecg.modules.fwbz.mdm.controller;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.fwbz.entity.DeviceAttributeEntity;
import org.jeecg.modules.fwbz.entity.DeviceEntity;
import org.jeecg.modules.fwbz.entity.DeviceInfo;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.entity.EquipmentCategory;
import org.jeecg.modules.fwbz.mdm.entity.Space;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.mdm.service.IEquipmentCategoryService;
import org.jeecg.modules.fwbz.mdm.service.ISpaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fwbz/device/api")
@AllArgsConstructor
public class DeviceApiController {

    private final IDeviceService service;
    private final IEquipmentCategoryService categoryService;
    private final ISpaceService spaceService;

    private final IDeviceAttributeService deviceAttributeService;

    @IgnoreAuth
    @GetMapping("/deviceList")
    public List<DeviceEntity> deviceList(){
        List<DeviceEntity> result = new ArrayList<>();
        service.list().forEach(item -> {
            result.add(new DeviceEntity(item.getId(),item.getDeviceCode(),item.getCategoryId()));
        });
        return result;
    }

    @IgnoreAuth
    @GetMapping("/deviceInfoList")
    public List<DeviceInfo> deviceInfoList(@RequestParam String deviceIds){
        List<Device> devices = service.listByIds(Arrays.asList(deviceIds.split(",")));
        if (CollectionUtil.isEmpty(devices)) {
            return Collections.emptyList();
        }

        // 收集所有类别ID和空间ID
        List<Long> categoryIds = devices.stream()
                .map(Device::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Long> spaceIds = devices.stream()
                .map(Device::getSpaceId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询类别和空间
        Map<Long, String> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryService.findByIds(categoryIds).stream()
                        .collect(Collectors.toMap(EquipmentCategory::getId, EquipmentCategory::getFullName));

        Map<Long, String> spaceMap = spaceIds.isEmpty() ? Collections.emptyMap() :
                spaceService.listByIds(spaceIds).stream()
                        .collect(Collectors.toMap(Space::getId, Space::getFullName));

        // 构建返回结果
        return devices.stream()
                .map(device -> new DeviceInfo(
                        device.getId(),
                        device.getDeviceCode(),
                        device.getDeviceName(),
                        categoryMap.getOrDefault(device.getCategoryId(), ""),
                        spaceMap.getOrDefault(device.getSpaceId(), "")
                ))
                .collect(Collectors.toList());
    }

    @IgnoreAuth
    @GetMapping("/deviceAttributeList")
    public List<DeviceAttributeEntity> deviceAttributeList(){
        return deviceAttributeService.list()
                .stream()
                .map(item -> new DeviceAttributeEntity(item.getDeviceId(), item.getAttributeCode(), item.getAttributeName(), item.getAcquisitionCoding()))
                .toList();
    }
}
