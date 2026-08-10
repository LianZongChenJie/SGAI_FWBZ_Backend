package org.jeecg.modules.fwbz.activeMeetReport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.activeMeetReport.entity.ActiveMeetReport;
import org.jeecg.modules.fwbz.activeMeetReport.mapper.ActiveMeetReportMapper;
import org.jeecg.modules.fwbz.activeMeetReport.service.IActiveMeetReportStatisticsService;
import org.jeecg.modules.fwbz.activeMeetReport.vo.StatCardVO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 活动报告统计服务实现
 */
@Service
public class ActiveMeetReportStatisticsServiceImpl extends ServiceImpl<ActiveMeetReportMapper, ActiveMeetReport>
        implements IActiveMeetReportStatisticsService {

    @Override
    public StatCardVO countPendingSummary() {
        long count = count(new LambdaQueryWrapper<ActiveMeetReport>()
                .eq(ActiveMeetReport::getStatus, "0"));

        StatCardVO vo = new StatCardVO();
        vo.setTitle("待总结展会");
        vo.setValue(count);
        vo.setContext("需出具报告");
        return vo;
    }

    @Override
    public StatCardVO countSummarized() {
        long thisMonthCount = countByMonthAndStatus(0, "1");
        long lastMonthCount = countByMonthAndStatus(-1, "1");

        StatCardVO vo = new StatCardVO();
        vo.setTitle("已总结展会");
        vo.setValue(thisMonthCount);
        vo.setContext(buildCompareContext(thisMonthCount, lastMonthCount) + " 本月");
        return vo;
    }

    @Override
    public StatCardVO reportGeneration() {
        StatCardVO vo = new StatCardVO();
        vo.setTitle("报告生成");
        vo.setValue("AI");
        vo.setContext("自动+人工");
        return vo;
    }

    @Override
    public StatCardVO knowledgeBaseAccumulation() {
        StatCardVO vo = new StatCardVO();
        vo.setTitle("知识库积累");
        vo.setValue(156);
        vo.setContext("↑12 条经验");
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        return Arrays.asList(
                countPendingSummary(),
                countSummarized(),
                reportGeneration(),
                knowledgeBaseAccumulation()
        );
    }

    /**
     * 按月份偏移和状态统计报告数量
     *
     * @param monthOffset 0=本月, -1=上月
     * @param status      状态 0=待总结, 1=已总结
     */
    private long countByMonthAndStatus(int monthOffset, String status) {
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

        return count(new LambdaQueryWrapper<ActiveMeetReport>()
                .eq(ActiveMeetReport::getStatus, status)
                .ge(ActiveMeetReport::getStartDate, monthStart)
                .le(ActiveMeetReport::getStartDate, monthEnd));
    }

    /**
     * 构建较上月对比文案
     */
    private String buildCompareContext(double current, double last) {
        double diff = current - last;
        if (diff > 0) {
            return "↑" + formatDiff(diff);
        } else if (diff < 0) {
            return "↓" + formatDiff(-diff);
        }
        return "-";
    }

    /**
     * 格式化差值（整数不带小数）
     */
    private String formatDiff(double diff) {
        if (diff == (long) diff) {
            return String.valueOf((long) diff);
        }
        return String.format("%.1f", diff);
    }
}
