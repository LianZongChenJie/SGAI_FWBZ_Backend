package org.sgai.util;

import lombok.Data;

@Data
public class WaterMeter {

    /**
     * 水表编号
     */
    private String meterCode;
    /**
     * 水表类型
     */
    private String meterType;
    /**
     * 水表走水类型(冷水、中水、热水、纯水)
     */
    private String waterType;

    /**
     * 阀门状态（开阀、关阀）
     */
    private String valveStatus;
    /**
     * 电池电压（v:伏特）
     */
    private String batteryVoltage;
    /**
     * 信号强度
     */
    private String rssi;
    /**
     * 水表读数
     */
    private String meterReading;

    /**
     * 最后通讯时间
     */
    private String reportingTime;
    /**
     * 水表阀控指令状态
     */
    private String haveTask;
    /**
     * 压力（Mpa）
     */
    private String pressure;
    /**
     * 温度（摄氏度）
     */
    private String temperature;

}
