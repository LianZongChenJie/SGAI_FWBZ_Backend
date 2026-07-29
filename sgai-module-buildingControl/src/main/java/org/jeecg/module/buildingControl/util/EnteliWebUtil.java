package org.jeecg.module.buildingControl.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import dm.jdbc.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * enteliWEB REST API 工具类
 * 提供 BACnet 对象属性值的读取和设置
 */
@Slf4j
public class EnteliWebUtil {
    private static final int READ_TIMEOUT = 30000;
    private static final CsmlParser parser = new CsmlParser();

    private EnteliWebUtil() {
    }

    /**
     * 获取BACnet对象属性值
     *
     * @param baseUrl  enteliWEB地址，如 http://10.74.10.2/enteliweb
     * @param username 用户名
     * @param password 密码
     * @param path     API路径，如 /api/.bacnet/首钢774/774001/binary-output,2/present-value?alt=json
     * @return 属性值字符串
     */
    public static String getProperty(String baseUrl, String username, String password,
                                     String path) throws IOException {
        String response = doGet(baseUrl, username, password, path);
        try {
            CsmlNode root = parser.parse(response);
            String value = root.getValue();
            log.debug("getProperty {} = {}", path, value);
            return value;
        } catch (Exception e) {
            throw new IOException("解析属性值失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置BACnet对象属性值
     *
     * @param baseUrl  enteliWEB地址
     * @param username 用户名
     * @param password 密码
     * @param path     API路径，如 /api/.bacnet/首钢774/774001/binary-output,2/present-value
     * @param value    要设置的值
     * @return 是否设置成功
     */
    public static boolean setProperty(String baseUrl, String username, String password,
                                      String path, String value) throws IOException {
        String csmlType = inferCsmlType(value);
        String body = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><%s xmlns=\"http://bacnet.org/csml/1.2\" value=\"%s\"/>",
                csmlType, escapeXml(value));

        String response = doPut(baseUrl, username, password, path, body);
        try {
            CsmlNode root = parser.parse(response);
            log.debug("setProperty {} = {}, result: {}", path, value, root.getValue());
            return true;
        } catch (Exception e) {
            log.error("setProperty 解析响应失败", e);
            return false;
        }
    }

    private static String inferCsmlType(String value) {
        if (value == null) return "String";
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) return "Boolean";
        try {
            Double.parseDouble(value);
            return "Real";
        } catch (NumberFormatException e) {
            return "String";
        }
    }

    private static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&apos;");
    }

    private static String doGet(String baseUrl, String username, String password, String path) throws IOException {
        return request(baseUrl, username, password, path, "GET", null);
    }

    private static String doPut(String baseUrl, String username, String password, String path, String body) throws IOException {
        return request(baseUrl, username, password, path, "PUT", body);
    }

    private static String doGetWithCookie(String baseUrl, String enteliWebID, String path) throws IOException {
        return requestWithCookie(baseUrl, enteliWebID, path, "GET", null);
    }

    private static String doPutWithCookie(String baseUrl, String enteliWebID, String path, String body) throws IOException {
        return requestWithCookie(baseUrl, enteliWebID, path, "PUT", body);
    }

