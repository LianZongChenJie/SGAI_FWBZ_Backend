package org.jeecg.modules.fwbz.coldSourceSystem.service;

import com.sunwayland.pspace.PSpaceClient;
import com.sunwayland.pspace.entity.PsConnectInfo;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsServerProp;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.config.ColdSourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 冷源系统(pSpace) Server API 服务（对应 SDK 自带 ServerAPIDemo）
 *
 * 连接流程：
 * 1. PSpaceClient.getInstance(host, port, username, password) 获取客户端实例
 * 2. client.connect() 建立连接
 * 3. 调用业务接口（返回 PsResult&lt;T&gt;，成功判断 result.isSuccess()，数据在 getData()）
 * 4. client.disconnect() 断开连接
 *
 * 说明：服务启动时自动连接（失败仅告警不影响主系统），连接参数在 application.yml 的 fwbz.cold-source 下配置。
 * 使用示例：
 * <pre>
 *   &#64;Autowired
 *   private ColdSourceServerService coldSourceServerService;
 *
 *   coldSourceServerService.connect();
 *   coldSourceServerService.serverGetAllConnectInfo();
 *   coldSourceServerService.disconnect();
 * </pre>
 */
@Slf4j
@Service
public class ColdSourceServerService {

    @Autowired
    private ColdSourceProperties properties;

    private volatile PSpaceClient client;

    /**
     * 启动时后台异步连接冷源系统；连接失败仅告警，不影响 Spring 容器启动
     */
    @PostConstruct
    public void init() {
        if (properties.isMock()) {
            log.warn("【模拟模式】fwbz.cold-source.mock=true：跳过冷源系统连接，数据由内置模拟数据源提供");
            return;
        }
        Thread connectThread = new Thread(() -> {
            try {
                log.info("连接冷源系统中。。。");
                connect();
            } catch (Exception e) {
                log.warn("冷源系统连接失败，请检查配置 fwbz.cold-source 后重启或手动调用 connect() 重试", e);
            }
        }, "cold-source-connect");
        // 守护线程：连接慢/失败都不影响应用启动与退出
        connectThread.setDaemon(true);
        connectThread.start();
    }

    /**
     * 建立连接（幂等：已连接则直接返回）
     */
    public synchronized PSpaceClient connect() {
        if (client != null) {
            return client;
        }
        client = PSpaceClient.getInstance(
                properties.getHost(),
                properties.getPort(),
                properties.getUsername(),
                properties.getPassword());
        try {
            client.connect();
            log.info("冷源系统连接成功: {}:{}", properties.getHost(), properties.getPort());
        } catch (Exception e) {
            client = null;
            throw e;
        }
        return client;
    }

    /**
     * 获取服务器连接信息
     */
    public PsResult<PsConnectInfo> serverGetAllConnectInfo() {
        PsResult<PsConnectInfo> result = connect().serverGetAllConnectInfo();
        if (result.isSuccess()) {
            for (PsConnectInfo info : result.getData()) {
                log.info("连接信息: {}", info);
            }
        } else {
            log.error("获取连接信息失败: {}", result.getCode());
        }
        return result;
    }

    /**
     * 获取服务器时间
     */
    public PsResult<Long> serverGetTime() {
        PsResult<Long> result = connect().serverGetTime();
        if (result.isSuccess()) {
            log.info("服务器时间: {}", result.getData().get(0));
        } else {
            log.error("获取服务器时间失败: {}", result.getCode());
        }
        return result;
    }

    /**
     * 获取服务器属性（安全区、权限等）
     */
    public PsResult<PsServerProp> serverGetProp() {
        PsResult<PsServerProp> result = connect().serverGetProp();
        if (result.isSuccess()) {
            PsServerProp prop = result.getData().get(0);
            log.info("服务器属性: {}", prop);
        } else {
            log.error("获取服务器属性失败: {}", result.getCode());
        }
        return result;
    }

    /**
     * 断开连接
     */
    public synchronized void disconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
            log.info("冷源系统连接已断开");
        }
    }
}
