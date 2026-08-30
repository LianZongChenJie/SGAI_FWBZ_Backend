package org.jeecg.modules.fwbz.hikvision.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * 门禁设备列表VO，供前端展示门禁设备列表
 *
 * @author fwbz
 */
@Data
public class AcsDeviceListVO {

    @Excel(name = "资源唯一编码", width = 30)
    @ApiModelProperty(value = "资源唯一编码")
    private String indexCode;

    @Excel(name = "资源名称", width = 30)
    @ApiModelProperty(value = "资源名称")
    private String name;

    @Excel(name = "设备类型编码", width = 20)
    @ApiModelProperty(value = "门禁设备类型编码")
    private String devTypeCode;

    @Excel(name = "设备类型型号", width = 20)
    @ApiModelProperty(value = "门禁设备类型型号")
    private String devTypeDesc;

    @Excel(name = "主动设备编号", width = 20)
    @ApiModelProperty(value = "主动设备编号")
    private String deviceCode;

    @Excel(name = "厂商", width = 15)
    @ApiModelProperty(value = "厂商")
    private String manufacturer;

    @ApiModelProperty(value = "所属区域")
    private String regionIndexCode;

    @Excel(name = "区域名称", width = 25)
    @ApiModelProperty(value = "区域名称")
    private String regionName;

    @Excel(name = "接入协议", width = 15)
    @ApiModelProperty(value = "接入协议")
    private String treatyType;

    @Excel(name = "IP", width = 18)
    @ApiModelProperty(value = "门禁设备IP")
    private String ip;

    @Excel(name = "端口", width = 10)
    @ApiModelProperty(value = "门禁设备端口")
    private String port;

    @Excel(name = "在线状态", width = 12, replace = {"在线_1", "离线_0"})
    @ApiModelProperty(value = "在线状态，0离线，1在线")
    private String online;

    @Excel(name = "创建时间", width = 22)
    @ApiModelProperty(value = "创建时间（设备侧上报）")
    private String createTime;

    @Excel(name = "更新时间", width = 22)
    @ApiModelProperty(value = "更新时间（设备侧上报）")
    private String updateTime;
}
