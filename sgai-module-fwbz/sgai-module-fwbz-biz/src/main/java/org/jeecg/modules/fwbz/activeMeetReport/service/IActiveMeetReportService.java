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
     * 保存总结报告（仅根据id更新数据字段，状态置为已总结）
     *
     * @param report 报告数据
     */
    void saveReport(ActiveMeetReport report);

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

    /**
     * 根据报告ID查询并动态计算报告数据
     * 已总结(status=1)直接返回，未总结则从各表实时计算
     *
     * @param reportId 报告ID
     * @return 计算后的报告实体
     */
    ActiveMeetReport computeReport(Long reportId);
}
