package org.jeecg.modules.fwbz.parkingStatistics.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.parkingStatistics.dto.ExternalParkingFlowItemDto;
import org.jeecg.modules.fwbz.parkingStatistics.dto.ExternalParkingSpaceItemDto;
import org.jeecg.modules.fwbz.parkingStatistics.entity.ParkingCount;
import org.jeecg.modules.fwbz.parkingStatistics.mapper.ParkingCountMapper;
import org.jeecg.modules.fwbz.parkingStatistics.service.IParkingStatisticsService;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingFlowStatVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingSpaceStatVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingStatCardVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 停车统计Service实现
 * <p>
 * 逻辑：查询卡片 → 去外部系统同步数据四项 → 存入/更新DB（一天一行）→ 从DB读取返回前端
 */
@Service
@Slf4j
public class ParkingStatisticsServiceImpl extends ServiceImpl<ParkingCountMapper, ParkingCount> implements IParkingStatisticsService {

    @Value("${parking.statistics.api.host:}")
    private String apiHost;

    @Value("${parking.statistics.api.todayEntryPath:/api/parking/todayEntry}")
    private String todayEntryPath;

    @Value("${parking.statistics.api.currentInPath:/api/parking/currentIn}")
    private String currentInPath;

    @Value("${parking.statistics.api.remainingSpacePath:/api/parking/remainingSpace}")
    private String remainingSpacePath;

    @Value("${parking.statistics.api.avgDurationPath:/api/parking/avgDuration}")
    private String avgDurationPath;

    @Value("${parking.statistics.api.spaceDistributionPath:/api/parking/spaceDistribution}")
    private String spaceDistributionPath;

    @Value("${parking.statistics.api.flow24hPath:/api/parking/flow24h}")
    private String flow24hPath;

    private static final int TIMEOUT_MS = 5000;

    // ==================== 数据查询（同步 → 写入 → 返回） ====================

    @Override
    public ParkingStatCardVO todayEntryCount() {
        syncTodayEntryFromApi();
        return queryTodayEntryCount();
    }

    @Override
    public ParkingStatCardVO currentInCount() {
        syncCurrentInFromApi();
        return queryCurrentInCount();
    }

    @Override
    public ParkingStatCardVO remainingSpaceCount() {
        syncRemainingSpaceFromApi();
        return queryRemainingSpaceCount();
    }

    @Override
    public ParkingStatCardVO averageParkingDuration() {
        syncAvgDurationFromApi();
        return queryAverageParkingDuration();
    }

    @Override
    public List<ParkingStatCardVO> getSummary() {
        syncAllFromApi();
        return Arrays.asList(
                queryTodayEntryCount(),
                queryCurrentInCount(),
                queryRemainingSpaceCount(),
                queryAverageParkingDuration()
        );
    }

