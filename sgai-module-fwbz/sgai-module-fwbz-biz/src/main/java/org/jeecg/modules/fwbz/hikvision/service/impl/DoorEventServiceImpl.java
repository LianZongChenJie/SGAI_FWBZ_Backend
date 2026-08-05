package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.entity.DoorEvent;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventSearchResponse;
import org.jeecg.modules.fwbz.hikvision.service.IDoorEventService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.mapper.DoorEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final int DEFAULT_LOOKBACK_DAYS = 90;

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
        saveBatch(newEvents);
        log.info("门禁点事件增量同步完成, 获取{}条, 新增{}条", allItems.size(), newEvents.size());
        return newEvents.size();
    }

    /**
     * 解析起始时间：取 DB 中最新的 event_time；DB 为空时往前回溯90天。
     */
    private String resolveStartTime() {
        // 查询数据库中最新的事件时间
        LambdaQueryWrapper<DoorEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DoorEvent::getEventTime)
               .last("FETCH FIRST 1 ROW ONLY");
        DoorEvent latest = baseMapper.selectOne(wrapper);

        if (latest != null && latest.getEventTime() != null) {
            return latest.getEventTime();
        }

        // DB 为空，使用默认回溯时间
        String defaultStart = ZonedDateTime.now().minusDays(DEFAULT_LOOKBACK_DAYS)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        log.info("数据库无事件记录，使用默认起始时间: {}", defaultStart);
        return defaultStart;
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

    private DoorEvent convertToEntity(DoorEventSearchResponse.DoorEventItem item) {
        DoorEvent entity = new DoorEvent();
        entity.setEventId(item.getEventId());
        entity.setEventName(item.getEventName());
        entity.setEventTime(item.getEventTime());
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
}
