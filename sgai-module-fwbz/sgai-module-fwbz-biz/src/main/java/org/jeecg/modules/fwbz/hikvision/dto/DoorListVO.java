package org.jeecg.modules.fwbz.hikvision.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 门禁点列表VO，供前端展示门禁点列表
 *
 * @author fwbz
 */
@Data
public class DoorListVO {

    @ApiModelProperty(value = "资源唯一编码")
    private String indexCode;

    @ApiModelProperty(value = "资源名称")
    private String name;

    @ApiModelProperty(value = "门禁点编号")
    private String doorNo;

    @ApiModelProperty(value = "通道号")
    private String channelNo;

    @ApiModelProperty(value = "所属区域")
    private String regionIndexCode;

    @ApiModelProperty(value = "区域名称")
    private String regionName;

    @ApiModelProperty(value = "安装位置")
    private String installLocation;

    @ApiModelProperty(value = "门状态，0-初始状态，1-开门状态，2-关门状态，3-离线状态")
    private String doorState;

    @ApiModelProperty(value = "接入协议")
    private String treatyType;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间（设备侧上报）")
    private String createTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间（设备侧上报）")
    private String updateTime;
}
