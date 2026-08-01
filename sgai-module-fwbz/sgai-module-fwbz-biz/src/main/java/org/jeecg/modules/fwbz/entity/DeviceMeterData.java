package org.jeecg.modules.fwbz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备计量数据
 *
 * 区别于MeterData,其他的是存储带时间的计量数据，表中有多个时间的多个值，
 * 此表存储最新表读数和今日用量和本月累计，
 * 数据来源为接收mq表读数存储数据时，同步更新此表读数数据，今日用量，
 * 设置定时任务，每天凌晨将今日用量累加到本月累计中，然后清空今日用量，
 * 页面展示本月累计的时候展示本月累计+今日用量的数据为实时最新的本月累计
 *
 */
@Data
@TableName("device_meter_data")
public class DeviceMeterData implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private BigDecimal meterReading;

    private BigDecimal dayUsage;

    private BigDecimal mouthTotal;
}
