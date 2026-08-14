package org.jeecg.modules.fwbz.lighting.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 子系统免密登录对接配置（application.yml / nacos 中 fwbz.subsystem.* 前缀）
 */
@Component
@ConfigurationProperties(prefix = "fwbz.subsystem")
public class SubsystemProperties {

    /** 子系统根地址，如 http://10.61.13.140:888 */
    private String baseUrl;

    /** 免密登录固定账号 */
    private String username;

    /** 免密登录固定密码 */
    private String password;

    /** 登录成功后跳转的子系统页面（相对路径） */
    private String redirectUrl = "/appWebtopoPreview";

    /** 登录接口完整地址 */
    public String getLoginUrl() {
        return baseUrl + "/prod-api/login";
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
