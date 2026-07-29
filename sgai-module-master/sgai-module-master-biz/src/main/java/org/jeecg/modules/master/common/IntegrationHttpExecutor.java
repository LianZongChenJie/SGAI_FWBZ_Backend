package org.jeecg.modules.master.common;

/** 推送 HTTP 端口（抽象以便测试 mock，生产用 hutool）。返回 HTTP 状态码；异常/超时返回 -1。 */
public interface IntegrationHttpExecutor {
    int post(String url, String token, String body);
}
