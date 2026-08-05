package org.jeecg.modules.fwbz.interfaceStatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;
import org.jeecg.modules.fwbz.dataInterface.service.IInterfaceInfoService;
import org.jeecg.modules.fwbz.interfaceStatistics.service.IInterfaceHistoryService;
import org.jeecg.modules.fwbz.interfaceStatistics.service.IInterfaceStatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class InterfaceStatisticsServiceImpl implements IInterfaceStatisticsService {

    private final IInterfaceInfoService interfaceInfoService;
    private final IInterfaceHistoryService interfaceHistoryService;

    @Override
    public StatCardVO connectedSystemCount() {
        long count = interfaceInfoService.listAll().stream()
                .map(InterfaceInfo::getSysName)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        StatCardVO vo = new StatCardVO();
        vo.setTitle("对接系统数");
        vo.setValue(count);
        vo.setContext("");
        return vo;
    }

    @Override
    public StatCardVO onlineRate() {
        long total = interfaceInfoService.count();
        if (total == 0) {
            StatCardVO vo = new StatCardVO();
            vo.setTitle("接口在线率");
            vo.setValue(0);
            vo.setContext("");
            return vo;
        }
        long online = interfaceInfoService.count(
                new LambdaQueryWrapper<InterfaceInfo>()
                        .eq(InterfaceInfo::getState, InterfaceInfo.STATE_ONLINE)
        );
        double rate = BigDecimal.valueOf(online)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                .doubleValue();

        StatCardVO vo = new StatCardVO();
        vo.setTitle("接口在线率");
        vo.setValue(rate);
        vo.setContext("");
        return vo;
    }

    @Override
    public StatCardVO todayDataSize() {
        Calendar today = Calendar.getInstance();
        clearTime(today);
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        double todayKb = interfaceHistoryService.getTodayDataSize(today.getTime());
        double yesterdayKb = interfaceHistoryService.getTodayDataSize(yesterday.getTime());
        if(todayKb>1024)
        {
            todayKb=todayKb/1024;
            yesterdayKb = yesterdayKb/1024;
        }
        double roundedMb = Math.round(todayKb * 10.0) / 10.0;

        StatCardVO vo = new StatCardVO();
        vo.setTitle("今日数据量（KB）");
        if(todayKb>1024){
            vo.setTitle("今日数据量（MB）");
        }
        vo.setValue(roundedMb);
        vo.setContext(buildDataSizeContext(todayKb, yesterdayKb));
        return vo;
    }

    @Override
    public StatCardVO abnormalCount() {
        long count = interfaceInfoService.count(
                new LambdaQueryWrapper<InterfaceInfo>()
                        .eq(InterfaceInfo::getState, InterfaceInfo.STATE_ABNORMAL)
        );

        StatCardVO vo = new StatCardVO();
        vo.setTitle("异常接口");
        vo.setValue(count);
        vo.setContext("需处理");
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        return Arrays.asList(connectedSystemCount(), onlineRate(), todayDataSize(), abnormalCount());
    }

    /**
     * 构建今日数据量较昨日的对比文案
     */
    private String buildDataSizeContext(double todayKb, double yesterdayKb) {
        if (yesterdayKb == 0) {
            return todayKb > 0 ? "较昨日 ↑" : "";
        }
        double diff = (todayKb - yesterdayKb) / yesterdayKb * 100;
        if (Math.abs(diff) < 0.1) {
            return "较昨日 持平";
        }
        return "较昨日 " + (diff > 0 ? "↑" : "↓") + formatDiff(diff > 0 ? diff : -diff) + "%";
    }

    private void clearTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private String formatDiff(double diff) {
        if (diff == (long) diff) {
            return String.valueOf((long) diff);
        }
        return String.format("%.1f", diff);
    }
}
