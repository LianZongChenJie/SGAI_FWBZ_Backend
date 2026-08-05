package org.jeecg.modules.fwbz.interfaceStatistics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;
import org.jeecg.modules.fwbz.dataInterface.service.IInterfaceInfoService;
import org.jeecg.modules.fwbz.interfaceStatistics.service.IInterfaceHistoryService;
import org.jeecg.modules.fwbz.interfaceStatistics.vo.InterfaceStatisticsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * 接口统计 Controller
 */
@RestController
@RequestMapping("/fwbz/interfaceStatistics")
@AllArgsConstructor
public class InterfaceStatisticsController {

    private final IInterfaceInfoService interfaceInfoService;
    private final IInterfaceHistoryService interfaceHistoryService;

    /**
     * 看板统计（聚合四个指标）
     */
    @GetMapping("/dashboard")
    public Result<InterfaceStatisticsVO> dashboard() {
        InterfaceStatisticsVO vo = new InterfaceStatisticsVO();
        vo.setConnectedSystemCount(getConnectedSystemCount());
        vo.setOnlineRate(getOnlineRateStr());
        vo.setTodayDataSize(getTodayDataSizeStr());
        vo.setAbnormalCount(getAbnormalCount());
        return Result.ok(vo);
    }

    /**
     * 对接系统数
     */
    @GetMapping("/connectedSystemCount")
    public Result<Long> connectedSystemCount() {
        return Result.ok(getConnectedSystemCount());
    }

    /**
     * 接口在线率
     */
    @GetMapping("/onlineRate")
    public Result<String> onlineRate() {
        return Result.ok(getOnlineRateStr());
    }

    /**
     * 今日数据量
     */
    @GetMapping("/todayDataSize")
    public Result<String> todayDataSize() {
        return Result.ok(getTodayDataSizeStr());
    }

    /**
     * 异常接口数
     */
    @GetMapping("/abnormalCount")
    public Result<Long> abnormalCount() {
        return Result.ok(getAbnormalCount());
    }

    private Long getConnectedSystemCount() {
        return interfaceInfoService.listAll().stream()
                .map(InterfaceInfo::getSysName)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private String getOnlineRateStr() {
        long total = interfaceInfoService.count();
        if (total == 0) {
            return "0.0%";
        }
        long online = interfaceInfoService.count(
                new LambdaQueryWrapper<InterfaceInfo>()
                        .eq(InterfaceInfo::getState, InterfaceInfo.STATE_ONLINE)
        );
        BigDecimal rate = BigDecimal.valueOf(online)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
        return rate + "%";
    }

    private String getTodayDataSizeStr() {
        Date start = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        double kb = interfaceHistoryService.getTodayDataSize(start, end);
        return formatDataSize(kb);
    }

    private Long getAbnormalCount() {
        return interfaceInfoService.count(
                new LambdaQueryWrapper<InterfaceInfo>()
                        .eq(InterfaceInfo::getState, InterfaceInfo.STATE_ABNORMAL)
        );
    }

    private String formatDataSize(double kb) {
        DecimalFormat df = new DecimalFormat("#.##");
        if (kb < 1024) {
            return df.format(kb) + "K";
        } else if (kb < 1024 * 1024) {
            return df.format(kb / 1024) + "M";
        } else {
            return df.format(kb / 1024 / 1024) + "G";
        }
    }
}
