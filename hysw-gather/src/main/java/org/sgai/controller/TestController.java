package org.sgai.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import org.sgai.job.GatherJob;
import org.sgai.mq.MqSendService;
import org.sgai.util.HyswService;
import org.sgai.util.WaterMeter;
import org.sgai.util.WaterMeterLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {

    private final HyswService hyswService;

    private final GatherJob gatherJob;

    private final MqSendService mqSendService;

    @GetMapping("/waterList")
    public void waterList(){
        List<WaterMeter> waterMeters = hyswService.waterList();
        System.out.println(JSONObject.toJSONString(waterMeters));
    }

    @GetMapping("/waterMeterLog")
    public void waterMeterLog(@RequestParam String meterCode, @RequestParam String meterType,@RequestParam String startTime,@RequestParam String endTime){
        List<WaterMeterLog> log = hyswService.waterMeterLog(meterCode,meterType,startTime,endTime);
        System.out.println(JSONObject.toJSONString(log));
    }

    @GetMapping("/collectionHistoryData")
    public void collectionHistoryData(@RequestParam(required = false) String meteringCode,@RequestParam String startTime,@RequestParam String endTime){
        List<WaterMeter> waterMeters = hyswService.waterList();
        if(StrUtil.isNotEmpty(meteringCode)){
            waterMeters = waterMeters.stream().filter(waterMeter -> waterMeter.getMeterCode().equals(meteringCode)).toList();
        }
        // 获取历史数据
        for (WaterMeter waterMeter : waterMeters) {
            try {
                // 获取历史数据并按照recordTime升序排列
                List<WaterMeterLog> log = hyswService.waterMeterLog(waterMeter.getMeterCode(), waterMeter.getMeterType(), startTime, endTime);
                log.sort(Comparator.comparing(WaterMeterLog::getRecordTime));
                // 发送消息
                for(WaterMeterLog meterLog : log) {
                    mqSendService.sendDeviceEnergyDataGather(meterLog.getMeterCode(),meterLog.getRecordTime(),meterLog.getMeterReading());
                }
            }catch (Exception e){
                System.out.println("获取历史数据失败：" + waterMeter.getMeterCode());
            }
            System.out.println("获取历史数据成功：" + waterMeter.getMeterCode());
        }
    }

    @GetMapping("/job")
    public void testJob(){
        gatherJob.gather();
    }

}
