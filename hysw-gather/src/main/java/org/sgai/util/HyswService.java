package org.sgai.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 慧怡水务工具类
 */
@Component
@Data
public class HyswService {

    @Value("${hysw.appId}")
    private String appId;
    @Value("${hysw.appSecret}")
    private String appSecret;
    @Value("${hysw.host}")
    private String host;

    final TimedCache<String, String> timedCache = CacheUtil.newTimedCache(5L * 59L * 60L * 1000L);

    public HyswService() {
        // 启动定时清理任务，每分钟清理一次过期缓存
        timedCache.schedulePrune(60 * 1000L);
    }


    /**
     * 获取token
     */
    private static final String token_url = "/auth/oauth2/token?username=%s&grant_type=password&scope=server";
    /**
     * 查询水表设备信息
     */
    private static final String water_meter_list_url = "/open/api/waterMeter/findWaterMeterListByCondition";
    /**
     * 查询水表抄表记录
     */
    private static final String water_meter_log = "/open/api/waterMeter/findWaterMeterLog";


    /**
     * 获取access_token
     * @return token
     */
    private String getToken(){
        String token = timedCache.get("token");
        if(StrUtil.isNotEmpty(token)){
            return token;
        }
        String body = HttpUtil.createPost(String.format(host + token_url,appId))
                .addHeaders(tokenHeaders())
                .body("password=" + appSecret, ContentType.FORM_URLENCODED.toString())
                .execute().body();
        String accessToken = JSONObject.parseObject(body).getString("access_token");
        timedCache.put("token",accessToken);
        return accessToken;
    }

    public List<WaterMeter> waterList(){
        String token = getToken();
        Map<String,String> params = new HashMap<>();
        params.put("appId",appId);
        params.put("appSecret",appSecret);
        params.put("accessToken",token);
        String body = HttpUtil.createGet(host + water_meter_list_url + "?" + HttpUtil.toParams(params))
                .addHeaders(apiHeaders(token))
                .execute().body();
        return JSONObject.parseObject(body).getJSONArray("data").toList(WaterMeter.class);
    }

    /**
     * 水表抄表记录
     * @param meterCode 水表编号
     * @param meterType 水表类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    public List<WaterMeterLog> waterMeterLog(String meterCode, String meterType, String startTime, String endTime){
        String token = getToken();
        Map<String,String> params = new HashMap<>();
        params.put("meterCode",meterCode);
        params.put("meterType",meterType);
        params.put("startTime",startTime);
        params.put("endTime",endTime);
        String body = HttpUtil.createGet(host + water_meter_log + "?" + HttpUtil.toParams(params))
                .addHeaders(apiHeaders(token))
                .execute().body();
        return JSONObject.parseObject(body).getJSONArray("data").toList(WaterMeterLog.class);
    }

    private Map<String,String> apiHeaders(String token){
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","Bearer " + token);
        headers.put("TENANT-ID",appId);
        headers.put("CLIENT-OPEN","Y");
        return headers;
    }

    private Map<String,String> tokenHeaders(){
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","Basic b3BlbjpvcGVu");
        headers.put("TENANT-ID",appId);
        headers.put("CLIENT-OPEN","Y");
        return headers;
    }
}
