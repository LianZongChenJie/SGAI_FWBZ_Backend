package org.jeecg.modules.fwbz.activeMeetPreparation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 消防设备（烟感/温感）
 */
@Data
@TableName("table_smoke_detector")
public class SmokeDetector {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceName;
    private String status;
    private String deviceType;
}
