package org.jeecg.module.third.service;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.module.third.config.SpsSystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 安防系统登录
 */
@Service
@Slf4j
@Data
public class SpsSystemService {

    @Autowired
    private SpsSystemConfig config;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 将登陆成功的信息发送到安防系统
     *
     * @param sysUser
     * @param username
     * @param token
     */
    public void asyncPostUserLoginInfoToSpsSystem(LoginUser sysUser, String username, String token) {
        // 使用 rest 请求 请求其他系统的 token
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // url从配置文件读取（this.spsTokenUrl必须是成员变量）
                String url = config.getSpsTokenUrl();
                if (url == null || url.trim().isEmpty()) {
                    log.warn("未配置安防系统token URL, 已跳过安防系统同步！");
                    return; // 没有配置时直接跳过
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> params = new HashMap<>();
                params.put("userName", username);
                params.put("realName", sysUser.getRealname());
                params.put("token", token);
                params.put("expiresIn", JwtUtil.EXPIRE_TIME);
                params.put("userId", sysUser.getId());

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
                JSONObject jsonObject = restTemplate.postForObject(url, entity, JSONObject.class);
                log.info("\n登陆 向安防系统地址:[{}] \n 发送的数据：[{}]", url, params);
                log.warn("登陆 获取到的安防系统的 token 返回值是：[{}]\n", jsonObject);
            } catch (Exception ex) {
                log.error("登陆 异步获取安防系统token异常", ex);
            }
        });
    }

}
