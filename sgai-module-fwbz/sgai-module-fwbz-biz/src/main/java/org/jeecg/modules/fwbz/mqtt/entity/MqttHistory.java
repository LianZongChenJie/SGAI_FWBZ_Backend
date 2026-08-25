package org.jeecg.modules.fwbz.mqtt.entity;

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
import java.time.LocalDateTime;

/**
 * MQTT低压配电数据
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "MQTT低压配电数据", description = "MQTT低压配电数据")
@TableName("table_mqtt_history")
public class MqttHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("设备代码")
    private String devKeys;

    @ApiModelProperty("数据的时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timeStamp;

    @ApiModelProperty("传感器唯一键")
    private String uniqueKey;

    @ApiModelProperty("量测详细信息，测点含义")
    private String desc;

    @ApiModelProperty("遥测值")
    private String value;

    @TableField(exist = false)
    @ApiModelProperty("数据类型：yc-遥测，yx-遥信，kwh-电度（表底值）")
    private String dataType;
}
