package org.sgai.mq;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson2.JSONObject;
import org.sgai.dto.DeviceLastGatherTimeMsg;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class MqSendService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_DEVICE_ENERGY_DATA = "device_energy-data_gather";

    private static final String QUEUE_DEVICE_LAST_GATHER_TIME = "device_last_gather_time";

    public static final MessageProperties messageProperties = MessagePropertiesBuilder.newInstance()
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .build();

    public void sendDeviceEnergyData(String deviceId,String value,String time){
        Map<String,String> msg = new HashMap<>();
        msg.put("deviceCode",deviceId);
        msg.put("time", time);
        msg.put("value",value);
        String jsonString = JSONObject.toJSONString(msg);
        Message message = new Message(jsonString.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", QUEUE_DEVICE_ENERGY_DATA, message);
    }

    public void sendDeviceLastGatherTime(String deviceId,String timeStr){
        LocalDateTime time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        DeviceLastGatherTimeMsg msg = new DeviceLastGatherTimeMsg();
        msg.setDeviceCode(deviceId);
        msg.setTime(time);
        msg.setOffLineTime(time.plusMinutes(20));
        Message message = new Message(JSONObject.toJSONString(msg).getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", QUEUE_DEVICE_LAST_GATHER_TIME, message);
    }

}