package org.jeecg.modules.fwbz.venueVisitorFlow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VenueFlowHour;
import org.jeecg.modules.fwbz.venueVisitorFlow.mapper.VenueFlowHourMapper;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueFlowHourService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueHeatmapItemVO;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueHourlyTrendVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 各场馆客流分时统计 Service 实现
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class VenueFlowHourServiceImpl extends ServiceImpl<VenueFlowHourMapper, VenueFlowHour>
        implements IVenueFlowHourService {

    private final IVenueInfoService venueInfoService;

    /** 周几的中文标签 */
    private static final String[] WEEKDAY_LABELS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    // ==================== 本日分时趋势 ====================

    @Override
    public VenueHourlyTrendVO queryHourlyTrend(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        final LocalDate queryDate = date;

        log.info("查询各场馆分时客流趋势, date={}", queryDate);

        List<VenueFlowHour> list = list(
                new LambdaQueryWrapper<VenueFlowHour>()
                        .eq(VenueFlowHour::getDataDate, queryDate)
                        .orderByAsc(VenueFlowHour::getDataHour));
        if (list.isEmpty()) {
            log.warn("当日无分时客流数据, date={}", queryDate);
            return emptyTrend();
        }

        Map<Long, String> venueNameMap = buildVenueNameMap();

        Map<Long, List<VenueFlowHour>> venueGroup = list.stream()
                .filter(r -> r.getVenueId() != null && r.getDataHour() != null)
                .collect(Collectors.groupingBy(VenueFlowHour::getVenueId));

        List<Time> timeAxis = list.stream()
                .map(VenueFlowHour::getDataHour)
                .distinct()
                .sorted(Comparator.comparing(Time::getTime))
                .collect(Collectors.toList());
        List<String> dateLabels = timeAxis.stream()
                .map(this::formatTime)
                .collect(Collectors.toList());

        return buildHourlyTrend(dateLabels, timeAxis, venueGroup, venueNameMap);
    }

    // ==================== 本周日度趋势 ====================

    @Override
    public VenueHourlyTrendVO queryWeeklyTrend(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        log.info("查询本周日度客流趋势, 范围=[{} ~ {}]", monday, sunday);

        return queryDailyTrend(monday, sunday, new ArrayList<>(Arrays.asList(WEEKDAY_LABELS)));
    }

    // ==================== 本月日度趋势 ====================

    @Override
    public VenueHourlyTrendVO queryMonthlyTrend(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        YearMonth ym = YearMonth.from(date);
        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();
        int days = lastDay.getDayOfMonth();

        log.info("查询本月日度客流趋势, 范围=[{} ~ {}], 共{}天", firstDay, lastDay, days);

        List<String> monthLabels = new ArrayList<>(days);
        for (int d = 1; d <= days; d++) {
            monthLabels.add(d + "日");
        }

        return queryDailyTrend(firstDay, lastDay, monthLabels);
    }

    // ==================== 热力图 ====================

    @Override
    public List<VenueHeatmapItemVO> queryHeatmap() {
        LocalDate today = LocalDate.now();
        log.info("查询各场馆今日热力图数据, date={}", today);

        List<VenueFlowHour> list = list(
                new LambdaQueryWrapper<VenueFlowHour>()
                        .eq(VenueFlowHour::getDataDate, today)
                        .isNotNull(VenueFlowHour::getVenueId));
        if (list.isEmpty()) {
            log.warn("当日无分时客流数据, date={}", today);
            return new ArrayList<>();
        }

        Map<Long, VenueFlowHour> latestMap = list.stream()
                .collect(Collectors.toMap(
                        VenueFlowHour::getVenueId,
                        r -> r,
                        (a, b) -> a.getDataHour().after(b.getDataHour()) ? a : b));

        Map<Long, VenueInfo> venueInfoMap = venueInfoService.list().stream()
                .collect(Collectors.toMap(VenueInfo::getId, v -> v, (a, b) -> a));

        List<VenueHeatmapItemVO> result = new ArrayList<>();
        for (Map.Entry<Long, VenueFlowHour> entry : latestMap.entrySet()) {
            Long venueId = entry.getKey();
            VenueFlowHour row = entry.getValue();
            VenueInfo info = venueInfoMap.get(venueId);
            if (info == null) {
                continue;
            }
            result.add(buildHeatmapItem(venueId, info, row));
        }
        log.info("热力图数据查询完成, 共{}个场馆", result.size());
        return result;
    }

    // ==================== 日度趋势通用方法 ====================

    /**
     * 查询指定日期范围内的日度客流趋势（按天聚合，取每天各馆最新在场人数）。
     *
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @param dayLabels 日期标签列表（如 ["周一","周二",...] 或 ["1日","2日",...]）
     */
    private VenueHourlyTrendVO queryDailyTrend(LocalDate startDate, LocalDate endDate, List<String> dayLabels) {
        List<VenueFlowHour> list = list(
                new LambdaQueryWrapper<VenueFlowHour>()
                        .between(VenueFlowHour::getDataDate, startDate, endDate)
                        .orderByAsc(VenueFlowHour::getDataDate, VenueFlowHour::getDataHour));
        if (list.isEmpty()) {
            log.warn("日期范围 [{}, {}] 无数据", startDate, endDate);
            return emptyTrendWithLabels(dayLabels);
        }

        Map<Long, String> venueNameMap = buildVenueNameMap();

        // 按 (dataDate, venueId) 分组，每组取 dataHour 最大的那条（每天每馆最新）
        Map<LocalDate, Map<Long, VenueFlowHour>> dailyVenueLatest = new LinkedHashMap<>();
        for (VenueFlowHour row : list) {
            dailyVenueLatest
                    .computeIfAbsent(row.getDataDate(), k -> new HashMap<>())
                    .merge(row.getVenueId(), row,
                            (old, neu) -> old.getDataHour().after(neu.getDataHour()) ? old : neu);
        }

        List<Long> sortedVenueIds = list.stream()
                .map(VenueFlowHour::getVenueId)
                .distinct()
                .sorted(Long::compareTo)
                .collect(Collectors.toList());

        int dayCount = dayLabels.size();
        Map<String, List<Long>> venueData = new LinkedHashMap<>();
        List<Long> total = new ArrayList<>(Collections.nCopies(dayCount, 0L));

        for (Long venueId : sortedVenueIds) {
            String venueName = venueNameMap.getOrDefault(venueId, "场馆" + venueId);
            List<Long> dailyValues = new ArrayList<>(dayCount);

            for (int d = 0; d < dayCount; d++) {
                LocalDate dayDate = startDate.plusDays(d);
                VenueFlowHour row = dailyVenueLatest.getOrDefault(dayDate, Collections.emptyMap()).get(venueId);
                long count = (row != null && row.getTodayNowCount() != null) ? row.getTodayNowCount() : 0L;
                dailyValues.add(count);
                total.set(d, total.get(d) + count);
            }
            venueData.put(venueName, dailyValues);
        }

        // 汇总：取最后一天各馆最新数据
        long todayInTotal = 0L;
        Map<Long, VenueFlowHour> lastDayMap = dailyVenueLatest.getOrDefault(endDate, Collections.emptyMap());
        for (VenueFlowHour row : lastDayMap.values()) {
            todayInTotal += row.getTodayInCount() == null ? 0L : row.getTodayInCount();
        }

        VenueHourlyTrendVO vo = new VenueHourlyTrendVO();
        vo.setDate(dayLabels);
        vo.setVenueData(venueData);
        vo.setTotal(total);
        vo.setTodayInTotal(todayInTotal);
        vo.setTodayInOutTotal(todayInTotal);
        return vo;
    }

    // ==================== 小时趋势构建 ====================

    private VenueHourlyTrendVO buildHourlyTrend(List<String> dateLabels, List<Time> timeAxis,
                                                 Map<Long, List<VenueFlowHour>> venueGroup,
                                                 Map<Long, String> venueNameMap) {
        Map<String, List<Long>> venueData = new LinkedHashMap<>();
        List<Long> total = new ArrayList<>(Collections.nCopies(timeAxis.size(), 0L));

        List<Long> sortedVenueIds = new ArrayList<>(venueGroup.keySet());
        sortedVenueIds.sort(Long::compareTo);

        for (Long venueId : sortedVenueIds) {
            String venueName = venueNameMap.getOrDefault(venueId, "场馆" + venueId);
            Map<Time, Long> hourNowMap = venueGroup.get(venueId).stream()
                    .collect(Collectors.toMap(VenueFlowHour::getDataHour,
                            r -> r.getTodayNowCount() == null ? 0L : r.getTodayNowCount(),
                            (a, b) -> a));

            List<Long> counts = new ArrayList<>(timeAxis.size());
            for (int i = 0; i < timeAxis.size(); i++) {
                Time t = timeAxis.get(i);
                Long count = hourNowMap.getOrDefault(t, 0L);
                counts.add(count);
                total.set(i, total.get(i) + count);
            }
            venueData.put(venueName, counts);
        }

        long todayInTotal = 0L;
        long latestNowTotal = 0L;
        for (List<VenueFlowHour> rows : venueGroup.values()) {
            VenueFlowHour latest = rows.get(rows.size() - 1);
            todayInTotal += latest.getTodayInCount() == null ? 0L : latest.getTodayInCount();
            latestNowTotal += latest.getTodayNowCount() == null ? 0L : latest.getTodayNowCount();
        }

        VenueHourlyTrendVO vo = new VenueHourlyTrendVO();
        vo.setDate(dateLabels);
        vo.setVenueData(venueData);
        vo.setTotal(total);
        vo.setTodayInTotal(todayInTotal);
        vo.setTodayInOutTotal(todayInTotal + latestNowTotal);
        return vo;
    }

    // ==================== 工具方法 ====================

    private Map<Long, String> buildVenueNameMap() {
        return venueInfoService.list().stream()
                .collect(Collectors.toMap(VenueInfo::getId, VenueInfo::getVenueName, (a, b) -> a));
    }

    private VenueHourlyTrendVO emptyTrend() {
        VenueHourlyTrendVO vo = new VenueHourlyTrendVO();
        vo.setDate(new ArrayList<>());
        vo.setVenueData(new LinkedHashMap<>());
        vo.setTotal(new ArrayList<>());
        vo.setTodayInTotal(0L);
        vo.setTodayInOutTotal(0L);
        return vo;
    }

    private VenueHourlyTrendVO emptyTrendWithLabels(List<String> labels) {
        VenueHourlyTrendVO vo = new VenueHourlyTrendVO();
        vo.setDate(labels);
        vo.setVenueData(new LinkedHashMap<>());
        int n = labels.size();
        vo.setTotal(new ArrayList<>(Collections.nCopies(n, 0L)));
        vo.setTodayInTotal(0L);
        vo.setTodayInOutTotal(0L);
        return vo;
    }

    private String formatTime(Time time) {
        if (time == null) {
            return "";
        }
        java.time.LocalTime lt = time.toLocalTime();
        return String.format("%02d:%02d", lt.getHour(), lt.getMinute());
    }

    private VenueHeatmapItemVO buildHeatmapItem(Long venueId, VenueInfo info, VenueFlowHour row) {
        long used = row.getTodayNowCount() == null ? 0L : row.getTodayNowCount();
        long total = row.getMaxCount() == null ? 0L : row.getMaxCount();
        if (total < used) {
            total = used;
        }
        long shengyu = total - used;

        VenueHeatmapItemVO vo = new VenueHeatmapItemVO();
        vo.setId(venueId);
        vo.setName(info.getVenueName());
        vo.setLng(info.getLongitude());
        vo.setLat(info.getLatitude());
        vo.setUsed(used);
        vo.setTotal(total);
        vo.setShengyu(shengyu);

        if (total == 0) {
            vo.setState("宽松");
            vo.setSaturation(BigDecimal.ONE);
            vo.setUsageRate(BigDecimal.ZERO);
            vo.setUsedRate(BigDecimal.ZERO);
        } else {
            BigDecimal usedBd = BigDecimal.valueOf(used);
            BigDecimal totalBd = BigDecimal.valueOf(total);
            BigDecimal shengyuBd = BigDecimal.valueOf(shengyu);
            BigDecimal ratio = usedBd.divide(totalBd, 4, RoundingMode.HALF_UP);

            vo.setState(ratio.compareTo(BigDecimal.ONE) >= 0 ? "拥挤"
                    : ratio.compareTo(new BigDecimal("0.7")) >= 0 ? "适中" : "宽松");
            vo.setSaturation(shengyuBd.divide(totalBd, 2, RoundingMode.HALF_UP));
            vo.setUsageRate(ratio.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP));
            vo.setUsedRate(ratio.setScale(2, RoundingMode.HALF_UP));
        }
        return vo;
    }
}
