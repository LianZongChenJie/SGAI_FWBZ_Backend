package org.jeecg.module.third.service;

import cn.hutool.core.util.StrUtil;
import org.jeecg.module.third.util.SM4Utils;
import org.springframework.stereotype.Service;

/**
 * 会议系统登录
 */
@Service
public class MeetingSystemService {

    public String getToken(String username){
        if(StrUtil.isEmpty(username)){
            username = "ceshi";
        }
        String str = "username=" + username + "&time="+System.currentTimeMillis();
        return SM4Utils.sm4EncryptHex(str);
    }

}
