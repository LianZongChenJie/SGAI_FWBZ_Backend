package org.jeecg.modules.fwbz.runGuarantee.vo;

import lombok.Data;

/**
 * 各系统设备在线统计
 */
@Data
public class SystemDeviceStatVO {

    /**
     * 系统名称（对应设备类型名称）
     */
    private String systemName;

    /**
     * 在线数量
     */
    private Long online;

    /**
     * 设备总数
     */
    private Long deviceCount;

    /**
     * 在线率（百分比整数，如100表示100%）
     */
    private Integer onlineRate;
}
