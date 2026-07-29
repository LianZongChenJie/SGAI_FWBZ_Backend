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
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: FWBZ设备管理Demo
 * @Author: jeecg-boot
 * @Date:   2026-07-21
 * @Version: V1.0
 */
@Data
@TableName("fwbz_device")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="fwbz_device对象", description="设备管理")
public class FwbzDevice implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
	/**设备编号*/
	@Excel(name = "设备编号", width = 15)
    @ApiModelProperty(value = "设备编号")
    private String deviceCode;
	/**设备类型 1-电表 2-水表 3-气表 4-温控器*/
	@Excel(name = "设备类型", width = 15, dicCode = "device_type")
    @ApiModelProperty(value = "设备类型 1-电表 2-水表 3-气表 4-温控器")
    private Integer deviceType;
	/**安装位置*/
	@Excel(name = "安装位置", width = 15)
    @ApiModelProperty(value = "安装位置")
    private String location;
	/**设备状态 0-离线 1-在线 2-故障*/
	@Excel(name = "设备状态", width = 15, dicCode = "device_status")
    @ApiModelProperty(value = "设备状态 0-离线 1-在线 2-故障")
    private Integer status;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private String remark;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
}
