package org.jeecg.modules.fwbz.activeMeetReport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetInfoMapper;
import org.jeecg.modules.fwbz.activeMeetReport.entity.ActiveMeetReport;
import org.jeecg.modules.fwbz.activeMeetReport.mapper.ActiveMeetReportMapper;
import org.jeecg.modules.fwbz.activeMeetReport.service.IActiveMeetReportService;
import org.jeecg.modules.fwbz.alarm.mapper.AlarmRecordMapper;
import org.jeecg.modules.fwbz.api.SgaiTpApi;
import org.jeecg.modules.fwbz.alarm.entity.AlarmRecord;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintInfo;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintType;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintInfoMapper;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintTypeMapper;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeterPointDataQueryDto;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointDataService;
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.mapper.VenueInfoMapper;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VenueFlowHour;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VenueFlowHourMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @Description: 展会总结报告
 * @Author: jeecg-boot
 * @Date: 2026-08-08
 * @Version: V1.0
 */
@Slf4j
@Service
public class ActiveMeetReportServiceImpl extends ServiceImpl<ActiveMeetReportMapper, ActiveMeetReport> implements IActiveMeetReportService {

    @Resource
    private ActiveMeetInfoMapper activeMeetInfoMapper;

    @Resource
    private VenueInfoMapper venueInfoMapper;

    @Resource
    private VenueFlowHourMapper venueFlowHourMapper;

    @Resource
    private ComplaintInfoMapper complaintInfoMapper;

    @Resource
    private ComplaintTypeMapper complaintTypeMapper;

    @Resource
    private AlarmRecordMapper alarmRecordMapper;
    @Resource
    private SgaiTpApi sgaiTpApi;

    private final IMeteringPointDataService service;

