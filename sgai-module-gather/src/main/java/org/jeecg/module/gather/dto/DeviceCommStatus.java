package org.jeecg.module.gather.dto;

import lombok.Data;

@Data
public class DeviceCommStatus {
    /**
     * 设备编号
     */
    private String deviceId;
    /**
     * 设备通讯状态。1：在线；0：离线
     */
    private Integer commStatus;
}
