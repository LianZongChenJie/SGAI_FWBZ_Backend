package org.jeecg.modules.fwbz.mqtt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.boot.starter.lock.client.RedissonLockClient;
import org.jeecg.modules.fwbz.main.entity.DayData;
import org.jeecg.modules.fwbz.main.entity.HourData;
import org.jeecg.modules.fwbz.main.entity.MinuteData;
import org.jeecg.modules.fwbz.main.entity.MonthData;
import org.jeecg.modules.fwbz.main.entity.YearData;
import org.jeecg.modules.fwbz.main.service.IDayDataService;
import org.jeecg.modules.fwbz.main.service.IHourDataService;
import org.jeecg.modules.fwbz.main.service.IMinuteDataService;
import org.jeecg.modules.fwbz.main.service.IMonthDataService;
import org.jeecg.modules.fwbz.main.service.IRealDataService;
import org.jeecg.modules.fwbz.main.service.IYearDataService;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.mqtt.entity.MqttHistory;
import org.jeecg.modules.fwbz.mqtt.mapper.MDeviceAttributeMapper;
import org.jeecg.modules.fwbz.mqtt.mapper.MqttHistoryMapper;
import org.jeecg.modules.fwbz.mqtt.service.IMqttHistoryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MQTT低压配电数据 Service 实现
 *
 * @author fwbz
 */
