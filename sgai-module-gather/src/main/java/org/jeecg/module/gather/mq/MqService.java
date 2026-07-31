package org.jeecg.module.gather.mq;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class MqService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 设备运行状态
     * @param deviceCode 设备编号
     * @param status 设备状态。在线：1；离线：0
     */
    public void sendDeviceRunStatus(String deviceCode,Integer status){
        // 消息发送逻辑
        log.info("设备运行状态。deviceCode: {},status：{}",deviceCode,status);
        Map<String,Object> msg = new HashMap<>();
        msg.put("deviceCode",deviceCode);
        msg.put("status",status);
        rabbitTemplate.convertAndSend("", MqConstant.QUEUE_DEVICE_RUN_STATUS_CHANGE,msg,item -> item);
    }

    /**
     * 设备能耗数据
     * @param deviceCode 设备编号
     * @param time 采集时间
     * @param value 值
     */
    public void sendDeviceEnergyData(String deviceCode, LocalDateTime time, BigDecimal value){
        // 消息发送逻辑
        log.info("设备能耗数据。deviceCode：{},time：{},value：{}",deviceCode,time,value);
        Map<String,Object> msg = new HashMap<>();
        msg.put("deviceCode",deviceCode);
        msg.put("time",time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        msg.put("value",value);
        rabbitTemplate.convertAndSend("", MqConstant.QUEUE_DEVICE_ENERGY_DATA_GATHER,msg,item -> item);
    }
}
