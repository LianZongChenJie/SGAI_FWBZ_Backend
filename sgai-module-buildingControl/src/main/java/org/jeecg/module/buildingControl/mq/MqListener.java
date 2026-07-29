package org.jeecg.module.buildingControl.mq;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.annotation.RabbitComponent;
import org.jeecg.module.buildingControl.dto.BcControlMsg;
import org.jeecg.module.buildingControl.service.EnteliWebService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RabbitComponent(value = "bc_control_listener")
@Slf4j
@AllArgsConstructor
public class MqListener {

    private final EnteliWebService enteliWebService;

    @RabbitListener(queues = BuildingControlQueueConfig.BC_CONTROL, ackMode = "AUTO")
    public void listener(Message message){
        String body = new String(message.getBody());
        try {
            log.info("收到消息：{}", body);
            BcControlMsg data = JSONObject.parseObject(body, BcControlMsg.class);
            if(StringUtils.isEmpty(data.getPath()) || StringUtils.isEmpty(data.getValue())){
                log.error("mq消息消费失败。queue:{}，message:{}", BuildingControlQueueConfig.BC_CONTROL, body);
                return;
            }
            enteliWebService.setProperty(data.getPath(), data.getValue());
        }catch (Exception e){
            log.error("mq消息消费失败。queue:{}，message:{}", BuildingControlQueueConfig.BC_CONTROL, body, e);
        }
    }
}
