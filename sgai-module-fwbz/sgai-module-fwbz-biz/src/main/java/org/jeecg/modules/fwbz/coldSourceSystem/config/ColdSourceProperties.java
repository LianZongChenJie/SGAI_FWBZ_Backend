package org.jeecg.modules.fwbz.coldSourceSystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 冷源系统(pSpace)连接配置，对应 application.yml 中 fwbz.cold-source.*
 */
@Component
@ConfigurationProperties(prefix = "fwbz.cold-source")
public class ColdSourceProperties {

    /** 冷源系统服务地址 */
    private String host = "10.22.163.239";

    /** 冷源系统服务端口 */
    private int port = 8889;

    /** 登录账号 */
    private String username = "admin";

    /** 登录密码 */
    private String password = "admin888";

    /** pSpace WebApi(HTTP) 地址：留空则复用 host（/RealData 等 REST 接口所在主机） */
    private String webApiHost = "";

    /** pSpace WebApi(HTTP) 端口，默认 8080 */
    private int webApiPort = 8080;

    /** pSpace WebApi(HTTP) 请求超时（毫秒） */
    private int webApiTimeoutMs = 5000;

    public String getWebApiHost() {
        return webApiHost;
    }

    public void setWebApiHost(String webApiHost) {
        this.webApiHost = webApiHost;
    }

    public int getWebApiPort() {
        return webApiPort;
    }

    public void setWebApiPort(int webApiPort) {
        this.webApiPort = webApiPort;
    }

    public int getWebApiTimeoutMs() {
        return webApiTimeoutMs;
    }

    public void setWebApiTimeoutMs(int webApiTimeoutMs) {
        this.webApiTimeoutMs = webApiTimeoutMs;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
