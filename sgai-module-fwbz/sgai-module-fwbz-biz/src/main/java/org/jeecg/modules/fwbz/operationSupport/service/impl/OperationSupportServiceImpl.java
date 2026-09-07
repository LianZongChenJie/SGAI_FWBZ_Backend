package org.jeecg.modules.fwbz.operationSupport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.modules.fwbz.bc.entity.BuildingControlPointSendHistory;
import org.jeecg.modules.fwbz.bc.service.IBuildingControlPointSendHistoryService;
import org.jeecg.modules.fwbz.main.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.constant.BusinessConfigConstant;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AirConditioningUnitStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.ExhaustFanStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.FanCoilStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.FreshAirStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.HeatRecoveryStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.OverViewStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.PowerStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPoint;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPointDataDay;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointDataDayService;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointDataService;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointService;
import org.jeecg.modules.fwbz.energyAnalysis.util.TableUtil;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Table;
import org.jeecg.modules.fwbz.energyAnalysis.vo.TableData;
import org.jeecg.modules.fwbz.energyAnalysis.vo.TableHeader;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.dto.DeviceAttributeHistoryQueryDto;
import org.jeecg.modules.fwbz.mdm.dto.DeviceRunStateStatisticsDto;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttributeHistory;
import org.jeecg.modules.fwbz.mdm.entity.EquipmentCategory;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeHistoryService;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.mdm.service.IEquipmentCategoryService;
import org.jeecg.modules.fwbz.operationSupport.service.IOperationSupportService;
import org.jeecg.modules.fwbz.operationSupport.vo.EquipmentCategoryStatisticsVo;
import org.jeecg.modules.fwbz.main.service.DeviceAttributeOperationService;
import org.jeecg.modules.fwbz.main.service.IBusinessConfigService;
import org.jeecg.modules.fwbz.main.vo.DeviceDataVo;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@AllArgsConstructor
@Slf4j
public class OperationSupportServiceImpl implements IOperationSupportService {
    private final DateTimeFormatter filedForMatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 排风机故障属性关键词（属性名需同时含"风机"与"故障"，如"排风机1故障状态"）
     */
    private static final String FAN_NAME_KEYWORD = "风机";
    private static final String FAN_FAULT_KEYWORD = "故障";

    private final IDeviceService deviceService;
    private final IEquipmentCategoryService equipmentCategoryService;
    private final IMeteringPointService meteringPointService;
    private final IMeteringPointDataDayService meteringPointDataDayService;
    private final IDeviceAttributeService deviceAttributeService;
    private final IMeteringPointDataService meteringPointDataService;
    private final IDeviceAttributeHistoryService deviceAttributeHistoryService;
    private final DeviceAttributeOperationService deviceAttributeOperationService;
    private final IBuildingControlPointSendHistoryService buildingControlPointSendHistoryService;

    private final IBusinessConfigService businessConfigService;

