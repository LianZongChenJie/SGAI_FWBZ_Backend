package org.jeecg.modules.fwbz.activeMeetReport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.activeMeetReport.entity.ActiveMeetReport;
import org.jeecg.modules.fwbz.activeMeetReport.mapper.ActiveMeetReportMapper;
import org.jeecg.modules.fwbz.activeMeetReport.service.IActiveMeetReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 展会总结报告
 * @Author: jeecg-boot
 * @Date:   2026-08-08
 * @Version: V1.0
 */
@Service
public class ActiveMeetReportServiceImpl extends ServiceImpl<ActiveMeetReportMapper, ActiveMeetReport> implements IActiveMeetReportService {

    @Override
    public boolean save(ActiveMeetReport entity) {
        // 校验活动名称是否已存在
        if (count(new LambdaQueryWrapper<ActiveMeetReport>()
                .eq(ActiveMeetReport::getActiveName, entity.getActiveName())) > 0) {
            throw new JeecgBootException("该活动名称已存在报告！");
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(ActiveMeetReport entity) {
        // 校验活动名称是否重复（排除自身）
        if (count(new LambdaQueryWrapper<ActiveMeetReport>()
                .eq(ActiveMeetReport::getActiveName, entity.getActiveName())
                .ne(ActiveMeetReport::getId, entity.getId())) > 0) {
            throw new JeecgBootException("该活动名称已存在报告！");
        }
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateReport(ActiveMeetReport report) {
        report.setStatus("1");
        if (report.getId() != null) {
            updateById(report);
        } else {
            save(report);
        }
    }

    @Override
    public ActiveMeetReport getByActiveName(String activeName) {
        return getOne(new LambdaQueryWrapper<ActiveMeetReport>()
                .eq(ActiveMeetReport::getActiveName, activeName));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromActivity(String activeName, java.util.Date startDate) {
        if (activeName == null || activeName.isEmpty()) {
            return;
        }
        ActiveMeetReport existing = getByActiveName(activeName);
        if (existing == null) {
            // 无同名报告：新建，开始日期和结束日期均为活动日期
            ActiveMeetReport report = new ActiveMeetReport();
            report.setActiveName(activeName);
            report.setStartDate(startDate);
            report.setEndDate(startDate);
            report.setStatus("0");
            baseMapper.insert(report);
        } else {
            // 已有报告：按规则更新日期范围
            boolean needUpdate = false;
            if (startDate != null && existing.getStartDate() != null
                    && startDate.before(existing.getStartDate())) {
                existing.setStartDate(startDate);
                needUpdate = true;
            }
            if (startDate != null && existing.getEndDate() != null
                    && startDate.after(existing.getEndDate())) {
                existing.setEndDate(startDate);
                needUpdate = true;
            }
            if (needUpdate) {
                baseMapper.updateById(existing);
            }
        }
    }
}
