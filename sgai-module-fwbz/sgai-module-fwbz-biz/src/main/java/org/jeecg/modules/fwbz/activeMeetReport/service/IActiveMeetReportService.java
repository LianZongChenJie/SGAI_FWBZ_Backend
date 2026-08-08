package org.jeecg.modules.fwbz.activeMeetReport.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.activeMeetReport.entity.ActiveMeetReport;

/**
 * @Description: 展会总结报告
 * @Author: jeecg-boot
 * @Date:   2026-08-08
 * @Version: V1.0
 */
public interface IActiveMeetReportService extends IService<ActiveMeetReport> {

    /**
     * 生成总结报告（将状态从待总结更新为已总结，并汇总数据）
     *
     * @param report 报告数据
     */
    void generateReport(ActiveMeetReport report);

    /**
     * 根据活动名称查询报告
     *
     * @param activeName 活动名称
     * @return 报告实体
     */
    ActiveMeetReport getByActiveName(String activeName);

    /**
     * 根据活动信息同步活动报告
     * 无同名报告则新建，有则按规则更新日期范围
     *
     * @param activeName 活动名称
     * @param startDate  活动开始日期
     */
    void syncFromActivity(String activeName, java.util.Date startDate);
}
