package org.sgai.util;

import lombok.Data;

@Data
public class WaterMeterLog {

    /** 主键ID */
    private String id;

    /** 水表ID */
    private String meterId;

    /** 水表编码 */
    private String meterCode;

    /** IMEI号 */
    private String imei;

    /** 水表读数 */
    private String meterReading;

    /** 增量 */
    private Double increment;

    /** 金额 */
    private Double amount;

    /** 记录时间 */
    private String recordTime;

    /** 信号强度 */
    private Integer rssi;

    /** 上传类型 */
    private String uploadType;

    /** 电池电压 */
    private Double batteryVoltage;

    /** 电池状态 */
    private Integer batteryStatus;

    /** 阀门状态 */
    private Integer valveStatus;

    /** 温度 */
    private Double temperature;

    /** 压力 */
    private Double pressure;

    /** 计量单位 */
    private Double meteringUnit;

    /** 流量冻结记录 */
    private String flowFreezingRecord;

    /** 创建时间 */
    private String createTime;

    /** 部门ID */
    private String deptId;

    /** 租户ID */
    private String tenantId;

    /** IMSI号 */
    private String imsi;

    /** ICCID号 */
    private String iccid;

    /** 上传周期 */
    private Integer uploadCycle;
}
