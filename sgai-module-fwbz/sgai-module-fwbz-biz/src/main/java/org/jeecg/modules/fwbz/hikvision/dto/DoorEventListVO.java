package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 门禁点事件列表VO，供前端展示
 *
 * @author fwbz
 */
@Data
public class DoorEventListVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "事件ID")
    private String eventId;

    @ApiModelProperty(value = "事件名称")
    private String eventName;

    @ApiModelProperty(value = "事件产生时间")
    private String eventTime;

    @ApiModelProperty(value = "人员唯一编码")
    private String personId;

    @ApiModelProperty(value = "卡号")
    private String cardNo;

    @ApiModelProperty(value = "人员姓名")
    private String personName;

    @ApiModelProperty(value = "人员所属组织名称")
    private String orgName;

    @ApiModelProperty(value = "门禁点名称")
    private String doorName;

    @ApiModelProperty(value = "门禁点编码")
    private String doorIndexCode;

    @ApiModelProperty(value = "事件类型")
    private Integer eventType;

    @ApiModelProperty(value = "进出类型：1-进 0-出 -1-未知")
    private Integer inAndOutType;

    @ApiModelProperty(value = "读卡器名称")
    private String readerDevName;

    @ApiModelProperty(value = "控制器设备名称")
    private String devName;

    @ApiModelProperty(value = "抓拍图片地址")
    private String picUri;

    @ApiModelProperty(value = "记录创建时间")
    private String gmtCreate;
}
