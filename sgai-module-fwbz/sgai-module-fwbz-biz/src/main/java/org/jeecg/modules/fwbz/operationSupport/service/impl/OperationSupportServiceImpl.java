package org.jeecg.modules.fwbz.operationSupport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.modules.fwbz.dto.DeviceDataFindDto;
import org.jeecg.modules.fwbz.energyAnalysis.constant.BusinessConfigConstant;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AirConditioningUnitStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.FreshAirStatisticsDto;
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
import org.jeecg.modules.fwbz.service.IBusinessConfigService;
import org.jeecg.modules.fwbz.vo.DeviceDataVo;
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

    private final IDeviceService deviceService;
    private final IEquipmentCategoryService equipmentCategoryService;
    private final IMeteringPointService meteringPointService;
    private final IMeteringPointDataDayService meteringPointDataDayService;
    private final IDeviceAttributeService deviceAttributeService;
    private final IMeteringPointDataService meteringPointDataService;
    private final IDeviceAttributeHistoryService deviceAttributeHistoryService;

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
                if(deviceAttribute.getAttributeCode().equals("STOP_RUN")){
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
                if(deviceAttribute.getAttributeCode().equals("STOP_RUN")){
                    record.setRunStop(deviceAttribute.getValue());
                }
                if(deviceAttribute.getAttributeCode().equals("SA_TEMP_SETPOINT")){
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
        dto.setAvgCop("4.2");

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

        //分批次查询属性信息
        int i = list.size() / 200;

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (int j = 0; j < i + 1; j++) {
            ArrayList<Long> deviceIds = new ArrayList<>();
            for (int k = 0; k < 200; k++) {
                Device device = list.get(j * k);
                deviceIds.add(device.getId());
            }
            List<DeviceAttribute> byDeviceIds = deviceAttributeService.findByDeviceIds(deviceIds);
            for (DeviceAttribute byDeviceId : byDeviceIds) {
                if (byDeviceId.getAttributeCode().equals("PM25")) {
                    total = total.add(BigDecimal.valueOf(Double.parseDouble(byDeviceId.getValue())));
                    count++;
                }
            }
        }

        BigDecimal average = total.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
        dto.setAvgPm25(average);

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
        int i = list.size() / 200;

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (int j = 0; j < i + 1; j++) {
            ArrayList<Long> deviceIds = new ArrayList<>();
            for (int k = 0; k < 200; k++) {
                Device device = list.get(j * k);
                deviceIds.add(device.getId());
            }
            List<DeviceAttribute> byDeviceIds = deviceAttributeService.findByDeviceIds(deviceIds);
            for (DeviceAttribute byDeviceId : byDeviceIds) {
                if (byDeviceId.getAttributeCode().equals("13Cs")) {
                    total = total.add(BigDecimal.valueOf(Double.parseDouble(byDeviceId.getValue())));
                    count++;
                }
            }
        }

        BigDecimal average = total.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
        dto.setAvgPowerFactor(average);

        return dto;

    }
    @Override
    public OverViewStatisticsDto overviewStatistics() {
        List<SelectTreeModel> selectTreeModels = equipmentCategoryService.queryListByPid(0L);

        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>().select(Device::getId,Device::getRunState));
        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));

        OverViewStatisticsDto dto = new OverViewStatisticsDto();

        dto.setCount((long) selectTreeModels.size());
        dto.setOnline(runStateMap.getOrDefault(DeviceConstant.DEVICE_RUN_STATA_ONLINE, 0L));
        dto.setRemoteControlEquipment("2456");
        dto.setTodayInstructionWasIssued("1234");


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
        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESH_AIR_CATEGORYID);
        String attrCode = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_FRESHAIR_RETURNAIR);
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
        String category = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_CATEGORYID);
        String attrCode = businessConfigService.getValueByKey(BusinessConfigConstant.OPERATIONSUPPORT_AIR_RETURNAIR);
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
        ArrayList<DeviceRunStateStatisticsDto> objects = new ArrayList<>();
        if(categoryId==null){
            List<SelectTreeModel> selectTreeModels = equipmentCategoryService.queryListByPid(0L);
            for (SelectTreeModel selectTreeModel : selectTreeModels) {
                DeviceRunStateStatisticsDto dto = getDto(Long.valueOf(selectTreeModel.getKey()));
                objects.add(dto);
            }
        }else{
            DeviceRunStateStatisticsDto dto = getDto(categoryId);
            objects.add(dto);
        }

        return objects;
    }

    @NotNull
    private DeviceRunStateStatisticsDto getDto(Long categoryId) {
        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>().select(Device::getId,Device::getRunState,Device::getSpaceId).eq(categoryId != null, Device::getCategoryId, categoryId));
        Map<String, Long> runStateMap = list.stream().filter(item -> item.getRunState() != null).collect(Collectors.groupingBy(Device::getRunState, Collectors.counting()));
        DeviceRunStateStatisticsDto dto = new DeviceRunStateStatisticsDto();
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

        DeviceAttributeHistoryQueryDto param = new DeviceAttributeHistoryQueryDto();
        param.setDeviceAttributeIds(deviceProperties.keySet().stream().toList());
        param.setStartTime(LocalDateTime.of(localDate, LocalTime.MIN));
        param.setEndTime(LocalDateTime.of(localDate, LocalTime.MIN.withHour(23)));
        List<DeviceAttributeHistory> deviceAttributeHistories = deviceAttributeHistoryService.listByAttributeIds(param);

        List<TableHeader> tableHeaderList = TableUtil.dayHeaders(localDate);
        Table table = createTable(tableHeaderList, deviceProperties, deviceAttributeHistories);
        return table;
    }

    /**
     * 根据分类id 和属性Code 和时间查询实时数据
     *
     * @param localDate
     * @param categoryId
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


    /**
     * 计算环比增长率（返回百分比数值，如 20.5 表示 20.5%）
     *
     * @param current  本期值
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
     *
     * @param numerator   分子
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
