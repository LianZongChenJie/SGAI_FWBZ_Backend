package org.jeecg.modules.fwbz.hikvision.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 摄像头列表VO，供前端展示摄像头列表
 *
 * @author fwbz
 */
@Data
public class CameraListVO {

    @ApiModelProperty(value = "唯一编码")
    private String indexCode;

    @ApiModelProperty(value = "资源名称")
    private String name;

    @ApiModelProperty(value = "监控点类型：0-枪机，1-半球，2-快球，3-带云台枪机")
    private Integer cameraType;

    @ApiModelProperty(value = "安装位置")
    private String installLocation;

    @ApiModelProperty(value = "所属区域")
    private String regionIndexCode;

    @ApiModelProperty(value = "所属区域名称")
    private String regionName;

    @ApiModelProperty(value = "经度")
    private BigDecimal longitude;

    @ApiModelProperty(value = "纬度")
    private BigDecimal latitude;

    @ApiModelProperty(value = "通道子类型")
    private String channelType;

    @ApiModelProperty(value = "在线状态，0-离线，1-在线")
    private Integer online;

    @ApiModelProperty(value = "监控点国标编号")
    private String externalIndexCode;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
