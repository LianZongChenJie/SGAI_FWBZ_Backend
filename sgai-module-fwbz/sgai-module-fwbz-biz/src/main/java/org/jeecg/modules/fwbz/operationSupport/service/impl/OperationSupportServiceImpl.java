package org.jeecg.modules.fwbz.operationSupport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.constant.BusinessConfigConstant;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AirConditioningUnitStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.entity.*;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointDataDayService;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointRelService;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointService;
import org.jeecg.modules.fwbz.energyAnalysis.util.TableUtil;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Table;
import org.jeecg.modules.fwbz.energyAnalysis.vo.TableHeader;
import org.jeecg.modules.fwbz.entity.DayData;
import org.jeecg.modules.fwbz.entity.MonthData;
import org.jeecg.modules.fwbz.entity.RealData;
import org.jeecg.modules.fwbz.mdm.constant.CategoryConstant;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.dto.DeviceRunStateStatisticsDto;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.entity.EquipmentCategory;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.operationSupport.service.IOperationSupportService;
import org.jeecg.modules.fwbz.service.IBusinessConfigService;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;

@Service
@AllArgsConstructor
@Slf4j
public class OperationSupportServiceImpl implements IOperationSupportService {

    private final IDeviceService deviceService;
    private final IMeteringPointService meteringPointService;
    private final IMeteringPointDataDayService meteringPointDataDayService;
    private final IDeviceAttributeService deviceAttributeService;

    private final IBusinessConfigService businessConfigService;
    @Override
    public IPage<DeviceDataVo> equipmentList(DeviceDataFindDto params) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceType, Device.DEVICE_TYPE_EQUIPMENT)
                .eq(Device::getCategoryId, params.getCategoryId())
                .eq(params.getSpaceId() != null,  Device::getSpaceId, params.getSpaceId())
                .eq(params.getRunState() != null,  Device::getRunState, params.getRunState())
                .orderByDesc(Device::getSort);

        IPage<Device> page = new Page<>(params.getPageNo(), params.getPageSize());
        IPage<DeviceDataVo> listPage = deviceService.page(page, wrapper).convert(DeviceDataVo::convert);;
        List<DeviceDataVo> records = listPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return listPage;
        }

        List<Long> deviceIds = records.stream().map(DeviceDataVo::getDeviceId).toList();

        Map<Long, List<DeviceAttribute>> deviceAttributeMap = deviceAttributeService.findByDeviceIds(deviceIds)
                .stream().collect(groupingBy(DeviceAttribute::getDeviceId));
        for (DeviceDataVo record : records) {
            //设置属性
            record.setDeviceAttributeList(deviceAttributeMap.getOrDefault(record.getDeviceId(), new ArrayList<>()));
        }

        return listPage;
    }

    public IPage<DeviceDataVo> airConditioningUnitList(DeviceDataFindDto params) {
        //查询 空调机组配置id
        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_TAB_AIR_CATEGORYID);
        params.setCategoryId(Long.valueOf(longByKey));
        IPage<DeviceDataVo> deviceDataVoIPage = equipmentList(params);
        //查询 空调机组展示配置项 然后过滤， 只展示配置的属性列
        String columns = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_TAB_AIR_COLUMNS);
        Set<String> strings = stream(columns.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toSet());

        List<DeviceDataVo> records = deviceDataVoIPage.getRecords();
        for (DeviceDataVo record : records) {
            List<DeviceAttribute> deviceAttributeList = record.getDeviceAttributeList();
            List<DeviceAttribute> collect = deviceAttributeList
                    .stream().filter(attr->strings.contains(attr.getAttributeCode())).toList();
            record.setDeviceAttributeList(collect);
        }
        return deviceDataVoIPage;
    }


    @Override
    public AirConditioningUnitStatisticsDto airConditioningUnitStatistics() {
        //查询 空调机组配置id
        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_TAB_AIR_CATEGORYID);

        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, Long.valueOf(longByKey)));

        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

        String longByKey2 = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_TAB_AIR_POINT_ID);

        MeteringPoint byId = meteringPointService.getById(Long.valueOf(longByKey2));
        BigDecimal energyConsumption = BigDecimal.ZERO;

        if(byId!=null){
            MeteringPointDataDay byDateAndPointId = meteringPointDataDayService.findByDateAndPointId(LocalDate.now(),byId.getId());
            if(byDateAndPointId!=null){
                if(byDateAndPointId.getValue() != null){
                    energyConsumption = byDateAndPointId.getValue();
                }
            }
        }
        AirConditioningUnitStatisticsDto dto = new AirConditioningUnitStatisticsDto();

        dto.setCount((long) list.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setEnergyConsumption(energyConsumption);
        dto.setAvgCop("4.2");

        return dto;

    }


    @Override
    public Table airEnergyFindDay(String energyFlowDiagramIds, LocalDate localDate) {
//        List<MeteringPoint> configs = meteringPointService.getByIds(strToLongList(energyFlowDiagramIds));
//        List<Long> configIds = configs.stream().map(MeteringPoint::getId).collect(Collectors.toList());
//        List<TableHeader> tableHeaderList = TableUtil.dayHeaders(localDate);
//        List<? extends MeteringPointData> meterDataList = hourDataService.findByTimeRangeAndPointIds(
//                LocalDateTime.of(localDate, LocalTime.MIN),
//                LocalDateTime.of(localDate, LocalTime.MIN.withHour(23)),
//                configIds
//        );
//        return createTable(tableHeaderList, configs, meterDataList);
        return null;
    }



