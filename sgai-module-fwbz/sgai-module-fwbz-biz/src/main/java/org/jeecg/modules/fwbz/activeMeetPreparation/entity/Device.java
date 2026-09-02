package org.jeecg.modules.fwbz.activeMeetPreparation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 设备基础信息
 */
@Data
@TableName("device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceCode;
    private String deviceName;
    private Long categoryId;
    private Long spaceId;
    private String runState;
    private Long modelId;
    private String deviceType;
    private Long venueId;
}
