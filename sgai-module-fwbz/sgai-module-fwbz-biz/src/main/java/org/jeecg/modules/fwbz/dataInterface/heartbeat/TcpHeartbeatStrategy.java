package org.jeecg.modules.fwbz.dataInterface.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TCP 心跳检测策略
 * <p>
 * 通过 TCP Socket 连接检测 MQTT、BACnet、Modbus TCP、OPC UA 等协议的连通性。
 * 支持多种地址格式：host:port、tcp://host:port、mqtt://host:port 等。
 * 如未指定端口则使用对应协议的默认端口。
 */
@Slf4j
@Component
public class TcpHeartbeatStrategy implements HeartbeatStrategy {

    private static final int CONNECT_TIMEOUT = 5000;

    /**
     * 地址解析正则：匹配 协议前缀://主机:端口 或 主机:端口
     */
    private static final Pattern ADDRESS_PATTERN =
            Pattern.compile("^(?:[a-zA-Z]+://)?([^:/]+):?(\\d+)?$");

    /**
     * 执行 TCP 心跳检测（不使用协议默认端口，必须从地址中解析或外部指定）
     */
    @Override
    public HeartbeatResult check(String address) {
        // 此方法需要协议类型信息来确定默认端口，请使用 check(address, protocolTypeId)
        throw new UnsupportedOperationException("请使用 check(address, protocolTypeId) 方法");
    }

    /**
     * 根据协议类型执行 TCP 心跳检测
     *
     * @param address        接口地址
     * @param protocolTypeId 协议类型ID
     * @return 检测结果
     */
    public HeartbeatResult check(String address, Long protocolTypeId) {
        int defaultPort = getDefaultPort(protocolTypeId);
        return doTcpCheck(address, defaultPort);
    }

    /**
     * 执行 TCP 连接检测
     */
    private HeartbeatResult doTcpCheck(String address, int defaultPort) {
        long startTime = System.currentTimeMillis();

        String host = parseHost(address);
        int port = parsePort(address, defaultPort);

        if (host == null || host.isEmpty()) {
            log.warn("TCP心跳地址解析失败 - address: {}", address);
            return HeartbeatResult.offline(System.currentTimeMillis() - startTime);
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
            long elapsed = System.currentTimeMillis() - startTime;
            log.debug("TCP心跳在线 - {}:{}, 耗时: {}ms", host, port, elapsed);
            return HeartbeatResult.online(elapsed);
        } catch (java.net.SocketTimeoutException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("TCP心跳超时 - {}:{}, 耗时: {}ms", host, port, elapsed);
            return HeartbeatResult.abnormal(elapsed);
        } catch (java.net.ConnectException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("TCP心跳连接被拒绝 - {}:{}, 耗时: {}ms", host, port, elapsed);
            return HeartbeatResult.offline(elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("TCP心跳连接失败 - {}:{}, 耗时: {}ms, 原因: {}",
                    host, port, elapsed, e.getMessage());
            return HeartbeatResult.offline(elapsed);
        }
    }

    /**
     * 从地址中解析主机名
     */
    private String parseHost(String address) {
        if (address == null) {
            return null;
        }
        Matcher m = ADDRESS_PATTERN.matcher(address.trim());
        if (m.find()) {
            return m.group(1);
        }
        return address.trim();
    }

    /**
     * 从地址中解析端口，未指定则使用默认端口
     */
    private int parsePort(String address, int defaultPort) {
        if (address == null) {
            return defaultPort;
        }
        Matcher m = ADDRESS_PATTERN.matcher(address.trim());
        if (m.find() && m.group(2) != null) {
            return Integer.parseInt(m.group(2));
        }
        return defaultPort;
    }

    /**
     * 根据协议类型获取默认端口
     */
    private int getDefaultPort(Long protocolTypeId) {
        if (InterfaceInfo.PROTOCOL_TYPE_MQTT.equals(protocolTypeId)) {
            return 1883;
        }
        if (InterfaceInfo.PROTOCOL_TYPE_BACNET.equals(protocolTypeId)) {
            return 47808;
        }
        if (InterfaceInfo.PROTOCOL_TYPE_MODBUS.equals(protocolTypeId)) {
            return 502;
        }
        if (InterfaceInfo.PROTOCOL_TYPE_OPC_UA.equals(protocolTypeId)) {
            return 4840;
        }
        // 未知协议，默认 80
        return 80;
    }
}
