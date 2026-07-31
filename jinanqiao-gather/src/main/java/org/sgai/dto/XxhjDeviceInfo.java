package org.sgai.dto;

import lombok.Data;

@Data
public class XxhjDeviceInfo {

    /**
     * 设备类型
     */
    private String deviceType;
    /**
     * 设备编号
     */
    private String code;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 父级 ID -1 时为根目录
     */
    private String superId;
    /**
     * 设备 ID
     */
    private String deviceId;
    /**
     * 安装位置
     */
    private String installLocation;

}
