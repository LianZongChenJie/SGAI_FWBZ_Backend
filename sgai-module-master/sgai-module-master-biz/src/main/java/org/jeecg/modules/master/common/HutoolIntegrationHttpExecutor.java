package org.jeecg.modules.master.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HutoolIntegrationHttpExecutor implements IntegrationHttpExecutor {

    private static final int TIMEOUT_MS = 5000;

    @Override
    public int post(String url, String token, String body) {
        if (url == null || url.isEmpty()) {
            return -1;
        }
        try (HttpResponse resp = HttpRequest.post(url)
                .header("X-Integration-Token", token == null ? "" : token)
                .header("X-Source", "sgai-master")
                .body(body == null ? "" : body)
                .timeout(TIMEOUT_MS)
                .execute()) {
            return resp.getStatus();
        } catch (Exception e) {
            return -1;
        }
    }
}
