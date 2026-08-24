package org.jeecg.modules.fwbz.complaint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetInfoMapper;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.alarm.entity.AlarmRecord;
import org.jeecg.modules.fwbz.alarm.mapper.AlarmRecordMapper;
import org.jeecg.modules.fwbz.complaint.entity.BuildingControlPointSendHistory;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintInfo;
import org.jeecg.modules.fwbz.complaint.entity.LightingOperationLog;
import org.jeecg.modules.fwbz.complaint.mapper.CBuildingControlPointSendHistoryMapper;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintInfoMapper;
import org.jeecg.modules.fwbz.complaint.mapper.LightingOperationLogMapper;
import org.jeecg.modules.fwbz.complaint.service.IComplaintStatisticsService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Description: 首页统计卡片
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Service
public class ComplaintStatisticsServiceImpl implements IComplaintStatisticsService {

    private final ActiveMeetInfoMapper activeMeetInfoMapper;
    private final CBuildingControlPointSendHistoryMapper buildingControlPointSendHistoryMapper;
    private final LightingOperationLogMapper lightingOperationLogMapper;
    private final ComplaintInfoMapper complaintInfoMapper;
    private final AlarmRecordMapper alarmRecordMapper;

    public ComplaintStatisticsServiceImpl(ActiveMeetInfoMapper activeMeetInfoMapper,
                                          CBuildingControlPointSendHistoryMapper buildingControlPointSendHistoryMapper,
                                          LightingOperationLogMapper lightingOperationLogMapper,
                                          ComplaintInfoMapper complaintInfoMapper,
                                          AlarmRecordMapper alarmRecordMapper) {
        this.activeMeetInfoMapper = activeMeetInfoMapper;
        this.buildingControlPointSendHistoryMapper = buildingControlPointSendHistoryMapper;
        this.lightingOperationLogMapper = lightingOperationLogMapper;
        this.complaintInfoMapper = complaintInfoMapper;
        this.alarmRecordMapper = alarmRecordMapper;
    }

    @Override
    public StatCardVO todayActiveMeet() {
        Date[] today = getTodayRange();
        List<ActiveMeetInfo> list = activeMeetInfoMapper.selectList(
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .ge(ActiveMeetInfo::getStartDate, today[0])
                        .lt(ActiveMeetInfo::getStartDate, today[1])
                        .orderByAsc(ActiveMeetInfo::getStartDate));

        StatCardVO vo = new StatCardVO();
        vo.setTitle("当前展会");
        vo.setValue(list.size());
        if (list.isEmpty()) {
            vo.setContext("暂无");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size() && i < 3; i++) {
                if (i > 0) sb.append("、");
                sb.append(list.get(i).getActiveName());
            }
            if (list.size() > 3) {
                sb.append("等");
            }
            sb.append(" 进行中");
            vo.setContext(sb.toString());
        }
        return vo;
    }

    @Override
    public StatCardVO todayDispatchOrder() {
        Date[] today = getTodayRange();
        Long buildingCount = buildingControlPointSendHistoryMapper.selectCount(
                new LambdaQueryWrapper<BuildingControlPointSendHistory>()
                        .ge(BuildingControlPointSendHistory::getCollectionTime, today[0])
                        .lt(BuildingControlPointSendHistory::getCollectionTime, today[1]));
        Long lightingCount = lightingOperationLogMapper.selectCount(
                new LambdaQueryWrapper<LightingOperationLog>()
                        .ge(LightingOperationLog::getOperationTime, today[0])
                        .lt(LightingOperationLog::getOperationTime, today[1]));
        long total = buildingCount + lightingCount;

        StatCardVO vo = new StatCardVO();
        vo.setTitle("现场调度指令");
        vo.setValue(total);
        vo.setContext("楼控" + buildingCount + " 照明" + lightingCount + " 今日");
        return vo;
    }

    @Override
    public StatCardVO todayComplaint() {
        Date[] today = getTodayRange();
        Date[] yesterday = getYesterdayRange();

        long todayCount = complaintInfoMapper.selectCount(
                new LambdaQueryWrapper<ComplaintInfo>()
                        .ge(ComplaintInfo::getComplaintDate, today[0])
                        .lt(ComplaintInfo::getComplaintDate, today[1]));
        long yesterdayCount = complaintInfoMapper.selectCount(
                new LambdaQueryWrapper<ComplaintInfo>()
                        .ge(ComplaintInfo::getComplaintDate, yesterday[0])
                        .lt(ComplaintInfo::getComplaintDate, yesterday[1]));

        StatCardVO vo = new StatCardVO();
        vo.setTitle("投诉建议");
        vo.setValue(todayCount);
        vo.setContext(buildCompareContext(todayCount, yesterdayCount, " 较昨日"));
        return vo;
    }

    @Override
    public StatCardVO todayAlarm() {
        Date[] today = getTodayRange();
        Date[] yesterday = getYesterdayRange();

        long todayCount = alarmRecordMapper.selectCount(
                new LambdaQueryWrapper<AlarmRecord>()
                        .ge(AlarmRecord::getAlarmTime, today[0])
                        .lt(AlarmRecord::getAlarmTime, today[1]));
        long yesterdayCount = alarmRecordMapper.selectCount(
                new LambdaQueryWrapper<AlarmRecord>()
                        .ge(AlarmRecord::getAlarmTime, yesterday[0])
                        .lt(AlarmRecord::getAlarmTime, yesterday[1]));

        // 已处理数量
        long handledCount = alarmRecordMapper.selectCount(
                new LambdaQueryWrapper<AlarmRecord>()
                        .ge(AlarmRecord::getAlarmTime, today[0])
                        .lt(AlarmRecord::getAlarmTime, today[1])
                        .eq(AlarmRecord::getAlarmStatus, "2"));

        StatCardVO vo = new StatCardVO();
        vo.setTitle("设备异常");
        vo.setValue(todayCount);
        vo.setContext("已处理" + handledCount + " " + buildCompareContext(todayCount, yesterdayCount, " 较昨日"));
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        return Arrays.asList(todayActiveMeet(), todayDispatchOrder(), todayComplaint(), todayAlarm());
    }

    /**
     * 获取今日时间范围 [00:00:00, 次日00:00:00)
     */
    private Date[] getTodayRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date end = cal.getTime();
        return new Date[]{start, end};
    }

    /**
     * 获取昨日时间范围 [昨日00:00:00, 今日00:00:00)
     */
    private Date[] getYesterdayRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date end = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date start = cal.getTime();
        return new Date[]{start, end};
    }

    /**
     * 构建较昨日的对比文案
     */
    private String buildCompareContext(long current, long last, String suffix) {
        long diff = current - last;
        if (diff > 0) {
            return "↑" + diff + suffix;
        } else if (diff < 0) {
            return "↓" + (-diff) + suffix;
        }
        return "";
    }
}
