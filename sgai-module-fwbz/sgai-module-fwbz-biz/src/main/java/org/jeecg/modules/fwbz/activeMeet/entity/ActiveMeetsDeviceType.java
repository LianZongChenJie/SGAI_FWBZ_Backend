package org.jeecg.modules.fwbz.activeMeet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会前后备设备类型
 */
@Data
@TableName("table_activeMeets_device_type")
public class ActiveMeetsDeviceType {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 筹备类型id
     */
    private Long typeId;

    /**
     * 设备类型id
     */
    private Long deviceTypeId;

    /**
     * 设备类型名称
     */
    private String deviceTypeName;
}
