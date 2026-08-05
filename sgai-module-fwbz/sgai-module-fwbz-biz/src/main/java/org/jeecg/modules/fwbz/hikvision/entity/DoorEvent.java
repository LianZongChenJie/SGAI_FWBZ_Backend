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
 * 门禁点事件表
 *
 * @author fwbz
 */
@Data
@TableName("table_door_event")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "table_door_event对象", description = "门禁点事件表")
public class DoorEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "事件ID，唯一标识")
    private String eventId;

    @ApiModelProperty(value = "事件名称")
    private String eventName;

    @ApiModelProperty(value = "事件产生时间，ISO8601格式")
    private String eventTime;

    @ApiModelProperty(value = "人员唯一编码")
    private String personId;

    @ApiModelProperty(value = "卡号")
    private String cardNo;

    @ApiModelProperty(value = "人员姓名")
    private String personName;

    @ApiModelProperty(value = "人员所属组织编码")
    private String orgIndexCode;

    @ApiModelProperty(value = "人员所属组织名称")
    private String orgName;

    @ApiModelProperty(value = "门禁点名称")
    private String doorName;

    @ApiModelProperty(value = "门禁点编码")
    private String doorIndexCode;

    @ApiModelProperty(value = "门禁点所在区域编码")
    private String doorRegionIndexCode;

    @ApiModelProperty(value = "抓拍图片地址（相对地址，需配合svr_index_code通过接口获取图片）")
    private String picUri;

    @ApiModelProperty(value = "图片存储服务唯一标识（与pic_uri配对使用）")
    private String svrIndexCode;

    @ApiModelProperty(value = "事件类型")
    private Integer eventType;

    @ApiModelProperty(value = "进出类型：1-进 0-出 -1-未知")
    private Integer inAndOutType;

    @ApiModelProperty(value = "读卡器唯一标识")
    private String readerDevIndexCode;

    @ApiModelProperty(value = "读卡器名称")
    private String readerDevName;

    @ApiModelProperty(value = "控制器设备唯一标识")
    private String devIndexCode;

    @ApiModelProperty(value = "控制器设备名称")
    private String devName;

    @ApiModelProperty(value = "身份证图片地址（相对地址，需通过接口获取图片）")
    private String identityCardUri;

    @ApiModelProperty(value = "事件入库时间，ISO8601格式")
    private String receiveTime;

    @ApiModelProperty(value = "工号")
    private String jobNo;

    @ApiModelProperty(value = "学号")
    private String studentId;

    @ApiModelProperty(value = "证件号码")
    private String certNo;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录创建时间")
    private Date gmtCreate;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录更新时间")
    private Date gmtModified;
}