    @Override
    public IPage<DeviceDataVo> deviceListWithAttrBycategoryId(DeviceDataFindDto params) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, params.getCategoryId())
                .eq(params.getSpaceId() != null, Device::getSpaceId, params.getSpaceId())
                .eq(params.getRunState() != null, Device::getRunState, params.getRunState())
                .orderByDesc(Device::getSort);
        if (StringUtils.isNotEmpty(params.getIds())) {
            wrapper.in(Device::getId, Arrays.stream(params.getIds().split(",")).map(Long::parseLong).collect(Collectors.toList()));
        }
        IPage<Device> page = new Page<>(params.getPageNo(), params.getPageSize());
        IPage<DeviceDataVo> listPage = deviceService.page(page, wrapper).convert(DeviceDataVo::convert);
        ;
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
        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_CATEGORYID);
        String columns = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_COLUMNS);
        IPage<DeviceDataVo> deviceWithAttr = findDeviceWithAttr(params, longByKey, columns);
        //空调机组冗余展示字段 启停状态
        for (DeviceDataVo record : deviceWithAttr.getRecords()) {
            for (DeviceAttribute deviceAttribute : record.getDeviceAttributeList()) {
                if (deviceAttribute.getAttributeCode().equals("STOP_RUN")) {
                    record.setRunStop(deviceAttribute.getValue());
                }
            }
        }
        return deviceWithAttr;
    }

    public IPage<DeviceDataVo> airList(DeviceDataFindDto params) {
        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_CATEGORYID);
        String columns = "STOP_RUN,SA_TEMP_SETPOINT";
        IPage<DeviceDataVo> deviceWithAttr = findDeviceWithAttr(params, longByKey, columns);
        //空调机组冗余展示字段 启停状态
        for (DeviceDataVo record : deviceWithAttr.getRecords()) {
            for (DeviceAttribute deviceAttribute : record.getDeviceAttributeList()) {
                if (deviceAttribute.getAttributeCode().equals("STOP_RUN")) {
                    record.setRunStop(deviceAttribute.getValue());
                }
                if (deviceAttribute.getAttributeCode().equals("SA_TEMP_SETPOINT")) {
                    record.setSetTemperature(deviceAttribute.getValue());
                }
            }
        }
        return deviceWithAttr;
    }

    public IPage<DeviceDataVo> freshAirHandlingUnitList(DeviceDataFindDto params) {
        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESH_AIR_CATEGORYID);
        String columns = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESH_AIR_COLUMNS);
        return findDeviceWithAttr(params, longByKey, columns);
    }

    public IPage<DeviceDataVo> powerDistributionSystemList(DeviceDataFindDto params) {
        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_POWER_CATEGORYID);
        String columns = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_POWER_COLUMNS);
        return findDeviceWithAttr(params, longByKey, columns);
    }


    @NotNull
    private IPage<DeviceDataVo> findDeviceWithAttr(DeviceDataFindDto params, String longByKey, String columns) {
        params.setCategoryId(Long.valueOf(longByKey));
        IPage<DeviceDataVo> deviceDataVoIPage = deviceListWithAttrBycategoryId(params);
        //查询 空调机组展示配置项 然后过滤， 只展示配置的属性列
        List<String> strings = stream(columns.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();

        List<DeviceDataVo> records = deviceDataVoIPage.getRecords();
        for (DeviceDataVo record : records) {
            List<DeviceAttribute> deviceAttributeList = record.getDeviceAttributeList();
            Map<String, DeviceAttribute> collect1 = deviceAttributeList.stream().collect(toMap(DeviceAttribute::getAttributeCode, Function.identity()));
            List<DeviceAttribute> newList = new ArrayList<>();
            for (String string : strings) {
                DeviceAttribute deviceAttribute = collect1.get(string);
                if (deviceAttribute != null) {
                    newList.add(deviceAttribute);
                }
            }
            record.setDeviceAttributeList(newList);
        }
        return deviceDataVoIPage;
    }


    @Override
    public AirConditioningUnitStatisticsDto airConditioningUnitStatistics() {
        //查询 空调机组配置id
        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_CATEGORYID);

        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, Long.valueOf(longByKey)));

        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

        String longByKey2 = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_POINT_ID);

        MeteringPoint byId = meteringPointService.getById(Long.valueOf(longByKey2));
        BigDecimal energyConsumption = BigDecimal.ZERO;

        if (byId != null) {
            MeteringPointDataDay byDateAndPointId = meteringPointDataDayService.findByDateAndPointId(LocalDate.now(), byId.getId());
            if (byDateAndPointId != null) {
                if (byDateAndPointId.getValue() != null) {
                    energyConsumption = byDateAndPointId.getValue();
                }
            }
        }
        AirConditioningUnitStatisticsDto dto = new AirConditioningUnitStatisticsDto();

        dto.setCount((long) list.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setEnergyConsumption(energyConsumption);

        // 平均PM2.5：查询所有空调机组设备"回风PM2.5"属性并取平均（参考新风统计）
        BigDecimal pm25Total = BigDecimal.ZERO;
        int pm25Count = 0;
        // 分批次查询属性，防止in语句过大
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
            List<DeviceAttribute> byDeviceIds = deviceAttributeService.findByDeviceIds(deviceIds);
            for (DeviceAttribute byDeviceId : byDeviceIds) {
                if ("回风PM2.5".equals(byDeviceId.getAttributeName()) && StringUtils.isNotEmpty(byDeviceId.getValue())) {
                    pm25Total = pm25Total.add(BigDecimal.valueOf(Double.parseDouble(byDeviceId.getValue())));
                    pm25Count++;
                }
            }
        }
        if (pm25Count > 0) {
            dto.setAvgPm25(pm25Total.divide(new BigDecimal(pm25Count), 2, RoundingMode.HALF_UP));
        }

        return dto;

    }

    @Override
    public FreshAirStatisticsDto freshAirStatistics() {

        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESH_AIR_CATEGORYID);

        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, Long.valueOf(longByKey)));


        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

        String longByKey2 = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESHAIR_POINT_ID);

        MeteringPoint byId = meteringPointService.getById(Long.valueOf(longByKey2));
        BigDecimal energyConsumption = BigDecimal.ZERO;

        if (byId != null) {
            MeteringPointDataDay byDateAndPointId = meteringPointDataDayService.findByDateAndPointId(LocalDate.now(), byId.getId());
            if (byDateAndPointId != null) {
                if (byDateAndPointId.getValue() != null) {
                    energyConsumption = byDateAndPointId.getValue();
                }
            }
        }
        FreshAirStatisticsDto dto = new FreshAirStatisticsDto();

        dto.setCount((long) list.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setEnergyConsumption(energyConsumption);

        //查询所有新风设备"回风PM2.5"属性并取平均
        BigDecimal pm25Total = BigDecimal.ZERO;
        int pm25Count = 0;
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
            List<DeviceAttribute> byDeviceIds = deviceAttributeService.findByDeviceIds(deviceIds);
            for (DeviceAttribute byDeviceId : byDeviceIds) {
                if ("回风PM2.5".equals(byDeviceId.getAttributeName()) && StringUtils.isNotEmpty(byDeviceId.getValue())) {
                    pm25Total = pm25Total.add(BigDecimal.valueOf(Double.parseDouble(byDeviceId.getValue())));
                    pm25Count++;
                }
            }
        }

        if (pm25Count > 0) {
            dto.setAvgPm25(pm25Total.divide(new BigDecimal(pm25Count), 2, RoundingMode.HALF_UP));
        }

        return dto;

    }

    @Override
    public ExhaustFanStatisticsDto exhaustFanStatistics() {

        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_PF_POINT_ID);

        // 排风分类（38）下含下级分类（50、58），先展开为"自身+全部子孙类别"再统一统计
        List<Long> categoryIds = deviceService.expandCategoryIds(Collections.singletonList(Long.valueOf(longByKey)));
        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .in(Device::getCategoryId, categoryIds));


        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

        String longByKey2 = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_PF_POINT_ID);

        MeteringPoint byId = meteringPointService.getById(Long.valueOf(longByKey2));
        BigDecimal energyConsumption = BigDecimal.ZERO;

        if (byId != null) {
            MeteringPointDataDay byDateAndPointId = meteringPointDataDayService.findByDateAndPointId(LocalDate.now(), byId.getId());
            if (byDateAndPointId != null) {
                if (byDateAndPointId.getValue() != null) {
                    energyConsumption = byDateAndPointId.getValue();
                }
            }
        }
        ExhaustFanStatisticsDto dto = new ExhaustFanStatisticsDto();

        dto.setEnergyConsumption(energyConsumption);

        //统计故障数：参照集水坑统计故障方法——属性名同时含"风机"与"故障"且值为1的设备即为故障设备（同一设备去重）
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
                //故障：排风机故障属性（如"排风机1故障状态"）任一为1即故障
                if (isFanFaultAttribute(attribute.getAttributeName())
                        && isSignalOn(attribute.getValue())) {
                    faultDeviceIds.add(attribute.getDeviceId());
                }
            }
        }
        dto.setFaultCount(faultDeviceIds.size());

        return dto;

    }

    /**
     * 判断属性是否为排风机故障类属性（属性名同时含"风机"与"故障"）
     */
    private boolean isFanFaultAttribute(String attributeName) {
        if (attributeName == null) {
            return false;
        }
        return attributeName.contains(FAN_NAME_KEYWORD) && attributeName.contains(FAN_FAULT_KEYWORD);
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

    /**
     * 风机盘管统计
     * @return
     */
    @Override
    public FanCoilStatisticsDto fanCoilStatistics() {

        String categoryId = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_PG_POINT_ID);

        if (StringUtils.isBlank(categoryId)) {
            log.warn("风机盘管统计配置缺失, key={}", BusinessConfigConstant.OPERATIONSUPPORT_PG_POINT_ID);
            return new FanCoilStatisticsDto();
        }

        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, Long.valueOf("40")));


        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

        String pointId = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_PG_POINT_ID);
        BigDecimal energyConsumption = BigDecimal.ZERO;

        if (StringUtils.isNotBlank(pointId)) {
            MeteringPoint byId = meteringPointService.getById(Long.valueOf(pointId.trim()));
            if (byId != null) {
                MeteringPointDataDay byDateAndPointId = meteringPointDataDayService.findByDateAndPointId(LocalDate.now(), byId.getId());
                if (byDateAndPointId != null) {
                    if (byDateAndPointId.getValue() != null) {
                        energyConsumption = byDateAndPointId.getValue();
                    }
                }
            }
        }
        FanCoilStatisticsDto dto = new FanCoilStatisticsDto();

        dto.setCount((long) list.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setEnergyConsumption(energyConsumption);
        // 查询风机盘管设备属性名含"温度"的属性并取平均（等价于
        // SELECT da.* FROM device_attribute da LEFT JOIN device d ON da.device_id=d.id
        //  WHERE d.category_id=该类别 AND da.attribute_name like '%温度%'）
        BigDecimal Temperature = new BigDecimal(0);
        int count = 0;
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
            List<DeviceAttribute> byDeviceIds = deviceAttributeService.findByDeviceIds(deviceIds);
            for (DeviceAttribute byDeviceId : byDeviceIds) {
                if ("温度".equals(byDeviceId.getAttributeName()) && StringUtils.isNotEmpty(byDeviceId.getValue())) {
                    Temperature = Temperature.add(BigDecimal.valueOf(Double.parseDouble(byDeviceId.getValue())));
                    count++;
                }
            }
        }
        if (count > 0) {
            dto.setAverageTemperature(Temperature.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP));
        }
        return dto;

    }

    /**
     * 统计给定设备列表中，属性名包含指定关键词的属性实时值平均值。
     * 分批查询属性防止 in 语句过大；单个属性值非数字时跳过并告警，不影响其它值。
     *
     * @param devices 设备列表（均为同一类别）
     * @param nameKeyword 属性名关键词（如"温度"，等价 attribute_name like '%温度%'）
     * @return 平均值（保留2位四舍五入）；无匹配属性/无有效值时返回 null
     */
    private BigDecimal averageDeviceAttributeByName(List<Device> devices, String nameKeyword) {
        if (CollectionUtils.isEmpty(devices) || StringUtils.isBlank(nameKeyword)) {
            return null;
        }
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        // 分批次查询属性，防止 in 语句过大
        int batch = devices.size() / 1000;
        for (int j = 0; j <= batch; j++) {
            List<Long> deviceIds = new ArrayList<>();
            for (int k = 0; k < 1000; k++) {
                int index = j * 1000 + k;
                if (index >= devices.size()) {
                    break;
                }
                deviceIds.add(devices.get(index).getId());
            }
            if (CollectionUtils.isEmpty(deviceIds)) {
                continue;
            }
            List<DeviceAttribute> attributes = deviceAttributeService.findByDeviceIds(deviceIds);
            for (DeviceAttribute attribute : attributes) {
                if (attribute == null || attribute.getAttributeName() == null
                        || !attribute.getAttributeName().contains(nameKeyword)
                        || StringUtils.isEmpty(attribute.getValue())) {
                    continue;
                }
                try {
                    total = total.add(BigDecimal.valueOf(Double.parseDouble(attribute.getValue())));
                    count++;
                } catch (NumberFormatException e) {
                    log.warn("属性值非数字, 跳过求平均: deviceId={}, attributeName={}, value={}",
                            attribute.getDeviceId(), attribute.getAttributeName(), attribute.getValue());
                }
            }
        }
        if (count == 0) {
            return null;
        }
        return total.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
    }

    @Override
    public HeatRecoveryStatisticsDto heatRecoveryStatistics() {

        String categoryId = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_RHS_POINT_ID);

        if (StringUtils.isBlank(categoryId)) {
            log.warn("热回收统计配置缺失, key={}", BusinessConfigConstant.OPERATIONSUPPORT_RHS_POINT_ID);
            return new HeatRecoveryStatisticsDto();
        }

        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, Long.valueOf(categoryId.trim())));


        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

        String pointId = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_RHS_POINT_ID);
        BigDecimal energyConsumption = BigDecimal.ZERO;

        if (StringUtils.isNotBlank(pointId)) {
            MeteringPoint byId = meteringPointService.getById(Long.valueOf(pointId.trim()));
            if (byId != null) {
                MeteringPointDataDay byDateAndPointId = meteringPointDataDayService.findByDateAndPointId(LocalDate.now(), byId.getId());
                if (byDateAndPointId != null) {
                    if (byDateAndPointId.getValue() != null) {
                        energyConsumption = byDateAndPointId.getValue();
                    }
                }
            }
        }
        HeatRecoveryStatisticsDto dto = new HeatRecoveryStatisticsDto();

        dto.setCount((long) list.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setEnergyConsumption(energyConsumption);

        return dto;

    }

    @Override
    public PowerStatisticsDto powerStatistics() {

        String longByKey = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_POWER_CATEGORYID);

        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, Long.valueOf(longByKey)));


        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

        String longByKey2 = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_POWER_POINT_ID);

        MeteringPoint byId = meteringPointService.getById(Long.valueOf(longByKey2));
        BigDecimal energyConsumption = BigDecimal.ZERO;

        if (byId != null) {
            MeteringPointDataDay byDateAndPointId = meteringPointDataDayService.findByDateAndPointId(LocalDate.now(), byId.getId());
            if (byDateAndPointId != null) {
                if (byDateAndPointId.getValue() != null) {
                    energyConsumption = byDateAndPointId.getValue();
                }
            }
        }
        PowerStatisticsDto dto = new PowerStatisticsDto();

        dto.setCount((long) list.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setEnergyConsumption(energyConsumption);

        //分批次查询属性信息
        int i = list.size() / 1000;

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (int j = 0; j <= i; j++) {
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
            List<DeviceAttribute> byDeviceIds = deviceAttributeService.findByDeviceIds(deviceIds);
            for (DeviceAttribute byDeviceId : byDeviceIds) {
                if ("13Cs".equals(byDeviceId.getAttributeCode()) && StringUtils.isNotEmpty(byDeviceId.getValue())) {
                    total = total.add(BigDecimal.valueOf(Double.parseDouble(byDeviceId.getValue())));
                    count++;
                }
            }
        }

        if (count > 0) {
            BigDecimal average = total.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
            dto.setAvgPowerFactor(average);
        }

        return dto;

    }

    @Override
    public OverViewStatisticsDto overviewStatistics() {
        List<SelectTreeModel> selectTreeModels = equipmentCategoryService.queryListByPid(0L);

        // 仅统计设备型（device_type=2）设备：在线数、远程控制设备数均出自该列表
        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>()
                .select(Device::getId, Device::getRunState)
                .eq(Device::getDeviceType, Device.DEVICE_TYPE_EQUIPMENT));



        LocalDate now = LocalDate.now();
        LocalDateTime startOfDay = now.atStartOfDay();
        LocalDateTime endOfDay = now.atTime(LocalTime.MAX);

        long count = buildingControlPointSendHistoryService.count(new LambdaQueryWrapper<BuildingControlPointSendHistory>()
                .between(BuildingControlPointSendHistory::getCollectionTime, startOfDay, endOfDay));


        OverViewStatisticsDto dto = new OverViewStatisticsDto();

        dto.setCount((long) selectTreeModels.size());
        // 在线数：从设备型（device_type=2）列表中统计运行状态为"在线"的设备
        dto.setOnline(list.stream()
                .filter(item -> DeviceConstant.DEVICE_RUN_STATA_ONLINE.equals(item.getRunState()))
                .count());
        dto.setRemoteControlEquipment((long) list.size());

        dto.setTodayInstructionWasIssued(count);


        return dto;

    }


    @Override
    public Table airEnergyFindDay(String energyFlowDiagramIds, LocalDate localDate) {
        //查询业务key
        String longByKey2 = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_ENERGY_POINTIDS);
        if (localDate == null) {
            localDate = LocalDate.now();
        }
        //调用分时数据
        return meteringPointDataService.findDay(longByKey2, localDate);
    }

    /**
     * 送风温度当天的温度曲线
     *
     * @param energyFlowDiagramIds
     * @param localDate
     * @return
     */
    @Override
    public Table supplyAirTemperature(String energyFlowDiagramIds, LocalDate localDate) {
        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_CATEGORYID);
        String attrCode = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_SUPPLYAIR);
        return findAttrHistoryDay(localDate, category, attrCode);

    }

    /**
     * 送风温度当天的温度曲线
     *
     * @param energyFlowDiagramIds
     * @param localDate
     * @return
     */
    @Override
    public Table freshSupplyAirTemperature(String energyFlowDiagramIds, LocalDate localDate) {
        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESH_AIR_CATEGORYID);
        String attrCode = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESHAIR_SUPPLYAIR);
        return findAttrHistoryDay(localDate, category, attrCode);

    }

    /**
     * 回风温度当天的温度曲线
     *
     * @param energyFlowDiagramIds
     * @param localDate
     * @return
     */
    @Override
    public Table returnAirTemperature(String energyFlowDiagramIds, LocalDate localDate) {
        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_CATEGORYID);
        String attrCode = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_RETURNAIR);
        return findAttrHistoryDay(localDate, category, attrCode);

    }

    /**
     * 回风温度当天的温度曲线
     *
     * @param energyFlowDiagramIds
     * @param localDate
     * @return
     */
    @Override
    public Table freshReturnAirTemperature(String energyFlowDiagramIds, LocalDate localDate) {
        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESH_AIR_CATEGORYID);
        String attrCode = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESHAIR_RETURNAIR);
        return findAttrHistoryDay(localDate, category, attrCode);

    }

    @Override
    public Table pm25(String energyFlowDiagramIds, LocalDate localDate) {
        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESH_AIR_CATEGORYID);
        String attrCode = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESHAIR_PM25);

        DeviceDataFindDto params = new DeviceDataFindDto();
        params.setCategoryId(Long.valueOf(category));
        params.setPageNo(1);
        params.setPageSize(9999);
        if (StringUtils.isNotBlank(energyFlowDiagramIds)) {
            params.setIds(energyFlowDiagramIds);
        }

        return findAttrReal(params, localDate, attrCode);

    }

    @Override
    public Table activePower(String energyFlowDiagramIds, LocalDate localDate) {
        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_POWER_CATEGORYID);
        String attrCode = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_POWER_ACTIVE);

        DeviceDataFindDto params = new DeviceDataFindDto();
        params.setCategoryId(Long.valueOf(category));
        params.setPageNo(1);
        params.setPageSize(3);
        if (StringUtils.isNotBlank(energyFlowDiagramIds)) {
            params.setIds(energyFlowDiagramIds);
        }

        return findAttrReal(params, localDate, attrCode);


    }


    @Override
    public List<DeviceRunStateStatisticsDto> equipmentOverview(Long categoryId) {

        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_OVERVIEW_CATEGORYIDS);
        List<Long> longs = strToLongList(category);
        ArrayList<DeviceRunStateStatisticsDto> objects = new ArrayList<>();
        for (Long aLong : longs) {
            List<EquipmentCategory> selectTreeModels = equipmentCategoryService.list(new LambdaQueryWrapper<EquipmentCategory>()
                    .eq(EquipmentCategory::getId, aLong));
            DeviceRunStateStatisticsDto dto = getDto(selectTreeModels.get(0));
            objects.add(dto);
        }

