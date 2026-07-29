package org.jeecg.module.buildingControl.service;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.module.buildingControl.util.BacnetPropertyResult;
import org.jeecg.module.buildingControl.util.EnteliWebUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RefreshScope
public class EnteliWebService {

    @Value("${enteliweb.baseUrl}")
    private String baseUrl;

    @Value("${enteliweb.username}")
    private String username;

    @Value("${enteliweb.password}")
    private String password;

    @Value("${enteliweb.authMode:basic}")
    private String authMode;

    @Value("${enteliweb.enteliWebID}")
    private String enteliWebID = "jlck3rkn9slcoeh5l46n0v5sdufqtqcl";


    /**
     * 获取BACnet对象属性值
     *
     * @param path API路径，如 /api/.bacnet/首钢774/774001/binary-output,2/present-value?alt=json
     * @return 属性值字符串
     */
    public String getProperty(String path) throws IOException {
        if ("token".equalsIgnoreCase(authMode)) {
            return EnteliWebUtil.getPropertyWithCookie(baseUrl, enteliWebID, path);
        }
        return EnteliWebUtil.getProperty(baseUrl, username, password, path);
    }

    /**
     * 获取BACnet对象属性（带类型信息）
     *
     * @param path API路径，如 /api/.bacnet/首钢774/774001/analog-input,1/present-value
     * @return BacnetPropertyResult
     */
    public BacnetPropertyResult getPropertyWithType(String path) throws IOException {
        if ("token".equalsIgnoreCase(authMode)) {
            return EnteliWebUtil.getPropertyWithTypeWithCookie(baseUrl, enteliWebID, path);
        }
        return EnteliWebUtil.getPropertyWithType(baseUrl, username, password, path);
    }

    /**
     * 设置BACnet对象属性值
     *
     * @param path  API路径，如 /api/.bacnet/首钢774/774001/binary-output,2/present-value
     * @param value 要设置的值
     * @return 是否设置成功
     */
    public boolean setProperty(String path, String value) throws IOException {
        String fullPath = path.contains("?") ? path + "&priority=5" : path + "?priority=5";
        if ("token".equalsIgnoreCase(authMode)) {
            return EnteliWebUtil.setPropertyWithCookie(baseUrl, enteliWebID, fullPath, value);
        }
        return EnteliWebUtil.setProperty(baseUrl, username, password, fullPath, value);
    }
}
