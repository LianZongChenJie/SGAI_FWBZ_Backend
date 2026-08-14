package org.jeecg.modules.fwbz.lighting.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jeecg.modules.fwbz.lighting.properties.SubsystemProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/fwbz/lighting")
public class SubsystemProxyController {

    // 子系统免密登录对接配置（application.yml: fwbz.subsystem.*）
    @Autowired
    private SubsystemProperties subsystemProperties;

    // cookie 键名（登录后种给前端）
    private static final String TOKEN_COOKIE_NAME = "Admin-Token";

    // 服务端 token 缓存：避免并发请求时每个请求都重复登录子系统
    private static volatile String cachedToken;
    private static volatile long cachedTokenExpireTime; // 过期时间戳(ms)
    private static final long TOKEN_TTL_MS = 50 * 60 * 1000; // 缓存 50 分钟

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 首次免密登录跳转入口。
     * 访问 /autoLogin 时：固定账号登录子系统 → 种 token cookie → 重定向到目标页面。
     */
    @GetMapping("/autoLogin")
    public void autoLogin(HttpServletResponse response, HttpServletRequest request) throws Exception {
        String token = getOrCreateToken();
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"msg\":\"免密登录失败\"}");
            return;
        }

        // 种下 token cookie，后续访问子系统页面时自动携带
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, token);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 重定向到子系统页面
        response.sendRedirect(subsystemProperties.getRedirectUrl());
    }

    // 代理子系统页面(/appWebtopoPreview/**)、业务接口(/prod-api/**)及静态资源
    @RequestMapping({"/appWebtopoPreview/**", "/prod-api/**", "/assets/**", "/imgs/**", "/bjng.png"})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        // 1. 获取 token：优先用前端携带的 Cookie token，否则用服务端缓存（避免并发重复登录）
        String token = getTokenFromCookie(request);
        if (token == null || token.isEmpty()) {
            System.out.println("token为空，登录获取");

            token = getOrCreateToken();
            System.out.println("登录获取token为：token"+token);

            if (token != null && !token.isEmpty()) {
                // 种下 token cookie，后续请求自动携带
                Cookie cookie = new Cookie("Admin-Token", token);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }else{
            System.out.println("token不为空");

        }

        // 2. 构造转发 URL
        String targetUri;
        if (uri.startsWith("/appWebtopoPreview")) {
            // 去掉代理前缀，映射到子系统根路径
            targetUri = uri.replace("/appWebtopoPreview", "");
        } else {
            // /prod-api、/assets 等路径原样透传
            targetUri = uri;
        }
        String url = subsystemProperties.getBaseUrl() + targetUri;
        System.out.println("targetUri = " + targetUri);


        // getRequestURI() 不含 query string，需手动拼接（如 ?lang=ch）
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            url = url + "?" + queryString;
        }

        // 3. 构造转发请求头
        HttpHeaders headers = new HttpHeaders();
        // 透传前端 Authorization（若带了），否则用自动登录的 token
        String authHeader = request.getHeader("Authorization");
        System.out.println("token = " + token);
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
        } else if (token != null && !token.isEmpty()) {
            headers.set("Authorization", "Bearer " + token);
        }
        // 透传前端 Cookie，并补全 token cookie
        String reqCookie = request.getHeader("Cookie");
        StringBuilder cookieBuilder = new StringBuilder();
        if (reqCookie != null && !reqCookie.isEmpty()) {
            cookieBuilder.append(reqCookie);
        }
        if (token != null && !token.isEmpty() && cookieBuilder.indexOf("token=") < 0) {
            if (cookieBuilder.length() > 0) {
                cookieBuilder.append("; ");
            }
            cookieBuilder.append("Admin-Token=").append(token);
        }
        if (cookieBuilder.length() > 0) {
            headers.set("Cookie", cookieBuilder.toString());
        }

        // 4. 构造请求体并转发
        try {
            HttpEntity<byte[]> requestEntity = buildRequestEntity(request, headers);
            return restTemplate.exchange(
                    url,
                    HttpMethod.valueOf(request.getMethod()),
                    requestEntity,
                    byte[].class
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpServletResponse.SC_BAD_GATEWAY)
                    .body(("{\"success\":false,\"msg\":\"转发请求失败: " + e.getMessage() + "\"}").getBytes());
        }
    }

    /**
     * 构造转发给子系统的请求体。
     * - GET：无 body
     * - form(x-www-form-urlencoded)：转成 JSON 再转发（子系统只接受 JSON）
     * - 其他(JSON 等)：透传原始 body
     */
    private HttpEntity<byte[]> buildRequestEntity(HttpServletRequest request, HttpHeaders headers) throws Exception {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            return new HttpEntity<>(headers);
        }

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
            // form 提交：把参数转成 JSON body
            Map<String, String[]> params = request.getParameterMap();
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append('"')
                  .append(entry.getKey())
                  .append("\":\"")
                  .append(entry.getValue()[0])
                  .append('"');
            }
            sb.append('}');
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new HttpEntity<>(sb.toString().getBytes(StandardCharsets.UTF_8), headers);
        }

        // 其他格式（JSON 等）：透传原始 body
        byte[] body = request.getInputStream().readAllBytes();
        if (contentType != null && !contentType.isEmpty()) {
            headers.setContentType(MediaType.parseMediaType(contentType));
        }
        return new HttpEntity<>(body, headers);
    }

    /**
     * 从请求 Cookie 中提取 token
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Admin-Token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 获取（或创建）token：synchronized 保证并发时只登录一次，多个请求共享同一 token。
     */
    private synchronized String getOrCreateToken() {
        // 缓存有效且未过期，直接复用，避免重复登录
        if (cachedToken != null && !cachedToken.isEmpty()
                && System.currentTimeMillis() < cachedTokenExpireTime) {
            return cachedToken;
        }
        String token = loginToSubsystemWithFixedAccount();
        if (token != null && !token.isEmpty()) {
            cachedToken = token;
            cachedTokenExpireTime = System.currentTimeMillis() + TOKEN_TTL_MS;
        }
        return token;
    }

    /**
     * 使用固定账号登录子系统，返回 token
     * 子系统登录返回结构：{ "msg": "操作成功", "code": 200, "token": "..." }（token 在顶层）
     */
    private String loginToSubsystemWithFixedAccount() {
        try {
            CookieStore cookieStore = new BasicCookieStore();
            var httpClient = HttpClientBuilder.create()
                    .setDefaultCookieStore(cookieStore)
                    .build();

            HttpPost httpPost = new HttpPost(subsystemProperties.getLoginUrl());

            String jsonBody = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    subsystemProperties.getUsername(), subsystemProperties.getPassword()
            );
            // 必须给 entity 显式指定 JSON 的 Content-Type，
            // 否则 Apache HttpClient 会默认用 application/x-www-form-urlencoded
            StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            HttpResponse loginResponse = httpClient.execute(httpPost);
            int statusCode = loginResponse.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 302) {
                System.out.println("子系统登录失败，状态码: " + statusCode);
                return null;
            }

            String responseBody = EntityUtils.toString(loginResponse.getEntity(), "UTF-8");

            // 从响应体中提取顶层 token（子系统返回结构 token 在顶层）
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);
            String token = jsonNode.path("token").asText(null);
            if (token != null && !token.isEmpty()) {
                return token;
            }

            // 兼容 result.token
            JsonNode resultNode = jsonNode.path("result");
            JsonNode tokenNode = resultNode.isMissingNode() || resultNode.isNull()
                    ? jsonNode.path("token")
                    : resultNode.path("token");
            token = tokenNode.asText(null);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
