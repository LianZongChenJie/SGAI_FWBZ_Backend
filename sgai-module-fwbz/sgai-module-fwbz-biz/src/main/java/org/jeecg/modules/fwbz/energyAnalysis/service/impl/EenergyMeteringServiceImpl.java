package org.jeecg.modules.fwbz.energyAnalysis.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.EnergyMeteringStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.service.*;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.service.*;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class EenergyMeteringServiceImpl implements IEenergyMeteringService {

    private final IDeviceDataService deviceDataService;


    @Override
    public EnergyMeteringStatisticsDto statistics() {

        DeviceDataFindDto params = new DeviceDataFindDto();
        params.setDeviceType(Device.DEVICE_TYPE_MEASURING);
        LocalDateTime now = LocalDate.now().atStartOfDay();
        params.setStartTime(now);

        List<DeviceDataVo> list = deviceDataService.findListWithDay(params);
        params.setStartTime(now.plusDays(-1));

        List<DeviceDataVo> listYestoday = deviceDataService.findListWithDay(params);

        Long addCount = 0L;
        for (DeviceDataVo deviceDataVo : list) {
            if(deviceDataVo.getCreateTime()!=null){
                LocalDate localDate = LocalDate.ofInstant(deviceDataVo.getCreateTime().toInstant(), ZoneId.systemDefault());
                if (localDate.isEqual(LocalDate.now())) {
                    addCount++;
                }
            }
        }


        Map<String, Long> collect = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(DeviceDataVo::getRunState, Collectors.counting()));
        Map<Long, BigDecimal> collect1 = list.stream()
                .filter(item -> item.getCategoryId() != null)
                .collect(Collectors.groupingBy(DeviceDataVo::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, DeviceDataVo::getDayTotal, BigDecimal::add)));


        Map<Long, BigDecimal> collect2 = listYestoday.stream()
                .filter(item -> item.getCategoryId() != null)
                .collect(Collectors.groupingBy(DeviceDataVo::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, DeviceDataVo::getDayTotal, BigDecimal::add)));




        EnergyMeteringStatisticsDto dto = new EnergyMeteringStatisticsDto();
        dto.setCount((long) list.size());
        if(addCount==0){
            dto.setAddCount("-0");
        }else{
            dto.setAddCount("↑"+addCount);

        }

        dto.setOnlineRate(new BigDecimal(
                (double) collect.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L) / list.size() * 100
        ).setScale(0, RoundingMode.HALF_UP)
                .toString()+"5");

        BigDecimal todayElectricity = collect1.getOrDefault(DeviceConstant.CATEGORY_ELECTRICITY, BigDecimal.ZERO);
        dto.setElectricCount(todayElectricity);


        BigDecimal todayWater = collect1.getOrDefault(DeviceConstant.CATEGORY_WATER, BigDecimal.ZERO);
        dto.setWaterCount(todayWater);

        BigDecimal yestodayElectricity = collect2.getOrDefault(DeviceConstant.CATEGORY_ELECTRICITY, BigDecimal.ZERO);

        dto.setElectricCountDoD(calculateMom(todayElectricity,yestodayElectricity)+"%");


        BigDecimal yestodayWater = collect2.getOrDefault(DeviceConstant.CATEGORY_WATER, BigDecimal.ZERO);
        dto.setWaterCountDoD(calculateMom(todayWater,yestodayWater)+"%");






        return dto;




    }

    /**
     * 计算环比增长率（返回百分比数值，如 20.5 表示 20.5%）
     * @param current 本期值
     * @param previous 上期值
     * @return 环比增长率，保留2位小数
     */
    public static BigDecimal calculateMom(BigDecimal current, BigDecimal previous) {
        // 1. 判空
        if (current == null || previous == null) {
            return null;
        }

        // 2. 处理上期为0的情况
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;  // 两者都为0，增长率为0
            }
            return null;  // 上期为0，本期>0，增长率无穷大，返回null或特殊值
        }

        // 3. 计算：(current - previous) / previous * 100
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)  // 先除，保留4位小数提高精度
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);  // 最终保留2位小数
    }



}
