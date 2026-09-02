package org.jeecg.modules.fwbz.coldSourceSystem.dto;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.time.LocalDateTime;

/**
 * 冷源历史记录分页返回
 */
@Data
public class ColdSourceHistoryPageDto {

    /** 采集点id */
    @Excel(name = "采集点id", width = 12)
    private Long tagId;

    /** 描述 */
    @Excel(name = "描述", width = 30)
    private String desc;

    /** 采集时间 */
    @Excel(name = "采集时间", width = 22, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataTime;

    /** 值 */
    @Excel(name = "值", width = 15)
    private String value;
}
