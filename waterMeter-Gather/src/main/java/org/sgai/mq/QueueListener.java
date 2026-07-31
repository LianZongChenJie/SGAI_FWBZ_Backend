package org.sgai.mq;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sgai.dto.AttributeData;
import org.sgai.dto.DeviceAttributeData;
import org.sgai.service.MqSendService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class QueueListener {

    @Value("${water.meterData.key}")
    private String attributeKey;

    private final MqSendService mqSendService;

    public static final String QUEUE_NAME = "water_meter_raw_process";

    @RabbitListener(queues = QUEUE_NAME, ackMode = "AUTO")
    public void listener(Message message){
        String body = new String(message.getBody());
        try{
            DeviceAttributeData data = JSONObject.parseObject(body, DeviceAttributeData.class);
            for(AttributeData item : data.getData()){
                if(StrUtil.isEmpty(item.getUniqueKey())){
                    continue;
                }
                if(this.attributeKey.equals(item.getUniqueKey())){
                    mqSendService.sendDeviceEnergyDataGather(data.getEquipmentCode(),data.getTimestamp(),item.getValue());
                }
            }
            mqSendService.sendDeviceAttributeDataChange(data);
            mqSendService.sendDeviceLastGatherTime(data.getEquipmentCode(),data.getTimestamp());
        }catch (Exception e){
            log.error("mq消息消费失败。queue:{},message:{}", QUEUE_NAME, body, e);
        }
    }
}
