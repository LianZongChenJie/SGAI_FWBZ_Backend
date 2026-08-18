package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.hikvision.entity.DoorEvent;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventListVO;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventPageDto;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventSearchResponse;
import org.jeecg.modules.fwbz.hikvision.service.IDoorEventService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.hikvision.mapper.DoorEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 门禁点事件增量同步服务实现
 * <p>每次同步以DB中最新的 event_time 为 startTime，当前时间为 endTime，
 * 仅拉取增量事件，按 event_id 去重后批量插入。</p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class DoorEventServiceImpl extends ServiceImpl<DoorEventMapper, DoorEvent>
        implements IDoorEventService {

    private static final String DOOR_EVENT_API = "/api/acs/v2/door/events";

    private static final int PAGE_SIZE = 1000;

    /** 数据库为空时，默认往前回溯的天数（API限制最大3个月） */
    private static final int DEFAULT_LOOKBACK_DAYS = 7;

    private final HikvisionUtil hikvisionUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromHikvision() {
        log.info("开始从海康平台增量同步门禁点事件...");

        // 1. 确定时间范围
        String startTime = resolveStartTime();
        String endTime = ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        log.info("事件同步时间范围: startTime={}, endTime={}", startTime, endTime);

        // 2. 逐页拉取海康事件数据
        List<DoorEventSearchResponse.DoorEventItem> allItems = fetchAllFromHikvision(startTime, endTime);
        if (allItems.isEmpty()) {
            log.info("海康在该时间范围内无新事件，跳过同步");
            return 0;
        }
        log.info("从海康获取到{}条事件记录", allItems.size());

        // 3. 根据 event_id 去重，过滤已存在的记录
        Set<String> existingEventIds = getExistingEventIds(allItems);
        List<DoorEvent> newEvents = new ArrayList<>();
        for (DoorEventSearchResponse.DoorEventItem item : allItems) {
            if (!existingEventIds.contains(item.getEventId())) {
                newEvents.add(convertToEntity(item));
            }
        }

        if (newEvents.isEmpty()) {
            log.info("所有事件已存在，无新增, 总数={}", allItems.size());
            return 0;
        }

        // 4. 批量插入新增事件
        Date now = new Date();
        for (DoorEvent event : newEvents) {
            event.setGmtCreate(now);
            event.setGmtModified(now);
        }
        // 达梦驱动对JDBC批量(executeBatch)支持有缺陷，会报index out of range/TypeException，改为循环单条插入绕开该问题
        for (DoorEvent event : newEvents) {
            baseMapper.insert(event);
        }
        log.info("门禁点事件增量同步完成, 获取{}条, 新增{}条", allItems.size(), newEvents.size());
        return newEvents.size();
    }

    /**
     * 解析起始时间：取 DB 中最新的 event_time（格式归一化为海康要求的ISO8601带时区格式）；
     * DB 为空或格式无法解析时往前回溯90天。
     */
    private String resolveStartTime() {
        // 查询数据库中最新的事件时间
        LambdaQueryWrapper<DoorEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DoorEvent::getEventTime)
               .last("FETCH FIRST 1 ROW ONLY");
        DoorEvent latest = baseMapper.selectOne(wrapper);

        if (latest != null && StringUtils.isNotBlank(latest.getEventTime())) {
            String normalized = normalizeTime(latest.getEventTime());
            if (normalized != null) {
                return normalized;
            }
            log.warn("DB中最新event_time格式无法解析: {}, 改用默认回溯时间", latest.getEventTime());
        }

        // DB 为空或格式异常，使用默认回溯时间
        String defaultStart = ZonedDateTime.now().minusDays(DEFAULT_LOOKBACK_DAYS)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        log.info("数据库无事件记录或格式异常，使用默认起始时间: {}", defaultStart);
        return defaultStart;
    }

    /**
     * 将时间字符串统一转换为海康要求的格式（yyyy-MM-dd'T'HH:mm:ssXXX）。
     * 兼容格式：yyyy-MM-dd'T'HH:mm:ss(.SSS)(+08:00)、yyyy-MM-dd HH:mm:ss(.SSS) 等。
     * 解析失败返回null。
     */
    private String normalizeTime(String time) {
        if (time == null) {
            return null;
        }
        String trimmed = time.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        // 已是海康要求的带时区格式则直接返回
        if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?[+-]\\d{2}:\\d{2}")) {
            return trimmed;
        }
        try {
            LocalDateTime dateTime;
            if (trimmed.contains("T")) {
                dateTime = LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                // 兼容 yyyy-MM-dd HH:mm:ss 和 yyyy-MM-dd HH:mm:ss.SSS
                String pattern = trimmed.contains(".") ? "yyyy-MM-dd HH:mm:ss.SSS" : "yyyy-MM-dd HH:mm:ss";
                dateTime = LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern(pattern));
            }
            return dateTime.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        } catch (Exception e) {
            log.warn("时间字符串解析失败: {}", time);
            return null;
        }
    }

    /**
     * 逐页从海康拉取指定时间范围内的全部门禁点事件
     */
    private List<DoorEventSearchResponse.DoorEventItem> fetchAllFromHikvision(
            String startTime, String endTime) {
        List<DoorEventSearchResponse.DoorEventItem> allItems = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            DoorEventSearchRequest request = new DoorEventSearchRequest()
                    .setPageNo(pageNo)
                    .setPageSize(PAGE_SIZE)
                    .setStartTime(startTime)
                    .setEndTime(endTime);

            try {
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康门禁点事件, pageNo={}, pageSize={}", pageNo, PAGE_SIZE);

                String responseBody = hikvisionUtil.doPostJson(DOOR_EVENT_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("海康门禁点事件查询失败: {}", responseBody);
                    throw new RuntimeException("海康门禁点事件查询失败: " + responseBody);
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null) {
                    log.warn("海康返回的data为空");
                    break;
                }

                DoorEventSearchResponse response = dataJson.toJavaObject(DoorEventSearchResponse.class);
                List<DoorEventSearchResponse.DoorEventItem> eventList = response.getList();

                if (eventList == null || eventList.isEmpty()) {
                    log.info("海康门禁点事件列表为空，拉取结束");
                    break;
                }

                allItems.addAll(eventList);
                log.info("第{}页拉取完成, 本页{}条, 累计{}条", pageNo, eventList.size(), allItems.size());

                int total = response.getTotal() != null ? response.getTotal() : 0;
                if (pageNo * PAGE_SIZE >= total) {
                    hasMore = false;
                } else {
                    pageNo++;
                }

            } catch (Exception e) {
                log.error("拉取海康门禁点事件异常, pageNo={}", pageNo, e);
                throw new RuntimeException("拉取海康门禁点事件失败: " + e.getMessage(), e);
            }
        }

        log.info("海康事件数据拉取完成, 共获取{}条", allItems.size());
        return allItems;
    }

    /**
     * 根据海康返回的 eventId 列表，查询DB中已存在的 eventId。
     */
    private Set<String> getExistingEventIds(List<DoorEventSearchResponse.DoorEventItem> items) {
        if (items.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        List<String> eventIds = items.stream()
                .map(DoorEventSearchResponse.DoorEventItem::getEventId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (eventIds.isEmpty()) {
            return java.util.Collections.emptySet();
        }

        List<DoorEvent> existing = baseMapper.selectList(
                new LambdaQueryWrapper<DoorEvent>()
                        .in(DoorEvent::getEventId, eventIds)
                        .select(DoorEvent::getEventId));

        return existing.stream()
                .map(DoorEvent::getEventId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public IPage<DoorEventListVO> getEventList(DoorEventPageDto dto) {
        log.info("分页查询门禁点事件列表, pageNo={}, pageSize={}, personName={}, doorName={}, doorIndexCode={}, eventType={}, inAndOutType={}, cardNo={}, startTime={}, endTime={}",
                dto.getPageNo(), dto.getPageSize(), dto.getPersonName(), dto.getDoorName(), dto.getDoorIndexCode(),
                dto.getEventType(), dto.getInAndOutType(), dto.getCardNo(), dto.getStartTime(), dto.getEndTime());

        LambdaQueryWrapper<DoorEvent> wrapper = new LambdaQueryWrapper<DoorEvent>()
                .like(StringUtils.isNotBlank(dto.getPersonName()), DoorEvent::getPersonName, dto.getPersonName())
                .like(StringUtils.isNotBlank(dto.getDoorName()), DoorEvent::getDoorName, dto.getDoorName())
                .eq(StringUtils.isNotBlank(dto.getDoorIndexCode()), DoorEvent::getDoorIndexCode, dto.getDoorIndexCode())
                .eq(dto.getEventType() != null, DoorEvent::getEventType, dto.getEventType())
                .eq(dto.getInAndOutType() != null, DoorEvent::getInAndOutType, dto.getInAndOutType())
                .like(StringUtils.isNotBlank(dto.getCardNo()), DoorEvent::getCardNo, dto.getCardNo())
                .ge(StringUtils.isNotBlank(dto.getStartTime()), DoorEvent::getEventTime, dto.getStartTime())
                .le(StringUtils.isNotBlank(dto.getEndTime()), DoorEvent::getEventTime, dto.getEndTime())
                .orderByDesc(DoorEvent::getEventTime);

        IPage<DoorEvent> eventPage = page(new Page<>(dto.getPageNo(), dto.getPageSize()), wrapper);

        List<DoorEventListVO> voList = new ArrayList<>((int) eventPage.getSize());
        for (DoorEvent event : eventPage.getRecords()) {
            DoorEventListVO vo = new DoorEventListVO();
            vo.setId(event.getId());
            vo.setEventId(event.getEventId());
            vo.setEventName(event.getEventName());
            vo.setEventTime(event.getEventTime());
            vo.setPersonId(event.getPersonId());
            vo.setCardNo(event.getCardNo());
            vo.setPersonName(event.getPersonName());
            vo.setOrgName(event.getOrgName());
            vo.setDoorName(event.getDoorName());
            vo.setDoorIndexCode(event.getDoorIndexCode());
            vo.setEventType(event.getEventType());
            vo.setInAndOutType(event.getInAndOutType());
            vo.setReaderDevName(event.getReaderDevName());
            vo.setDevName(event.getDevName());
            vo.setPicUri(event.getPicUri());
            vo.setGmtCreate(event.getGmtCreate() != null ? event.getGmtCreate().toString() : null);
            voList.add(vo);
        }

        IPage<DoorEventListVO> resultPage = new Page<>(dto.getPageNo(), dto.getPageSize(), eventPage.getTotal());
        resultPage.setRecords(voList);

        log.info("分页查询门禁点事件列表完成, 共{}条, 当前页{}条", eventPage.getTotal(), voList.size());
        return resultPage;
    }

    private DoorEvent convertToEntity(DoorEventSearchResponse.DoorEventItem item) {
        DoorEvent entity = new DoorEvent();
        entity.setEventId(item.getEventId());
        entity.setEventName(item.getEventName());
        entity.setEventTime(formatEventTimeForDb(item.getEventTime()));
        entity.setPersonId(item.getPersonId());
        entity.setCardNo(item.getCardNo());
        entity.setPersonName(item.getPersonName());
        entity.setOrgIndexCode(item.getOrgIndexCode());
        entity.setOrgName(item.getOrgName());
        entity.setDoorName(item.getDoorName());
        entity.setDoorIndexCode(item.getDoorIndexCode());
        entity.setDoorRegionIndexCode(item.getDoorRegionIndexCode());
        entity.setPicUri(item.getPicUri());
        entity.setSvrIndexCode(item.getSvrIndexCode());
        entity.setEventType(item.getEventType());
        entity.setInAndOutType(item.getInAndOutType());
        entity.setReaderDevIndexCode(item.getReaderDevIndexCode());
        entity.setReaderDevName(item.getReaderDevName());
        entity.setDevIndexCode(item.getDevIndexCode());
        entity.setDevName(item.getDevName());
        entity.setIdentityCardUri(item.getIdentityCardUri());
        entity.setReceiveTime(item.getReceiveTime());
        entity.setJobNo(item.getJobNo());
        entity.setStudentId(item.getStudentId());
        entity.setCertNo(item.getCertNo());
        return entity;
    }

    /**
     * 将海康返回的ISO8601事件时间转换为达梦DATETIME列可接受的格式（yyyy-MM-dd HH:mm:ss）。
     * <p>达梦驱动setString对DATETIME列会做隐式日期转换，无法解析ISO8601中的'T'和时区偏移，
     * 直接存入会报"错误的日期时间类型格式"，故需先归一化。</p>
     */
    private String formatEventTimeForDb(String isoTime) {
        if (StringUtils.isBlank(isoTime)) {
            return null;
        }
        String trimmed = isoTime.trim();
        try {
            // 兼容 2026-08-17T17:30:08.000+08:00 与 2026-08-17T17:30:08+08:00
            OffsetDateTime odt = OffsetDateTime.parse(trimmed, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return odt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            try {
                // 兼容无时区格式：2026-08-17T17:30:08 或 2026-08-17 17:30:08
                String candidate = trimmed.contains("T")
                        ? trimmed
                        : trimmed.replace(" ", "T");
                LocalDateTime ldt = LocalDateTime.parse(candidate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                log.warn("event_time格式解析失败: {}, 该事件将不记录事件时间", isoTime);
                return null;
            }
        }
    }
}
