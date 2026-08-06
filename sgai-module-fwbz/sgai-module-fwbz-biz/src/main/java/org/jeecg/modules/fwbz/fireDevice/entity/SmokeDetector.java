package org.jeecg.modules.fwbz.fireDevice.entity;

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
import java.math.BigDecimal;
import java.util.Date;

/**
 * 消防设备（烟感/温感/光感/消防栓）
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "消防设备", description = "消防设备")
@TableName("table_smoke_detector")
public class SmokeDetector implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("设备类型ID（关联 table_smoke_detector_type）")
    private String deviceType;

    @ApiModelProperty("场馆ID")
    private Long venueId;

    @ApiModelProperty("最后巡检时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastCheckTime;

    @ApiModelProperty("经度")
    private BigDecimal longitude;

    @ApiModelProperty("纬度")
    private BigDecimal latitude;

    @ApiModelProperty("安装位置")
    private String location;

    @ApiModelProperty("信号强度")
    private String signal;

    @ApiModelProperty("电量")
    private String powerLevel;

    /** 设备类型名称（联动 table_smoke_detector_type，非数据库字段） */
    @TableField(exist = false)
    @ApiModelProperty("设备类型名称")
    private String typeName;
}