//        if (categoryId == null) {
//            List<EquipmentCategory> selectTreeModels = equipmentCategoryService.list(new LambdaQueryWrapper<EquipmentCategory>()
//                    .eq(EquipmentCategory::getPid, 0L));
//            for (EquipmentCategory selectTreeModel : selectTreeModels) {
//                DeviceRunStateStatisticsDto dto = getDto(selectTreeModel);
//                objects.add(dto);
//            }
//        } else {
//            List<EquipmentCategory> selectTreeModels = equipmentCategoryService.list(new LambdaQueryWrapper<EquipmentCategory>()
//                    .eq(EquipmentCategory::getId, categoryId));
//            DeviceRunStateStatisticsDto dto = getDto(selectTreeModels.get(0));
//            objects.add(dto);
//        }

        return objects;
    }

    @NotNull
    private DeviceRunStateStatisticsDto getDto(EquipmentCategory selectTreeModel) {
        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>().select(Device::getId, Device::getRunState, Device::getSpaceId)
                .eq(selectTreeModel.getId() != null, Device::getCategoryId, selectTreeModel.getId()));
        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));
        DeviceRunStateStatisticsDto dto = new DeviceRunStateStatisticsDto();
        dto.setCategory(selectTreeModel);
        dto.setCount((long) list.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setOffline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_OFFLINE, 0L));
        return dto;
    }

    /**
     * 根据分类id 和属性Code 和时间查询当天的分时间变化曲线
     *
     * @param localDate
     * @param categoryId
     * @param attrCode
     * @return
     */
    @NotNull
    private Table findAttrHistoryDay(LocalDate localDate, String categoryId, String attrCode) {

        DeviceDataFindDto params = new DeviceDataFindDto();
        params.setCategoryId(Long.valueOf(categoryId));
        params.setPageNo(1);
        params.setPageSize(9999);
        params.setDeviceType(Device.DEVICE_TYPE_EQUIPMENT);

        IPage<DeviceDataVo> deviceDataVoIPage = deviceListWithAttrBycategoryId(params);
        if (localDate == null) {
            localDate = LocalDate.now();
        }

        List<DeviceDataVo> records = deviceDataVoIPage.getRecords();

        Map<Long, String> deviceProperties = new HashMap<>();
        for (DeviceDataVo record : records) {
            List<DeviceAttribute> deviceAttributeList = record.getDeviceAttributeList();
            for (DeviceAttribute deviceAttribute : deviceAttributeList) {
                if (deviceAttribute.getAttributeCode().equals(attrCode)) {
                    deviceProperties.put(deviceAttribute.getId(), record.getDeviceCode());
                }
            }
        }
        List<DeviceAttributeHistory> deviceAttributeHistories = new ArrayList<DeviceAttributeHistory>();
        if (!deviceProperties.isEmpty()) {
            DeviceAttributeHistoryQueryDto param = new DeviceAttributeHistoryQueryDto();
            param.setDeviceAttributeIds(deviceProperties.keySet().stream().toList());
            param.setStartTime(LocalDateTime.of(localDate, LocalTime.MIN));
            param.setEndTime(LocalDateTime.of(localDate, LocalTime.MIN.withHour(23)));
            deviceAttributeHistories = deviceAttributeHistoryService.listByAttributeIds(param);

        }

        List<TableHeader> tableHeaderList = TableUtil.dayHeaders(localDate);
        Table table = createTable(tableHeaderList, deviceProperties, deviceAttributeHistories);
        return table;
    }

    /**
     * 根据分类id 和属性Code 和时间查询实时数据
     *
     * @param localDate
     * @param attrCode
     * @return
     */
    @NotNull
    private Table findAttrReal(DeviceDataFindDto params, LocalDate localDate, String attrCode) {

        IPage<DeviceDataVo> deviceDataVoIPage = deviceListWithAttrBycategoryId(params);
        if (localDate == null) {
            localDate = LocalDate.now();
        }

        List<DeviceDataVo> records = deviceDataVoIPage.getRecords();

        Map<Long, String> deviceProperties = new HashMap<>();
        ArrayList<DeviceAttributeHistory> meterDataList = new ArrayList<>();
        for (DeviceDataVo record : records) {
            List<DeviceAttribute> deviceAttributeList = record.getDeviceAttributeList();
            for (DeviceAttribute deviceAttribute : deviceAttributeList) {
                if (deviceAttribute.getAttributeCode().equals(attrCode)) {
                    deviceProperties.put(deviceAttribute.getId(), record.getDeviceCode());
                    DeviceAttributeHistory e = new DeviceAttributeHistory();
                    e.setAttributeId(deviceAttribute.getId());
                    e.setCollectionTime(LocalDateTime.of(localDate, LocalTime.MIN));
                    String value = deviceAttribute.getValue();
                    if (value == null) {
                        value = "0";
                    }
                    e.setValue(value);
                    meterDataList.add(e);
                }
            }
        }

        List<TableHeader> tableHeaderList = TableUtil.only(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        Table table = createTable(tableHeaderList, deviceProperties, meterDataList);
        return table;
    }

    @Override
    public void airControl(List<DeviceAttribute> params) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }
        for (DeviceAttribute param : params) {
            DeviceAttribute byDeviceIdAndCode = deviceAttributeService.findByDeviceIdAndCode(param.getDeviceId(), param.getAttributeCode());

            //保存设备属性值
            DeviceAttribute entity = new DeviceAttribute();
            entity.setValue(param.getValue());
            deviceAttributeService.update(entity,
                    new LambdaQueryWrapper<DeviceAttribute>().eq(DeviceAttribute::getId, byDeviceIdAndCode.getId()));

            //发送楼控数据
            deviceAttributeOperationService.operationDeviceAttribute(byDeviceIdAndCode.getId(), param.getValue());
        }
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

    @Override
    public List<EquipmentCategoryStatisticsVo> equipmentCategoryStatistics() {
        // 查询所有一级分类为设备（type=2，pid=0）的分类
        List<EquipmentCategory> topCategories = equipmentCategoryService.list(new LambdaQueryWrapper<EquipmentCategory>()
                .eq(EquipmentCategory::getType, EquipmentCategory.TYPE_EQUIPMENT)
                .eq(EquipmentCategory::getPid, 0L));

        List<EquipmentCategoryStatisticsVo> result = new ArrayList<>();
        for (EquipmentCategory category : topCategories) {
            // 展开该一级分类及所有子孙分类ID
            List<Long> allCategoryIds = deviceService.expandCategoryIds(Collections.singletonList(category.getId()));
            // 查询这些分类下的所有设备
            List<Device> devices;
            if (CollectionUtils.isEmpty(allCategoryIds)) {
                devices = Collections.emptyList();
            } else {
                devices = deviceService.list(new LambdaQueryWrapper<Device>()
                        .select(Device::getId, Device::getRunState)
                        .in(Device::getCategoryId, allCategoryIds));
            }
            Map<String, Long> runStateMap = devices.stream()
                    .filter(item -> item.getRunState() != null)
                    .collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

            EquipmentCategoryStatisticsVo vo = new EquipmentCategoryStatisticsVo();
            vo.setCategoryName(category.getCategoryName());
            vo.setCount((long) devices.size());
            vo.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
            vo.setOffline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_OFFLINE, 0L));
            result.add(vo);
        }
        return result;
    }

    private Table createTable(List<TableHeader> tableHeaderList, Map<Long, String> configs, List<DeviceAttributeHistory> meterDataList) {
        Map<Long, Map<LocalDateTime, String>> dataMap = meterDataList.stream()
                .collect(Collectors.groupingBy(DeviceAttributeHistory::getAttributeId,
                        Collectors.toMap(DeviceAttributeHistory::getCollectionTime, DeviceAttributeHistory::getValue)));
        List<TableData> tableDataList = new ArrayList<>();
        // 表格尾行合计
        TableData sum = new TableData();
        sum.put("name", "合计");
        sum.put("sum", BigDecimal.ZERO);

        for (Long key : configs.keySet()) {
            String name = configs.get(key);


            TableData tableData = new TableData();
            Map<LocalDateTime, String> dateTimeBigDecimalMap = dataMap.get(key);
            for (TableHeader header : tableHeaderList) {
                String field = header.getField();
                if (field.equals("sum")) {
                    continue;
                }
                if (field.equals("name")) {
                    tableData.put(field, name);
                    continue;
                }
                LocalDateTime localDateTime = LocalDateTime.parse(field, filedForMatter);
                if (!sum.containsKey(field)) {
                    sum.put(field, BigDecimal.ZERO);
                }
                BigDecimal value = dateTimeBigDecimalMap == null ? BigDecimal.ZERO : BigDecimal.valueOf(Double.parseDouble(dateTimeBigDecimalMap.getOrDefault(localDateTime, "0")));
                tableData.put(field, value);
                sum.put(field, ((BigDecimal) sum.get(field)).add(value));
            }
            tableData.calculateSum();
            sum.put("sum", ((BigDecimal) sum.get("sum")).add((BigDecimal) tableData.get("sum")));
            tableDataList.add(tableData);
        }
        tableDataList.add(sum);
        Table table = new Table();
        table.setTableHeaderList(tableHeaderList);
        table.setTableDataList(tableDataList);
        return table;
    }

}
