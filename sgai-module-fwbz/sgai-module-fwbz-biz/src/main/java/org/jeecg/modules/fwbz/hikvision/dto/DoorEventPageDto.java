package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 门禁点事件分页查询DTO
 *
 * @author fwbz
 */
@Data
public class DoorEventPageDto {

    @ApiModelProperty(value = "当前页码")
    private int pageNo = 1;

    @ApiModelProperty(value = "每页大小")
    private int pageSize = 10;

    @ApiModelProperty(value = "人员姓名（模糊查询）")
    private String personName;

    @ApiModelProperty(value = "门禁点名称（模糊查询）")
    private String doorName;

    @ApiModelProperty(value = "门禁点编码（精确查询）")
    private String doorIndexCode;

    @ApiModelProperty(value = "事件类型（精确查询）")
    private Integer eventType;

    @ApiModelProperty(value = "进出类型：1-进 0-出 -1-未知（精确查询）")
    private Integer inAndOutType;

    @ApiModelProperty(value = "卡号（模糊查询）")
    private String cardNo;

    @ApiModelProperty(value = "事件起始时间，格式yyyy-MM-dd HH:mm:ss（eventTime >= startTime）")
    private String startTime;

    @ApiModelProperty(value = "事件截止时间，格式yyyy-MM-dd HH:mm:ss（eventTime <= endTime）")
    private String endTime;
}
