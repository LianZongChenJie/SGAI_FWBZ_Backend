package org.jeecg.modules.fwbz.centralizedAirCooling.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.centralizedAirCooling.dto.CentralizedAirCoolingOverviewDto;
import org.jeecg.modules.fwbz.centralizedAirCooling.mapper.CentralizedAirCoolingMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * 集中风冷系统 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CentralizedAirCoolingService {

    /** 总功率 tagid */
    private static final long TAG_ID_TOTAL_POWER = 557L;
    /** 冷量累计 tagid */
    private static final long TAG_ID_COOLING_CAPACITY = 556L;

    /** 今日累计用电 tagid 组 */
    private static final long[] TAG_ID_TODAY_POWER =
            {6730L, 6732L, 6748L, 6750L, 6752L, 6754L, 6756L, 6758L, 6760L, 6762L, 6764L, 6766L, 6768L, 6770L};

    private final CentralizedAirCoolingMapper centralizedAirCoolingMapper;

    /**
     * 查询总览数据：
     * - 风冷系统总功率：冷源历史表 tagid=557 最新一条
     * - 当前制冷量：冷源历史表 tagid=556 今天最新的数 - 今天零点的数
     * - 风冷系统COP：制冷量 / 总功率
     * - 今日累计用电量：各 tagid(最新 - 今日最早) 差值之和
     */
    public CentralizedAirCoolingOverviewDto getOverview() {
        CentralizedAirCoolingOverviewDto dto = new CentralizedAirCoolingOverviewDto();

        // 1. 总功率：tagid=557 最新一条
        BigDecimal totalPower = getLatestValue(TAG_ID_TOTAL_POWER);
        dto.setTotalPower(scale(totalPower, 1));

        // 2. 制冷量：tagid=556 今天最新的数 - 今天零点的数
        BigDecimal current = getLatestValue(TAG_ID_COOLING_CAPACITY);
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        TableColdSourceHistory firstToday =
                centralizedAirCoolingMapper.selectFirstTodayByTagId(TAG_ID_COOLING_CAPACITY, dayStart);
        BigDecimal coolingCapacity = BigDecimal.ZERO;
        if (current != null && firstToday != null && parseValue(firstToday.getValue()) != null) {
            coolingCapacity = current.subtract(parseValue(firstToday.getValue()));
            if (coolingCapacity.compareTo(BigDecimal.ZERO) < 0) {
                coolingCapacity = BigDecimal.ZERO;
            }
        }
        dto.setCurrentCoolingCapacity(scale(coolingCapacity, 1));

        // 3. COP = 制冷量 / 功率
        BigDecimal cop = BigDecimal.ZERO;
        if (totalPower != null && totalPower.compareTo(BigDecimal.ZERO) > 0) {
            cop = coolingCapacity.divide(totalPower, 2, RoundingMode.HALF_UP);
        }
        dto.setCop(cop);

        // 4. 今日累计用电量：各 tagid(最新 - 今日最早) 差值之和
        Map<Long, TableColdSourceHistory> powerLatest =
                centralizedAirCoolingMapper.selectLatestByTagIds(Arrays.asList(toLongArray(TAG_ID_TODAY_POWER)));
        Map<Long, TableColdSourceHistory> powerFirstToday =
                centralizedAirCoolingMapper.selectFirstTodayByTagIds(
                        Arrays.asList(toLongArray(TAG_ID_TODAY_POWER)), dayStart);
        BigDecimal todayPower = BigDecimal.ZERO;
        for (long tid : TAG_ID_TODAY_POWER) {
            todayPower = todayPower.add(diffToday(powerLatest, powerFirstToday, tid));
        }
        dto.setTodayPowerConsumption(scale(todayPower, 0));
        return dto;
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

    private Long[] toLongArray(long[] arr) {
        Long[] result = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private BigDecimal getLatestValue(long tagId) {
        TableColdSourceHistory history = centralizedAirCoolingMapper.selectLatestByTagId(tagId);
        if (history == null) {
            return null;
        }
        return parseValue(history.getValue());
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
}
