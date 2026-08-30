package org.jeecg.modules.fwbz.hikvision.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.Date;

/**
 * 门禁点列表VO，供前端展示门禁点列表
 *
 * @author fwbz
 */
@Data
public class DoorListVO {

    @Excel(name = "资源唯一编码", width = 30)
    @ApiModelProperty(value = "资源唯一编码")
    private String indexCode;

    @Excel(name = "资源名称", width = 30)
    @ApiModelProperty(value = "资源名称")
    private String name;

    @Excel(name = "门禁点编号", width = 20)
    @ApiModelProperty(value = "门禁点编号")
    private String doorNo;

    @Excel(name = "通道号", width = 15)
    @ApiModelProperty(value = "通道号")
    private String channelNo;

    @ApiModelProperty(value = "所属区域")
    private String regionIndexCode;

    @Excel(name = "区域名称", width = 25)
    @ApiModelProperty(value = "区域名称")
    private String regionName;

    @Excel(name = "安装位置", width = 30)
    @ApiModelProperty(value = "安装位置")
    private String installLocation;

    @Excel(name = "门状态", width = 12, replace = {"开门_1", "关门_2", "离线_3", "初始_0"})
    @ApiModelProperty(value = "门状态，0-初始状态，1-开门状态，2-关门状态，3-离线状态")
    private String doorState;

    @Excel(name = "接入协议", width = 15)
    @ApiModelProperty(value = "接入协议")
    private String treatyType;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 22)
    @ApiModelProperty(value = "创建时间（设备侧上报）")
    private String createTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 22)
    @ApiModelProperty(value = "更新时间（设备侧上报）")
    private String updateTime;
}
