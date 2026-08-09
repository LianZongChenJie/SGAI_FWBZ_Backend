package org.jeecg.modules.fwbz.energyAnalysis.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.energyAnalysis.constant.BusinessConfigConstant;
import org.jeecg.modules.fwbz.energyAnalysis.dto.EnergyMeteringStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointDataDay;
import org.jeecg.modules.fwbz.energyAnalysis.service.IEnergyMeteringService;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointDataDayService;
import org.jeecg.modules.fwbz.energyAnalysis.util.pricing.CalculationUtil;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.main.service.IBusinessConfigService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
            dto.setAddCount("0");
        }else{
            dto.setAddCount("↑"+addCount);
        }
        dto.setOnlineRate(CalculationUtil.calculatePercentageToString(collect.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L), (long) list.size()));


        dto.setElectricCount(todayElectricValue);
        dto.setWaterCount(todayWaterValue);
        dto.setElectricCountDoD(CalculationUtil.calculateMomToString(todayElectricValue, yestodayElectricValue));
        dto.setWaterCountDoD(CalculationUtil.calculateMomToString(todayWaterValue, yestodayWaterValue));


        return dto;
    }

}