    public ActiveMeetReportServiceImpl(IMeteringPointDataService service) {
        this.service = service;
    }

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
    public void saveReport(ActiveMeetReport report) {
        if (report.getId() == null) {
            throw new JeecgBootException("报告ID不能为空");
        }
        baseMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ActiveMeetReport>()
                        .eq(ActiveMeetReport::getId, report.getId())
                        .set(ActiveMeetReport::getStatus, "1")
                        .set(ActiveMeetReport::getServicePersonnel, report.getServicePersonnel())
                        .set(ActiveMeetReport::getComplaintsTotal, report.getComplaintsTotal())
                        .set(ActiveMeetReport::getRecommendedTotal, report.getRecommendedTotal())
                        .set(ActiveMeetReport::getDeviceFailuresTotal, report.getDeviceFailuresTotal())
                        .set(ActiveMeetReport::getConsumptionElectricity, report.getConsumptionElectricity())
                        .set(ActiveMeetReport::getPersonEnergyConsumption, report.getPersonEnergyConsumption())
                        .set(ActiveMeetReport::getDayNumber, report.getDayNumber())
                        .set(ActiveMeetReport::getPassengerFlow, report.getPassengerFlow())
                        .set(ActiveMeetReport::getPeakFlow, report.getPeakFlow())
                        .set(ActiveMeetReport::getExhibitors, report.getExhibitors())
        );
    }

    @Override
    public ActiveMeetReport getByActiveName(String activeName) {
        return getOne(new LambdaQueryWrapper<ActiveMeetReport>()
                .eq(ActiveMeetReport::getActiveName, activeName));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromActivity(String activeName, Date startDate) {
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

    @Override
    public ActiveMeetReport computeReport(Long reportId) {
        ActiveMeetReport report = getById(reportId);
        if (report == null) {
            return null;
        }
        // 已总结，直接返回库中数据
        if ("1".equals(report.getStatus())) {
            return report;
        }

        // 未总结：根据活动名称查询所有活动信息
        List<ActiveMeetInfo> activities = activeMeetInfoMapper.selectList(
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .eq(ActiveMeetInfo::getActiveName, report.getActiveName())
        );

        if (activities == null || activities.isEmpty()) {
            return report;
        }

        // 关联场馆信息，填充计量点id（table_venue_info.point_id）
        for (ActiveMeetInfo activity : activities) {
            if (activity.getVenueId() == null) {
                continue;
            }
            VenueInfo venueInfo = venueInfoMapper.selectById(activity.getVenueId());
            if (venueInfo != null && venueInfo.getPointId() != null) {
                activity.setPointId(venueInfo.getPointId().toString());
            }
        }

        // 展会天数 = 活动信息条数
        report.setDayNumber((long) activities.size());

        // 总服务人次 & 总客流 & 峰值客流：逐条活动从场馆分时客流表取数
        long servicePersonnel = 0L;
        long passengerFlow = 0L;
        long peakFlow = 0L;
        for (ActiveMeetInfo activity : activities) {
            ServiceFlowResult flowResult = calcVenueFlow(activity);
            servicePersonnel += flowResult.servicePersonnel;
            passengerFlow += flowResult.passengerFlow;
            peakFlow += flowResult.peakFlow;
        }
        report.setServicePersonnel(servicePersonnel);
        report.setPassengerFlow(passengerFlow);
        report.setPeakFlow(peakFlow);

        // 投诉数量 & 建议数量：按会展时间段（报告开始日期~结束日期）区间统计
        long complaintsTotal = countComplaintsByType("投诉", report.getStartDate(), report.getEndDate());
        long recommendedTotal = countComplaintsByType("建议", report.getStartDate(), report.getEndDate());
        report.setComplaintsTotal(complaintsTotal);
        report.setRecommendedTotal(recommendedTotal);

        // 设备故障数：报告整体时间段内的报警数
        long deviceFailuresTotal = alarmRecordMapper.selectCount(
                new LambdaQueryWrapper<AlarmRecord>()
                        .ge(report.getStartDate() != null, AlarmRecord::getAlarmTime, report.getStartDate())
                        .le(report.getEndDate() != null, AlarmRecord::getAlarmTime, report.getEndDate())
        );
        report.setDeviceFailuresTotal(deviceFailuresTotal);

        // 总用电量：空方法返回0
        Double consumptionElectricity = calcConsumptionElectricity(activities);
        report.setConsumptionElectricity(consumptionElectricity);

        // 单人次能耗 = 总用电量 / 总服务人次，人次为0时即为总用电量
        double personEnergyConsumption = servicePersonnel > 0
                ? consumptionElectricity / servicePersonnel
                : consumptionElectricity;
        report.setPersonEnergyConsumption(personEnergyConsumption);

        // 参展商数：直接取库，空为0
        if (report.getExhibitors() == null) {
            report.setExhibitors(0L);
        }

        return report;
    }

    /**
     * 根据单条活动信息计算场馆分时客流数据
     */
    private ServiceFlowResult calcVenueFlow(ActiveMeetInfo activity) {
        ServiceFlowResult result = new ServiceFlowResult();
        if (activity.getVenueId() == null || activity.getStartDate() == null) {
            return result;
        }

        LocalDate localDate = activity.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        LambdaQueryWrapper<VenueFlowHour> wrapper = new LambdaQueryWrapper<VenueFlowHour>()
                .eq(VenueFlowHour::getVenueId, activity.getVenueId())
                .eq(VenueFlowHour::getDataDate, localDate);

        if (activity.getStartTime() != null) {
            wrapper.ge(VenueFlowHour::getDataHour, activity.getStartTime());
        }
        if (activity.getEndTime() != null) {
            wrapper.le(VenueFlowHour::getDataHour, activity.getEndTime());
        }

        List<VenueFlowHour> flowList = venueFlowHourMapper.selectList(wrapper);
        if (flowList == null || flowList.isEmpty()) {
            return result;
        }

        // 总服务人次：取该时间段内todayInCount的最大值（todayInCount为累计值）
        result.servicePersonnel = flowList.stream()
                .mapToLong(f -> f.getTodayInCount() != null ? f.getTodayInCount() : 0L)
                .max().orElse(0L);

        // 总客流：同总服务人次（todayInCount累计最大值）
        result.passengerFlow = result.servicePersonnel;

        // 峰值客流：maxCount总和（所有时段峰值累加）
        result.peakFlow = flowList.stream()
                .mapToLong(f -> f.getMaxCount() != null ? f.getMaxCount() : 0L)
                .sum();

        return result;
    }

    /**
     * 按投诉类型和时间段统计数量（含开始日期和结束日期）
     */
    private long countComplaintsByType(String typeName, Date startDate, Date endDate) {
        if (startDate == null) {
            return 0L;
        }
        // 查询类型ID
        ComplaintType complaintType = complaintTypeMapper.selectOne(
                new LambdaQueryWrapper<ComplaintType>()
                        .eq(ComplaintType::getTypeName, typeName));
        if (complaintType == null) {
            return 0L;
        }
        return complaintInfoMapper.selectCount(
                new LambdaQueryWrapper<ComplaintInfo>()
                        .eq(ComplaintInfo::getTypeId, complaintType.getId())
                        .ge(ComplaintInfo::getComplaintDate, startDate)
                        .le(endDate != null, ComplaintInfo::getComplaintDate, endDate)
        );
    }

    /**
     * 计算总用电量（空方法，待后续实现，当前返回0）
     */
    private double calcConsumptionElectricity(List<ActiveMeetInfo> activities) {
        BigDecimal totalElectricity = new BigDecimal(0);
        for (ActiveMeetInfo activity : activities) {
            LocalDateTime startTime = activity.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(activity.getStartTime().toLocalTime());
            LocalDateTime endTime = activity.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(activity.getEndTime().toLocalTime());
            try {
                // pointId 为空时无法查询用电量，跳过
                if (StringUtils.isBlank(activity.getPointId())) {
                    continue;
                }
                MeterPointDataQueryDto dto = new MeterPointDataQueryDto();
                dto.setPointId(activity.getPointId());
                dto.setStartTime(startTime);
                dto.setEndTime(endTime);
                totalElectricity = totalElectricity.add(service.findHourElectricityByDateRange(dto));
            } catch (Exception e) {
                log.error("调用sgai-tp用电量接口异常, startTime={}, endTime={}",startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), e);
            }
        }
        return Double.parseDouble(totalElectricity.toString());
    }

    /**
     * 场馆客流计算结果
     */
    private static class ServiceFlowResult {
        long servicePersonnel;
        long passengerFlow;
        long peakFlow;
    }
}
