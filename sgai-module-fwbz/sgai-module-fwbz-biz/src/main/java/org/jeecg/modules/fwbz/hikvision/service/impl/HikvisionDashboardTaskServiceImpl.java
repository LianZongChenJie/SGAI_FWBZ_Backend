package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.dto.CurrentOnsiteCountVO;
import org.jeecg.modules.fwbz.hikvision.dto.RecognitionRecordRequest;
import org.jeecg.modules.fwbz.hikvision.dto.RecognitionRecordResponse;
import org.jeecg.modules.fwbz.hikvision.dto.RecognitionRecordResponse.RecognitionRecordItem;
import org.jeecg.modules.fwbz.hikvision.dto.StatCardVO;
import org.jeecg.modules.fwbz.hikvision.dto.TodayEntryCountVO;
import org.jeecg.modules.fwbz.hikvision.entity.EventNotify;
import org.jeecg.modules.fwbz.hikvision.entity.PersonRecognition;
import org.jeecg.modules.fwbz.hikvision.entity.PersonnelStatistics;
import org.jeecg.modules.fwbz.hikvision.mapper.EventNotifyMapper;
import org.jeecg.modules.fwbz.hikvision.mapper.PersonRecognitionMapper;
import org.jeecg.modules.fwbz.hikvision.mapper.PersonnelStatisticsMapper;
import org.jeecg.modules.fwbz.hikvision.service.IHikvisionDashboardService;
import org.jeecg.modules.fwbz.hikvision.service.IHikvisionDashboardTaskService;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VisitorFlow;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VisitorFlowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 海康数据看板定时任务服务实现
 *
 * @author fwbz
 */
@Slf4j
@Service
public class HikvisionDashboardTaskServiceImpl implements IHikvisionDashboardTaskService {

    private final IHikvisionDashboardService dashboardService;
    private final VisitorFlowMapper visitorFlowMapper;
    private final PersonnelStatisticsMapper personnelStatisticsMapper;
    private final PersonRecognitionMapper personRecognitionMapper;
    private final EventNotifyMapper eventNotifyMapper;

    public HikvisionDashboardTaskServiceImpl(IHikvisionDashboardService dashboardService,
                                             VisitorFlowMapper visitorFlowMapper,
                                             PersonnelStatisticsMapper personnelStatisticsMapper,
                                             PersonRecognitionMapper personRecognitionMapper,
                                             EventNotifyMapper eventNotifyMapper) {
        this.dashboardService = dashboardService;
        this.visitorFlowMapper = visitorFlowMapper;
        this.personnelStatisticsMapper = personnelStatisticsMapper;
        this.personRecognitionMapper = personRecognitionMapper;
        this.eventNotifyMapper = eventNotifyMapper;
    }

    // ========== 同步 ==========

    @Override
    public void syncVisitorFlow() {
        log.debug("海康客流数据同步开始");
        VisitorFlow entity = getOrCreateTodayVisitorFlow();

        try {
            TodayEntryCountVO entryVo = dashboardService.getTodayEntryCount();
            entity.setTodayCount(entryVo.getEntryCount().longValue());
        } catch (Exception e) {
            log.error("获取今日进场人数失败", e);
        }

        try {
            CurrentOnsiteCountVO onsiteVo = dashboardService.getCurrentOnsiteCount();
            long nowCount = onsiteVo.getOnsiteCount().longValue();
            entity.setNowCount(nowCount);
            long currentMax = entity.getMaxCount() != null ? entity.getMaxCount() : 0L;
            if (nowCount > currentMax) {
                entity.setMaxCount(nowCount);
            }
        } catch (Exception e) {
            log.error("获取当前在场人数失败", e);
        }

        insertOrUpdateVisitorFlow(entity);
        log.info("海康客流数据同步完成, todayCount={}, nowCount={}, maxCount={}",
                entity.getTodayCount(), entity.getNowCount(), entity.getMaxCount());
    }

