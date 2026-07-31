package org.jeecg.modules.fwbz.activeMeetStatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetInfoMapper;
import org.jeecg.modules.fwbz.activeMeetStatistics.service.IActiveMeetStatisticsService;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ActiveMeetStatisticsServiceImpl extends ServiceImpl<ActiveMeetInfoMapper, ActiveMeetInfo> implements IActiveMeetStatisticsService {

    private final IVenueInfoService venueInfoService;

    public ActiveMeetStatisticsServiceImpl(IVenueInfoService venueInfoService) {
        this.venueInfoService = venueInfoService;
    }

    @Override
    public StatCardVO countThisMonth() {
        long thisMonthCount = countByMonth(0);
        long lastMonthCount = countByMonth(-1);

        StatCardVO vo = new StatCardVO();
        vo.setTitle("本月活动数");
        vo.setValue(thisMonthCount);
        vo.setContext(buildCompareContext(thisMonthCount, lastMonthCount, ""));
        return vo;
    }

    @Override
    public StatCardVO countToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        Date endOfDay = cal.getTime();

        long todayCount = count(new LambdaQueryWrapper<ActiveMeetInfo>()
                .ge(ActiveMeetInfo::getStartDate, startOfDay)
                .le(ActiveMeetInfo::getStartDate, endOfDay));

        StatCardVO vo = new StatCardVO();
        vo.setTitle("今日活动数");
        vo.setValue(todayCount);
        vo.setContext("进行中");
        return vo;
    }

    @Override
    public StatCardVO countNextWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date nextWeekStart = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date nextWeekEnd = cal.getTime();

        long nextWeekCount = count(new LambdaQueryWrapper<ActiveMeetInfo>()
                .ge(ActiveMeetInfo::getStartDate, nextWeekStart)
                .le(ActiveMeetInfo::getStartDate, nextWeekEnd));

        StatCardVO vo = new StatCardVO();
        vo.setTitle("待筹备活动");
        vo.setValue(nextWeekCount);
        vo.setContext("下周开始");
        return vo;
    }

    @Override
    public StatCardVO venueUtilization() {
        double thisMonthRate = calcUtilizationRate(0);
        double lastMonthRate = calcUtilizationRate(-1);

        StatCardVO vo = new StatCardVO();
        vo.setTitle("场馆利用率");
        vo.setValue(Math.round(thisMonthRate * 100.0) / 100.0);
        vo.setContext(buildCompareContext(thisMonthRate, lastMonthRate, "%"));
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        return Arrays.asList(countThisMonth(), countToday(), countNextWeek(), venueUtilization());
    }

    /**
     * 计算指定月份偏移的活动数
     * @param monthOffset 0=本月, -1=上月
     */
    private long countByMonth(int monthOffset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, monthOffset);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date monthStart = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date monthEnd = cal.getTime();

        return count(new LambdaQueryWrapper<ActiveMeetInfo>()
                .ge(ActiveMeetInfo::getStartDate, monthStart)
                .le(ActiveMeetInfo::getStartDate, monthEnd));
    }

    /**
     * 计算指定月份偏移的场馆利用率
     * @param monthOffset 0=本月, -1=上月
     */
    private double calcUtilizationRate(int monthOffset) {
        long totalVenues = venueInfoService.count();
        if (totalVenues == 0) {
            return 0;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, monthOffset);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date monthStart = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date monthEnd = cal.getTime();

        List<ActiveMeetInfo> list = super.list(new LambdaQueryWrapper<ActiveMeetInfo>()
                .select(ActiveMeetInfo::getVenueId)
                .ge(ActiveMeetInfo::getStartDate, monthStart)
                .le(ActiveMeetInfo::getStartDate, monthEnd));
        long usedVenues = list.stream()
                .map(ActiveMeetInfo::getVenueId)
                .distinct()
                .count();

        return (double) usedVenues / totalVenues * 100;
    }

    /**
     * 构建较上月的对比文案
     * @param current 本月值
     * @param last 上月值
     * @param suffix 后缀（如 %）
     * @return 如 "↑3"、"↓2"、""(持平)
     */
    private String buildCompareContext(double current, double last, String suffix) {
        double diff = current - last;
        if (diff > 0) {
            return "↑" + formatDiff(diff) + suffix;
        } else if (diff < 0) {
            return "↓" + formatDiff(-diff) + suffix;
        }
        return "";
    }

    /**
     * 格式化差值（整数不带小数，非整数保留1位）
     */
    private String formatDiff(double diff) {
        if (diff == (long) diff) {
            return String.valueOf((long) diff);
        }
        return String.format("%.1f", diff);
    }
}
