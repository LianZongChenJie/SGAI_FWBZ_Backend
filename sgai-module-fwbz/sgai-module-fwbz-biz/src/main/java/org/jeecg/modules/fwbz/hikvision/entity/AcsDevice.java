package org.jeecg.modules.fwbz.hikvision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 门禁设备资源表
 *
 * @author fwbz
 */
@Data
@TableName("table_acs_device")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "table_acs_device对象", description = "门禁设备资源表")
public class AcsDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "资源唯一编码")
    private String indexCode;

    @ApiModelProperty(value = "资源类型")
    private String resourceType;

    @ApiModelProperty(value = "资源名称")
    private String name;

    @ApiModelProperty(value = "父级资源编号")
    private String parentIndexCode;

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

    @ApiModelProperty(value = "所属区域目录，以@符号分割，包含本节点")
    private String regionPath;

    @ApiModelProperty(value = "接入协议")
    private String treatyType;

    @ApiModelProperty(value = "设备卡容量")
    private Integer cardCapacity;

    @ApiModelProperty(value = "指纹容量")
    private Integer fingerCapacity;

    @ApiModelProperty(value = "指静脉容量")
    private Integer veinCapacity;

    @ApiModelProperty(value = "人脸容量")
    private Integer faceCapacity;

    @ApiModelProperty(value = "门容量")
    private Integer doorCapacity;

    @ApiModelProperty(value = "拨码")
    private String deployId;

    @ApiModelProperty(value = "所属网域")
    private String netZoneId;

    @ApiModelProperty(value = "创建时间（设备侧上报）")
    private String createTime;

    @ApiModelProperty(value = "更新时间（设备侧上报），字段名避开MybatisInterceptor按updateTime自动填充")
    @TableField("update_time")
    private String devUpdateTime;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "支持认证方式，数据为十进制")
    private String acsReaderVerifyModeAbility;

    @ApiModelProperty(value = "区域名称，@分隔，最大10级")
    private String regionName;

    @ApiModelProperty(value = "所属区域目录名，以\"/\"分隔")
    private String regionPathName;

    @ApiModelProperty(value = "门禁设备IP")
    private String ip;

    @ApiModelProperty(value = "门禁设备端口")
    private String port;

    @ApiModelProperty(value = "设备能力集（含设备上的智能能力）")
    private String capability;

    @ApiModelProperty(value = "设备序列号")
    private String devSerialNum;

    @ApiModelProperty(value = "版本号")
    private String dataVersion;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录创建时间")
    private Date gmtCreate;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录更新时间")
    private Date gmtModified;

    @ApiModelProperty(value = "在线状态，0离线，1在线")
    private String online;
}