    @Override
    public void syncPersonnelStatistics() {
        log.debug("人员统计数据同步开始");
        LocalDate today = LocalDate.now();

        PersonnelStatistics entity = personnelStatisticsMapper.selectOne(
                new LambdaQueryWrapper<PersonnelStatistics>()
                        .eq(PersonnelStatistics::getStatDate, today));
        if (entity == null) {
            entity = new PersonnelStatistics();
            entity.setStatDate(today);
        }

        // 今日进场 + 当前在场：从 Hikvision API 获取
        try {
            TodayEntryCountVO entryVo = dashboardService.getTodayEntryCount();
            entity.setTodayEntryCount(entryVo.getEntryCount().longValue());
        } catch (Exception e) {
            log.error("获取今日进场人数失败", e);
        }
        try {
            CurrentOnsiteCountVO onsiteVo = dashboardService.getCurrentOnsiteCount();
            entity.setCurrentInCount(onsiteVo.getOnsiteCount().longValue());
        } catch (Exception e) {
            log.error("获取当前在场人数失败", e);
        }

        // 人员识别记录数：从 table_person_recognition 统计当日
        entity.setRecognitionRecordCount(
                personRecognitionMapper.selectCount(
                        new LambdaQueryWrapper<PersonRecognition>()
                                .ge(PersonRecognition::getRecognizeTime, today.atStartOfDay())
                                .lt(PersonRecognition::getRecognizeTime, today.plusDays(1).atStartOfDay())));

        if (entity.getId() != null) {
            personnelStatisticsMapper.updateById(entity);
        } else {
            personnelStatisticsMapper.insert(entity);
        }
        log.info("人员统计数据同步完成, entry={}, in={}, recognition={}",
                entity.getTodayEntryCount(), entity.getCurrentInCount(),
                entity.getRecognitionRecordCount());
    }

    @Override
    public void syncPersonRecognition() {
        log.debug("人员识别记录同步开始");
        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        // 1. 确定查询起始时间：从 DB 中今日最大识别时间开始，避免重复拉取
        LocalDateTime startLdt = getMaxRecognizeTimeToday(today);
        if (startLdt == null) {
            startLdt = today.atStartOfDay();
        }
        String startTime = startLdt.atZone(zone).format(formatter);
        String endTime = ZonedDateTime.now(zone).format(formatter);

        log.info("人员识别记录查询时间范围: {} ~ {}", startTime, endTime);

        // 2. 分页获取识别记录
        int pageNo = 1;
        int pageSize = 500;
        int totalSaved = 0;
        int total;

        do {
            RecognitionRecordRequest request = new RecognitionRecordRequest()
                    .setStartTime(startTime)
                    .setEndTime(endTime)
                    .setPageNo(pageNo)
                    .setPageSize(pageSize);

            RecognitionRecordResponse response = dashboardService.getRecognitionRecords(request);
            List<RecognitionRecordItem> items = response.getList();
            total = response.getTotal() != null ? response.getTotal() : 0;

            if (items != null && !items.isEmpty()) {
                for (RecognitionRecordItem item : items) {
                    try {
                        PersonRecognition entity = convertToPersonRecognition(item);
                        personRecognitionMapper.insert(entity);
                        totalSaved++;
                    } catch (Exception e) {
                        log.warn("插入人员识别记录失败, eventId={}", item.getEventId(), e);
                    }
                }
            }
            pageNo++;
        } while ((pageNo - 1) * pageSize < total);

        log.info("人员识别记录同步完成, 共插入{}条", totalSaved);
    }

