package org.jeecg.modules.fwbz.centralizedWaterCooling.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.centralizedWaterCooling.dto.CentralizedWaterCoolingOverviewDto;
import org.jeecg.modules.fwbz.centralizedWaterCooling.mapper.CentralizedWaterCoolingMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 集中水冷系统 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CentralizedWaterCoolingService {

    /** 总功率 tagid */
    private static final long TAG_ID_TOTAL_POWER = 578L;
    /** 冷量累计 tagid */
    private static final long TAG_ID_COOLING_CAPACITY = 577L;

    private final CentralizedWaterCoolingMapper centralizedWaterCoolingMapper;

    /**
     * 查询总览数据：
     * - 系统总功率：冷源历史表 tagid=578 最新一条
     * - 当前制冷量：冷源历史表 tagid=577 今天最新的数 - 今天零点的数
     * - 系统效能COP：制冷量 / 总功率
     * - 今日累计用电量：暂返回 0
     */
    public CentralizedWaterCoolingOverviewDto getOverview() {
        CentralizedWaterCoolingOverviewDto dto = new CentralizedWaterCoolingOverviewDto();

        // 1. 系统总功率：tagid=578 最新一条
        BigDecimal totalPower = getLatestValue(TAG_ID_TOTAL_POWER);
        dto.setTotalPower(scale(totalPower, 1));

        // 2. 当前制冷量：tagid=577 今天最新的数 - 今天零点的数
        BigDecimal current = getLatestValue(TAG_ID_COOLING_CAPACITY);
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        TableColdSourceHistory firstToday =
                centralizedWaterCoolingMapper.selectFirstTodayByTagId(TAG_ID_COOLING_CAPACITY, dayStart);
        BigDecimal coolingCapacity = BigDecimal.ZERO;
        if (current != null && firstToday != null && parseValue(firstToday.getValue()) != null) {
            coolingCapacity = current.subtract(parseValue(firstToday.getValue()));
            if (coolingCapacity.compareTo(BigDecimal.ZERO) < 0) {
                coolingCapacity = BigDecimal.ZERO;
            }
        }
        dto.setCurrentCoolingCapacity(scale(coolingCapacity, 1));

        // 3. 系统效能COP = 制冷量 / 功率
        BigDecimal cop = BigDecimal.ZERO;
        if (totalPower != null && totalPower.compareTo(BigDecimal.ZERO) > 0) {
            cop = coolingCapacity.divide(totalPower, 2, RoundingMode.HALF_UP);
        }
        dto.setCop(cop);

        // 4. 今日累计用电量：暂返回 0
        dto.setTodayPowerConsumption(BigDecimal.ZERO);
        return dto;
    }

    /**
     * 取指定 tagid 最新一条记录的数值
     */
    private BigDecimal getLatestValue(long tagId) {
        TableColdSourceHistory history = centralizedWaterCoolingMapper.selectLatestByTagId(tagId);
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
