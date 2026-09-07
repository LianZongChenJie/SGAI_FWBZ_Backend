package org.jeecg.modules.fwbz.branchAirCooling.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.branchAirCooling.dto.BranchAirCoolingOverviewDto;
import org.jeecg.modules.fwbz.branchAirCooling.mapper.BranchAirCoolingMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * 分馆风冷系统 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchAirCoolingService {

    /** 总功率 tagid 组 */
    private static final long TAG_ID_POWER_A = 550L;
    private static final long TAG_ID_POWER_B = 564L;

    /** 制冷量 tagid 组 */
    private static final long TAG_ID_COOL_A = 549L;
    private static final long TAG_ID_COOL_B = 563L;

    /** 今日累计用电 tagid 组 */
    private static final long[] TAG_ID_TODAY_POWER = {6746L, 6744L, 6742L, 6738L, 6736L, 6734L};

    private final BranchAirCoolingMapper branchAirCoolingMapper;

    /**
     * 总览数据：
     * - 分馆风冷总功率 = tagid(550+564) 最新值之和
     * - 当前制冷量 = (tagid549 最新 - 今日最早) + (tagid563 最新 - 今日最早)
     * - 分馆综合COP = 制冷量 / 总功率
     * - 今日累计用电 = tagid(6746/6744/6742/6738/6736/6734) 最新值之和
     */
    public BranchAirCoolingOverviewDto getOverview() {
        BranchAirCoolingOverviewDto dto = new BranchAirCoolingOverviewDto();

        // 1. 总功率：tagid=550 + 564
        Map<Long, TableColdSourceHistory> powerLatest =
                branchAirCoolingMapper.selectLatestByTagIds(Arrays.asList(TAG_ID_POWER_A, TAG_ID_POWER_B));
        BigDecimal totalPower = sumValues(powerLatest, TAG_ID_POWER_A, TAG_ID_POWER_B);
        dto.setTotalPower(scale(totalPower, 1));

        // 2. 制冷量：(549最新-今日最早) + (563最新-今日最早)
        Map<Long, TableColdSourceHistory> coolLatest =
                branchAirCoolingMapper.selectLatestByTagIds(Arrays.asList(TAG_ID_COOL_A, TAG_ID_COOL_B));
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        Map<Long, TableColdSourceHistory> coolFirstToday =
                branchAirCoolingMapper.selectFirstTodayByTagIds(
                        Arrays.asList(TAG_ID_COOL_A, TAG_ID_COOL_B), dayStart);
        BigDecimal coolingCapacity = BigDecimal.ZERO;
        coolingCapacity = coolingCapacity.add(diffToday(coolLatest, coolFirstToday, TAG_ID_COOL_A));
        coolingCapacity = coolingCapacity.add(diffToday(coolLatest, coolFirstToday, TAG_ID_COOL_B));
        dto.setCurrentCoolingCapacity(scale(coolingCapacity, 1));

        // 3. COP = 制冷量 / 总功率
        BigDecimal cop = BigDecimal.ZERO;
        if (totalPower != null && totalPower.compareTo(BigDecimal.ZERO) > 0) {
            cop = coolingCapacity.divide(totalPower, 2, RoundingMode.HALF_UP);
        }
        dto.setCop(cop);

        // 4. 今日累计用电：六个 tagid 最新值之和
        Map<Long, TableColdSourceHistory> todayPowerLatest =
                branchAirCoolingMapper.selectLatestByTagIds(toList(TAG_ID_TODAY_POWER));
        dto.setTodayPowerConsumption(scale(sumValues(todayPowerLatest, TAG_ID_TODAY_POWER), 0));

        return dto;
    }

    /** 取指定 tagid 最新值之和 */
    private BigDecimal sumValues(Map<Long, TableColdSourceHistory> map, long... tagIds) {
        BigDecimal sum = BigDecimal.ZERO;
        for (long tid : tagIds) {
            TableColdSourceHistory row = map.get(tid);
            BigDecimal v = row == null ? null : parseValue(row.getValue());
            if (v != null) {
                sum = sum.add(v);
            }
        }
        return sum;
    }

    /** 单 tagid：最新值 - 今日最早值；任一为空则贡献 0；负值兜底为 0 */
    private BigDecimal diffToday(Map<Long, TableColdSourceHistory> latest,
                                 Map<Long, TableColdSourceHistory> firstToday,
                                 long tagId) {
        TableColdSourceHistory latestRow = latest.get(tagId);
        TableColdSourceHistory firstRow = firstToday.get(tagId);
        if (latestRow == null || firstRow == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cur = parseValue(latestRow.getValue());
        BigDecimal base = parseValue(firstRow.getValue());
        if (cur == null || base == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal diff = cur.subtract(base);
        return diff.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : diff;
    }

    private BigDecimal parseValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("冷源历史数据值解析失败: {}", value);
            return null;
        }
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value == null ? BigDecimal.ZERO : value.setScale(scale, RoundingMode.HALF_UP);
    }

    private java.util.List<Long> toList(long... arr) {
        java.util.List<Long> list = new java.util.ArrayList<>(arr.length);
        for (long v : arr) {
            list.add(v);
        }
        return list;
    }
}
