package org.jeecg.module.third.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@Data
public class SpsSystemConfig {

    @Value("${third.sps-token-url}")
    private String spsTokenUrl;

}
