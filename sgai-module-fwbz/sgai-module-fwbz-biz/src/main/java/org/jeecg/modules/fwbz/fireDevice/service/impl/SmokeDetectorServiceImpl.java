package org.jeecg.modules.fwbz.fireDevice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetector;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetectorType;
import org.jeecg.modules.fwbz.fireDevice.mapper.FireSmokeDetectorMapper;
import org.jeecg.modules.fwbz.fireDevice.mapper.FireSmokeDetectorTypeMapper;
import org.jeecg.modules.fwbz.fireDevice.service.ISmokeDetectorService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 消防设备 Service 实现
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmokeDetectorServiceImpl extends ServiceImpl<FireSmokeDetectorMapper, SmokeDetector>
        implements ISmokeDetectorService {

    private final FireSmokeDetectorTypeMapper fireSmokeDetectorTypeMapper;

    @Override
    public IPage<SmokeDetector> getSmokeDetectorPage(IPage<SmokeDetector> page,
                                                      String deviceName,
                                                      String status,
                                                      String deviceType,
                                                      Long venueId,
                                                      Date startTime,
                                                      Date endTime,
                                                      String signal,
                                                      String powerLevel) {

        LambdaQueryWrapper<SmokeDetector> qw = new LambdaQueryWrapper<>();
        qw.like(StringUtils.hasText(deviceName), SmokeDetector::getDeviceName, deviceName);
        qw.eq(StringUtils.hasText(status), SmokeDetector::getStatus, status);
        qw.eq(StringUtils.hasText(deviceType), SmokeDetector::getDeviceType, deviceType);
        qw.eq(venueId != null, SmokeDetector::getVenueId, venueId);
        qw.ge(startTime != null, SmokeDetector::getLastCheckTime, startTime);
        qw.le(endTime != null, SmokeDetector::getLastCheckTime, endTime);
        qw.eq(StringUtils.hasText(signal), SmokeDetector::getSignal, signal);
        qw.eq(StringUtils.hasText(powerLevel), SmokeDetector::getPowerLevel, powerLevel);
        qw.orderByDesc(SmokeDetector::getId);

        IPage<SmokeDetector> result = page(page, qw);

        // 联动消防设备类型：收集 deviceType 值，批量查询 typeName
        List<SmokeDetector> records = result.getRecords();
        if (!records.isEmpty()) {
            Set<Long> typeIds = records.stream()
                    .map(SmokeDetector::getDeviceType)
                    .filter(Objects::nonNull)
                    .filter(t -> t.matches("\\d+"))
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());

            if (!typeIds.isEmpty()) {
                List<SmokeDetectorType> types = fireSmokeDetectorTypeMapper.selectBatchIds(typeIds);
                Map<Long, String> typeNameMap = types.stream()
                        .collect(Collectors.toMap(SmokeDetectorType::getId, SmokeDetectorType::getTypeName, (a, b) -> a));

                records.forEach(record -> {
                    if (record.getDeviceType() != null && record.getDeviceType().matches("\\d+")) {
                        record.setTypeName(typeNameMap.getOrDefault(Long.valueOf(record.getDeviceType()), "未知类型"));
                    } else {
                        record.setTypeName(record.getDeviceType());
                    }
                });
            }
        }

        return result;
    }
}
