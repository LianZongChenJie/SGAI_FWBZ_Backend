package org.jeecg.modules.fwbz.activeMeetPreparation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetInfoMapper;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetsDeviceTypeMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.service.IPreparationStatisticsService;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class PreparationStatisticsServiceImpl implements IPreparationStatisticsService {

    private final ActiveMeetInfoMapper activeMeetInfoMapper;
    private final ActiveMeetsDeviceTypeMapper activeMeetsDeviceTypeMapper;

    public PreparationStatisticsServiceImpl(ActiveMeetInfoMapper activeMeetInfoMapper,
                                            ActiveMeetsDeviceTypeMapper activeMeetsDeviceTypeMapper) {
        this.activeMeetInfoMapper = activeMeetInfoMapper;
        this.activeMeetsDeviceTypeMapper = activeMeetsDeviceTypeMapper;
    }

    @Override
    public StatCardVO pendingCount() {
        Date tomorrow = getTomorrowStart();
        long currentCount = activeMeetInfoMapper.selectCount(
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .ge(ActiveMeetInfo::getStartDate, tomorrow));

        Date lastWeekTomorrow = getLastWeekTomorrow();
        long lastWeekCount = activeMeetInfoMapper.selectCount(
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .ge(ActiveMeetInfo::getStartDate, lastWeekTomorrow));

        StatCardVO vo = new StatCardVO();
        vo.setTitle("待筹备会展");
        vo.setValue(currentCount);
        vo.setContext(buildCompareContext(currentCount, lastWeekCount, " 新增"));
        return vo;
    }

    @Override
    public StatCardVO completionRate() {
        Date tomorrow = getTomorrowStart();
        List<ActiveMeetInfo> currentList = activeMeetInfoMapper.selectList(
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .ge(ActiveMeetInfo::getStartDate, tomorrow)
                        .isNotNull(ActiveMeetInfo::getActiveProgress));

        double currentRate = calcAvgProgress(currentList);

        Date lastWeekTomorrow = getLastWeekTomorrow();
        List<ActiveMeetInfo> lastWeekList = activeMeetInfoMapper.selectList(
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .ge(ActiveMeetInfo::getStartDate, lastWeekTomorrow)
                        .isNotNull(ActiveMeetInfo::getActiveProgress));

        double lastWeekRate = calcAvgProgress(lastWeekList);

        StatCardVO vo = new StatCardVO();
        vo.setTitle("筹备完成率");
        vo.setValue(Math.round(currentRate * 100.0) / 100.0);
        vo.setContext(buildCompareContext(currentRate, lastWeekRate, "% 较上周"));
        return vo;
    }

    @Override
    public StatCardVO tomorrowCount() {
        Date tomorrowStart = getTomorrowStart();
        Date dayAfterTomorrow = getDayAfterTomorrowStart();

        long count = activeMeetInfoMapper.selectCount(
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .ge(ActiveMeetInfo::getStartDate, tomorrowStart)
                        .lt(ActiveMeetInfo::getStartDate, dayAfterTomorrow));

        List<ActiveMeetInfo> list = activeMeetInfoMapper.selectList(
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .ge(ActiveMeetInfo::getStartDate, tomorrowStart)
                        .lt(ActiveMeetInfo::getStartDate, dayAfterTomorrow)
                        .orderByAsc(ActiveMeetInfo::getStartDate)
                        .last("limit 1"));

        StatCardVO vo = new StatCardVO();
        vo.setTitle("明日开展");
        vo.setValue(count);
        if (list != null && !list.isEmpty()) {
            vo.setContext(list.get(0).getActiveName());
        } else {
            vo.setContext("暂无");
        }
        return vo;
    }

    @Override
    public StatCardVO checkItemCount() {
        long total = activeMeetsDeviceTypeMapper.selectCount(null);

        StatCardVO vo = new StatCardVO();
        vo.setTitle("会展检查项");
        vo.setValue(total);
        vo.setContext("全部覆盖");
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        return Arrays.asList(pendingCount(), completionRate(), tomorrowCount(), checkItemCount());
    }

    /**
     * 获取明天 00:00:00
     */
    private Date getTomorrowStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * 获取后天 00:00:00
     */
    private Date getDayAfterTomorrowStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 2);
        return cal.getTime();
    }

    /**
     * 获取上周明天 00:00:00（用于较上周对比）
     */
    private Date getLastWeekTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);  // 明天
        cal.add(Calendar.DAY_OF_MONTH, -7); // 上周明天
        return cal.getTime();
    }

    /**
     * 计算活动列表的平均进度
     */
    private double calcAvgProgress(List<ActiveMeetInfo> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        double sum = 0;
        int count = 0;
        for (ActiveMeetInfo info : list) {
            if (info.getActiveProgress() != null) {
                sum += info.getActiveProgress();
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    /**
     * 构建较上周的对比文案
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