    private static String request(String baseUrl, String username, String password,
                                  String path, String method, String reqBody) throws IOException {
        String urlStr = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String fullUrl = urlStr + encodePath(path);
        log.debug("enteliWEB request: {} {}", method, fullUrl);

        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8));

        HttpRequest httpRequest = "PUT".equals(method)
                ? HttpRequest.put(fullUrl).body(reqBody, "application/xml")
                : HttpRequest.get(fullUrl);
        httpRequest.header("Authorization", authHeader)
                .header("Accept", "application/xml")
                .timeout(READ_TIMEOUT);

        try (HttpResponse response = httpRequest.execute()) {
            return response.body();
        }
    }

    private static String requestWithCookie(String baseUrl, String enteliWebID,
                                             String path, String method, String reqBody) throws IOException {
        String urlStr = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String fullUrl = urlStr + encodePath(path);
        log.debug("enteliWEB request(cookie): {} {}", method, fullUrl);

        HttpRequest httpRequest = "PUT".equals(method)
                ? HttpRequest.put(fullUrl).body(reqBody, "application/xml")
                : HttpRequest.get(fullUrl);
        httpRequest.header("Cookie", "enteliWebID=" + enteliWebID + ";path=/enteliweb;secure=HttpOnly;SameSite=strict;")
                .header("Accept", "application/xml")
                .timeout(READ_TIMEOUT);

        try (HttpResponse response = httpRequest.execute()) {
            return response.body();
        }
    }

    /**
     * 通过用户名密码获取 enteliWebID
     *
     * @param baseUrl  enteliWEB地址
     * @param username 明文用户名
     * @param password 明文密码
     * @return enteliWebID
     */
    public static String login(String baseUrl, String username, String password) throws IOException {
        String loginUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        log.debug("enteliWEB login: {}", loginUrl);

        String encodedUsername = Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8));
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
        String jsonBody = "{\"username\":\"" + encodedUsername + "\",\"password\":\"" + encodedPassword + "\"}";

        try (HttpResponse response = HttpRequest.post(loginUrl)
                .body(jsonBody, "application/json")
                .timeout(READ_TIMEOUT)
                .execute()) {
            String webID = response.getCookieValue("enteliWebID");
            if(StringUtils.isEmpty(webID)){
                log.error("登录失败");
                throw new IOException("登录失败 HTTP : " + response.body());
            }
            return webID;
        }
    }

    /**
     * 获取所有站点列表
     */
    public static List<String> getSites(String baseUrl, String username, String password) throws IOException {
        String response = doGet(baseUrl, username, password, "/api/.bacnet");
        try {
            CsmlNode root = parser.parse(response);
            List<String> sites = new ArrayList<>();
            for (CsmlNode child : root.getChildren()) {
                String name = child.getName();
                if (name != null && !name.isEmpty()) {
                    sites.add(name);
                }
            }
            return sites;
        } catch (Exception e) {
            throw new IOException("解析站点列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定站点下的设备名称列表
     */
    public static List<String> getDevices(String baseUrl, String username, String password,
                                           String site) throws IOException {
        String path = "/api/.bacnet/" + encode(site);
        String response = doGet(baseUrl, username, password, path);
        try {
            CsmlNode root = parser.parse(response);
            List<String> devices = new ArrayList<>();
            for (CsmlNode child : root.getChildren()) {
                String name = child.getName();
                if (name != null && !name.isEmpty()) {
                    devices.add(name);
                }
            }
            return devices;
        } catch (Exception e) {
            throw new IOException("解析设备列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取BACnet对象属性（带类型信息）
     *
     * @param path API路径，如 /api/.bacnet/首钢774/774001/analog-input,1/present-value
     * @return BacnetPropertyResult
     */
    public static BacnetPropertyResult getPropertyWithType(String baseUrl, String username, String password,
                                                           String path) throws IOException {
        String response = doGet(baseUrl, username, password, path);
        try {
            CsmlNode root = parser.parse(response);
            BacnetPropertyResult result = new BacnetPropertyResult();
            result.setValue(root.getValue());
            result.setDataType(root.getType().name());
            return result;
        } catch (Exception e) {
            throw new IOException("解析属性值失败: " + e.getMessage(), e);
        }
    }

    public static String getPropertyWithCookie(String baseUrl, String enteliWebID, String path) throws IOException {
        String response = doGetWithCookie(baseUrl, enteliWebID, path);
        try {
            CsmlNode root = parser.parse(response);
            String value = root.getValue();
            log.debug("getProperty {} = {}", path, value);
            return value;
        } catch (Exception e) {
            throw new IOException("解析属性值失败: " + e.getMessage(), e);
        }
    }

    public static boolean setPropertyWithCookie(String baseUrl, String enteliWebID, String path, String value) throws IOException {
        String csmlType = inferCsmlType(value);
        String body = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><%s xmlns=\"http://bacnet.org/csml/1.2\" value=\"%s\"/>",
                csmlType, escapeXml(value));

        String response = doPutWithCookie(baseUrl, enteliWebID, path, body);
        try {
            CsmlNode root = parser.parse(response);
            log.debug("setProperty {} = {}, result: {}", path, value, root.getValue());
            return true;
        } catch (Exception e) {
            log.error("setProperty 解析响应失败", e);
            return false;
        }
    }

    public static BacnetPropertyResult getPropertyWithTypeWithCookie(String baseUrl, String enteliWebID, String path) throws IOException {
        String response = doGetWithCookie(baseUrl, enteliWebID, path);
        try {
            CsmlNode root = parser.parse(response);
            BacnetPropertyResult result = new BacnetPropertyResult();
            result.setValue(root.getValue());
            result.setDataType(root.getType().name());
            return result;
        } catch (Exception e) {
            throw new IOException("解析属性值失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取趋势日志记录
     */
    public static String getTrendLogRecords(String baseUrl, String username, String password,
                                             String site, String device, String instance,
                                             int maxResults) throws IOException {
        String path = String.format("/api/.bacnet/%s/%s/trend-log,%s/log-buffer?max-results=%d",
                encode(site), encode(device), encode(instance), maxResults);
        return doGet(baseUrl, username, password, path);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    /**
     * 对路径中的每段进行URL编码，保留 / ? = & 等结构字符
     */
    private static String encodePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            return encodePath(path.substring(0, queryIndex)) + path.substring(queryIndex);
        }
        String[] segments = path.split("/", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(encode(segments[i]));
        }
        return sb.toString();
    }
}