//    @Override
//    public EnergyMeteringStatisticsDto statistics() {
//
//
//        Device device = new Device();
//        device.setDeviceType(Device.DEVICE_TYPE_MEASURING);
//
//        List<Device> list = deviceService.findAll(device);
//
//        Long addCount = 0L;
//        LocalDate now = LocalDate.now();
//        LocalDate yestoday = now.plusDays(-1);
//        for (Device deviceDataVo : list) {
//            if(deviceDataVo.getCreateTime()!=null){
//                LocalDate localDate = LocalDate.ofInstant(deviceDataVo.getCreateTime().toInstant(), ZoneId.systemDefault());
//                if (localDate.isEqual(now)) {
//                    addCount++;
//                }
//            }
//        }
//
//        Map<String, Long> collect = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));
//
//
//        //查询计量点位数据
//
//        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.ENERGYMETERING_DAY_ELECTRIC);
//        MeteringPointDataDay todayElectric = dayDataService.findByDateAndPointId(now, Long.valueOf(longByKey));
//        MeteringPointDataDay yestodayElectric = dayDataService.findByDateAndPointId(yestoday, Long.valueOf(longByKey));
//
//
//        String longByKey2 = businessConfigService.getValueByKey(BusinessConfigConstant.ENERGYMETERING_DAY_WATER);
//        MeteringPointDataDay todayWater = dayDataService.findByDateAndPointId(now, Long.valueOf(longByKey2));
//        MeteringPointDataDay yestodayWater = dayDataService.findByDateAndPointId(yestoday, Long.valueOf(longByKey2));
//
//        BigDecimal todayElectricValue = Optional.ofNullable(todayElectric).map(MeteringPointDataDay::getValue).orElse(BigDecimal.ZERO);
//        BigDecimal yestodayElectricValue = Optional.ofNullable(yestodayElectric).map(MeteringPointDataDay::getValue).orElse(BigDecimal.ZERO);
//        BigDecimal todayWaterValue = Optional.ofNullable(todayWater).map(MeteringPointDataDay::getValue).orElse(BigDecimal.ZERO);
//        BigDecimal yestodayWaterValue = Optional.ofNullable(yestodayWater).map(MeteringPointDataDay::getValue).orElse(BigDecimal.ZERO);
//
//
//        EnergyMeteringStatisticsDto dto = new EnergyMeteringStatisticsDto();
//        dto.setCount((long) list.size());
//        if(addCount==0){
//            dto.setAddCount("-0");
//        }else{
//            dto.setAddCount("↑"+addCount);
//        }
//        BigDecimal bigDecimal = calculatePercentage(collect.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L), (long) list.size());
//        dto.setOnlineRate(bigDecimal+"%");
//
//
//        dto.setElectricCount(todayElectricValue);
//        dto.setWaterCount(todayWaterValue);
//        dto.setElectricCountDoD(formatData(calculateMom(todayElectricValue, yestodayElectricValue)));
//        dto.setWaterCountDoD(formatData(calculateMom(todayWaterValue, yestodayWaterValue)));
//
//
//        return dto;
//    }
//
//    @NotNull
//    private static String formatData(BigDecimal bigDecimal2) {
//        String waterCountDoD;
//        if(bigDecimal2.compareTo(BigDecimal.ZERO)>0){
//             waterCountDoD = "↑" + bigDecimal2 + "%";
//        }else if (bigDecimal2.compareTo(BigDecimal.ZERO)<0){
//             waterCountDoD = "↓" + bigDecimal2 + "%";
//        }else{
//             waterCountDoD = bigDecimal2 + "%";
//        }
//        return waterCountDoD;
//    }

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

    private List<Long> strToLongList(String str) {
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private List<String> strToList(String str) {
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());
    }

}
