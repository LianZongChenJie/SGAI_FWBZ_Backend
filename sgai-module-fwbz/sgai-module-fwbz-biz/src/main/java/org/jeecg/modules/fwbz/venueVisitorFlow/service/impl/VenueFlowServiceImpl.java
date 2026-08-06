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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
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
    private static final String VENUE_FLOW_API_URL = "http://api.example.com/api/visitorFlow/venueList";

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

        JSONObject resp = fetchVenueFlowListFromApi();
        if (resp == null) {
            log.error("HTTP API 返回空数据，终止场馆客流同步");
            return 0;
        }

        JSONArray list = resp.getJSONArray("list");
        if (list == null || list.isEmpty()) {
            log.warn("HTTP API 场馆列表为空");
            return 0;
        }

        for (int i = 0; i < list.size(); i++) {
            JSONObject item = list.getJSONObject(i);
            try {
                Long venueId = item.getLong("venueId");
                if (venueId == null) {
                    log.warn("跳过无 venueId 的数据项: {}", item);
                    continue;
                }

                VenueFlowHour flow = parseVenueFlowHourItem(item, venueId);
                upsertVenueFlowHour(flow);
                successCount++;

                log.info("场馆客流入库(flow_hour)成功 venueId={}, venueName={}, nowCount={}, maxCount={}",
                        venueId, item.getString("venueName"), flow.getTodayNowCount(), flow.getMaxCount());

            } catch (Exception e) {
                log.error("场馆客流入库异常, item={}", item, e);
            }
        }

        log.info("HTTP API 场馆客流同步完成，共同步 {} 个场馆", successCount);
        return successCount;
    }

    @Override
    public boolean syncOneVenueFlowFromApi(Long venueId) {
        JSONObject resp = fetchVenueFlowListFromApi();
        if (resp == null) {
            return false;
        }

        JSONArray list = resp.getJSONArray("list");
        if (list == null || list.isEmpty()) {
            return false;
        }

        for (int i = 0; i < list.size(); i++) {
            JSONObject item = list.getJSONObject(i);
            Long itemVenueId = item.getLong("venueId");
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
     */
    private JSONObject fetchVenueFlowListFromApi() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.postForEntity(VENUE_FLOW_API_URL, request, String.class);
            String responseBody = response.getBody();
            log.debug("HTTP API 场馆客流响应: {}", responseBody);

            JSONObject json = JSONObject.parseObject(responseBody);
            if (json == null || json.getInteger("code") == null || json.getInteger("code") != 200) {
                log.error("请求 HTTP API 场馆客流失败: {}", responseBody);
                return null;
            }
            return json.getJSONObject("data");
        } catch (Exception e) {
            log.error("请求 HTTP API 场馆客流异常", e);
            return null;
        }
    }

    /**
     * 将 API 返回的 JSON 解析为 VenueFlowHour 实体。
     */
    private VenueFlowHour parseVenueFlowHourItem(JSONObject item, Long venueId) {
        VenueFlowHour flow = new VenueFlowHour();
        flow.setDataDate(LocalDate.now());
        flow.setVenueId(venueId);
        flow.setTodayInCount(item.getLong("todayInCount"));
        flow.setTodayNowCount(item.getLong("nowCount"));
        flow.setMaxCount(item.getLong("maxCount"));
        // maxTime 解析为 java.sql.Time
        String maxTimeStr = item.getString("maxTime");
        if (maxTimeStr != null && !maxTimeStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                flow.setMaxTime(new Time(sdf.parse(maxTimeStr).getTime()));
            } catch (Exception e) {
                log.warn("maxTime 解析失败: {}", maxTimeStr, e);
            }
        }
        flow.setAverageDuration(item.getDouble("averageDuration"));
        return flow;
    }

    /**
     * 按 (dataDate, venueId) 唯一键 upsert：存在则更新，否则插入。
     */
    private void upsertVenueFlowHour(VenueFlowHour flow) {
        VenueFlowHour exist = getOne(new LambdaQueryWrapper<VenueFlowHour>()
                .eq(VenueFlowHour::getDataDate, LocalDate.now())
                .eq(VenueFlowHour::getVenueId, flow.getVenueId()));
        if (exist != null) {
            flow.setId(exist.getId());
            updateById(flow);
        } else {
            save(flow);
        }
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
