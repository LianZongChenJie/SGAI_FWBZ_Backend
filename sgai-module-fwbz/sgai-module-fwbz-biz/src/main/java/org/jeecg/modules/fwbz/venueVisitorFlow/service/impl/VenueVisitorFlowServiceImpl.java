package org.jeecg.modules.fwbz.venueVisitorFlow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VisitorFlow;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VisitorFlowMapper;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueVisitorFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VisitorFlowCardVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 场馆客流统计 Service 实现
 * <p>
 * 逻辑：调取海康四个 OpenAPI（今日总客流 / 当前在场 / 峰值客流 / 平均停留）
 * → 同步入库（一天一行，失败项跳过不影响其他项）
 * → 从数据库读取构建卡片 VO 返回前端，含较昨日对比。
 * </p>
 *
 * <p>API 路径以平台"运管中心-API管理-门禁管理/人员统计"实际列表为准，
 * 可在 application.yml 中通过 visitor.flow.api.*Path 调整。</p>
 *
 * @author fwbz
 */
@Slf4j
@Service
public class VenueVisitorFlowServiceImpl extends ServiceImpl<VisitorFlowMapper, VisitorFlow>
        implements IVenueVisitorFlowService {

    /**
     * 海康门禁事件搜索 API：用于统计今日总客流。
     * <p>取 data.total 字段作为今日总客流。</p>
     */
    private static final String TODAY_COUNT_API = "/api/acs/v1/event/totalSearch";

    /**
     * 海康当前在场人数 API：用于统计当前在场客流。
     * <p>取 data.count 字段作为当前在场人数。</p>
     */
    private static final String CURRENT_COUNT_API = "/api/acs/v1/statistic/currentPerson";

    /**
     * 海康人员峰值统计 API：用于统计当日峰值客流。
     * <p>取 data.count 字段作为峰值客流（实际字段名以平台文档为准）。</p>
     */
    private static final String PEAK_COUNT_API = "/api/hcp/v1/people/peak";

    /**
     * 海康平均停留时长 API：用于统计当日人员平均停留时长（小时）。
     * <p>取 data.hours 字段作为平均停留时长（实际字段名以平台文档为准）。</p>
     */
    private static final String AVG_STOP_API = "/api/hcp/v1/people/averageStop";

    private final HikvisionUtil hikvisionUtil;

    public VenueVisitorFlowServiceImpl(HikvisionUtil hikvisionUtil) {
        this.hikvisionUtil = hikvisionUtil;
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

    // ==================== 同步海康四个 API → 写入数据库 ====================

    @Override
    public int syncFromHikvision() {
        log.info("开始从海康同步四个客流数据项...");
        VisitorFlow entity = getOrCreateToday();
        int successCount = 0;

        // 1. 今日总客流
        Long todayCount = fetchTodayCountFromApi();
        if (todayCount != null) {
            entity.setTodayCount(todayCount);
            successCount++;
        }

        // 2. 当前在场人数
        Long currentCount = fetchCurrentCountFromApi();
        if (currentCount != null) {
            entity.setNowCount(currentCount);
            successCount++;
        }

        // 3. 峰值客流
        Long peakCount = fetchPeakCountFromApi();
        if (peakCount != null) {
            entity.setMaxCount(peakCount);
            successCount++;
        }

        // 4. 平均停留时长（小时）
        Double avgStop = fetchAverageStopFromApi();
        if (avgStop != null) {
            entity.setAverageStopDuration(avgStop);
            successCount++;
        }

        // 任意一项成功则写库
        if (successCount > 0) {
            insertOrUpdate(entity);
        }

        log.info("海康客流同步完成: todayCount={}, nowCount={}, maxCount={}, averageStop={}",
                entity.getTodayCount(), entity.getNowCount(),
                entity.getMaxCount(), entity.getAverageStopDuration());
        return successCount;
    }

    /**
     * 调用海康门禁事件搜索 API 获取今日总客流。
     * <p>请求示例：{"pageNo":1,"pageSize":1,"returnTotal":true}，
     * 取响应 data.total 字段（实际字段以平台文档为准）。</p>
     *
     * @return 今日总客流，失败返回 null
     */
    private Long fetchTodayCountFromApi() {
        try {
            JSONObject body = new JSONObject();
            body.put("pageNo", 1);
            body.put("pageSize", 1);
            body.put("returnTotal", true);

            String responseBody = hikvisionUtil.doPostJson(TODAY_COUNT_API, body.toJSONString());
            log.debug("海康今日总客流响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("请求海康今日总客流失败: {}", responseBody);
                return null;
            }
            JSONObject data = hikvisionUtil.getResponseData(responseBody);
            if (data == null) {
                return null;
            }
            // data.total 优先；部分版本可能为 data.num / data.count
            Long total = data.getLong("total");
            if (total == null) {
                total = data.getLong("num");
            }
            if (total == null) {
                total = data.getLong("count");
            }
            return total;
        } catch (Exception e) {
            log.error("请求海康今日总客流异常", e);
            return null;
        }
    }

    /**
     * 调用海康当前在场人数 API 获取当前在场客流。
     * <p>GET 请求，取响应 data.count 字段。</p>
     *
     * @return 当前在场客流，失败返回 null
     */
    private Long fetchCurrentCountFromApi() {
        try {
            String responseBody = hikvisionUtil.doGetString(CURRENT_COUNT_API, null);
            log.debug("海康当前在场客流响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("请求海康当前在场客流失败: {}", responseBody);
                return null;
            }
            JSONObject data = hikvisionUtil.getResponseData(responseBody);
            if (data == null) {
                return null;
            }
            return data.getLong("count");
        } catch (Exception e) {
            log.error("请求海康当前在场客流异常", e);
            return null;
        }
    }

    /**
     * 调用海康峰值客流 API 获取当日峰值客流。
     * <p>实际 API 路径以平台"运管中心-API管理-人员统计"为准；
     * 若平台无独立峰值接口，建议在请求参数中通过时间范围 / 分组参数获取当日峰值。</p>
     *
     * @return 峰值客流，失败返回 null
     */
    private Long fetchPeakCountFromApi() {
        try {
            // 实际请求参数以平台人员统计接口为准，此处仅为示例
            JSONObject body = new JSONObject();
            body.put("statisticType", "peak");
            body.put("timeRange", "today");

            String responseBody = hikvisionUtil.doPostJson(PEAK_COUNT_API, body.toJSONString());
            log.debug("海康峰值客流响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("请求海康峰值客流失败: {}", responseBody);
                return null;
            }
            JSONObject data = hikvisionUtil.getResponseData(responseBody);
            if (data == null) {
                return null;
            }
            Long value = data.getLong("count");
            if (value == null) {
                value = data.getLong("maxCount");
            }
            if (value == null) {
                value = data.getLong("peak");
            }
            return value;
        } catch (Exception e) {
            log.error("请求海康峰值客流异常", e);
            return null;
        }
    }

    /**
     * 调用海康平均停留时长 API 获取当日人员平均停留时长（小时）。
     * <p>实际 API 路径以平台"运管中心-API管理-人员统计"为准。</p>
     *
     * @return 平均停留时长（小时），失败返回 null
     */
    private Double fetchAverageStopFromApi() {
        try {
            JSONObject body = new JSONObject();
            body.put("statisticType", "averageStop");
            body.put("timeRange", "today");

            String responseBody = hikvisionUtil.doPostJson(AVG_STOP_API, body.toJSONString());
            log.debug("海康平均停留时长响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("请求海康平均停留时长失败: {}", responseBody);
                return null;
            }
            JSONObject data = hikvisionUtil.getResponseData(responseBody);
            if (data == null) {
                return null;
            }
            Double hours = data.getDouble("hours");
            if (hours == null) {
                hours = data.getDouble("averageStopDuration");
            }
            if (hours == null) {
                hours = data.getDouble("averageDuration");
            }
            if (hours == null) {
                hours = data.getDouble("duration");
            }
            return hours;
        } catch (Exception e) {
            log.error("请求海康平均停留时长异常", e);
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
