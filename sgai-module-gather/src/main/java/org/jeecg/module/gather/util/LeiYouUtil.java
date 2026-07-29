package org.jeecg.module.gather.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.module.gather.dto.DeviceCommStatus;
import org.jeecg.module.gather.dto.DeviceData;
import org.jeecg.module.gather.dto.PointData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 雷优科技相关api
 */
@Slf4j
public class LeiYouUtil {
    /**
     * 获取token认证信息
     */
    public static String getToken(String host,String clientId,String clientSecret){
        try {
            String url = host + ":8004/token";
            Map<String, Object> params = new HashMap<>();
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("grant_type", "client_credentials");
            HashMap<String, String> header = new HashMap<>();
            header.put("Content-Type", "application/x-www-form-urlencoded");
            String result = HttpUtil.createPost(url)
                    .addHeaders(header)
                    .form(params)
                    .execute().body();
            log.info("host:{},leiyou-getToken:{}", host,result);
            return JSONObject.parseObject(result).getString("access_token");
        }catch (Exception e){
            log.error("获取token失败。host:{}",host,e);
            return "";
        }
    }

    /**
     * 获取设备最新采集值
     * @param deviceCodeList 设备编号列表
     */
    public static List<DeviceData> getDeviceData(String host,String token,Collection<String> deviceCodeList) {
        if(CollectionUtil.isEmpty(deviceCodeList)){
            return Collections.emptyList();
        }
        List<List<String>> split = ListUtil.split(deviceCodeList.stream().toList(), 50);
        List<DeviceData> result = new ArrayList<>();
        for (List<String> list : split){
            List<DeviceData> res = getDeviceData1(host, token, list);
            if(CollectionUtil.isNotEmpty(res)){
                result.addAll(res);
            }
        }
        return result;
    }

    private static List<DeviceData> getDeviceData1(String host,String token,List<String> deviceCodeList){
        try {
            String url = host + ":8005/api/v1/device/functions";
            Map<String, String> headers = new HashMap<>();
            headers.put("token", token);
            Map<String, Object> params = new HashMap<>();
            params.put("deviceIds", String.join(",", deviceCodeList));
            String result = HttpUtil.createGet(url).addHeaders(headers).form(params).execute().body();
            log.info("host:{},leiyou-device/functions:{}",host, result);
            return JSON.parseObject(result).getJSONArray("data").toJavaList(DeviceData.class);
        }catch (Exception e){
            log.error("获取设备最新采集值失败。host:{}",host,e);
            return Collections.emptyList();
        }
    }

    public static List<DeviceCommStatus> getOnlineData(String host,String token,Collection<String> deviceCodeList) {
        if(CollectionUtil.isEmpty(deviceCodeList)){
            return Collections.emptyList();
        }
        // 拆分deviceCodeList为50个一组
        List<List<String>> split = ListUtil.split(deviceCodeList.stream().toList(), 50);
        List<DeviceCommStatus> result = new ArrayList<>();
        for(List<String> list:split){
            List<DeviceCommStatus> res = getOnlineData1(host, token, list);
            if(CollectionUtil.isEmpty( res)){
                continue;
            }
            result.addAll(res);
        }
        return result;
    }

    private static List<DeviceCommStatus> getOnlineData1(String host,String token,Collection<String> deviceCodeList){
        try {
            String url = host + ":8005/api/v1/device/comm-status";
            Map<String, String> headers = new HashMap<>();
            headers.put("token", token);
            Map<String, Object> params = new HashMap<>();
            params.put("deviceIds", String.join(",", deviceCodeList));
            String result = HttpUtil.createGet(url).addHeaders(headers).form(params).execute().body();
            log.info("host:{},leiyou-/device/comm-status:{}",host, result);
            return JSON.parseObject(result).getJSONArray("data").toJavaList(DeviceCommStatus.class);
        }catch (Exception e){
            log.error("获取设备运行状态失败。host:{}",host,e);
            return Collections.emptyList();
        }
    }


    public static List<PointData> getHistoryDeviceData(String host,
                                                       String token,
                                                       String deviceCode,
                                                       String identifiers,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime){
        try {
            String url = host + ":8005/api/v1/device/function/history-data";
            Map<String, String> headers = new HashMap<>();
            headers.put("token", token);
            Map<String, Object> params = new HashMap<>();
            params.put("deviceId", deviceCode);
            params.put("identifiers", identifiers);
            params.put("startTime", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            params.put("endTime", endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            String result = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .form(params)
                    .execute().body();
            return JSONObject.parseObject(result)
                    .getJSONObject("data")
                    .getJSONObject("values")
                    .getJSONArray(identifiers)
                    .toJavaList(PointData.class);
        }catch (Exception e){
            log.error("获取设备历史数据失败。host:{},deviceCode:{}",host,deviceCode,e);
            return Collections.emptyList();
        }
    }

}
