package org.jeecg.modules.fwbz.mdm.dto;

import lombok.Data;

/**
 * 设备运行状态统计
 */
@Data
public class DeviceRunStateStatisticsDto {

    /**
     * 总数
     */
    private Long count;

    /**
     * 在线
     */
    private Long online;

    /**
     * 离线
     */
    private Long offline;


    /**
     * 仪表数量
     */
    private Long measuringCount;

    /**
     * 运行设备
     */
    private Long equipmentCount;







}
