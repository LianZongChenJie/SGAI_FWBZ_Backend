package org.jeecg.modules.fwbz.venueVisitorFlow.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VenueFlowHour;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VenueFlowHourMapper;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueFlowVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 各场馆客流统计 Service 实现
 * <p>
 * 数据来源：table_venue_flow_hour（各场馆客流分时统计表）。
 * 查询逻辑：按日期 + venueId 分组，取每个场馆最新一条记录展示。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Service
public class VenueFlowServiceImpl extends ServiceImpl<VenueFlowHourMapper, VenueFlowHour>
        implements IVenueFlowService {

    /**
     * 各场馆客流统计 HTTP API 地址。
     */
    private static final String VENUE_FLOW_API_URL = "http://10.168.47.26:9999/sgai-api/openApi/haikang/changguankeliu";

    @Value("${parking.statistics.api.appKey:R6VOSNoijW3o4WA5eFjW5l2bO}")
    private String appKey;

    @Value("${parking.statistics.api.appSecret:GDg18aNuWaKsIX33euL0maXbSVqZSp}")
    private String appSecret;

    private final RestTemplate restTemplate;
    private final IVenueInfoService venueInfoService;

    public VenueFlowServiceImpl(IVenueInfoService venueInfoService) {
        this.restTemplate = new RestTemplate();
        this.venueInfoService = venueInfoService;
    }

    // ==================== 查询（从 table_venue_flow_hour 取各场馆最新一条） ====================

    @Override
    public List<VenueFlowVO> queryToday() {
        return queryByDate(LocalDate.now());
    }

    @Override
    public List<VenueFlowVO> queryByDate(LocalDate date) {
        // 查询当日所有分时数据
        List<VenueFlowHour> all = list(new LambdaQueryWrapper<VenueFlowHour>()
                .eq(VenueFlowHour::getDataDate, date));

        if (all == null || all.isEmpty()) {
            return new ArrayList<>();
        }

        // 按 venueId 分组，取每组中 id 最大的（最新记录）
        Map<Long, VenueFlowHour> latestMap = all.stream()
                .collect(Collectors.toMap(
                        VenueFlowHour::getVenueId,
                        v -> v,
                        (a, b) -> a.getId() > b.getId() ? a : b));

        // 场馆名称映射
        Map<Long, String> venueNameMap = buildVenueNameMap();

        // 昨日数据（也取各场馆最新）
        LocalDate yesterday = date.minusDays(1);
        List<VenueFlowHour> yesterdayAll = list(new LambdaQueryWrapper<VenueFlowHour>()
                .eq(VenueFlowHour::getDataDate, yesterday));
        Map<Long, VenueFlowHour> yesterdayLatestMap = new HashMap<>();
        if (yesterdayAll != null && !yesterdayAll.isEmpty()) {
            yesterdayLatestMap = yesterdayAll.stream()
                    .collect(Collectors.toMap(
                            VenueFlowHour::getVenueId,
                            v -> v,
                            (a, b) -> a.getId() > b.getId() ? a : b));
        }

        List<VenueFlowVO> result = new ArrayList<>();
        for (VenueFlowHour today : latestMap.values()) {
            VenueFlowVO vo = new VenueFlowVO();
            vo.setVenueId(today.getVenueId());
            vo.setVenueName(venueNameMap.getOrDefault(today.getVenueId(), "未知场馆"));
            vo.setTodayInCount(today.getTodayInCount());
            vo.setTodayNowCount(today.getTodayNowCount());
            vo.setMaxCount(today.getMaxCount());
            vo.setMaxTime(today.getMaxTime());
            vo.setAverageDuration(round(today.getAverageDuration(), 1));

            VenueFlowHour yesterdayRow = yesterdayLatestMap.get(today.getVenueId());
            vo.setYesterdayInCount(yesterdayRow == null ? 0L : nvl(yesterdayRow.getTodayInCount()));
            vo.setYesterdayNowCount(yesterdayRow == null ? 0L : nvl(yesterdayRow.getTodayNowCount()));

            result.add(vo);
        }
        return result;
    }

    /**
     * 通过 IVenueInfoService 构建 venueId → venueName 映射。
     */
    private Map<Long, String> buildVenueNameMap() {
        return venueInfoService.list().stream()
                .collect(Collectors.toMap(VenueInfo::getId, VenueInfo::getVenueName, (a, b) -> a));
    }

    // ==================== 同步：HTTP API → 写 table_venue_flow_hour ====================

    @Override
    public int syncAllVenueFlowFromApi() {
        log.info("开始从 HTTP API 同步各场馆客流数据(写入 table_venue_flow_hour)...");
        int successCount = 0;

        JSONArray list = fetchVenueFlowListFromApi();
        if (list == null || list.isEmpty()) {
            log.error("HTTP API 返回空数据，终止场馆客流同步");
            return 0;
        }

        for (int i = 0; i < list.size(); i++) {
            JSONObject item = list.getJSONObject(i);
            try {
                Long venueId = resolveVenueId(item.getString("name"));
                if (venueId == null) {
                    log.warn("跳过无法识别场馆的数据项: {}", item);
                    continue;
                }

                VenueFlowHour flow = parseVenueFlowHourItem(item, venueId);
                upsertVenueFlowHour(flow);
                successCount++;

                log.info("场馆客流入库(flow_hour)成功 venueId={}, venueName={}, nowCount={}, maxCount={}",
                        venueId, item.getString("name"), flow.getTodayNowCount(), flow.getMaxCount());

            } catch (Exception e) {
                log.error("场馆客流入库异常, item={}", item, e);
            }
        }

        log.info("HTTP API 场馆客流同步完成，共同步 {} 个场馆", successCount);
        return successCount;
    }

    @Override
    public boolean syncOneVenueFlowFromApi(Long venueId) {
        JSONArray list = fetchVenueFlowListFromApi();
        if (list == null || list.isEmpty()) {
            return false;
        }

        for (int i = 0; i < list.size(); i++) {
            JSONObject item = list.getJSONObject(i);
            Long itemVenueId = resolveVenueId(item.getString("name"));
            if (venueId.equals(itemVenueId)) {
                try {
                    VenueFlowHour flow = parseVenueFlowHourItem(item, venueId);
                    upsertVenueFlowHour(flow);
                    return true;
                } catch (Exception e) {
                    log.error("同步单个场馆客流失败 venueId={}", venueId, e);
                    return false;
                }
            }
        }
        log.warn("未找到场馆数据 venueId={}", venueId);
        return false;
    }

    /**
     * 调用 HTTP API 获取各场馆客流列表。
     * <p>
     * 请求方式 GET，需携带 appKey/appSecret 请求头；
     * 响应结构: {success, message, code, result: [...]}，result 为场馆列表。
     * </p>
     *
     * @return 场馆客流列表，失败返回 null
     */
    private JSONArray fetchVenueFlowListFromApi() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("appKey", appKey);
            headers.set("appSecret", appSecret);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(VENUE_FLOW_API_URL, HttpMethod.GET, request, String.class);
            String responseBody = response.getBody();
            log.debug("HTTP API 场馆客流响应: {}", responseBody);

            JSONObject json = JSONObject.parseObject(responseBody);
            if (json == null || !Boolean.TRUE.equals(json.getBoolean("success"))
                    || json.getInteger("code") == null || json.getInteger("code") != 200) {
                log.error("请求 HTTP API 场馆客流失败: {}", responseBody);
                return null;
            }
            return json.getJSONArray("result");
        } catch (Exception e) {
            log.error("请求 HTTP API 场馆客流异常", e);
            return null;
        }
    }

    /**
     * 将 API 返回的 JSON 解析为 VenueFlowHour 实体。
     * <p>
     * 新接口字段映射：enterSum(总进入人数) → todayInCount，holdValue(在馆人数) → todayNowCount；
     * exitSum(总离开人数) 表无对应字段暂不存储；maxCount/maxTime 接口不再返回，
     * 由 upsert 时根据在馆人数本地判断峰值。
     * </p>
     */
    private VenueFlowHour parseVenueFlowHourItem(JSONObject item, Long venueId) {
        VenueFlowHour flow = new VenueFlowHour();
        flow.setDataDate(LocalDate.now());
        flow.setDataHour(Time.valueOf(LocalTime.now().withMinute(0).withSecond(0).withNano(0)));
        flow.setStatus(1);
        flow.setVenueId(venueId);
        flow.setTodayInCount(item.getLong("enterSum"));
        flow.setTodayNowCount(item.getLong("holdValue"));
        return flow;
    }

    /**
     * 按 (dataDate, venueId, dataHour) 唯一键 upsert：存在则更新，否则插入。
     * <p>
     * 表为分时结构，每天每馆每小时一条记录，dataHour 取当前整点小时。
     * 峰值人数本地判断：接口不再返回 maxCount/maxTime，改为跨小时累计当天峰值——
     * 取当天该馆所有小时记录中的最大峰值作为基准，当前在馆人数(holdValue)超过该峰值时，
     * 更新峰值人数并记录峰值时间(当前时间)；否则保留当天历史峰值及对应峰值时间。
     * </p>
     */
    private void upsertVenueFlowHour(VenueFlowHour flow) {
        VenueFlowHour exist = getOne(new LambdaQueryWrapper<VenueFlowHour>()
                .eq(VenueFlowHour::getDataDate, flow.getDataDate())
                .eq(VenueFlowHour::getVenueId, flow.getVenueId())
                .eq(VenueFlowHour::getDataHour, flow.getDataHour()));

        // 当天该馆历史峰值（跨小时累计）
        long dayMax = 0L;
        Time dayMaxTime = null;
        List<VenueFlowHour> dayRecords = list(new LambdaQueryWrapper<VenueFlowHour>()
                .eq(VenueFlowHour::getDataDate, flow.getDataDate())
                .eq(VenueFlowHour::getVenueId, flow.getVenueId()));
        for (VenueFlowHour r : dayRecords) {
            if (r.getMaxCount() != null && r.getMaxCount() > dayMax) {
                dayMax = r.getMaxCount();
                dayMaxTime = r.getMaxTime();
            }
        }

        // 峰值人数判断：当前在馆人数超过当天历史峰值时，更新峰值及峰值时间(当前时间)
        long holdValue = flow.getTodayNowCount() == null ? 0L : flow.getTodayNowCount();
        if (holdValue > dayMax) {
            flow.setMaxCount(holdValue);
            flow.setMaxTime(new Time(System.currentTimeMillis()));
        } else {
            flow.setMaxCount(dayMax);
            flow.setMaxTime(dayMaxTime == null ? new Time(System.currentTimeMillis()) : dayMaxTime);
        }

        if (exist != null) {
            flow.setId(exist.getId());
            updateById(flow);
        } else {
            save(flow);
        }
    }

    /**
     * 根据场馆名称解析 venueId。
     * <p>
     * 新接口不再返回 venueId，改为返回场馆名称 name（如 "1号馆"）。
     * 优先按名称匹配 table_venue_info.venue_name；匹配不到时从名称中提取数字兜底（如 "1号馆" → 1）。
     * </p>
     */
    private Long resolveVenueId(String venueName) {
        if (venueName == null || venueName.trim().isEmpty()) {
            return null;
        }
        String name = venueName.trim();
        List<VenueInfo> infos = venueInfoService.list(new LambdaQueryWrapper<VenueInfo>()
                .eq(VenueInfo::getVenueName, name));
        if (infos != null && !infos.isEmpty()) {
            return infos.get(0).getId();
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(name);
        if (matcher.find()) {
            return Long.parseLong(matcher.group());
        }
        return null;
    }

    // ==================== 工具方法 ====================

    private double round(Double value, int scale) {
        if (value == null) {
            return 0.0;
        }
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }
}
