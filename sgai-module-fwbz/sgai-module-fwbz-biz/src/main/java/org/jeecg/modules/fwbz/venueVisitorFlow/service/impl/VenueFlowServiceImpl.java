package org.jeecg.modules.fwbz.venueVisitorFlow.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VenueFlow;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VenueFlowMapper;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueFlowVO;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 各场馆客流统计 Service 实现
 * <p>
 * 逻辑：调取海康各场馆客流统计 API → 解析返回的所有场馆数据 → 按 (dataDate, venueId) 唯一键写库
 * → 从数据库读取构建 VO 返回前端，含较昨日对比。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class VenueFlowServiceImpl extends ServiceImpl<VenueFlowMapper, VenueFlow>
        implements IVenueFlowService {

    /**
     * 海康各场馆客流统计统一 API：一次性返回所有场馆的今日进场/当前在场/峰值/峰值时间/平均停留。
     * <p>实际 API 路径以海康平台"运管中心-API管理-人员统计"为准。</p>
     */
    private static final String VENUE_FLOW_API = "/api/hcp/v1/people/regionFlowStatistic";

    private final HikvisionUtil hikvisionUtil;
    private final IVenueInfoService venueInfoService;

    // ==================== 同步：调海康统一 API → 解析所有场馆 → 写库 ====================

    @Override
    public int syncAllVenueFlowFromHikvision() {
        List<VenueInfo> venues = venueInfoService.list();
        if (venues == null || venues.isEmpty()) {
            log.warn("未配置任何场馆，跳过各场馆客流同步");
            return 0;
        }

        // 构建 venueId → VenueInfo 映射，用于匹配海康返回的 regionIndexCode
        Map<String, VenueInfo> codeVenueMap = venues.stream()
                .collect(Collectors.toMap(v -> String.valueOf(v.getId()), v -> v, (a, b) -> a));

        // 调用统一接口获取所有场馆客流数据
        List<VenueFlow> flowList = fetchAllVenueFlowFromApi();
        if (flowList == null || flowList.isEmpty()) {
            log.warn("各场馆客流API未返回数据");
            return 0;
        }

        LocalDate today = LocalDate.now();
        int successCount = 0;
        for (VenueFlow flow : flowList) {
            try {
                VenueInfo venue = codeVenueMap.get(String.valueOf(flow.getVenueId()));
                if (venue == null) {
                    log.debug("跳过未配置的场馆: venueId={}", flow.getVenueId());
                    continue;
                }
                // 获取已有记录，没有则新建
                VenueFlow entity = getOrCreateToday(flow.getVenueId(), today);
                entity.setTodayInCount(flow.getTodayInCount());
                entity.setTodayNowCount(flow.getTodayNowCount());
                entity.setMaxCount(flow.getMaxCount());
                entity.setMaxTime(flow.getMaxTime());
                entity.setAverageDuration(flow.getAverageDuration());
                entity.setStatus(1);

                insertOrUpdate(entity);
                log.info("场馆 {} 客流同步完成: todayIn={}, currentIn={}, maxCount={}, maxTime={}, avgStop={}",
                        venue.getVenueName(),
                        entity.getTodayInCount(), entity.getTodayNowCount(),
                        entity.getMaxCount(), entity.getMaxTime(), entity.getAverageDuration());
                successCount++;
            } catch (Exception e) {
                log.error("写库场馆客流失败: venueId={}", flow.getVenueId(), e);
            }
        }
        log.info("各场馆客流同步完成，成功 {}/{}", successCount, flowList.size());
        return successCount;
    }

    /**
     * 调海康各场馆客流统一 API，解析返回的场馆列表。
     * <p>响应 data.list 中每项含 regionIndexCode（对应 venueId）、todayInCount、todayNowCount、
     * maxCount、maxTime、averageDuration 等字段。实际字段名以平台文档为准。</p>
     */
    private List<VenueFlow> fetchAllVenueFlowFromApi() {
        try {
            String responseBody = hikvisionUtil.doPostJson(VENUE_FLOW_API, "{}");
            log.debug("海康各场馆客流响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("请求海康各场馆客流失败: {}", responseBody);
                return null;
            }
            JSONObject data = hikvisionUtil.getResponseData(responseBody);
            if (data == null) {
                return null;
            }
            JSONArray list = data.getJSONArray("list");
            if (list == null || list.isEmpty()) {
                return null;
            }

            List<VenueFlow> result = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                JSONObject item = list.getJSONObject(i);
                Long venueId = item.getLong("regionIndexCode");
                if (venueId == null) {
                    venueId = item.getLong("regionId");
                }
                if (venueId == null) {
                    venueId = item.getLong("venueId");
                }
                if (venueId == null) {
                    continue;
                }

                VenueFlow flow = new VenueFlow();
                flow.setVenueId(venueId);

                // 今日进场
                Long todayIn = item.getLong("todayInCount");
                if (todayIn == null) todayIn = item.getLong("entryCount");
                if (todayIn == null) todayIn = item.getLong("total");
                if (todayIn == null) todayIn = item.getLong("count");
                flow.setTodayInCount(todayIn);

                // 当前在场
                Long nowCount = item.getLong("todayNowCount");
                if (nowCount == null) nowCount = item.getLong("currentCount");
                if (nowCount == null) nowCount = item.getLong("inCount");
                flow.setTodayNowCount(nowCount);

                // 峰值人数
                Long maxCount = item.getLong("maxCount");
                if (maxCount == null) maxCount = item.getLong("peakCount");
                flow.setMaxCount(maxCount);

                // 峰值时间
                String maxTime = item.getString("maxTime");
                if (maxTime == null) maxTime = item.getString("peakTime");
                flow.setMaxTime(parseTime(maxTime));

                // 平均停留时长
                Double avgDuration = item.getDouble("averageDuration");
                if (avgDuration == null) avgDuration = item.getDouble("averageStopDuration");
                if (avgDuration == null) avgDuration = item.getDouble("hours");
                if (avgDuration == null) avgDuration = item.getDouble("duration");
                flow.setAverageDuration(avgDuration);

                result.add(flow);
            }
            return result;
        } catch (Exception e) {
            log.error("请求海康各场馆客流异常", e);
            return null;
        }
    }

    @Override
    public boolean syncOneVenueFlowFromHikvision(Long venueId) {
        // 统一接口已涵盖所有场馆，单独同步直接调用全量同步
        syncAllVenueFlowFromHikvision();
        return true;
    }

    /**
     * 解析时间字符串（HH:mm 或 HH:mm:ss）为 Time
     */
    private Time parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            if (timeStr.length() == 5) {
                timeStr = timeStr + ":00";
            }
            return Time.valueOf(timeStr);
        } catch (Exception e) {
            log.warn("时间解析失败: {}", timeStr);
            return null;
        }
    }

    // ==================== 数据库操作 ====================

    private VenueFlow getOrCreateToday(Long venueId, LocalDate date) {
        VenueFlow entity = getOne(new LambdaQueryWrapper<VenueFlow>()
                .eq(VenueFlow::getDataDate, date)
                .eq(VenueFlow::getVenueId, venueId));
        if (entity == null) {
            entity = new VenueFlow();
            entity.setDataDate(date);
            entity.setVenueId(venueId);
        }
        return entity;
    }

    private void insertOrUpdate(VenueFlow entity) {
        if (entity.getId() != null) {
            updateById(entity);
        } else {
            save(entity);
        }
    }

    private Map<Long, VenueFlow> mapByVenueAndDate(LocalDate date) {
        List<VenueFlow> list = list(new LambdaQueryWrapper<VenueFlow>()
                .eq(VenueFlow::getDataDate, date));
        if (list == null || list.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(VenueFlow::getVenueId, v -> v, (a, b) -> a));
    }

    // ==================== 查询：从 DB 构建返回 VO ====================

    @Override
    public List<VenueFlowVO> queryVenueFlowList() {
        return queryVenueFlowListByDate(LocalDate.now());
    }

    @Override
    public List<VenueFlowVO> queryVenueFlowListByDate(LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        LocalDate yesterday = target.minusDays(1);

        List<VenueInfo> venues = venueInfoService.list();
        if (venues == null || venues.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, VenueFlow> todayMap = mapByVenueAndDate(target);
        Map<Long, VenueFlow> yesterdayMap = mapByVenueAndDate(yesterday);

        return venues.stream().map(venue -> {
            VenueFlow today = todayMap.get(venue.getId());
            VenueFlow yesterdayRecord = yesterdayMap.get(venue.getId());
            return buildVenueFlowVO(venue, today, yesterdayRecord);
        }).collect(Collectors.toList());
    }

    private VenueFlowVO buildVenueFlowVO(VenueInfo venue, VenueFlow today, VenueFlow yesterday) {
        VenueFlowVO vo = new VenueFlowVO();
        vo.setVenueId(venue.getId());
        vo.setVenueName(venue.getVenueName());

        long todayIn = today == null ? 0L : nvl(today.getTodayInCount());
        long yesterdayIn = yesterday == null ? 0L : nvl(yesterday.getTodayInCount());

        vo.setTodayInCount(todayIn);
        vo.setTodayNowCount(today == null ? 0L : nvl(today.getTodayNowCount()));
        vo.setMaxCount(today == null ? 0L : nvl(today.getMaxCount()));
        vo.setMaxTime(today == null ? null : today.getMaxTime());
        vo.setAverageDuration(today == null ? 0.0 : nvl(today.getAverageDuration()));
        vo.setCompareRate(compareRate(todayIn, yesterdayIn));

        Integer status = today == null ? 0 : today.getStatus();
        vo.setStatus(status == null ? 0 : status);
        vo.setStatusLabel((status != null && status == 1) ? "正常" : "异常");
        return vo;
    }

    // ==================== 通用工具 ====================

    private String compareRate(long today, long yesterday) {
        if (yesterday == 0) {
            return today == 0 ? "—" : "↑100%";
        }
        double rate = (today - yesterday) * 100.0 / yesterday;
        String arrow = rate >= 0 ? "↑" : "↓";
        double abs = Math.abs(rate);
        return arrow + (abs == (long) abs ? String.valueOf((long) abs) : String.format("%.1f", abs)) + "%";
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private double nvl(Double v) {
        return v == null ? 0.0 : v;
    }
}
