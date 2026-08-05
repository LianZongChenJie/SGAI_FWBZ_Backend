package org.jeecg.modules.fwbz.hikvision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 门禁点资源表
 *
 * @author fwbz
 */
@Data
@TableName("table_door_resource")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "table_door_resource对象", description = "门禁点资源表")
public class DoorResource implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /** 资源唯一编码 */
    @ApiModelProperty(value = "资源唯一编码")
    private String indexCode;

    /** 资源类型 */
    @ApiModelProperty(value = "资源类型")
    private String resourceType;

    /** 资源名称 */
    @ApiModelProperty(value = "资源名称")
    private String name;

    /** 门禁点编号 */
    @ApiModelProperty(value = "门禁点编号")
    private String doorNo;

    /** 通道号 */
    @ApiModelProperty(value = "通道号")
    private String channelNo;

    /** 父级资源编号 */
    @ApiModelProperty(value = "父级资源编号")
    private String parentIndexCode;

    /** 一级控制器id */
    @ApiModelProperty(value = "一级控制器id")
    private String controlOneId;

    /** 二级控制器id */
    @ApiModelProperty(value = "二级控制器id")
    private String controlTwoId;

    /** 读卡器1（进方向） */
    @ApiModelProperty(value = "读卡器1（进方向）")
    private String readerInId;

    /** 读卡器2（出方向） */
    @ApiModelProperty(value = "读卡器2（出方向）")
    private String readerOutId;

    /** 门序号 */
    @ApiModelProperty(value = "门序号")
    private Integer doorSerial;

    /** 接入协议 */
    @ApiModelProperty(value = "接入协议")
    private String treatyType;

    /** 所属区域 */
    @ApiModelProperty(value = "所属区域")
    private String regionIndexCode;

    /** 所属区域目录，以@符号分割，包含本节点 */
    @ApiModelProperty(value = "所属区域目录")
    private String regionPath;

    /** 创建时间（设备侧上报） */
    @ApiModelProperty(value = "创建时间（设备侧上报）")
    private String createTime;

    /** 更新时间（设备侧上报） */
    @ApiModelProperty(value = "更新时间（设备侧上报）")
    private String updateTime;

    /** 描述 */
    @ApiModelProperty(value = "描述")
    private String description;

    /** 通道类型，door：门禁点 */
    @ApiModelProperty(value = "通道类型")
    private String channelType;

    /** 区域名称，@分隔，最大10级 */
    @ApiModelProperty(value = "区域名称")
    private String regionName;

    /** 所属区域目录名，@分隔 */
    @ApiModelProperty(value = "所属区域目录名")
    private String regionPathName;

    /** 安装位置 */
    @ApiModelProperty(value = "安装位置")
    private String installLocation;

    /** 记录创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录创建时间")
    private Date gmtCreate;

    /** 记录更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录更新时间")
    private Date gmtModified;

    /** 门状态，0-初始状态，1-开门状态，2-关门状态，3-离线状态 */
    @ApiModelProperty(value = "门状态，0-初始状态，1-开门状态，2-关门状态，3-离线状态")
    private String doorState;
}
