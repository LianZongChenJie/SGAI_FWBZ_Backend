package org.jeecg.module.maintenance.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * 描述:
 *
 * @author ppliu
 * created in 2022/1/6 16:35
 */
@Data
public class ResultInfo {
    @Excel(name = "序号", width = 10)
    private String index;
    @Excel(name = "错误信息", width = 100)
    private String info;
    @Excel(name = "关键数据", width = 100)
    private String keyMessage;

}