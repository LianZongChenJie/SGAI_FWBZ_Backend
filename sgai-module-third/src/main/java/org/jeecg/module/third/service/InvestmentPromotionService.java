package org.jeecg.module.third.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.module.third.config.InvestmentPromotionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 招商
 */
@Service
public class InvestmentPromotionService {
    @Autowired
    private InvestmentPromotionConfig config;
    @Autowired
    private RestTemplate restTemplate;

    public String getInvestToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        formData.add("userName", Base64.encode(sysUser.getUsername().getBytes()));
        formData.add("password", Base64.encode(config.getPassword().getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

        Map<String, Object> result = restTemplate.postForObject(config.getInvestUrl(), entity, Map.class);
        if (result != null && Integer.valueOf(1001).equals(result.get("code"))) {
            return (String) result.get("msg");
        }
        return null;
    }

    public String getInvestToUrl(String token){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String,String> map = new HashMap<>();
        map.put("token",token);
        map.put("username",sysUser.getUsername());
        return StrUtil.format(config.getToUrl(), map);
    }
}
