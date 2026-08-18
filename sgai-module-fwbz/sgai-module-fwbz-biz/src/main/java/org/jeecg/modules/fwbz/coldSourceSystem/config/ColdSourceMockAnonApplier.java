package org.jeecg.modules.fwbz.coldSourceSystem.config;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.config.shiro.ignore.InMemoryIgnoreAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 【模拟模式】将冷源实时数据 WebSocket 端点加入 token 免认证放行列表。
 *
 * <p>JEECG 的 Shiro/JWT 过滤器（JwtFilter）会拦截 WebSocket 握手请求（浏览器无法在
 * WS 握手时携带 X-Access-Token 请求头，仅能从 URL query 取 token），导致连接返回
 * "身份认证失败"。本类在 mock 模式下把 /fwbz/coldSource/ws 追加到 InMemoryIgnoreAuth，
 * 与真实链路无关、不受 nacos 配置覆盖影响，专用于无冷源网络的全链路联调测试。
 *
 * <p>仅 mock=true 时生效；正式接入真实冷源（mock=false）时不放行，保持原有安全策略。
 * 临时文件：联调结束后删除。
 */
@Slf4j
@Component
public class ColdSourceMockAnonApplier implements ApplicationRunner {

    /** 与 {@code ColdSourceWsEndpoint} 的 @ServerEndpoint 路径保持一致 */
    private static final String WS_PATH = "/fwbz/coldSource/ws";

    @Autowired
    private ColdSourceProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isMock()) {
            return;
        }
        List<String> ignoreList = new ArrayList<>(InMemoryIgnoreAuth.get());
        if (!ignoreList.contains(WS_PATH)) {
            ignoreList.add(WS_PATH);
            InMemoryIgnoreAuth.set(ignoreList);
        }
        log.warn("【模拟模式】已将 {} 加入 token 免认证放行列表（仅 mock 联调生效）", WS_PATH);
    }
}
