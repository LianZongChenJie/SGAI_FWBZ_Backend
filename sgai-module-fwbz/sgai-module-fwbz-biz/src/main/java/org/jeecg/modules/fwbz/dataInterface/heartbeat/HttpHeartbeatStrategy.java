package org.jeecg.modules.fwbz.dataInterface.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP API 心跳检测策略
 * <p>
 * 发送 HTTP GET 请求，根据响应状态码判断在线/离线/异常
 */
@Slf4j
@Component
public class HttpHeartbeatStrategy implements HeartbeatStrategy {

    private final RestTemplate restTemplate;

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    public HttpHeartbeatStrategy() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public HeartbeatResult check(String url) {
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            long elapsed = System.currentTimeMillis() - startTime;

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("HTTP心跳在线 - URL: {}, 耗时: {}ms", url, elapsed);
                return HeartbeatResult.online(elapsed);
            } else {
                log.warn("HTTP心跳异常状态码 - URL: {}, 状态码: {}, 耗时: {}ms",
                        url, response.getStatusCodeValue(), elapsed);
                return HeartbeatResult.abnormal(elapsed);
            }
        } catch (ResourceAccessException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (e.getCause() instanceof java.net.SocketTimeoutException) {
                log.warn("HTTP心跳超时 - URL: {}, 耗时: {}ms", url, elapsed);
                return HeartbeatResult.abnormal(elapsed);
            }
            log.warn("HTTP心跳连接失败 - URL: {}, 耗时: {}ms, 原因: {}", url, elapsed, e.getMessage());
            return HeartbeatResult.offline(elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("HTTP心跳异常 - URL: {}, 耗时: {}ms, 原因: {}", url, elapsed, e.getMessage());
            return HeartbeatResult.abnormal(elapsed);
        }
    }
}
