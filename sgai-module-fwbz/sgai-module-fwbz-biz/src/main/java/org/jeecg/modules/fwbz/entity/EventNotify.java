package org.jeecg.modules.fwbz.entity;

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
 * 事件订阅通知表
 *
 * @author fwbz
 */
@Data
@TableName("table_event_notify")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "table_event_notify对象", description = "事件订阅通知表")
public class EventNotify implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /** 事件从接收者（程序处理后）发出的时间，ISO8601格式 */
    @ApiModelProperty(value = "发送时间")
    private String sendTime;

    /** 事件类别，如：视频事件 */
    @ApiModelProperty(value = "事件类别")
    private String ability;

    /** 事件唯一标识，同一事件若上报多次则eventId相同 */
    @ApiModelProperty(value = "事件唯一标识")
    private String eventId;

    /** 事件源编号，物理设备是资源编号 */
    @ApiModelProperty(value = "事件源编号")
    private String srcIndex;

    /** 事件源类型 */
    @ApiModelProperty(value = "事件源类型")
    private String srcType;

    /** 事件源名称 */
    @ApiModelProperty(value = "事件源名称")
    private String srcName;

    /** 事件类型，数值编码 */
    @ApiModelProperty(value = "事件类型")
    private Integer eventType;

    /** 事件状态：0-瞬时 1-开始 2-停止 4-事件联动结果更新 5-事件图片异步上传 */
    @ApiModelProperty(value = "事件状态")
    private Integer status;

    /** 事件等级：0-未配置 1-低 2-中 3-高 */
    @ApiModelProperty(value = "事件等级")
    private Integer eventLvl;

    /** 脉冲超时时间，单位：秒 */
    @ApiModelProperty(value = "脉冲超时时间")
    private Integer timeout;

    /** 事件发生时间（设备时间），ISO8601格式 */
    @ApiModelProperty(value = "事件发生时间")
    private String happenTime;

    /** 事件发生的事件源父设备编码 */
    @ApiModelProperty(value = "父设备编码")
    private String srcParentIndex;

    /** 事件其它扩展信息，JSON格式存储 */
    @ApiModelProperty(value = "事件扩展数据")
    private String eventData;

    /** 记录创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录创建时间")
    private Date gmtCreate;

    /** 记录更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录更新时间")
    private Date gmtModified;
}
