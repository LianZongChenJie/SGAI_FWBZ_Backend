package org.jeecg.modules.fwbz.energyAnalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 能源计量数据统计
 */
@Data
public class OverViewStatisticsDto {

    /**
     * 对接子系统数
     */
    private Long count;

    /**
     * 设备在线率
     */
    private Long online;

    /**
     * 远程控制设备
     */
    private String remoteControlEquipment;

    /**
     * 今日指令下发
     */
    private String todayInstructionWasIssued;

}
