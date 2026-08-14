package org.jeecg.modules.fwbz.lighting;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/autoLogin")
public class AutoLoginController {

    // 子系统登录地址
    private static final String SUB_SYSTEM_LOGIN_URL = "http://10.61.13.140:888/prod-api/login";

    // 固定账号（免密登录）
    private static final String USERNAME = "user001";
    private static final String PASSWORD = "123456";

    // 登录成功后跳转的子系统页面（相对路径，不写死主机地址）
    private static final String REDIRECT_URL = "/appWebtopoPreview";

    // cookie 键名（与 SubsystemProxyController 保持一致）
    private static final String TOKEN_COOKIE_NAME = "Admin-Token";

    /**
     * 首次免密登录跳转入口。
     * 访问 /autoLogin 时：固定账号登录子系统 → 种 token cookie → 重定向到目标页面。
     */
    @GetMapping
    public void autoLogin(HttpServletResponse response, HttpServletRequest request) throws Exception {
        String token = loginToSubsystem();
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
        response.sendRedirect(request.getContextPath()+REDIRECT_URL);
    }

    /**
     * 使用固定账号登录子系统，返回 token。
     * 子系统登录返回结构：{ "msg": "操作成功", "code": 200, "token": "..." }（token 在顶层）
     */
    private String loginToSubsystem() throws Exception {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(SUB_SYSTEM_LOGIN_URL);

            String jsonBody = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    USERNAME, PASSWORD
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
            return tokenNode.asText(null);
        }
    }
}
