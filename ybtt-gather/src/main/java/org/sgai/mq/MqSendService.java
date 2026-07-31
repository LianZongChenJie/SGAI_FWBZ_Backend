package org.sgai.mq;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MqSendService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_DEVICE_ATTRIBUTE_DATA = "device_attribute_data_gather";

    private static final String QUEUE_DEVICE_LAST_GATHER_TIME = "device_last_gather_time";

    public static final MessageProperties messageProperties = MessagePropertiesBuilder.newInstance()
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .build();

    /**
     * 设备属性数据变更
     */
    public void sendDeviceAttributeDataChange(DeviceAttributeData msg){
        String jsonString = JSONObject.toJSONString(msg);
        Message message = new Message(jsonString.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", QUEUE_DEVICE_ATTRIBUTE_DATA, message);
    }


    public void sendDeviceLastGatherTime(String deviceId,LocalDateTime time){
        DeviceLastGatherTimeMsg msg = new DeviceLastGatherTimeMsg();
        msg.setDeviceCode(deviceId);
        msg.setTime(time);
        msg.setOffLineTime(time.plusMinutes(20));
        Message message = new Message(JSONObject.toJSONString(msg).getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", QUEUE_DEVICE_LAST_GATHER_TIME, message);
    }

}