package org.jeecg.modules.fwbz.venueVisitorFlow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VisitorFlow;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VisitorFlowMapper;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueVisitorFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VisitorFlowCardVO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * 场馆客流统计 Service 实现
 * <p>
 * 逻辑：调取 HTTP API（今日总客流 / 当前在场 / 峰值客流 / 平均停留）
 * → 同步入库（一天一行，失败项跳过不影响其他项）
 * → 从数据库读取构建卡片 VO 返回前端，含较昨日对比。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Service
public class VenueVisitorFlowServiceImpl extends ServiceImpl<VisitorFlowMapper, VisitorFlow>
        implements IVenueVisitorFlowService {

    /**
     * 场馆整体客流统计 HTTP API 地址（假地址，替换为实际地址）。
     */
    private static final String OVERALL_FLOW_API_URL = "http://api.example.com/api/visitorFlow/overall";

    private final RestTemplate restTemplate;

    public VenueVisitorFlowServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    // ==================== 卡片查询（仅读库，同步由定时任务负责） ====================

    @Override
    public VisitorFlowCardVO queryTodayVisitorCount() {
        return doQueryTodayVisitorCount();
    }

    @Override
    public VisitorFlowCardVO queryCurrentVisitorCount() {
        return doQueryCurrentVisitorCount();
    }

    @Override
    public VisitorFlowCardVO queryPeakVisitorCount() {
        return doQueryPeakVisitorCount();
    }

    @Override
    public VisitorFlowCardVO queryAverageStopDuration() {
        return doQueryAverageStopDuration();
    }

    @Override
    public List<VisitorFlowCardVO> querySummary() {
        return java.util.Arrays.asList(
                doQueryTodayVisitorCount(),
                doQueryCurrentVisitorCount(),
                doQueryPeakVisitorCount(),
                doQueryAverageStopDuration()
        );
    }

    // ==================== 同步 HTTP API → 写入数据库 ====================

    @Override
    public int syncFromApi() {
        log.info("开始从 HTTP API 同步四个客流数据项...");
        VisitorFlow entity = getOrCreateToday();
        int successCount = 0;

        // 调用统一接口获取四项整体客流数据
        JSONObject apiData = fetchOverallFlowFromApi();
        if (apiData != null) {
            // 1. 今日总客流
            Long todayCount = apiData.getLong("todayCount");
            if (todayCount != null) {
                entity.setTodayCount(todayCount);
                successCount++;
            }

            // 2. 当前在场人数
            Long nowCount = apiData.getLong("nowCount");
            if (nowCount != null) {
                entity.setNowCount(nowCount);
                successCount++;
            }

            // 3. 峰值客流
            Long maxCount = apiData.getLong("maxCount");
            if (maxCount != null) {
                entity.setMaxCount(maxCount);
                successCount++;
            }

            // 4. 平均停留时长（小时）
            Double avgStop = apiData.getDouble("averageStopDuration");
            if (avgStop != null) {
                entity.setAverageStopDuration(avgStop);
                successCount++;
            }
        }

        // 任意一项成功则写库
        if (successCount > 0) {
            insertOrUpdate(entity);
        }

        log.info("HTTP API 客流同步完成: todayCount={}, nowCount={}, maxCount={}, averageStop={}",
                entity.getTodayCount(), entity.getNowCount(),
                entity.getMaxCount(), entity.getAverageStopDuration());
        return successCount;
    }

    /**
     * 调用 HTTP API 获取整体客流数据。
     * <p>响应格式示例：
     * {"code":200,"msg":"success","data":{"todayCount":12345,"nowCount":678,"maxCount":1000,"averageStopDuration":1.5}}
     * </p>
     *
     * @return data 节点 JSONObject，失败返回 null
     */
    private JSONObject fetchOverallFlowFromApi() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(OVERALL_FLOW_API_URL, String.class);
            String responseBody = response.getBody();
            log.debug("HTTP API 整体客流响应: {}", responseBody);

            JSONObject json = JSONObject.parseObject(responseBody);
            if (json == null || json.getInteger("code") == null || json.getInteger("code") != 200) {
                log.error("请求 HTTP API 整体客流失败: {}", responseBody);
                return null;
            }
            return json.getJSONObject("data");
        } catch (Exception e) {
            log.error("请求 HTTP API 整体客流异常", e);
            return null;
        }
    }

    // ==================== 数据库操作 ====================

    /**
     * 获取今天的客流记录，没有则返回新对象（未入库）。
     */
    private VisitorFlow getOrCreateToday() {
        VisitorFlow today = getOne(new LambdaQueryWrapper<VisitorFlow>()
                .eq(VisitorFlow::getDate, LocalDate.now()));
        if (today == null) {
            today = new VisitorFlow();
            today.setDate(LocalDate.now());
        }
        return today;
    }

    /**
     * 保存或更新：有 id 则更新，无 id 则插入。
     */
    private void insertOrUpdate(VisitorFlow entity) {
        if (entity.getId() != null) {
            updateById(entity);
        } else {
            save(entity);
        }
    }

    /**
     * 获取今天的记录（从数据库重新读取）。
     */
    private VisitorFlow getToday() {
        return getOne(new LambdaQueryWrapper<VisitorFlow>()
                .eq(VisitorFlow::getDate, LocalDate.now()));
    }

    /**
     * 获取昨天的记录。
     */
    private VisitorFlow getYesterday() {
        return getOne(new LambdaQueryWrapper<VisitorFlow>()
                .eq(VisitorFlow::getDate, LocalDate.now().minusDays(1)));
    }

    // ==================== 从 DB 构建返回 VO ====================

    private VisitorFlowCardVO doQueryTodayVisitorCount() {
        VisitorFlow today = getToday();
        VisitorFlow yesterday = getYesterday();
        long todayVal = today == null ? 0L : nvl(today.getTodayCount());
        long yesterdayVal = yesterday == null ? 0L : nvl(yesterday.getTodayCount());

        return buildCard("今日总客流", todayVal, "",
                compareRate(todayVal, yesterdayVal) + " 较昨日");
    }

    private VisitorFlowCardVO doQueryCurrentVisitorCount() {
        VisitorFlow today = getToday();
        VisitorFlow yesterday = getYesterday();
        long todayVal = today == null ? 0L : nvl(today.getNowCount());
        long yesterdayVal = yesterday == null ? 0L : nvl(yesterday.getNowCount());

        return buildCard("当前在场", todayVal, "",
                compareRate(todayVal, yesterdayVal) + " 较昨日");
    }

    private VisitorFlowCardVO doQueryPeakVisitorCount() {
        VisitorFlow today = getToday();
        VisitorFlow yesterday = getYesterday();
        long todayVal = today == null ? 0L : nvl(today.getMaxCount());
        long yesterdayVal = yesterday == null ? 0L : nvl(yesterday.getMaxCount());

        return buildCard("峰值客流", todayVal, "",
                compareRate(todayVal, yesterdayVal) + " 较昨日");
    }

    private VisitorFlowCardVO doQueryAverageStopDuration() {
        VisitorFlow today = getToday();
        VisitorFlow yesterday = getYesterday();
        double todayVal = today == null ? 0.0 : nvl(today.getAverageStopDuration());
        double yesterdayVal = yesterday == null ? 0.0 : nvl(yesterday.getAverageStopDuration());

        return buildCard("平均停留", round(todayVal, 1), "h",
                compareChange(todayVal, yesterdayVal) + " 较昨日");
    }

    // ==================== 通用对比 & 格式化 ====================

    /**
     * 数值型卡片增减率对比（百分比），参考 activeMeetStatistics 实现。
     */
    private String compareRate(long today, long yesterday) {
        if (yesterday == 0) {
            if (today == 0) {
                return "—";
            }
            return "↑100%";
        }
        double rate = (today - yesterday) * 100.0 / yesterday;
        String arrow = rate >= 0 ? "↑" : "↓";
        double abs = Math.abs(rate);
        return arrow + (abs == (long) abs ? String.valueOf((long) abs) : String.format("%.1f", abs)) + "%";
    }

    /**
     * 时长型卡片增减对比（绝对差值，保留 1 位小数）。
     */
    private String compareChange(double today, double yesterday) {
        if (yesterday == 0) {
            if (today == 0) {
                return "—";
            }
            return "↑" + formatValue(today) + "h";
        }
        double change = today - yesterday;
        String arrow = change >= 0 ? "↑" : "↓";
        double abs = Math.abs(change);
        return arrow + (abs == (long) abs ? String.valueOf((long) abs) : String.format("%.1f", abs)) + "h";
    }

    private String formatValue(double value) {
        return value == (long) value ? String.valueOf((long) value) : String.format("%.1f", value);
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private double nvl(Double v) {
        return v == null ? 0.0 : v;
    }

    private VisitorFlowCardVO buildCard(String title, Number value, String unit, String context) {
        VisitorFlowCardVO vo = new VisitorFlowCardVO();
        vo.setTitle(title);
        vo.setValue(value);
        vo.setUnit(unit);
        vo.setContext(context);
        return vo;
    }
}
