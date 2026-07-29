package org.jeecg.module.gather.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 负碳楼设备信息
 */
@TableName(value = "ftl_device")
@Data
public class FtlDevice {

    @TableId
    private String deviceCode;

    private String deviceName;
}
