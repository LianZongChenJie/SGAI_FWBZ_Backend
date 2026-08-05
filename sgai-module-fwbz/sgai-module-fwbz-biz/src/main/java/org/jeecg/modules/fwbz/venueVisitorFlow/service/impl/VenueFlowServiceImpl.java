package org.jeecg.modules.fwbz.venueVisitorFlow.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VenueFlow;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VenueFlowMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 各场馆客流统计 Service 实现
 * <p>
 * 逻辑：调用 HTTP API（今日进场 / 当前在场 / 峰值人数 / 峰值时间 / 平均停留）
 * → 按 (dataDate, venueId) 唯一键写入各场馆流量表，前端从 DB 读取展示较昨日对比。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Service
public class VenueFlowServiceImpl extends ServiceImpl<VenueFlowMapper, VenueFlow>
        implements IVenueFlowService {

    /**
     * 各场馆客流统计 HTTP API 地址（假地址，替换为实际地址）。
     */
    private static final String VENUE_FLOW_API_URL = "http://api.example.com/api/visitorFlow/venueList";

    private final RestTemplate restTemplate;
    private final IVenueInfoService venueInfoService;

    public VenueFlowServiceImpl(IVenueInfoService venueInfoService) {
        this.restTemplate = new RestTemplate();
        this.venueInfoService = venueInfoService;
    }

    // ==================== 查询 ====================

    @Override
    public List<VenueFlowVO> queryToday() {
        return queryByDate(LocalDate.now());
    }

    @Override
    public List<VenueFlowVO> queryByDate(LocalDate date) {
        LambdaQueryWrapper<VenueFlow> qw = new LambdaQueryWrapper<>();
        qw.eq(VenueFlow::getDataDate, date);
        List<VenueFlow> list = list(qw);

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建 venueId → venueName 映射
        Map<Long, String> venueNameMap = buildVenueNameMap();

        // 昨日数据
        LocalDate yesterday = date.minusDays(1);
        LambdaQueryWrapper<VenueFlow> yesterdayQw = new LambdaQueryWrapper<>();
        yesterdayQw.eq(VenueFlow::getDataDate, yesterday);
        List<VenueFlow> yesterdayList = list(yesterdayQw);
        Map<Long, VenueFlow> yesterdayMap = (yesterdayList == null ? new ArrayList<VenueFlow>() : yesterdayList)
                .stream().collect(Collectors.toMap(VenueFlow::getVenueId, v -> v, (a, b) -> a));

        List<VenueFlowVO> result = new ArrayList<>();
        for (VenueFlow today : list) {
            VenueFlowVO vo = new VenueFlowVO();
            vo.setVenueId(today.getVenueId());
            vo.setVenueName(venueNameMap.getOrDefault(today.getVenueId(), "未知场馆"));
            vo.setTodayInCount(today.getTodayInCount());
            vo.setTodayNowCount(today.getTodayNowCount());
            vo.setMaxCount(today.getMaxCount());
            vo.setMaxTime(today.getMaxTime());
            vo.setAverageDuration(round(today.getAverageDuration(), 1));

            VenueFlow yesterdayRow = yesterdayMap.get(today.getVenueId());
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

    // ==================== 同步：HTTP API → 写库 ====================

    @Override
    public int syncAllVenueFlowFromApi() {
        log.info("开始从 HTTP API 同步各场馆客流数据...");
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

                VenueFlow flow = parseVenueFlowItem(item, venueId);
                upsertVenueFlow(flow);
                successCount++;

                log.info("场馆客流入库成功 venueId={}, venueName={}, nowCount={}, maxCount={}",
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
                    VenueFlow flow = parseVenueFlowItem(item, venueId);
                    upsertVenueFlow(flow);
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
     * <p>响应格式示例：
     * {"code":200,"msg":"success","data":{"list":[
     *   {"venueId":1,"venueName":"XX场馆","todayInCount":500,"nowCount":100,"maxCount":300,"maxTime":"14:30","averageDuration":1.2}
     * ]}}
     * </p>
     *
     * @return data 节点 JSONObject，失败返回 null
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
     * 将 API 返回的 JSON 解析为 VenueFlow 实体（仅数据库字段，不含 venueName）。
     */
    private VenueFlow parseVenueFlowItem(JSONObject item, Long venueId) {
        VenueFlow flow = new VenueFlow();
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
    private void upsertVenueFlow(VenueFlow flow) {
        VenueFlow exist = getOne(new LambdaQueryWrapper<VenueFlow>()
                .eq(VenueFlow::getDataDate, LocalDate.now())
                .eq(VenueFlow::getVenueId, flow.getVenueId()));
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
