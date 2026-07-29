package org.jeecg.module.buildingControl.service;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.fwbz.api.FwbzDeviceApi;
import org.jeecg.modules.fwbz.entity.DeviceAttributeEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Data
@AllArgsConstructor
@Slf4j
public class DeviceAttributeService {

    private final FwbzDeviceApi fwbzDeviceApi;

    public List<String> getAcquisitionCoding(){

        List<DeviceAttributeEntity> attributes;
        try {
            UserTokenContext.setToken(getTemporaryToken());
            attributes = fwbzDeviceApi.deviceAttributeList();
            UserTokenContext.remove();
        } catch (Exception e) {
            log.error("获取设备属性列表失败", e);
            return Collections.emptyList();
        }
        if(CollectionUtil.isEmpty(attributes)){
            log.warn("设备属性列表为空");
        }

        return attributes.stream()
                .filter(item -> StringUtils.isNotEmpty(item.getAcquisitionCoding()))
                .map(DeviceAttributeEntity::getAcquisitionCoding)
                .distinct()
                .toList();

    }

    /**
     * 获取临时令牌
     *
     * 模拟登陆接口，获取模拟 Token
     * @return
     */
    private String getTemporaryToken() {
        RedisUtil redisUtil = SpringContextUtils.getBean(RedisUtil.class);
        //模拟登录生成临时Token
        //参数说明：第一个参数是用户名、第二个参数是密码的加密串
        String token = JwtUtil.sign("building_control_gather", "9d11aac000bf73b5e791b329d1f3250dbe7be70392bba5d4");
        // 设置Token缓存有效时间为 5 分钟
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 5 * 60 * 1000);
        return token;
    }
}
