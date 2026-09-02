package org.jeecg.modules.fwbz.hikvision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 摄像头信息表
 *
 * @author fwbz
 */
@Data
@TableName("camera_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "camera_info对象", description = "摄像头信息表")
public class CameraInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 系统标识 */
    @ApiModelProperty(value = "系统标识")
    private String systemId;

    /** 摄像头名称 */
    @ApiModelProperty(value = "摄像头名称")
    private String name;

    /** 摄像头简称 */
    @ApiModelProperty(value = "摄像头简称")
    private String shortName;

    /** 设备IP地址 */
    @ApiModelProperty(value = "设备IP地址")
    private String ip;

    /** 设备端口 */
    @ApiModelProperty(value = "设备端口")
    private Integer port;

    /** 登录用户名 */
    @ApiModelProperty(value = "登录用户名")
    private String userName;

    /** 登录密码 */
    @ApiModelProperty(value = "登录密码")
    private String password;

    /** 远程平台设备ID */
    @ApiModelProperty(value = "远程平台设备ID")
    private Long remoteId;

    /** 视频编码 */
    @ApiModelProperty(value = "视频编码")
    private String videoCode;

    /** 厂商 */
    @ApiModelProperty(value = "厂商")
    private String manufacturers;

    /** 摄像头类型 */
    @ApiModelProperty(value = "摄像头类型")
    private Integer cameraType;

    /** 分组ID */
    @ApiModelProperty(value = "分组ID")
    private Long groupId;

    /** 分组名称 */
    @ApiModelProperty(value = "分组名称")
    private String groupName;

    /** 空间路径 */
    @ApiModelProperty(value = "空间路径")
    private String spacePath;

    /** 点位路径 */
    @ApiModelProperty(value = "点位路径")
    private String pointPath;

    /** 流地址/访问地址 */
    @ApiModelProperty(value = "流地址/访问地址")
    private String url;

    /** 图纸编码 */
    @ApiModelProperty(value = "图纸编码")
    private String drawingCode;

    /** 经度 */
    @ApiModelProperty(value = "经度")
    private String longitude;

    /** 纬度 */
    @ApiModelProperty(value = "纬度")
    private String latitude;

    /** 在线状态：1=在线，0=离线 */
    @ApiModelProperty(value = "在线状态：1=在线，0=离线")
    private Integer online;

    /** 排序号 */
    @ApiModelProperty(value = "排序号")
    private Integer sortNum;

    /** 是否初始化：0=否，1=是 */
    @ApiModelProperty(value = "是否初始化：0=否，1=是")
    private Integer isInit;
}