@Slf4j
@Service
public class MqttHistoryServiceImpl extends ServiceImpl<MqttHistoryMapper, MqttHistory>
        implements IMqttHistoryService {

    private final MDeviceAttributeMapper deviceAttributeMapper;

    private final MqttHistoryMapper mqttHistoryMapper;

    private final RedissonLockClient redissonLockClient;

    private final IDeviceService deviceService;

    /** 设备属性服务：用于回查属性 id、设备 id */
    private final IDeviceAttributeService deviceAttributeService;

    private final IRealDataService realDataService;

    private final IMinuteDataService minuteDataService;

    private final IHourDataService hourDataService;

    private final IDayDataService dayDataService;

    private final IMonthDataService monthDataService;

    private final IYearDataService yearDataService;

    public MqttHistoryServiceImpl(MDeviceAttributeMapper deviceAttributeMapper,
                                  MqttHistoryMapper mqttHistoryMapper,
                                  RedissonLockClient redissonLockClient,
                                  IDeviceService deviceService,
                                  IDeviceAttributeService deviceAttributeService,
                                  IRealDataService realDataService,
                                  IMinuteDataService minuteDataService,
                                  IHourDataService hourDataService,
                                  IDayDataService dayDataService,
                                  IMonthDataService monthDataService,
                                  IYearDataService yearDataService) {
        this.deviceAttributeMapper = deviceAttributeMapper;
        this.mqttHistoryMapper = mqttHistoryMapper;
        this.redissonLockClient = redissonLockClient;
        this.deviceService = deviceService;
        this.deviceAttributeService = deviceAttributeService;
        this.realDataService = realDataService;
        this.minuteDataService = minuteDataService;
        this.hourDataService = hourDataService;
        this.dayDataService = dayDataService;
        this.monthDataService = monthDataService;
        this.yearDataService = yearDataService;
    }

    @Override
    public int updateDeviceAttributeByUniqueKey(List<MqttHistory> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        // 更新设备属性表（实时采集值、采集时间）
        int rows = deviceAttributeMapper.updateValueByUniqueKeys(list);
        // MQTT 接收的点位数据不再写入 device_attribute_history 设备属性历史表，
        // 改为写入 table_mqtt_history（MQTT低压配电数据表），时间对齐15分钟槽位，有则更新无则新增
        try {
            saveMqttHistory(list);
        } catch (Exception e) {
            log.error("MQTT历史数据写入 table_mqtt_history 失败", e);
        }
        return rows;
    }

    /**
     * 将 MQTT 数据写入 table_mqtt_history 表：回查 device_attribute 补充属性 id、设备 id，
     * 采集时间按数据自带 timeStamp 对齐到整十五分钟槽位（如 08:00:05 -> 08:00:00），
     * 同一设备(deviceId) 同一属性(attributeId) 同一槽位(timeStamp) 已有历史数据则更新，否则新增。
     *
     * @param list MQTT数据列表
     */
    private void saveMqttHistory(List<MqttHistory> list) {
        // 收集需要回查的采集编码（uniqueKey 对应 device_attribute.acquisition_coding）
        List<String> uniqueKeys = list.stream()
                .map(MqttHistory::getUniqueKey)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueKeys.isEmpty()) {
            return;
        }
        // 回查 device_attribute 获取属性 id、设备 id（按采集编码索引）
        Map<String, DeviceAttribute> attrMap = deviceAttributeService.list(
                        new LambdaQueryWrapper<DeviceAttribute>()
                                .in(DeviceAttribute::getAcquisitionCoding, uniqueKeys))
                .stream()
                .collect(Collectors.toMap(DeviceAttribute::getAcquisitionCoding, Function.identity(), (a, b) -> a));
        // 补充 deviceId、attributeId，时间对齐到整十五分钟槽位
        List<MqttHistory> saveList = new ArrayList<>(list.size());
        for (MqttHistory history : list) {
            DeviceAttribute attr = attrMap.get(history.getUniqueKey());
            if (attr == null || attr.getId() == null || attr.getDeviceId() == null
                    || history.getTimeStamp() == null || StringUtils.isBlank(history.getValue())) {
                continue;
            }
            history.setDeviceId(attr.getDeviceId());
            history.setAttributeId(attr.getId());
            history.setTimeStamp(alignTo15MinuteSlot(history.getTimeStamp()));
            // 槽位结束时间 = 槽位起始 + 15分钟，用于时间段查询（左闭右开）
            history.setSlotEnd(history.getTimeStamp().plusMinutes(15));
            saveList.add(history);
        }
        if (saveList.isEmpty()) {
            return;
        }
        // 到 table_mqtt_history 查询这批数据在对应15分钟时间段 [槽位, 槽位+15min) 内是否已有记录
        List<MqttHistory> existList = mqttHistoryMapper.selectBySlotList(saveList);
        // 已存在记录的时间戳可能是旧的非对齐值，统一对齐后再构建唯一键匹配
        Map<String, MqttHistory> existMap = existList.stream()
                .collect(Collectors.toMap(this::historyKey, Function.identity(), (a, b) -> a));
        // 同槽位已有则更新（覆盖测点描述与遥测值），否则新增
        List<MqttHistory> updateList = new ArrayList<>();
        List<MqttHistory> insertList = new ArrayList<>();
        for (MqttHistory history : saveList) {
            MqttHistory exist = existMap.get(historyKey(history));
            if (exist != null) {
                exist.setDesc(history.getDesc());
                exist.setValue(history.getValue());
                updateList.add(exist);
            } else {
                insertList.add(history);
            }
        }
        int updateCount = 0;
        int insertCount = 0;
        if (!updateList.isEmpty()) {
            updateCount = mqttHistoryMapper.updateBatch(updateList);
        }
        if (!insertList.isEmpty()) {
            insertCount = mqttHistoryMapper.insertBatch(insertList);
        }
        log.info("MQTT历史数据写入 table_mqtt_history 完成: 新增={}, 更新={}", insertCount, updateCount);
    }

    /**
     * 构建历史记录唯一键：设备id + 属性id + 对齐后槽位时间
     * 查询返回的记录时间戳可能是旧的非对齐值，先对齐到15分钟槽位再匹配
     */
    private String historyKey(MqttHistory history) {
        return history.getDeviceId() + "_" + history.getAttributeId()
                + "_" + alignTo15MinuteSlot(history.getTimeStamp());
    }

    /**
     * 将时间对齐到当前整十五分钟槽位：分钟向下取整到 15 的倍数，秒与纳秒清零
     * 如 08:00:05 -> 08:00:00，08:16:59 -> 08:15:00
     *
     * @param time 原始时间
     * @return 对齐后的时间；入参为 null 时返回 null
     */
    private LocalDateTime alignTo15MinuteSlot(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        int slotMinute = (time.getMinute() / 15) * 15;
        return time.withMinute(slotMinute).withSecond(0).withNano(0);
    }

    @Override
    public void calculateEnergyData(List<MqttHistory> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        int matched = 0;
        for (MqttHistory history : list) {
            // 仅uniqueKey包含"01Wp"的电度数据为正向有功电能表底值，触发能耗计算
            if (history.getUniqueKey() == null || !history.getUniqueKey().contains("01Wp")) {
                continue;
            }
            matched++;
            String deviceCode = history.getDevKeys();
            if (StringUtils.isBlank(deviceCode)
                    || history.getTimeStamp() == null
                    || StringUtils.isBlank(history.getValue())) {
                log.warn("MQTT电度数据字段不完整，跳过能耗计算, uniqueKey={}, devKeys={}, timeStamp={}, value={}",
                        history.getUniqueKey(), deviceCode, history.getTimeStamp(), history.getValue());
                continue;
            }
            // 预检查设备存在性，避免能耗计算内部静默返回
            Device device = deviceService.getByDeviceCode(deviceCode);
            if (device == null) {
                log.warn("能耗计算跳过：设备不存在, deviceCode={}, uniqueKey={}", deviceCode, history.getUniqueKey());
                continue;
            }
            String lockKey = getLockKey(deviceCode);
            boolean locked = false;
            try {
                locked = redissonLockClient.tryLock(lockKey, 10, 60);
                if (locked) {
                    BigDecimal value = new BigDecimal(history.getValue().trim());
                    // 接收正向有功电能表底值，更新 实时/分钟/小时/日/月/年 数据
                    calculateEnergy(device, history.getTimeStamp(), value);
                } else {
                }
            } catch (Exception e) {
                log.error("MQTT电度数据能耗计算失败, deviceCode={}, uniqueKey={}, value={}",
                        deviceCode, history.getUniqueKey(), history.getValue(), e);
            } finally {
                if (locked) {
                    redissonLockClient.unlock(lockKey);
                }
            }
        }
    }

    /**
     * 正向有功电能表底值能耗计算：
     * 1. data_real：保存当前表底值
     * 2. 校验：若最新一条数据的结束值大于接收的表底数（表底倒退，疑似换表/重置），
     *    仅保存实时数据，不进行分钟/小时/日/月/年计算
     * 3. data_minute：无上一条则开始值=结束值=表底数；否则开始值=上一条结束值，结束值=当前表底数
     * 4. data_hour：本小时无记录则开始值=结束值=表底数；否则开始值不变，结束值=当前表底数
     * 5. data_day：今日所有小时value之和，有则更新，无则新增
     * 6. data_month：本月所有天value之和，有则更新，无则新增
     * 7. data_year：本年所有月value之和，有则更新，无则新增
     */
    private void calculateEnergy(Device device, LocalDateTime timeStamp, BigDecimal value) {
        Long deviceId = device.getId();

        // 1. 实时数据 data_real：保存当前表底值
        realDataService.save(deviceId, timeStamp, value);

        // 2. 校验表底数是否倒退：最新一条数据的结束值大于接收的表底数时，
        //    不进行分钟/小时/日/月/年计算，仅保留实时数据
        MinuteData lastMinute = minuteDataService.findLatest(deviceId);
        if (lastMinute != null && lastMinute.getEndValue() != null
                && lastMinute.getEndValue().compareTo(value) > 0) {
            return;
        }

        // 3. 分钟数据 data_minute
        LocalDateTime minuteTime = timeStamp.withSecond(0).withNano(0);
        if (lastMinute == null) {
            // 无上一条记录：开始值=结束值=表底数，value=结束值-开始值=0
            MinuteData minute = new MinuteData();
            minute.setDeviceId(deviceId);
            minute.setTime(minuteTime);
            minute.setStartValue(value);
            minute.setEndValue(value);
            minute.setValue(BigDecimal.ZERO);
            minuteDataService.saveOrUpdate(minute);
        } else if (lastMinute.getTime().equals(minuteTime)) {
            // 同一分钟再次上报：更新该条，开始值不变，结束值=当前表底数
            lastMinute.setEndValue(value);
            lastMinute.setValue(value.subtract(lastMinute.getStartValue() == null ? BigDecimal.ZERO : lastMinute.getStartValue()));
            minuteDataService.saveOrUpdate(lastMinute);
        } else {
            // 有上一条记录：开始值=上一条结束值，结束值=当前表底数
            MinuteData minute = new MinuteData();
            minute.setDeviceId(deviceId);
            minute.setTime(minuteTime);
            minute.setStartValue(lastMinute.getEndValue());
            minute.setEndValue(value);
            minute.setValue(value.subtract(lastMinute.getEndValue() == null ? BigDecimal.ZERO : lastMinute.getEndValue()));
            minuteDataService.saveOrUpdate(minute);
        }

        // 3. 小时数据 data_hour：时间为本小时的那条
        LocalDateTime hourTime = timeStamp.withMinute(0).withSecond(0).withNano(0);
        HourData hour = hourDataService.getOne(new LambdaQueryWrapper<HourData>()
                .eq(HourData::getDeviceId, deviceId)
                .eq(HourData::getTime, hourTime), false);
        if (hour == null) {
            // 本小时无记录：开始值=结束值=表底数，value=结束值-开始值=0
            hour = new HourData();
            hour.setDeviceId(deviceId);
            hour.setTime(hourTime);
            hour.setStartValue(value);
            hour.setEndValue(value);
            hour.setValue(BigDecimal.ZERO);
            hour.setComputeValue(BigDecimal.ZERO);
        } else {
            // 已有记录：开始值不变，结束值=当前表底数
            hour.setEndValue(value);
            hour.setValue(value.subtract(hour.getStartValue() == null ? BigDecimal.ZERO : hour.getStartValue()));
            hour.setComputeValue(hour.getValue());
        }
        hourDataService.saveOrUpdate(hour);

        // 4. 日数据 data_day：今天所有小时value之和
        LocalDateTime dayStart = hourTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        BigDecimal dayValue = sum(hourDataService.findByDeviceIdAndTimeRange(deviceId, dayStart, dayEnd), HourData::getValue);
        DayData day = dayDataService.findByDeviceIdAndTime(deviceId, dayStart);
        if (day == null) {
            day = new DayData();
            day.setDeviceId(deviceId);
            day.setTime(dayStart);
        }
        day.setValue(dayValue);
        dayDataService.saveOrUpdate(day);

        // 5. 月数据 data_month：本月所有天value之和
        LocalDateTime monthStart = dayStart.withDayOfMonth(1);
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        BigDecimal monthValue = sum(dayDataService.findByDeviceIdsAndTimeRange(Collections.singletonList(deviceId), monthStart, monthEnd), DayData::getValue);
        MonthData month = monthDataService.findByDeviceIdAndTime(deviceId, monthStart);
        if (month == null) {
            month = new MonthData();
            month.setDeviceId(deviceId);
            month.setTime(monthStart);
        }
        month.setValue(monthValue);
        monthDataService.saveOrUpdate(month);

        // 6. 年数据 data_year：本年所有月value之和
        LocalDateTime yearStart = monthStart.withMonth(1);
        LocalDateTime yearEnd = yearStart.plusYears(1);
        BigDecimal yearValue = sum(monthDataService.findByDeviceIdsAndTimeRange(Collections.singletonList(deviceId), yearStart, yearEnd), MonthData::getValue);
        YearData year = yearDataService.findByDeviceIdAndTime(deviceId, yearStart);
        if (year == null) {
            year = new YearData();
            year.setDeviceId(deviceId);
            year.setTime(yearStart);
        }
        year.setValue(yearValue);
        yearDataService.saveOrUpdate(year);
    }

    /**
     * 对列表中的值字段求和，跳过空值
     */
    private <T> BigDecimal sum(List<T> list, Function<T, BigDecimal> valueGetter) {
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return list.stream()
                .map(valueGetter)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String getLockKey(String deviceCode) {
        return "lock:device:data:gather" + deviceCode;
    }
}
