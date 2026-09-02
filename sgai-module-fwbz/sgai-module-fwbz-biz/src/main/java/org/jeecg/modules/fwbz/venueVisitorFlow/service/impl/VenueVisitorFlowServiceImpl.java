package org.jeecg.modules.fwbz.venueVisitorFlow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VenueFlowHour;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VenueFlowHourMapper;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueVisitorFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VisitorFlowCardVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 场馆客流统计 Service 实现
 * <p>
 * 数据来源：table_venue_flow_hour（各场馆客流分时统计表）。
 * 统计逻辑：取每个场馆当日最新一条记录，汇总计算四张卡片。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Service
public class VenueVisitorFlowServiceImpl extends ServiceImpl<VenueFlowHourMapper, VenueFlowHour>
        implements IVenueVisitorFlowService {

    @Override
    public int syncFromApi() {
        log.info("syncFromApi: 数据来源已切换至 table_venue_flow_hour，不需要此同步逻辑");
        return 0;
    }

    // ==================== 卡片查询（从 table_venue_flow_hour 统计） ====================

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
        // 一次查询，批量计算四项，避免重复查库
        List<VenueFlowHour> todayLatest = getLatestPerVenue(LocalDate.now());
        List<VenueFlowHour> yesterdayLatest = getLatestPerVenue(LocalDate.now().minusDays(1));

        // 今日进场 = 各馆最新 todayInCount 之和
        long todayInTotal = todayLatest.stream().mapToLong(v -> nvl(v.getTodayInCount())).sum();
        long yesterdayInTotal = yesterdayLatest.stream().mapToLong(v -> nvl(v.getTodayInCount())).sum();

        // 当前在场 = 各馆最新 todayNowCount 之和
        long todayNowTotal = todayLatest.stream().mapToLong(v -> nvl(v.getTodayNowCount())).sum();
        long yesterdayNowTotal = yesterdayLatest.stream().mapToLong(v -> nvl(v.getTodayNowCount())).sum();

        // 峰值客流 = 各馆最新 maxCount 的平均值
        long todayPeakAvg = todayLatest.isEmpty() ? 0L :
                Math.round(todayLatest.stream().mapToLong(v -> nvl(v.getMaxCount())).average().orElse(0));
        long yesterdayPeakAvg = yesterdayLatest.isEmpty() ? 0L :
                Math.round(yesterdayLatest.stream().mapToLong(v -> nvl(v.getMaxCount())).average().orElse(0));

        // 平均时长 = 各馆最新 averageDuration 的平均值
        double todayAvgDuration = todayLatest.stream()
                .filter(v -> v.getAverageDuration() != null)
                .mapToDouble(VenueFlowHour::getAverageDuration)
                .average().orElse(0);
        double yesterdayAvgDuration = yesterdayLatest.stream()
                .filter(v -> v.getAverageDuration() != null)
                .mapToDouble(VenueFlowHour::getAverageDuration)
                .average().orElse(0);

        return Arrays.asList(
                buildCard("今日总客流", todayInTotal, "",
                        compareRate(todayInTotal, yesterdayInTotal) + " 较昨日"),
                buildCard("当前在场", todayNowTotal, "",
                        compareRate(todayNowTotal, yesterdayNowTotal) + " 较昨日"),
                buildCard("峰值客流", todayPeakAvg, "",
                        compareRate(todayPeakAvg, yesterdayPeakAvg) + " 较昨日"),
                buildCard("平均停留", round(todayAvgDuration, 1), "h",
                        compareChange(todayAvgDuration, yesterdayAvgDuration) + " 较昨日")
        );
    }

    // ==================== 单独查询方法 ====================

    private VisitorFlowCardVO doQueryTodayVisitorCount() {
        List<VenueFlowHour> todayLatest = getLatestPerVenue(LocalDate.now());
        List<VenueFlowHour> yesterdayLatest = getLatestPerVenue(LocalDate.now().minusDays(1));

        long todayVal = todayLatest.stream().mapToLong(v -> nvl(v.getTodayInCount())).sum();
        long yesterdayVal = yesterdayLatest.stream().mapToLong(v -> nvl(v.getTodayInCount())).sum();

        return buildCard("今日总客流", todayVal, "",
                compareRate(todayVal, yesterdayVal) + " 较昨日");
    }

    private VisitorFlowCardVO doQueryCurrentVisitorCount() {
        List<VenueFlowHour> todayLatest = getLatestPerVenue(LocalDate.now());
        List<VenueFlowHour> yesterdayLatest = getLatestPerVenue(LocalDate.now().minusDays(1));

        long todayVal = todayLatest.stream().mapToLong(v -> nvl(v.getTodayNowCount())).sum();
        long yesterdayVal = yesterdayLatest.stream().mapToLong(v -> nvl(v.getTodayNowCount())).sum();

        return buildCard("当前在场", todayVal, "",
                compareRate(todayVal, yesterdayVal) + " 较昨日");
    }

    private VisitorFlowCardVO doQueryPeakVisitorCount() {
        List<VenueFlowHour> todayLatest = getLatestPerVenue(LocalDate.now());
        List<VenueFlowHour> yesterdayLatest = getLatestPerVenue(LocalDate.now().minusDays(1));

        long todayVal = todayLatest.isEmpty() ? 0L :
                Math.round(todayLatest.stream().mapToLong(v -> nvl(v.getMaxCount())).average().orElse(0));
        long yesterdayVal = yesterdayLatest.isEmpty() ? 0L :
                Math.round(yesterdayLatest.stream().mapToLong(v -> nvl(v.getMaxCount())).average().orElse(0));

        return buildCard("峰值客流", todayVal, "",
                compareRate(todayVal, yesterdayVal) + " 较昨日");
    }

    private VisitorFlowCardVO doQueryAverageStopDuration() {
        List<VenueFlowHour> todayLatest = getLatestPerVenue(LocalDate.now());
        List<VenueFlowHour> yesterdayLatest = getLatestPerVenue(LocalDate.now().minusDays(1));

        double todayVal = todayLatest.stream()
                .filter(v -> v.getAverageDuration() != null)
                .mapToDouble(VenueFlowHour::getAverageDuration)
                .average().orElse(0);
        double yesterdayVal = yesterdayLatest.stream()
                .filter(v -> v.getAverageDuration() != null)
                .mapToDouble(VenueFlowHour::getAverageDuration)
                .average().orElse(0);

        return buildCard("平均停留", round(todayVal, 1), "h",
                compareChange(todayVal, yesterdayVal) + " 较昨日");
    }

    // ==================== 数据获取：取每个场馆当日最新一条记录 ====================

    /**
     * 查询指定日期每个场馆的最新一条分时记录。
     * <p>按 venueId 分组，取 id 最大的记录（代表最新）。</p>
     */
    private List<VenueFlowHour> getLatestPerVenue(LocalDate date) {
        List<VenueFlowHour> all = list(new LambdaQueryWrapper<VenueFlowHour>()
                .eq(VenueFlowHour::getDataDate, date));
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(all.stream()
                .collect(Collectors.toMap(
                        VenueFlowHour::getVenueId,
                        v -> v,
                        (a, b) -> a.getId() > b.getId() ? a : b))
                .values());
    }

    // ==================== 通用对比 & 格式化 ====================

    private String compareRate(long today, long yesterday) {
        if (yesterday == 0) {
            return today == 0 ? "—" : "↑100%";
        }
        double rate = (today - yesterday) * 100.0 / yesterday;
        String arrow = rate >= 0 ? "↑" : "↓";
        double abs = Math.abs(rate);
        return arrow + (abs == (long) abs ? String.valueOf((long) abs) : String.format("%.1f", abs)) + "%";
    }

    private String compareChange(double today, double yesterday) {
        if (yesterday == 0) {
            return today == 0 ? "—" : "↑" + formatValue(today) + "h";
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

    private VisitorFlowCardVO buildCard(String title, Number value, String unit, String context) {
        VisitorFlowCardVO vo = new VisitorFlowCardVO();
        vo.setTitle(title);
        vo.setValue(value);
        vo.setUnit(unit);
        vo.setContext(context);
        return vo;
    }
}
