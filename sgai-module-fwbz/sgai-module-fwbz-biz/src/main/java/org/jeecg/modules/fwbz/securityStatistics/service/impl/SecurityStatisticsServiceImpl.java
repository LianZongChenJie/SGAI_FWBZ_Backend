package org.jeecg.modules.fwbz.securityStatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.jeecg.modules.fwbz.alarm.service.IAlarmRecordService;
import org.jeecg.modules.fwbz.entity.CameraResource;
import org.jeecg.modules.fwbz.hikvision.service.ICameraResourceService;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolHistory;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;
import org.jeecg.modules.fwbz.patorlPlan.mapper.PatrolHistoryMapper;
import org.jeecg.modules.fwbz.patorlPlan.service.IPatrolPlanService;
import org.jeecg.modules.fwbz.securityStatistics.service.ISecurityStatisticsService;
import org.jeecg.modules.fwbz.securityStatistics.vo.SecurityStatCardVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 安防统计服务实现
 */
@Service
@AllArgsConstructor
public class SecurityStatisticsServiceImpl implements ISecurityStatisticsService {

    private final ICameraResourceService cameraResourceService;
    private final IPatrolPlanService patrolPlanService;
    private final PatrolHistoryMapper patrolHistoryMapper;
    private final IAlarmRecordService alarmRecordService;

    @Override
    public SecurityStatCardVO cameraTotal() {
        long total = cameraResourceService.count();
        long todayNew = cameraResourceService.count(
                new LambdaQueryWrapper<CameraResource>()
                        .ge(CameraResource::getCreateTime, getStartOfDay(0))
                        .le(CameraResource::getCreateTime, getEndOfDay(0))
        );

        SecurityStatCardVO vo = new SecurityStatCardVO();
        vo.setTitle("监控摄像头总数");
        vo.setValue(String.valueOf(total));
        vo.setContext(todayNew + " 新增");
        return vo;
    }

    @Override
    public SecurityStatCardVO cameraOnline() {
        long total = cameraResourceService.count();
        long online = cameraResourceService.count(
                new LambdaQueryWrapper<CameraResource>()
                        .eq(CameraResource::getOnline, 1)
        );

        String rateText;
        if (total == 0) {
            rateText = "0.0% 在线率";
        } else {
            double rate = (double) online / total * 100;
            rateText = String.format("%.1f%% 在线率", rate);
        }

        SecurityStatCardVO vo = new SecurityStatCardVO();
        vo.setTitle("在线摄像头");
        vo.setValue(String.valueOf(online));
        vo.setContext(rateText);
        return vo;
    }

    @Override
    public SecurityStatCardVO patrolPlanToday() {
        // 启用/运行中的巡更计划总数（排除已停用）
        long total = patrolPlanService.count(
                new LambdaQueryWrapper<PatrolPlan>()
                        .ne(PatrolPlan::getStatus, PatrolPlan.STATUS_DISABLED)
        );

        // 今日已执行的巡更记录数
        long completed = patrolHistoryMapper.selectCount(
                new LambdaQueryWrapper<PatrolHistory>()
                        .ge(PatrolHistory::getRunTime, getStartOfDay(0))
                        .le(PatrolHistory::getRunTime, getEndOfDay(0))
        );

        String rateText;
        if (total == 0) {
            rateText = "0% 完成";
        } else {
            double rate = (double) completed / total * 100;
            rateText = String.format("%.0f%% 完成", rate);
        }

        SecurityStatCardVO vo = new SecurityStatCardVO();
        vo.setTitle("今日视频巡更");
        vo.setValue(completed + "/" + total);
        vo.setContext(rateText);
        return vo;
    }

    @Override
    public SecurityStatCardVO aiEventAnalysis() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayStart.minusSeconds(1);

        long todayCount = alarmRecordService.countByAlarmTimeRange(todayStart, todayEnd);
        long yesterdayCount = alarmRecordService.countByAlarmTimeRange(yesterdayStart, yesterdayEnd);

        SecurityStatCardVO vo = new SecurityStatCardVO();
        vo.setTitle("AI分析事件");
        vo.setValue(String.valueOf(todayCount));
        vo.setContext(buildCompareContext(todayCount, yesterdayCount) + " 较昨日");
        return vo;
    }

    @Override
    public List<SecurityStatCardVO> getSummary() {
        return Arrays.asList(cameraTotal(), cameraOnline(), patrolPlanToday(), aiEventAnalysis());
    }

    /**
     * 获取指定偏移天的开始时间（0=今天，-1=昨天）
     */
    private Date getStartOfDay(int dayOffset) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, dayOffset);
        return cal.getTime();
    }

    /**
     * 获取指定偏移天的结束时间（0=今天，-1=昨天）
     */
    private Date getEndOfDay(int dayOffset) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        cal.add(Calendar.DAY_OF_MONTH, dayOffset);
        return cal.getTime();
    }

    /**
     * 构建较昨日的对比文案
     */
    private String buildCompareContext(long current, long last) {
        long diff = current - last;
        if (diff > 0) {
            return "↑" + diff;
        } else if (diff < 0) {
            return "↓" + (-diff);
        }
        return "-";
    }
}
