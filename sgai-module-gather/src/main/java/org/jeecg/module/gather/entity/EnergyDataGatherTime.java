package org.jeecg.module.gather.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 能源数据采集时间
 */
@Data
@TableName(value = "energy_data_gather_time")
public class EnergyDataGatherTime {

    /**
     * 设备编号
     */
    @TableId
    private String deviceCode;

    private LocalDateTime time;

    private BigDecimal value;
}
