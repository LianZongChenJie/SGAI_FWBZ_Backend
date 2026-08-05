package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 门禁设备列表VO，供前端展示门禁设备列表
 *
 * @author fwbz
 */
@Data
public class AcsDeviceListVO {

    @ApiModelProperty(value = "资源唯一编码")
    private String indexCode;

    @ApiModelProperty(value = "资源名称")
    private String name;

    @ApiModelProperty(value = "门禁设备类型编码")
    private String devTypeCode;

    @ApiModelProperty(value = "门禁设备类型型号")
    private String devTypeDesc;

    @ApiModelProperty(value = "主动设备编号")
    private String deviceCode;

    @ApiModelProperty(value = "厂商")
    private String manufacturer;

    @ApiModelProperty(value = "所属区域")
    private String regionIndexCode;

    @ApiModelProperty(value = "区域名称")
    private String regionName;

    @ApiModelProperty(value = "接入协议")
    private String treatyType;

    @ApiModelProperty(value = "门禁设备IP")
    private String ip;

    @ApiModelProperty(value = "门禁设备端口")
    private String port;

    @ApiModelProperty(value = "在线状态，0离线，1在线")
    private String online;

    @ApiModelProperty(value = "创建时间（设备侧上报）")
    private String createTime;

    @ApiModelProperty(value = "更新时间（设备侧上报）")
    private String updateTime;
}
