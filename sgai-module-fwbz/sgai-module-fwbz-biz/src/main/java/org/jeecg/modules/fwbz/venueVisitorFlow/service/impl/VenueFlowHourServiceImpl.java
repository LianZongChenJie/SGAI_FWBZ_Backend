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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public VenueHourlyTrendVO queryHourlyTrend(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        final LocalDate queryDate = date;

        log.info("查询各场馆分时客流趋势, date={}", queryDate);

        // 1. 查询今日所有小时数据
        List<VenueFlowHour> list = list(
                new LambdaQueryWrapper<VenueFlowHour>()
                        .eq(VenueFlowHour::getDataDate, queryDate)
                        .orderByAsc(VenueFlowHour::getDataHour));
        if (list.isEmpty()) {
            log.warn("当日无分时客流数据, date={}", queryDate);
            return emptyTrend();
        }

        // 2. 场馆名称映射
        Map<Long, String> venueNameMap = venueInfoService.list().stream()
                .collect(Collectors.toMap(VenueInfo::getId, VenueInfo::getVenueName, (a, b) -> a));

        // 3. 按场馆分组
        Map<Long, List<VenueFlowHour>> venueGroup = list.stream()
                .filter(r -> r.getVenueId() != null && r.getDataHour() != null)
                .collect(Collectors.groupingBy(VenueFlowHour::getVenueId));

        // 4. 生成统一的时间轴
        List<Time> timeAxis = list.stream()
                .map(VenueFlowHour::getDataHour)
                .distinct()
                .sorted(Comparator.comparing(Time::getTime))
                .collect(Collectors.toList());
        List<String> dateLabels = timeAxis.stream()
                .map(this::formatTime)
                .collect(Collectors.toList());

        // 5. 构造每个场馆的序列及合计序列
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

        // 6. 计算今日汇总
        long todayInTotal = 0L;
        long latestNowTotal = 0L;
        for (List<VenueFlowHour> rows : venueGroup.values()) {
            VenueFlowHour latest = rows.get(rows.size() - 1);
            todayInTotal += latest.getTodayInCount() == null ? 0L : latest.getTodayInCount();
            latestNowTotal += latest.getTodayNowCount() == null ? 0L : latest.getTodayNowCount();
        }
        long todayInOutTotal = todayInTotal + latestNowTotal;

        VenueHourlyTrendVO vo = new VenueHourlyTrendVO();
        vo.setDate(dateLabels);
        vo.setVenueData(venueData);
        vo.setTotal(total);
        vo.setTodayInTotal(todayInTotal);
        vo.setTodayInOutTotal(todayInOutTotal);
        return vo;
    }

    @Override
    public List<VenueHeatmapItemVO> queryHeatmap() {
        LocalDate today = LocalDate.now();
        log.info("查询各场馆今日热力图数据, date={}", today);

        // 1. 查询今日所有分时数据
        List<VenueFlowHour> list = list(
                new LambdaQueryWrapper<VenueFlowHour>()
                        .eq(VenueFlowHour::getDataDate, today)
                        .isNotNull(VenueFlowHour::getVenueId));
        if (list.isEmpty()) {
            log.warn("当日无分时客流数据, date={}", today);
            return new ArrayList<>();
        }

        // 2. 按场馆分组，取最新一条（dataHour最大）
        Map<Long, VenueFlowHour> latestMap = list.stream()
                .collect(Collectors.toMap(
                        VenueFlowHour::getVenueId,
                        r -> r,
                        (a, b) -> a.getDataHour().after(b.getDataHour()) ? a : b));

        // 3. 场馆信息映射（id -> VenueInfo）
        Map<Long, VenueInfo> venueInfoMap = venueInfoService.list().stream()
                .collect(Collectors.toMap(VenueInfo::getId, v -> v, (a, b) -> a));

        // 4. 构造热力图VO
        List<VenueHeatmapItemVO> result = new ArrayList<>();
        for (Map.Entry<Long, VenueFlowHour> entry : latestMap.entrySet()) {
            Long venueId = entry.getKey();
            VenueFlowHour row = entry.getValue();
            VenueInfo info = venueInfoMap.get(venueId);
            if (info == null) {
                continue;
            }

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
            result.add(vo);
        }
        log.info("热力图数据查询完成, 共{}个场馆", result.size());
        return result;
    }

    /**
     * 无数据时返回空结构
     */
    private VenueHourlyTrendVO emptyTrend() {
        VenueHourlyTrendVO vo = new VenueHourlyTrendVO();
        vo.setDate(new ArrayList<>());
        vo.setVenueData(new LinkedHashMap<>());
        vo.setTotal(new ArrayList<>());
        vo.setTodayInTotal(0L);
        vo.setTodayInOutTotal(0L);
        return vo;
    }

    /**
     * 将 java.sql.Time 格式化为 HH:mm
     */
    private String formatTime(Time time) {
        if (time == null) {
            return "";
        }
        java.time.LocalTime lt = time.toLocalTime();
        return String.format("%02d:%02d", lt.getHour(), lt.getMinute());
    }
}
