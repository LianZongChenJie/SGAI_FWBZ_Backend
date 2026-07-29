package org.jeecg.module.third.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
public class InvestmentPromotionConfig {
    @Value("${third.invest-token.url}")
    private String investUrl;

    @Value("${third.invest-token.username}")
    private String userName;
    @Value("${third.invest-token.password}")
    private String password;
    @Value("${third.invest-token.to-url}")
    private String toUrl = "http://10.77.16.5:83/index.html?Auth-Token={token}&name=admin#/home";

}
