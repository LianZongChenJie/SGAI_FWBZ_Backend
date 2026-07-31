package org.sgai.controller;

import com.alibaba.fastjson2.JSONObject;
import org.sgai.dto.XxhjDeviceInfo;
import org.sgai.job.GatherJob;
import org.sgai.mq.MqSendService;
import org.sgai.service.XxhjService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private XxhjService service;

    @Autowired
    private MqSendService mqSendService;

    @Autowired
    private GatherJob gatherJob;

    @GetMapping("/getDeviceInfoList")
    public Object getDeviceInfoList(){
        List<XxhjDeviceInfo> heat = service.getDeviceInfoList("Heat");
        System.out.println(JSONObject.toJSONString( heat));
        return heat;
    }

    @GetMapping("/getDeviceRealTimeData")
    public Object getDeviceRealTimeData(@RequestParam String deviceType){
        try {
            JSONObject data = service.getRealTimeData(deviceType);
            if(data == null){
                return "获取数据失败";
            }
            // 获取时间
            String time = data.getString("Time");
            JSONObject jsonObject = data.getJSONObject(deviceType);
            if(jsonObject == null){
                return "设备类型数据为空";
            }
            String format = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).withMinute(0).withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                String string = JSONObject.parseObject(value).getString("ConsTotal");
                mqSendService.sendDeviceEnergyData(key,string,format);
            }
            return "数据处理成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "处理数据时发生错误: " + e.getMessage();
        }
    }

    @GetMapping("/gatherJob")
    public Object gatherJob(){
        gatherJob.gatherJob();
        return "数据处理成功";
    }

    @GetMapping("/gatherLastTimeJob")
    public Object gatherLastTimeJob(){
        gatherJob.gatherLastTimeJob();
        return "数据处理成功";
    }
}