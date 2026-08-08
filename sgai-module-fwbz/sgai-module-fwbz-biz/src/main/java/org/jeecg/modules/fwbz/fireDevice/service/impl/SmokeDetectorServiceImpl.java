package org.jeecg.modules.fwbz.fireDevice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.fireDevice.entity.FireAlarmRecord;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetector;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetectorType;
import org.jeecg.modules.fwbz.fireDevice.mapper.FireAlarmRecordMapper;
import org.jeecg.modules.fwbz.fireDevice.mapper.FireSmokeDetectorMapper;
import org.jeecg.modules.fwbz.fireDevice.mapper.FireSmokeDetectorTypeMapper;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.fireDevice.service.ISmokeDetectorService;
import org.jeecg.modules.fwbz.fireDevice.vo.DeviceTypeStatusVO;
import org.jeecg.modules.fwbz.fireDevice.vo.StatusCountVO;
import org.jeecg.modules.fwbz.fireDevice.vo.VenueDeviceCountVO;
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private final FireAlarmRecordMapper fireAlarmRecordMapper;
    private final IVenueInfoService venueInfoService;

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

        List<SmokeDetector> records = result.getRecords();
        if (!records.isEmpty()) {
            populateTypeName(records);
            populateVenueName(records);
        }

        return result;
    }

    @Override
    public IPage<FireAlarmRecord> getAlarmRecordsByDeviceId(IPage<FireAlarmRecord> page, Long deviceId) {
        log.info("根据设备ID查询报警记录, deviceId={}", deviceId);

        LambdaQueryWrapper<FireAlarmRecord> qw = new LambdaQueryWrapper<FireAlarmRecord>()
                .eq(FireAlarmRecord::getDeviceId, String.valueOf(deviceId))
                .eq(FireAlarmRecord::getStatus, 1)
                .orderByDesc(FireAlarmRecord::getAlarmDate)
                .orderByDesc(FireAlarmRecord::getAlarmTime);

        return fireAlarmRecordMapper.selectPage(page, qw);
    }

    private long doCountTotal() {
        return count();
    }

    private long doCountOnline() {
        LambdaQueryWrapper<SmokeDetector> qw = new LambdaQueryWrapper<SmokeDetector>()
                .ne(SmokeDetector::getStatus, "离线")
                .ne(SmokeDetector::getStatus, "故障");
        return count(qw);
    }

    private long doCountTodayCheck() {
        Date todayStart = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayEnd = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<SmokeDetector> qw = new LambdaQueryWrapper<SmokeDetector>()
                .ge(SmokeDetector::getLastCheckTime, todayStart)
                .lt(SmokeDetector::getLastCheckTime, todayEnd);
        return count(qw);
    }

    private long doCountPendingAlarm() {
        LambdaQueryWrapper<FireAlarmRecord> qw = new LambdaQueryWrapper<FireAlarmRecord>()
                .eq(FireAlarmRecord::getHandleStatus, 0)
                .eq(FireAlarmRecord::getStatus, 1);
        return fireAlarmRecordMapper.selectCount(qw);
    }

    @Override
    public StatCardVO countTotal() {
        long total = doCountTotal();
        StatCardVO vo = new StatCardVO();
        vo.setTitle("消防设备总数");
        vo.setValue(total);
        return vo;
    }

    @Override
    public StatCardVO countOnline() {
        long total = doCountTotal();
        long online = doCountOnline();
        double onlineRate = total == 0 ? 0 : Math.round(online * 1000.0 / total) / 10.0;
        StatCardVO vo = new StatCardVO();
        vo.setTitle("设备在线率");
        vo.setValue(onlineRate);
        vo.setContext(online + "/" + total + " 在线");
        return vo;
    }

    @Override
    public StatCardVO countTodayCheck() {
        long total = doCountTotal();
        long todayCheck = doCountTodayCheck();
        StatCardVO vo = new StatCardVO();
        vo.setTitle("今日巡检完成");
        vo.setValue(todayCheck);
        vo.setContext(todayCheck + "/" + total);
        return vo;
    }

    @Override
    public StatCardVO countPendingAlarm() {
        long pendingAlarm = doCountPendingAlarm();
        StatCardVO vo = new StatCardVO();
        vo.setTitle("待处理告警");
        vo.setValue(pendingAlarm);
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        List<StatCardVO> list = new ArrayList<>();
        list.add(countTotal());
        list.add(countOnline());
        list.add(countTodayCheck());
        list.add(countPendingAlarm());
        return list;
    }

    @Override
    public List<StatusCountVO> countByStatus() {
        log.info("按设备状态统计数量");
        return baseMapper.countByStatus();
    }

    @Override
    public List<VenueDeviceCountVO> countByVenue() {
        log.info("按场馆统计消防设备数量");
        return baseMapper.countByVenue();
    }

    @Override
    public List<DeviceTypeStatusVO> countByTypeAndStatus() {
        log.info("按设备类型分组统计各状态设备数量");
        List<Map<String, Object>> flatResults = baseMapper.countByTypeAndStatus();

        Map<String, List<StatusCountVO>> grouped = flatResults.stream()
                .collect(Collectors.groupingBy(
                        m -> (String) m.get("typeName"),
                        LinkedHashMap::new,
                        Collectors.mapping(m -> {
                            StatusCountVO vo = new StatusCountVO();
                            vo.setStatus((String) m.get("status"));
                            vo.setCount(((Number) m.get("count")).longValue());
                            return vo;
                        }, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .map(e -> {
                    DeviceTypeStatusVO vo = new DeviceTypeStatusVO();
                    vo.setTypeName(e.getKey());
                    vo.setData(e.getValue());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private void populateTypeName(List<SmokeDetector> records) {
        Set<Long> typeIds = records.stream()
                .map(SmokeDetector::getDeviceType)
                .filter(Objects::nonNull)
                .filter(t -> t.matches("\\d+"))
                .map(Long::valueOf)
                .collect(Collectors.toSet());

        if (typeIds.isEmpty()) {
            return;
        }

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

    private void populateVenueName(List<SmokeDetector> records) {
        Set<Long> venueIds = records.stream()
                .map(SmokeDetector::getVenueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (venueIds.isEmpty()) {
            return;
        }

        List<VenueInfo> venueList = venueInfoService.listByIds(venueIds);
        Map<Long, String> venueNameMap = venueList.stream()
                .collect(Collectors.toMap(VenueInfo::getId, VenueInfo::getVenueName, (a, b) -> a));

        records.forEach(record -> {
            if (record.getVenueId() != null) {
                record.setVenueName(venueNameMap.getOrDefault(record.getVenueId(), "未知场馆"));
            }
        });
    }
}
