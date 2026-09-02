package org.jeecg.modules.fwbz.mqtt.entity;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备属性
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "设备属性", description = "设备属性")
@TableName("device_attribute")
public class DeviceAttribute implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建日期")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("更新日期")
    private LocalDateTime updateTime;

    @ApiModelProperty("所属部门")
    private String sysOrgCode;

    @ApiModelProperty("设备id")
    private Long deviceId;

    @ApiModelProperty("属性名称")
    private String attributeName;

    @ApiModelProperty("属性编码")
    private String attributeCode;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("读写等级")
    private String readwriteLevel;

    @ApiModelProperty("排序字段")
    private Integer sort;

    @ApiModelProperty("采集值")
    private BigDecimal value;

    @ApiModelProperty("采集时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gatherTime;

    @ApiModelProperty("采集编码")
    private String acquisitionCoding;

    @ApiModelProperty("属性值类型")
    private String valueType;

    @ApiModelProperty("属性值配置")
    private String valueConfig;
}
