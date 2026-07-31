package org.sgai.dto;

import lombok.Data;

@Data
public class YbttAttribute {

    /**
     * 数据名称
     */
    private String dataName;

    /**
     * 数据值
     */
    private String dataValue;
    /**
     * 数据单位
     */
    private String dataUnit;

    /**
     * 数据参数序号ID
     */
    private String sensorId;

    /**
     * 数据记录时间
     */
    private Long dataLogTime;
}
