package org.jeecg.modules.fwbz.coldSourceSystem.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 冷源实时数据 WebSocket 端点注册
 *
 * @ServerEndpoint 端点需要 ServerEndpointExporter 扫描注册；若 sgai-boot-base-core
 * 已注册同名 bean 则跳过（@ConditionalOnMissingBean），避免 bean 冲突。
 */
@Configuration
public class ColdSourceWsConfig {

    @Bean
    @ConditionalOnMissingBean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
