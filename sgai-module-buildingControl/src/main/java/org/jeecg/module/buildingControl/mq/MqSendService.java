package org.jeecg.module.buildingControl.mq;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.module.buildingControl.dto.BcDataMsg;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class MqSendService {

    private final RabbitTemplate rabbitTemplate;

    public static final MessageProperties messageProperties = MessagePropertiesBuilder.newInstance()
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .build();

    public void sendMsg(String path, String value, LocalDateTime time){

        BcDataMsg message = new BcDataMsg();
        message.setKey(path);
        message.setValue(value);
        message.setTime(time);
        rabbitTemplate.send("",BuildingControlQueueConfig.BC_DATA,new Message(JSONObject.toJSONString(message).getBytes(),messageProperties));

    }

}
