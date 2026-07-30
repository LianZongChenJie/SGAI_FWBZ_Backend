package org.jeecg.modules.fwbz.dataInterface.heartbeat;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;
import org.springframework.stereotype.Component;

/**
 * 心跳检测策略工厂
 * <p>
 * 根据协议类型返回对应的检测策略：
 * - HTTP API        → HttpHeartbeatStrategy  (HTTP GET)
 * - MQTT            → TcpHeartbeatStrategy   (TCP :1883)
 * - BACnet          → TcpHeartbeatStrategy   (TCP :47808)
 * - Modbus TCP      → TcpHeartbeatStrategy   (TCP :502)
 * - OPC UA          → TcpHeartbeatStrategy   (TCP :4840)
 */
@Slf4j
@Component
@AllArgsConstructor
public class HeartbeatStrategyFactory {

    private final HttpHeartbeatStrategy httpStrategy;
    private final TcpHeartbeatStrategy tcpStrategy;

    /**
     * 根据协议类型获取心跳策略
     *
     * @param protocolTypeId 协议类型ID
     * @return 对应策略，未知协议返回 null
     */
    public HeartbeatStrategy getStrategy(Long protocolTypeId) {
        if (protocolTypeId == null) {
            log.warn("协议类型为空，使用 HTTP 策略兜底");
            return httpStrategy;
        }
        if (InterfaceInfo.PROTOCOL_TYPE_HTTP.equals(protocolTypeId)) {
            return httpStrategy;
        }
        if (InterfaceInfo.PROTOCOL_TYPE_MQTT.equals(protocolTypeId)
                || InterfaceInfo.PROTOCOL_TYPE_BACNET.equals(protocolTypeId)
                || InterfaceInfo.PROTOCOL_TYPE_MODBUS.equals(protocolTypeId)
                || InterfaceInfo.PROTOCOL_TYPE_OPC_UA.equals(protocolTypeId)) {
            return tcpStrategy;
        }
        log.warn("未知协议类型: {}, 跳过心跳检测", protocolTypeId);
        return null;
    }
}
