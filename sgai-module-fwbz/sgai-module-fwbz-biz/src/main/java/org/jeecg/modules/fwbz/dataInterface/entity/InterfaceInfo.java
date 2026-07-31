package org.jeecg.modules.fwbz.dataInterface.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.entity.BaseEntity;

import java.util.Date;

/**
 * 接口信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("table_interface_info")
public class InterfaceInfo extends BaseEntity {

    /**
     * 状态。在线
     */
    public static final Integer STATE_ONLINE = 1;
    /**
     * 状态。离线
     */
    public static final Integer STATE_OFFLINE = 0;
    /**
     * 状态。异常
     */
    public static final Integer STATE_ABNORMAL = 2;

    /**
     * 协议类型：HTTP API
     */
    public static final Long PROTOCOL_TYPE_HTTP = 1L;
    /**
     * 协议类型：MQTT
     */
    public static final Long PROTOCOL_TYPE_MQTT = 2L;
    /**
     * 协议类型：BACnet
     */
    public static final Long PROTOCOL_TYPE_BACNET = 3L;
    /**
     * 协议类型：Modbus TCP
     */
    public static final Long PROTOCOL_TYPE_MODBUS = 4L;
    /**
     * 协议类型：OPC UA
     */
    public static final Long PROTOCOL_TYPE_OPC_UA = 5L;

    /**
     * 系统名称
     */
    private String sysName;

    /**
     * 接口地址
     */
    private String interfacePath;

    /**
     * 协议类型ID
     */
    private Long protocolTypeId;

    /**
     * 状态。在线：1；离线：0；异常：2
     */
    private Integer state;

    /**
     * 最后心跳时间
     */
    private Date requestTime;

    /**
     * 响应时间(ms)
     */
    private Long responseTime;
}