    @Override
    public List<ParkingSpaceStatVO> getParkingSpaceDistribution() {
        // 直接从外部系统实时获取，不落库
        try {
            String body = HttpUtil.createGet(apiHost + spaceDistributionPath)
                    .timeout(TIMEOUT_MS)
                    .execute()
                    .body();
            log.debug("停车场车位分布外部API响应: {}", body);
            if (body == null || body.trim().isEmpty()) {
                return Collections.emptyList();
            }
            JSONArray array = extractArray(body, "data");
            if (array == null) {
                return Collections.emptyList();
            }
            List<ExternalParkingSpaceItemDto> items = array.toJavaList(ExternalParkingSpaceItemDto.class);
            if (items == null || items.isEmpty()) {
                return Collections.emptyList();
            }
            return items.stream().map(this::toSpaceStatVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取停车场车位分布失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ParkingFlowStatVO> getParkingFlow24h() {
        // 直接从外部系统实时获取，不落库
        try {
            String body = HttpUtil.createGet(apiHost + flow24hPath)
                    .timeout(TIMEOUT_MS)
                    .execute()
                    .body();
            log.debug("24小时停车流量外部API响应: {}", body);
            if (body == null || body.trim().isEmpty()) {
                return Collections.emptyList();
            }
            JSONArray array = extractArray(body, "data");
            if (array == null) {
                return Collections.emptyList();
            }
            List<ExternalParkingFlowItemDto> items = array.toJavaList(ExternalParkingFlowItemDto.class);
            if (items == null || items.isEmpty()) {
                return Collections.emptyList();
            }
            return items.stream()
                    .sorted((a, b) -> Integer.compare(nvl(a.getHour()), nvl(b.getHour())))
                    .map(this::toFlowStatVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取24小时停车流量失败", e);
            return Collections.emptyList();
        }
    }

    // ==================== 同步外部数据 → 写入DB ====================

    /**
     * 同步全部四项数据到DB，失败项跳过不更新
     */
    private void syncAllFromApi() {
        ParkingCount entity = getOrCreateToday();
        boolean hasData = false;

        Long todayEntry = fetchLongFromApi(apiHost + todayEntryPath, "todayEntryCount");
        if (todayEntry != null) {
            entity.setTodayEntryCount(todayEntry);
            hasData = true;
        }

        Long currentIn = fetchLongFromApi(apiHost + currentInPath, "currentInCount");
        if (currentIn != null) {
            entity.setCurrentInCount(currentIn);
            hasData = true;
        }

        Long remainingSpace = fetchLongFromApi(apiHost + remainingSpacePath, "remainingSpaceCount");
        if (remainingSpace != null) {
            entity.setRemainingSpaceCount(remainingSpace);
            hasData = true;
        }

        Double avgDuration = fetchDoubleFromApi(apiHost + avgDurationPath, "averageParkingDuration");
        if (avgDuration != null) {
            entity.setAverageParkingDuration(avgDuration);
            hasData = true;
        }

        if (hasData) {
            insertOrUpdate(entity);
        }
        log.info("同步停车统计完成: todayEntry={}, currentIn={}, remainingSpace={}, avgDuration={}",
                entity.getTodayEntryCount(), entity.getCurrentInCount(),
                entity.getRemainingSpaceCount(), entity.getAverageParkingDuration());
    }

    private void syncTodayEntryFromApi() {
        Long value = fetchLongFromApi(apiHost + todayEntryPath, "todayEntryCount");
        if (value != null) {
            ParkingCount entity = getOrCreateToday();
            entity.setTodayEntryCount(value);
            insertOrUpdate(entity);
            log.info("同步今日进场车辆数: {}", value);
        }
    }

    private void syncCurrentInFromApi() {
        Long value = fetchLongFromApi(apiHost + currentInPath, "currentInCount");
        if (value != null) {
            ParkingCount entity = getOrCreateToday();
            entity.setCurrentInCount(value);
            insertOrUpdate(entity);
            log.info("同步当前在场车辆数: {}", value);
        }
    }

    private void syncRemainingSpaceFromApi() {
        Long value = fetchLongFromApi(apiHost + remainingSpacePath, "remainingSpaceCount");
        if (value != null) {
            ParkingCount entity = getOrCreateToday();
            entity.setRemainingSpaceCount(value);
            insertOrUpdate(entity);
            log.info("同步剩余车位数: {}", value);
        }
    }

    private void syncAvgDurationFromApi() {
        Double value = fetchDoubleFromApi(apiHost + avgDurationPath, "averageParkingDuration");
        if (value != null) {
            ParkingCount entity = getOrCreateToday();
            entity.setAverageParkingDuration(value);
            insertOrUpdate(entity);
            log.info("同步平均停车时长: {}", value);
        }
    }

    // ==================== 数据库操作 ====================

    /**
     * 获取今天的记录，没有则创建新对象（不插入DB）
     */
    private ParkingCount getOrCreateToday() {
        ParkingCount today = getOne(new LambdaQueryWrapper<ParkingCount>()
                .eq(ParkingCount::getDate, LocalDate.now()));
        if (today == null) {
            today = new ParkingCount();
            today.setDate(LocalDate.now());
        }
        return today;
    }

    /**
     * 保存或更新：有id则更新，无id则插入
     */
    private void insertOrUpdate(ParkingCount entity) {
        if (entity.getId() != null) {
            updateById(entity);
        } else {
            save(entity);
        }
    }

    /**
     * 获取今天的统计记录（从DB重新读取）
     */
    private ParkingCount getToday() {
        return getOne(new LambdaQueryWrapper<ParkingCount>()
                .eq(ParkingCount::getDate, LocalDate.now()));
    }

    /**
     * 获取昨天的统计记录
     */
    private ParkingCount getYesterday() {
        return getOne(new LambdaQueryWrapper<ParkingCount>()
                .eq(ParkingCount::getDate, LocalDate.now().minusDays(1)));
    }

    // ==================== 查询DB → 构建VO ====================

    private ParkingStatCardVO queryTodayEntryCount() {
        ParkingCount today = getToday();
        ParkingCount yesterday = getYesterday();
        long todayVal = today == null ? 0L : nvl(today.getTodayEntryCount());
        long yesterdayVal = yesterday == null ? 0L : nvl(yesterday.getTodayEntryCount());

        return buildCard("今日进场车辆", todayVal, "", compareRate(todayVal, yesterdayVal), "较昨日");
    }

    private ParkingStatCardVO queryCurrentInCount() {
        ParkingCount today = getToday();
        ParkingCount yesterday = getYesterday();
        long todayVal = today == null ? 0L : nvl(today.getCurrentInCount());
        long yesterdayVal = yesterday == null ? 0L : nvl(yesterday.getCurrentInCount());

        return buildCard("当前在场车辆", todayVal, "", compareRate(todayVal, yesterdayVal), "较昨日");
    }

    private ParkingStatCardVO queryRemainingSpaceCount() {
        ParkingCount today = getToday();
        long todayVal = today == null ? 0L : nvl(today.getRemainingSpaceCount());

        return buildSimpleCard("剩余车位", todayVal, "", "可用");
    }

    private ParkingStatCardVO queryAverageParkingDuration() {
        ParkingCount today = getToday();
        ParkingCount yesterday = getYesterday();
        double todayVal = today == null ? 0.0 : nvl(today.getAverageParkingDuration());
        double yesterdayVal = yesterday == null ? 0.0 : nvl(yesterday.getAverageParkingDuration());

        return buildCard("平均停车时长", round(todayVal), "h", compareChange(todayVal, yesterdayVal), "较昨日");
    }

    // ==================== HTTP API请求 ====================

    private Long fetchLongFromApi(String url, String fieldName) {
        try {
            String body = HttpUtil.createGet(url)
                    .timeout(TIMEOUT_MS)
                    .execute()
                    .body();
            log.debug("外部API响应 [{}]: {}", fieldName, body);
            try {
                com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(body);
                if (json.containsKey("data")) {
                    return json.getLong("data");
                }
                if (json.containsKey(fieldName)) {
                    return json.getLong(fieldName);
                }
                return json.getLongValue("value");
            } catch (Exception e) {
                return Long.parseLong(body.trim());
            }
        } catch (Exception e) {
            log.error("请求外部API失败: url={}, field={}", url, fieldName, e);
            return null;
        }
    }

    private Double fetchDoubleFromApi(String url, String fieldName) {
        try {
            String body = HttpUtil.createGet(url)
                    .timeout(TIMEOUT_MS)
                    .execute()
                    .body();
            log.debug("外部API响应 [{}]: {}", fieldName, body);
            try {
                com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(body);
                if (json.containsKey("data")) {
                    return json.getDouble("data");
                }
                if (json.containsKey(fieldName)) {
                    return json.getDouble(fieldName);
                }
                return json.getDoubleValue("value");
            } catch (Exception e) {
                return Double.parseDouble(body.trim());
            }
        } catch (Exception e) {
            log.error("请求外部API失败: url={}, field={}", url, fieldName, e);
            return null;
        }
    }

    // ==================== 数据对比计算 ====================

    private String compareRate(long today, long yesterday) {
        if (yesterday == 0) {
            return today > 0 ? "↑100% " : "";
        }
        double rate = (today - yesterday) * 100.0 / yesterday;
        return formatRate(rate);
    }

    private String compareChange(double today, double yesterday) {
        if (yesterday == 0) {
            return today > 0 ? "↑" + formatValue(today) + "h " : "";
        }
        double change = today - yesterday;
        return formatChange(change);
    }

    // ==================== 格式化工具 ====================

    private String formatRate(double rate) {
        String arrow = rate >= 0 ? "↑" : "↓";
        double abs = Math.abs(rate);
        return arrow + (abs == (long) abs ? String.valueOf((long) abs) : String.format("%.1f", abs)) + "% ";
    }

    private String formatChange(double change) {
        String arrow = change >= 0 ? "↑" : "↓";
        double abs = Math.abs(change);
        return arrow + (abs == (long) abs ? String.valueOf((long) abs) : String.format("%.1f", abs)) + "h ";
    }

    private String formatValue(double value) {
        return value == (long) value ? String.valueOf((long) value) : String.format("%.1f", value);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private double nvl(Double v) {
        return v == null ? 0.0 : v;
    }

    // ==================== 卡片构建 ====================

    private ParkingStatCardVO buildCard(String title, Number value, String unit, String compare, String suffix) {
        ParkingStatCardVO vo = new ParkingStatCardVO();
        vo.setTitle(title);
        vo.setValue(value);
        vo.setUnit(unit);
        vo.setContext(compare + suffix);
        return vo;
    }

    private ParkingStatCardVO buildSimpleCard(String title, Number value, String unit, String context) {
        ParkingStatCardVO vo = new ParkingStatCardVO();
        vo.setTitle(title);
        vo.setValue(value);
        vo.setUnit(unit);
        vo.setContext(context);
        return vo;
    }

    // ==================== 图数据辅助 ====================

    /**
     * 从响应体中提取数组。
     * <p>
     * 支持三种格式：
     * 1. 整个 body 就是数组：[...]
     * 2. 顶层 data 是数组：{"data":[...]}
     * 3. 顶层 list 是数组：{"list":[...]}（兼容）
     */
    private JSONArray extractArray(String body, String fieldName) {
        try {
            if (body.trim().startsWith("[")) {
                return JSONArray.parseArray(body);
            }
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                return null;
            }
            if (json.containsKey("data") && json.get("data") instanceof JSONArray) {
                return json.getJSONArray("data");
            }
            if (json.containsKey("list") && json.get("list") instanceof JSONArray) {
                return json.getJSONArray("list");
            }
            // 兜底：尝试在 fieldName 中查找
            if (json.containsKey(fieldName) && json.get(fieldName) instanceof JSONArray) {
                return json.getJSONArray(fieldName);
            }
            // 兜底：扫描所有 value，找到第一个 JSONArray
            for (Object value : json.values()) {
                if (value instanceof JSONArray) {
                    return (JSONArray) value;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("解析外部API响应数组失败: body={}", body, e);
            return null;
        }
    }

    /**
     * 构造停车场车位分布VO，包含使用率
     */
    private ParkingSpaceStatVO toSpaceStatVO(ExternalParkingSpaceItemDto item) {
        ParkingSpaceStatVO vo = new ParkingSpaceStatVO();
        vo.setName(item.getName());
        long used = nvl(item.getUsed());
        long total = nvl(item.getTotal());
        vo.setUsed(used);
        vo.setTotal(total);
        if (total > 0) {
            double rate = Math.round(used * 1000.0 / total) / 10.0;
            vo.setUsageRate(rate);
        } else {
            vo.setUsageRate(0.0);
        }
        return vo;
    }

    /**
     * 构造 24 小时停车流量 VO
     */
    private ParkingFlowStatVO toFlowStatVO(ExternalParkingFlowItemDto item) {
        ParkingFlowStatVO vo = new ParkingFlowStatVO();
        vo.setHour(nvl(item.getHour()));
        vo.setInCount(nvl(item.getInCount()));
        vo.setOutCount(nvl(item.getOutCount()));
        return vo;
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}
