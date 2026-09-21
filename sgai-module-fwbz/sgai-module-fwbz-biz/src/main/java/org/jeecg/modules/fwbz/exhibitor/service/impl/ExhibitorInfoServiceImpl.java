package org.jeecg.modules.fwbz.exhibitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetInfoMapper;
import org.jeecg.modules.fwbz.activeMeetReport.entity.ActiveMeetReport;
import org.jeecg.modules.fwbz.activeMeetReport.mapper.ActiveMeetReportMapper;
import org.jeecg.modules.fwbz.exhibitor.ExhibitorInfo;
import org.jeecg.modules.fwbz.exhibitor.mapper.ExhibitorInfoMapper;
import org.jeecg.modules.fwbz.exhibitor.service.IExhibitorInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 参展厂商信息
 * @Author: jeecg-boot
 * @Date:   2026-09-02
 * @Version: V1.0
 */
@Service
public class ExhibitorInfoServiceImpl extends ServiceImpl<ExhibitorInfoMapper, ExhibitorInfo> implements IExhibitorInfoService {

    @Resource
    private ActiveMeetReportMapper activeMeetReportMapper;

    @Resource
    private ActiveMeetInfoMapper activeMeetInfoMapper;

    @Override
    public List<ExhibitorInfo> getListByVenueId(Long venueId) {
        return list(new LambdaQueryWrapper<ExhibitorInfo>()
                .eq(ExhibitorInfo::getVenueId, venueId));
    }

    @Override
    public Long countByVenueId(Long venueId) {
        return count(new LambdaQueryWrapper<ExhibitorInfo>()
                .eq(ExhibitorInfo::getVenueId, venueId));
    }

    @Override
    public Map<Long, Long> countGroupByVenueId(List<Long> venueIds) {
        if (venueIds == null || venueIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return list(new LambdaQueryWrapper<ExhibitorInfo>()
                .select(ExhibitorInfo::getVenueId)
                .in(ExhibitorInfo::getVenueId, venueIds))
                .stream()
                .collect(Collectors.groupingBy(ExhibitorInfo::getVenueId, Collectors.counting()));
    }

    @Override
    public List<ExhibitorInfo> getListByReportId(Long reportId) {
        if (reportId == null) {
            return Collections.emptyList();
        }
        // 1. 根据报告id查询展会总结报告
        ActiveMeetReport report = activeMeetReportMapper.selectById(reportId);
        if (report == null || StringUtils.isBlank(report.getActiveName())) {
            return Collections.emptyList();
        }
        // 2. 根据报告活动名称查询所有会展活动，提取去重后的场馆id
        List<Long> venueIds = activeMeetInfoMapper.selectList(
                        new LambdaQueryWrapper<ActiveMeetInfo>()
                                .eq(ActiveMeetInfo::getActiveName, report.getActiveName()))
                .stream()
                .map(ActiveMeetInfo::getVenueId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (venueIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 3. 根据场馆id列表查询参展商列表
        return list(new LambdaQueryWrapper<ExhibitorInfo>()
                .in(ExhibitorInfo::getVenueId, venueIds));
    }
}
