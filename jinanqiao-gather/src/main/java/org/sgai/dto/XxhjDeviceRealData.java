package org.sgai.dto;

import lombok.Data;

/**
 * 设备实时数据
 */
@Data
public class XxhjDeviceRealData {

    private String SensorId;

    private String P;

    private String ConsTotal;

    private String ConsToday;

    private String ConsMonth;

    private String value;

}
