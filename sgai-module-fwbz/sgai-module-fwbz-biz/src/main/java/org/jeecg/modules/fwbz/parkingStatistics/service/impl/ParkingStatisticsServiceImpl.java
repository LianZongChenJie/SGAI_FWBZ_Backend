package org.jeecg.modules.fwbz.parkingStatistics.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.parkingStatistics.dto.ExternalParkingSpaceItemDto;
import org.jeecg.modules.fwbz.parkingStatistics.entity.ParkingCount;
import org.jeecg.modules.fwbz.parkingStatistics.mapper.ParkingCountMapper;
import org.jeecg.modules.fwbz.parkingStatistics.service.IParkingStatisticsService;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingFlow24hVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingSpaceStatVO;
import org.jeecg.modules.fwbz.parkingStatistics.vo.ParkingStatCardVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
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

    @Value("${parking.statistics.api.carTotalAmountUrl:http://10.168.47.26:9999/sgai-api/openApi/yitingche/car_total_amount}")
    private String carTotalAmountUrl;

    @Value("${parking.statistics.api.parkingNumberUrl:http://10.168.47.26:9999/sgai-api/openApi/yitingche/parking_number}")
    private String parkingNumberUrl;

    @Value("${parking.statistics.api.avgStopTimeUrl:http://10.168.47.26:9999/sgai-api/openApi/yitingche/avg_stop_time}")
    private String avgStopTimeUrl;

    @Value("${parking.statistics.api.parkingInfoUrl:http://10.168.47.26:9999/sgai-api/openApi/yitingche/parking_info}")
    private String parkingInfoUrl;

    @Value("${parking.statistics.api.flow24hUrl:http://10.168.47.26:9999/sgai-api/openApi/yitingche/tingchechangliuliangqushijinchuzong}")
    private String flow24hUrl;

    @Value("${parking.statistics.api.appKey:R6VOSNoijW3o4WA5eFjW5l2bO}")
    private String appKey;

    @Value("${parking.statistics.api.appSecret:GDg18aNuWaKsIX33euL0maXbSVqZSp}")
    private String appSecret;

    // ========== 临时地址（后期替换） ==========
    @Value("${parking.statistics.api.tempBaseUrl:http://10.168.56.101:8088/api}")
    private String tempBaseUrl;

    @Value("${parking.statistics.api.tempTodayEntryPath:/fwbz/parkingStatistics/todayEntryCount}")
    private String tempTodayEntryPath;

    @Value("${parking.statistics.api.tempRemainingSpacePath:/fwbz/parkingStatistics/remainingSpaceCount}")
    private String tempRemainingSpacePath;

    @Value("${parking.statistics.api.tempAvgDurationPath:/fwbz/parkingStatistics/averageParkingDuration}")
    private String tempAvgDurationPath;

    @Value("${parking.statistics.api.tempSpaceDistributionPath:/fwbz/parkingStatistics/parkingSpaceDistribution}")
    private String tempSpaceDistributionPath;

    @Value("${parking.statistics.api.tempFlow24hPath:/fwbz/parkingStatistics/parkingFlow24h}")
    private String tempFlow24hPath;

    private static final int TIMEOUT_MS = 5000;

    private final RestTemplate restTemplate;

    public ParkingStatisticsServiceImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897)));
        this.restTemplate = new RestTemplate(factory);
    }

    // ==================== 卡片查询（仅读库，同步由定时任务负责） ====================

    @Override
    public ParkingStatCardVO queryTodayEntryCount() {
        return doQueryTodayEntryCount();
    }

    @Override
    public ParkingStatCardVO queryCurrentInCount() {
        return doQueryCurrentInCount();
    }

    @Override
    public ParkingStatCardVO queryRemainingSpaceCount() {
        return doQueryRemainingSpaceCount();
    }

    @Override
    public ParkingStatCardVO queryAverageParkingDuration() {
        return doQueryAverageParkingDuration();
    }

    @Override
    public List<ParkingStatCardVO> querySummary() {
        return Arrays.asList(
                doQueryTodayEntryCount(),
                doQueryCurrentInCount(),
                doQueryRemainingSpaceCount(),
                doQueryAverageParkingDuration()
        );
    }

    @Override
    public List<ParkingSpaceStatVO> getParkingSpaceDistribution() {
        // 使用临时地址实时获取，不落库（后期替换为正式地址）
        try {
            String url = tempBaseUrl + tempSpaceDistributionPath;
            log.info("url: {}", url);
            RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            log.debug("停车场车位分布外部API响应: {}", body);
            if (body == null || body.trim().isEmpty()) {
                return Collections.emptyList();
            }
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                return Collections.emptyList();
            }
            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                return Collections.emptyList();
            }
            JSONArray array = result.getJSONArray("detail");
            if (array == null || array.isEmpty()) {
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
    public ParkingFlow24hVO getParkingFlow24h() {
        // 使用临时地址实时获取，不落库（后期替换为正式地址）
        try {
            String url = tempBaseUrl + tempFlow24hPath;
            RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            log.debug("24小时停车流量外部API响应: {}", body);
            if (body == null || body.trim().isEmpty()) {
                return null;
            }
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                return null;
            }
            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                return null;
            }
            ParkingFlow24hVO vo = new ParkingFlow24hVO();
            vo.setDate(result.getJSONArray("date") != null
                    ? result.getJSONArray("date").toJavaList(String.class) : null);
            vo.setIn(result.getJSONArray("in") != null
                    ? result.getJSONArray("in").toJavaList(Long.class) : null);
            vo.setOut(result.getJSONArray("out") != null
                    ? result.getJSONArray("out").toJavaList(Long.class) : null);
            vo.setTotal(result.getJSONArray("total") != null
                    ? result.getJSONArray("total").toJavaList(Long.class) : null);
            vo.setTodayInTotal(result.getLong("todayInTotal"));
            vo.setTodayOutTotal(result.getLong("todayOutTotal"));
            vo.setTodayInOutTotal(result.getLong("todayInOutTotal"));
            return vo;
        } catch (Exception e) {
            log.error("获取24小时停车流量失败", e);
            return null;
        }
    }

    // ==================== 同步外部数据 → 写入DB ====================

    /**
     * 同步全部四项数据到DB，失败项跳过不更新。
     * <p>由定时任务 ParkingStatisticsJob 每5分钟自动调用。</p>
     */
    @Override
    public void syncAllFromApi() {
        ParkingCount entity = getOrCreateToday();
        boolean hasData = false;

        // 使用临时地址同步（后期替换为正式地址）
        Long todayEntry = fetchTodayEntryFromTempApi();
        if (todayEntry != null) {
            entity.setTodayEntryCount(todayEntry);
            hasData = true;
        }

        // 当前在场和剩余车位来自同一个API，只请求一次
        long[] parkingNumber = fetchParkingNumberFromTempApi();
        if (parkingNumber != null) {
            entity.setCurrentInCount(parkingNumber[0]);
            entity.setRemainingSpaceCount(parkingNumber[1]);
            hasData = true;
        }

        Double avgDuration = fetchAvgDurationFromTempApi();
        if (avgDuration != null) {
            entity.setAverageParkingDuration(avgDuration);
            hasData = true;
        }

        if (hasData) {
            insertOrUpdate(entity);
        }
        log.info("同步停车统计完成(临时地址): todayEntry={}, currentIn={}, remainingSpace={}, avgDuration={}",
                entity.getTodayEntryCount(), entity.getCurrentInCount(),
                entity.getRemainingSpaceCount(), entity.getAverageParkingDuration());
    }

    private void syncTodayEntryFromApi() {
        Long value = fetchTodayEntryFromCarTotalAmountApi();
        if (value != null) {
            ParkingCount entity = getOrCreateToday();
            entity.setTodayEntryCount(value);
            insertOrUpdate(entity);
            log.info("同步今日进场车辆数: {}", value);
        }
    }

    private void syncCurrentInFromApi() {
        long[] parkingNumber = fetchParkingNumberFromApi();
        if (parkingNumber != null) {
            ParkingCount entity = getOrCreateToday();
            entity.setCurrentInCount(parkingNumber[0]);
            insertOrUpdate(entity);
            log.info("同步当前在场车辆数: {}", parkingNumber[0]);
        }
    }

    private void syncRemainingSpaceFromApi() {
        long[] parkingNumber = fetchParkingNumberFromApi();
        if (parkingNumber != null) {
            ParkingCount entity = getOrCreateToday();
            entity.setRemainingSpaceCount(parkingNumber[1]);
            insertOrUpdate(entity);
            log.info("同步剩余车位数: {}", parkingNumber[1]);
        }
    }

    private void syncAvgDurationFromApi() {
        Double value = fetchAvgStopTimeFromApi();
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

    private ParkingStatCardVO doQueryTodayEntryCount() {
        ParkingCount today = getToday();
        ParkingCount yesterday = getYesterday();
        long todayVal = today == null ? 0L : nvl(today.getTodayEntryCount());
        long yesterdayVal = yesterday == null ? 0L : nvl(yesterday.getTodayEntryCount());

        return buildCard("今日进场车辆", todayVal, "", compareRate(todayVal, yesterdayVal), "较昨日");
    }

    private ParkingStatCardVO doQueryCurrentInCount() {
        ParkingCount today = getToday();
        ParkingCount yesterday = getYesterday();
        long todayVal = today == null ? 0L : nvl(today.getCurrentInCount());
        long yesterdayVal = yesterday == null ? 0L : nvl(yesterday.getCurrentInCount());

        return buildCard("当前在场车辆", todayVal, "", compareRate(todayVal, yesterdayVal), "较昨日");
    }

    private ParkingStatCardVO doQueryRemainingSpaceCount() {
        ParkingCount today = getToday();
        long todayVal = today == null ? 0L : nvl(today.getRemainingSpaceCount());

        return buildSimpleCard("剩余车位", todayVal, "", "可用");
    }

    private ParkingStatCardVO doQueryAverageParkingDuration() {
        ParkingCount today = getToday();
        ParkingCount yesterday = getYesterday();
        double todayVal = today == null ? 0.0 : nvl(today.getAverageParkingDuration());
        double yesterdayVal = yesterday == null ? 0.0 : nvl(yesterday.getAverageParkingDuration());

        return buildCard("平均停车时长", round(todayVal), "h", compareChange(todayVal, yesterdayVal), "较昨日");
    }

    // ==================== HTTP API请求 ====================

    /**
     * 请求停车场车辆统计API获取今日进场车辆数
     * <p>
     * 请求地址: car_total_amount，需携带 appKey/appSecret 请求头，
     * 从响应 result.carIn 字段提取今日进场车辆数。
     */
    private Long fetchTodayEntryFromCarTotalAmountApi() {
        try {
            RequestEntity<Void> request = RequestEntity.get(URI.create(carTotalAmountUrl))
                    .header("appKey", appKey)
                    .header("appSecret", appSecret)
                    .build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            log.debug("停车场车辆统计API响应: {}", body);
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                log.error("停车场车辆统计API响应解析失败");
                return null;
            }
            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                log.error("停车场车辆统计API响应中无result字段");
                return null;
            }
            return result.getLong("carIn");
        } catch (Exception e) {
            log.error("请求停车场车辆统计API失败", e);
            return null;
        }
    }

    /**
     * 请求停车场车位数量API获取当前在场和剩余车位
     * <p>
     * 请求地址: parking_number，需携带 appKey/appSecret 请求头，
     * 从响应 result 中提取 spaces(总车位) 和 shengyu(剩余车位)，
     * 在场车辆 = 总车位 - 剩余车位。
     *
     * @return [在场车辆, 剩余车位]，失败返回 null
     */
    private long[] fetchParkingNumberFromApi() {
        try {
            RequestEntity<Void> request = RequestEntity.get(URI.create(parkingNumberUrl))
                    .header("appKey", appKey)
                    .header("appSecret", appSecret)
                    .build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            log.debug("停车场车位数量API响应: {}", body);
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                log.error("停车场车位数量API响应解析失败");
                return null;
            }
            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                log.error("停车场车位数量API响应中无result字段");
                return null;
            }
            long spaces = result.getLongValue("spaces");
            long shengyu = result.getLongValue("shengyu");
            long currentIn = spaces - shengyu;
            return new long[]{currentIn, shengyu};
        } catch (Exception e) {
            log.error("请求停车场车位数量API失败", e);
            return null;
        }
    }

    /**
     * 请求平均停车时长API
     * <p>
     * 请求地址: avg_stop_time，需携带 appKey/appSecret 请求头，
     * 从响应 result.stopTime 字段提取平均停车时长（小时）。
     *
     * @return 平均停车时长（小时），失败返回 null
     */
    private Double fetchAvgStopTimeFromApi() {
        try {
            RequestEntity<Void> request = RequestEntity.get(URI.create(avgStopTimeUrl))
                    .header("appKey", appKey)
                    .header("appSecret", appSecret)
                    .build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            log.debug("平均停车时长API响应: {}", body);
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                log.error("平均停车时长API响应解析失败");
                return null;
            }
            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                log.error("平均停车时长API响应中无result字段");
                return null;
            }
            return result.getDouble("stopTime");
        } catch (Exception e) {
            log.error("请求平均停车时长API失败", e);
            return null;
        }
    }

    // ==================== 临时API请求（后期替换为正式地址） ====================

    /**
     * 请求临时-今日进场车辆数API
     * <p>
     * 响应格式: {"success":true, "code":200, "result":{"carIn":3604, "carOut":2527, "sum":6131}}
     * carIn 为今日进场车辆数。
     */
    private Long fetchTodayEntryFromTempApi() {
        try {
            String url = tempBaseUrl + tempTodayEntryPath;
            RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            log.debug("临时-今日进场车辆API响应: {}", body);
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                log.error("临时-今日进场车辆API响应解析失败");
                return null;
            }
            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                log.error("临时-今日进场车辆API响应中无result字段");
                return null;
            }
            return result.getLong("carIn");
        } catch (Exception e) {
            log.error("请求临时-今日进场车辆API失败", e);
            return null;
        }
    }

    /**
     * 请求临时-停车场车位数量API获取当前在场和剩余车位
     * <p>
     * 响应格式: {"success":true, "code":200, "result":{"shengyu":5021, "spaces":9662}}
     * spaces为总车位，shengyu为剩余车位，在场车辆 = 总车位 - 剩余车位。
     *
     * @return [在场车辆, 剩余车位]，失败返回 null
     */
    private long[] fetchParkingNumberFromTempApi() {
        try {
            String url = tempBaseUrl + tempRemainingSpacePath;
            RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            log.debug("临时-停车场车位数量API响应: {}", body);
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                log.error("临时-停车场车位数量API响应解析失败");
                return null;
            }
            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                log.error("临时-停车场车位数量API响应中无result字段");
                return null;
            }
            long spaces = result.getLongValue("spaces");
            long shengyu = result.getLongValue("shengyu");
            long currentIn = spaces - shengyu;
            return new long[]{currentIn, shengyu};
        } catch (Exception e) {
            log.error("请求临时-停车场车位数量API失败", e);
            return null;
        }
    }

    /**
     * 请求临时-平均停车时长API
     * <p>
     * 响应格式: {"success":true, "code":200, "result":{"avgDuration": 1.5}}
     *
     * @return 平均停车时长（小时），失败返回 null
     */
    private Double fetchAvgDurationFromTempApi() {
        try {
            String url = tempBaseUrl + tempAvgDurationPath;
            RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
            log.debug("临时-平均停车时长API响应: {}", body);
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                log.error("临时-平均停车时长API响应解析失败");
                return null;
            }
            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                log.error("临时-平均停车时长API响应中无result字段");
                return null;
            }
            // 尝试从 result 中获取平均停车时长，支持多种字段名
            if (result.containsKey("avgDuration")) {
                return result.getDouble("avgDuration");
            }
            if (result.containsKey("stopTime")) {
                return result.getDouble("stopTime");
            }
            if (result.containsKey("duration")) {
                return result.getDouble("duration");
            }
            log.error("临时-平均停车时长API响应中无有效字段, result={}", result);
            return null;
        } catch (Exception e) {
            log.error("请求临时-平均停车时长API失败", e);
            return null;
        }
    }

    private Long fetchLongFromApi(String url, String fieldName) {
        try {
            RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
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
            RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            String body = response.getBody();
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
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setLng(item.getLng());
        vo.setLat(item.getLat());
        long total = nvl(item.getSpaces());
        long shengyu = nvl(item.getShengyu());
        long used = total - shengyu;
        vo.setUsed(used);
        vo.setTotal(total);
        vo.setShengyu(shengyu);
        vo.setState(item.getState());
        vo.setSaturation(item.getSaturation());
        vo.setUsedRate(item.getUsedRate());
        if (total > 0) {
            double rate = Math.round(used * 1000.0 / total) / 10.0;
            vo.setUsageRate(rate);
        } else {
            vo.setUsageRate(0.0);
        }
        return vo;
    }
}
