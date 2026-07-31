package org.sgai.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sgai.dto.YbttDto;
import org.sgai.util.RSAUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Data
@Slf4j
public class YbttService {

    @Value("${ybtt.token}")
    private String token;
    @Value("${ybtt.private_key}")
    private String privateKey;
    @Value("${ybtt.public_key}")
    private String publicKey;
    @Value("${ybtt.hostPath}")
    private String hostPath;
    @Value("${ybtt.deviceNums}")
    private String[] deviceNums;


    public List<YbttDto> queryDeviceDetail(){
        if(deviceNums == null || deviceNums.length == 0){
            return Collections.emptyList();
        }
        List<YbttDto> result = new ArrayList<>();
        for(int i = 0; i < deviceNums.length; i++){
            YbttDto ybttDto = queryDeviceDetail(deviceNums[i]);
            if(ybttDto != null){
                result.add(ybttDto);
            }
        }
        return result;
    }

    public YbttDto queryDeviceDetail(String deviceNum) {
        try {
            if (StrUtil.isEmpty(deviceNum)) {
                return null;
            }
            Map<String, String> params = new HashMap<>();
            params.put("deviceNum", deviceNum);
            params.put("token", this.token);
            String body = HttpUtil.createPost(hostPath)
                    .body(JSONObject.toJSONString(params), ContentType.JSON.toString())
                    .execute()
                    .body();
            // 获取响应状态
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONObject data = jsonObject.getJSONObject("data");
            if (data == null) {
                return null;
            }
            String encryptData = data.getString("encryptData");
            String deviceAttribute = RSAUtil.decryptByPrivateKey(encryptData, this.privateKey);
            return JSONObject.parseObject(deviceAttribute, YbttDto.class);
        } catch (Exception e) {
            log.error("获取设备信息异常：设备编号：{}，{}", deviceNum, e.getMessage());
            return null;
        }
    }

}