    /**
     * 将海康API返回的识别记录转换为 PersonRecognition 实体
     */
    private PersonRecognition convertToPersonRecognition(RecognitionRecordItem item) {
        PersonRecognition entity = new PersonRecognition();
        // 解析识别时间
        if (item.getEventTime() != null) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(item.getEventTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                entity.setRecognizeTime(odt.toLocalDateTime());
            } catch (Exception e) {
                log.warn("解析识别时间失败: {}", item.getEventTime());
            }
        }
        entity.setPersonName(item.getPersonName());
        entity.setRecognizeLocation(item.getCameraName());
        // 解析相似度为置信度
        if (item.getSimilarity() != null) {
            try {
                entity.setConfidence(new BigDecimal(item.getSimilarity()));
            } catch (Exception e) {
                log.warn("解析相似度失败: {}", item.getSimilarity());
            }
        }
        entity.setGmtCreate(LocalDateTime.now());
        entity.setGmtModified(LocalDateTime.now());
        return entity;
    }

    /**
     * 查询今日表中最大的识别时间
     */
    private LocalDateTime getMaxRecognizeTimeToday(LocalDate today) {
        PersonRecognition latest = personRecognitionMapper.selectOne(
                new LambdaQueryWrapper<PersonRecognition>()
                        .ge(PersonRecognition::getRecognizeTime, today.atStartOfDay())
                        .lt(PersonRecognition::getRecognizeTime, today.plusDays(1).atStartOfDay())
                        .orderByDesc(PersonRecognition::getRecognizeTime)
                        .last("LIMIT 1"));
        return latest != null ? latest.getRecognizeTime() : null;
    }

    // ========== 查询 ==========

    @Override
    public TodayEntryCountVO queryTodayEntryCount() {
        PersonnelStatistics today = getTodayPersonnelStatistics();
        if (today == null || today.getTodayEntryCount() == null) {
            return TodayEntryCountVO.of(0);
        }
        return TodayEntryCountVO.of(today.getTodayEntryCount().intValue());
    }

    @Override
    public CurrentOnsiteCountVO queryCurrentOnsiteCount() {
        PersonnelStatistics today = getTodayPersonnelStatistics();
        if (today == null) {
            return CurrentOnsiteCountVO.of(0);
        }
        CurrentOnsiteCountVO vo = CurrentOnsiteCountVO.of(
                today.getCurrentInCount() != null ? today.getCurrentInCount().intValue() : 0);
        return vo;
    }

    @Override
    public Page<PersonRecognition> queryRecognitionRecords(int pageNo, int pageSize) {
        LocalDate today = LocalDate.now();
        Page<PersonRecognition> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<PersonRecognition> qw = new LambdaQueryWrapper<PersonRecognition>()
                .ge(PersonRecognition::getRecognizeTime, today.atStartOfDay())
                .lt(PersonRecognition::getRecognizeTime, today.plusDays(1).atStartOfDay())
                .orderByDesc(PersonRecognition::getRecognizeTime);
        return personRecognitionMapper.selectPage(page, qw);
    }

    @Override
    public Page<EventNotify> queryAbnormalAlerts(int pageNo, int pageSize) {
        LocalDate today = LocalDate.now();
        Page<EventNotify> page = new Page<>(pageNo, pageSize);
        // happen_time 是 ISO8601 字符串格式，用 like 匹配当天日期
        String datePrefix = today.toString(); // "2026-08-06"
        LambdaQueryWrapper<EventNotify> qw = new LambdaQueryWrapper<EventNotify>()
                .likeRight(EventNotify::getHappenTime, datePrefix)
                .orderByDesc(EventNotify::getHappenTime);
        return eventNotifyMapper.selectPage(page, qw);
    }

    // ========== 看板统计卡片 ==========

    @Override
    public StatCardVO getTodayEntryCard() {
        LocalDate today = LocalDate.now();
        VisitorFlow todayFlow = getVisitorFlowByDate(today);
        VisitorFlow yesterdayFlow = getVisitorFlowByDate(today.minusDays(1));
        long todayCount = extractCount(todayFlow, VisitorFlow::getTodayCount);
        long yesterdayCount = extractCount(yesterdayFlow, VisitorFlow::getTodayCount);
        return buildCard("今日进场人数", todayCount, yesterdayCount);
    }

    @Override
    public StatCardVO getCurrentOnsiteCard() {
        LocalDate today = LocalDate.now();
        VisitorFlow todayFlow = getVisitorFlowByDate(today);
        VisitorFlow yesterdayFlow = getVisitorFlowByDate(today.minusDays(1));
        long todayCount = extractCount(todayFlow, VisitorFlow::getNowCount);
        long yesterdayCount = extractCount(yesterdayFlow, VisitorFlow::getNowCount);
        return buildCard("当前在场人数", todayCount, yesterdayCount);
    }

    @Override
    public StatCardVO getRecognitionRecordCard() {
        LocalDate today = LocalDate.now();
        long todayCount = countPersonRecognition(today);
        long yesterdayCount = countPersonRecognition(today.minusDays(1));
        return buildCard("人员识别记录", todayCount, yesterdayCount);
    }

    @Override
    public StatCardVO getAbnormalAlertCard() {
        LocalDate today = LocalDate.now();
        long todayCount = countEventNotify(today);
        long yesterdayCount = countEventNotify(today.minusDays(1));
        return buildCard("异常行为预警", todayCount, yesterdayCount);
    }

    @Override
    public List<StatCardVO> getSummaryCards() {
        List<StatCardVO> list = new ArrayList<>(4);
        list.add(getTodayEntryCard());
        list.add(getCurrentOnsiteCard());
        list.add(getRecognitionRecordCard());
        list.add(getAbnormalAlertCard());
        return list;
    }

    // ========== 私有方法 ==========

    private VisitorFlow getVisitorFlowByDate(LocalDate date) {
        return visitorFlowMapper.selectOne(
                new LambdaQueryWrapper<VisitorFlow>()
                        .eq(VisitorFlow::getDate, date));
    }

    private long extractCount(VisitorFlow flow, java.util.function.Function<VisitorFlow, Long> getter) {
        return flow != null && getter.apply(flow) != null ? getter.apply(flow) : 0L;
    }

    private long countPersonRecognition(LocalDate date) {
        return personRecognitionMapper.selectCount(
                new LambdaQueryWrapper<PersonRecognition>()
                        .ge(PersonRecognition::getRecognizeTime, date.atStartOfDay())
                        .lt(PersonRecognition::getRecognizeTime, date.plusDays(1).atStartOfDay()));
    }

    private long countEventNotify(LocalDate date) {
        return eventNotifyMapper.selectCount(
                new LambdaQueryWrapper<EventNotify>()
                        .likeRight(EventNotify::getHappenTime, date.toString()));
    }

    private StatCardVO buildCard(String title, long todayCount, long yesterdayCount) {
        StatCardVO vo = new StatCardVO();
        vo.setTitle(title);
        vo.setValue(todayCount);
        vo.setContext(buildTrendContext(todayCount, yesterdayCount));
        return vo;
    }

    private String buildTrendContext(long todayCount, long yesterdayCount) {
        if (todayCount == yesterdayCount) {
            return "— 较昨日";
        }
        long diff = todayCount - yesterdayCount;
        String arrow = diff > 0 ? "↑" : "↓";
        String trend;
        if (yesterdayCount == 0 || todayCount < 100 || yesterdayCount < 100) {
            trend = NumberFormat.getInstance(Locale.CHINA).format(Math.abs(diff));
        } else {
            double percent = Math.abs(diff) * 100.0 / yesterdayCount;
            trend = String.format(Locale.ROOT, "%.1f%%", percent);
        }
        return arrow + trend + " 较昨日";
    }

    private VisitorFlow getOrCreateTodayVisitorFlow() {
        VisitorFlow today = visitorFlowMapper.selectOne(
                new LambdaQueryWrapper<VisitorFlow>()
                        .eq(VisitorFlow::getDate, LocalDate.now()));
        if (today == null) {
            today = new VisitorFlow();
            today.setDate(LocalDate.now());
        }
        return today;
    }

    private void insertOrUpdateVisitorFlow(VisitorFlow entity) {
        if (entity.getId() != null) {
            visitorFlowMapper.updateById(entity);
        } else {
            visitorFlowMapper.insert(entity);
        }
    }

    private PersonnelStatistics getTodayPersonnelStatistics() {
        return personnelStatisticsMapper.selectOne(
                new LambdaQueryWrapper<PersonnelStatistics>()
                        .eq(PersonnelStatistics::getStatDate, LocalDate.now()));
    }


}
