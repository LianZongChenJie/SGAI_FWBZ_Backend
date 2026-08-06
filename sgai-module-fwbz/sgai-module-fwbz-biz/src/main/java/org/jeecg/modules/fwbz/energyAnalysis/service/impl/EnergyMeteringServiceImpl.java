package org.jeecg.modules.fwbz.energyAnalysis.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.energyAnalysis.constant.BusinessConfigConstant;
import org.jeecg.modules.fwbz.energyAnalysis.dto.EnergyMeteringStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointDataDay;
import org.jeecg.modules.fwbz.energyAnalysis.service.IEnergyMeteringService;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointDataDayService;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.service.IBusinessConfigService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class EnergyMeteringServiceImpl implements IEnergyMeteringService {

    private final IDeviceService deviceService;
    private final IMeteringPointDataDayService dayDataService;

    private final IBusinessConfigService businessConfigService;

    @Override
    public EnergyMeteringStatisticsDto statistics() {


        Device device = new Device();
        device.setDeviceType(Device.DEVICE_TYPE_MEASURING);

        List<Device> list = deviceService.findAll(device);

        Long addCount = 0L;
        LocalDate now = LocalDate.now();
        LocalDate yestoday = now.plusDays(-1);
        for (Device deviceDataVo : list) {
            if(deviceDataVo.getCreateTime()!=null){
                LocalDate localDate = LocalDate.ofInstant(deviceDataVo.getCreateTime().toInstant(), ZoneId.systemDefault());
                if (localDate.isEqual(now)) {
                    addCount++;
                }
            }
        }

        Map<String, Long> collect = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));


        //查询计量点位数据

        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.ENERGYMETERING_ELECTRIC_POINTID);
        MeteringPointDataDay todayElectric = dayDataService.findByDateAndPointId(now, Long.valueOf(longByKey));
        MeteringPointDataDay yestodayElectric = dayDataService.findByDateAndPointId(yestoday, Long.valueOf(longByKey));


        String longByKey2 = businessConfigService.getValueByKey(BusinessConfigConstant.ENERGYMETERING_WATER_POINTID);
        MeteringPointDataDay todayWater = dayDataService.findByDateAndPointId(now, Long.valueOf(longByKey2));
        MeteringPointDataDay yestodayWater = dayDataService.findByDateAndPointId(yestoday, Long.valueOf(longByKey2));

        BigDecimal todayElectricValue = Optional.ofNullable(todayElectric).map(MeteringPointDataDay::getValue).orElse(BigDecimal.ZERO);
        BigDecimal yestodayElectricValue = Optional.ofNullable(yestodayElectric).map(MeteringPointDataDay::getValue).orElse(BigDecimal.ZERO);
        BigDecimal todayWaterValue = Optional.ofNullable(todayWater).map(MeteringPointDataDay::getValue).orElse(BigDecimal.ZERO);
        BigDecimal yestodayWaterValue = Optional.ofNullable(yestodayWater).map(MeteringPointDataDay::getValue).orElse(BigDecimal.ZERO);


        EnergyMeteringStatisticsDto dto = new EnergyMeteringStatisticsDto();
        dto.setCount((long) list.size());
        if(addCount==0){
            dto.setAddCount("-0");
        }else{
            dto.setAddCount("↑"+addCount);
        }
        BigDecimal bigDecimal = calculatePercentage(collect.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L), (long) list.size());
        dto.setOnlineRate(bigDecimal+"%");


        dto.setElectricCount(todayElectricValue);
        dto.setWaterCount(todayWaterValue);
        dto.setElectricCountDoD(formatData(calculateMom(todayElectricValue, yestodayElectricValue)));
        dto.setWaterCountDoD(formatData(calculateMom(todayWaterValue, yestodayWaterValue)));


        return dto;
    }

    @NotNull
    private static String formatData(BigDecimal bigDecimal2) {
        String waterCountDoD;
        if(bigDecimal2.compareTo(BigDecimal.ZERO)>0){
             waterCountDoD = "↑" + bigDecimal2 + "%";
        }else if (bigDecimal2.compareTo(BigDecimal.ZERO)<0){
             waterCountDoD = "↓" + bigDecimal2 + "%";
        }else{
             waterCountDoD = bigDecimal2 + "%";
        }
        return waterCountDoD;
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


    /**
     * 计算百分比：分子 / 分母 * 100
     * @param numerator 分子
     * @param denominator 分母
     * @return 百分比，保留2位小数
     */
    public static BigDecimal calculatePercentage(Long numerator, Long denominator) {
        // 1. 判空
        if (numerator == null || denominator == null) {
            return null;
        }
        // 2. 分母为0处理
        if (denominator == 0) {
            return numerator == 0 ? BigDecimal.ZERO : null;  // 0/0 返回0，非零/0 返回null
        }
        // 3. 计算：(numerator / denominator) * 100
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
