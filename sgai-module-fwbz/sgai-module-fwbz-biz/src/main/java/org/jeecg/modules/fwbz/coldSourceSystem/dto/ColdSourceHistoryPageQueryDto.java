package org.jeecg.modules.fwbz.coldSourceSystem.dto;

import lombok.Data;

/**
 * 冷源历史记录分页查询入参
 */
@Data
public class ColdSourceHistoryPageQueryDto {

    /** 采集点id（精确匹配） */
    private Long tagId;

    /** 描述（模糊匹配） */
    private String desc;

    /** 采集时间-开始（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss，仅日期默认 00:00:00） */
    private String startTime;

    /** 采集时间-结束（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss，仅日期默认 23:59:59） */
    private String endTime;

    private int pageNo = 1;

    private int pageSize = 10;
}
