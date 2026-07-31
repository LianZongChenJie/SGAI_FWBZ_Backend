package org.sgai.job;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.sgai.mq.MqSendService;
import org.sgai.service.XxhjService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Data
public class GatherJob {

    Log log = LogFactory.get();

    @Value("#{'${xxhj.deviceTypes}'.split(',')}")
    private List<String> deviceTypes;

    @Autowired
    private XxhjService service;

    @Autowired
    private MqSendService mqSendService;

    @Scheduled(cron = "0 10 * * * ? ")
    public void gatherJob(){
        log.info("表底数据job,start");
        for(String deviceType:deviceTypes){
            gatherData(deviceType);
        }
        log.info("表底数据job,end");
    }

    @Scheduled(cron = "0 0/15 * * * ? ")
    public void gatherLastTimeJob(){
        log.info("处理最后采集时间job,start");
        for(String deviceType : deviceTypes){
            gatherLastTimeJob(deviceType);
        }
        log.info("处理最后采集时间job,end");
    }

    private void gatherLastTimeJob(String deviceType){
        try {

            JSONObject data = service.getRealTimeData(deviceType);
            if (data == null) {
                log.error("获取数据失败");
                return;
            }
            String time = data.getString("Time");
            JSONObject jsonObject = data.getJSONObject(deviceType);
            if (jsonObject == null) {
                log.error("设备类型数据为空");
                return;
            }
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                // 最后采集时间消息
                mqSendService.sendDeviceLastGatherTime(key, time);
            }
        }catch (Exception e){
            log.error("处理采集时间数据错误：" + e.getMessage());
        }

    }

    private void gatherData(String deviceType){
        try {
            JSONObject data = service.getRealTimeData(deviceType);
            if(data == null){
                log.error("获取数据失败");
                return;
            }
            // 获取时间
            String time = data.getString("Time");
            JSONObject jsonObject = data.getJSONObject(deviceType);
            if(jsonObject == null){
                log.error("设备类型数据为空");
                return;
            }
            String format = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).withMinute(0).withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                String string = JSONObject.parseObject(value).getString("ConsTotal");
                mqSendService.sendDeviceEnergyData(key,string,format);
            }
            log.info("表底数据处理成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("处理表底数据时发生错误: " + e.getMessage());
        }
    }

}