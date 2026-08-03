package org.jeecg.modules.fwbz.energyAnalysis.service;

import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointChatDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointDataStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPoint;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Chat;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Table;
import org.jeecg.modules.fwbz.energyAnalysis.vo.chat.PieChat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IMeteringPointDataService {
    Table findMinute(String energyFlowDiagramIds, LocalDateTime hour);
    Table findDay(String energyFlowDiagramIds, LocalDate localDate);

    Table findMonth(String energyFlowDiagramIds,LocalDate localDate);

    Table findYear(String energyFlowDiagramIds,LocalDate localDate);


    Table findDayByConfig(String key, String energyFlowDiagramIds, LocalDate localDate);
    Table findMonthByConfig(String key, String energyFlowDiagramIds, LocalDate localDate);
    Table findYearByConfig(String key, String energyFlowDiagramIds, LocalDate localDate);



    void calculateValue(LocalDateTime hour);
    void calculateValue(List<LocalDateTime> hours);

    /**
     * 计量规则点位计算
     * @param deviceId 设备id
     * @param hour 小时
     */
    void calculateValue(Long deviceId,LocalDateTime hour);

    /**
     * 计量规则点位计算
     * @param pointId 点位id
     * @param hour 小时
     */
    void calculatePointValue(Long pointId,LocalDateTime hour);

    /**
     * 计量规则点位计算
     * @param pointId 点位id
     * @param minute 分钟
     */
    void calculatePointValueMinute(Long pointId,LocalDateTime minute);

    /**
     * 查询饼图数据
     */
    PieChat findPieChat(MeteringPointChatDto param);

    /**
     * 查询折线图数据
     */
    Chat findLineChat(MeteringPointChatDto param);

    /**
     * 查询柱状图数据
     */
    Chat findBarChat(MeteringPointChatDto param);

    /**
     * 查询堆叠柱状图数据
     */
    Chat findStackedColumnChart(MeteringPointChatDto param);

    /**
     * 计量分析数据统计
     * @return
     */
    MeteringPointDataStatisticsDto statistics();

}
