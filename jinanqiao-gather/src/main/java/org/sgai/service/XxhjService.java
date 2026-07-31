package org.sgai.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.sgai.dto.XxhjDeviceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小溪汇聚
 */
@Service
@Data
public class XxhjService {

    Log log = LogFactory.get();

    @Value("${xxhj.host}")
    private String host;

    @Value("${xxhj.username}")
    private String username;
    @Value("${xxhj.password}")
    private String password;
    // 示例：adminxxx000admin12341110165000
    @Value("${xxhj.moduleId}")
    private String moduleId;

    @Value("${xxhj.sKey}")
    private String sKey;
    @Value("${xxhj.ivParameter}")
    private String ivParameter;

    private static final String getList_url = "/xxhjapi/device/getList";

    /**
     * 获取设备信息列表
     * @param deviceType 设备类型
     * @return 设备信息列表
     */
    public List<XxhjDeviceInfo> getDeviceInfoList(String deviceType){
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("moduleId", moduleId);
            params.put("deviceType", deviceType);
            String body = HttpUtil.createPost(host + getList_url)
                    .header("Authorization", getAuthorization())
                    .body(encodeBody(params), ContentType.JSON.toString())
                    .execute()
                    .body();
            log.info("获取设备信息列表：{}", body);
            return JSONObject.parseObject(body).getJSONArray("info").toJavaList(XxhjDeviceInfo.class);
        }catch (Exception e){
            log.error("获取设备信息列表异常：{}",e.getMessage());
        }
        return null;
    }

    /**
     * 获取实时数据
     * @param deviceType 设备类型
     * @return 实时数据
     */
    public JSONObject getRealTimeData(String deviceType){
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("moduleId", moduleId);
            params.put("deviceType", deviceType);
            String body = HttpUtil.createPost(host + "/xxhjapi/energy/realTimeData/all")
                    .header("Authorization", getAuthorization())
                    .body(encodeBody(params), ContentType.JSON.toString())
                    .execute()
                    .body();
            log.info("获取实时数据：{}", body);
            return JSONObject.parseObject(body).getJSONObject("info");
        }catch (Exception e){
            log.error("获取实时数据异常：{}",e.getMessage());
        }
        return null;
    }

    private String encodeBody(Map<String,Object> params) throws Exception {
        String str = JSONObject.toJSONString(params);
        Map<String,String> body = new HashMap<>();
        body.put("param",encrypt(str, "utf-8", sKey, ivParameter));
        return JSONObject.toJSONString(body);
    }


    /**
     * 获取授权信息
     */
    private String getAuthorization(){
        String str = this.username + "xxhj000" + this.password + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        return Base64.encode(str.getBytes());
    }

    public static String encrypt(String sSrc, String encodingFormat,
                                 String sKey,String ivParameter) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes(encodingFormat));
        return Base64.encode(encrypted);//此处使用BASE64做转码。
    }

}
