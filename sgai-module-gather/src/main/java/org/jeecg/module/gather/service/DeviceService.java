package org.jeecg.module.gather.service;

import lombok.AllArgsConstructor;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.fwbz.api.FwbzDeviceApi;
import org.jeecg.modules.fwbz.entity.DeviceEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DeviceService {

    private final FwbzDeviceApi fwbzDeviceApi;

    /**
     * 获取设备信息
     */
    public List<DeviceEntity> findDevices(){
        //1.设置线程会话Token
        UserTokenContext.setToken(getTemporaryToken());
        List<DeviceEntity> deviceEntities = fwbzDeviceApi.deviceList();
        UserTokenContext.remove();
        return deviceEntities;

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
        String token = JwtUtil.sign("meter_gather", "47ddac622ba469ced43cf72fefc4cf2c");
        // 设置Token缓存有效时间为 5 分钟
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 5 * 60 * 1000);
        return token;
    }

}
