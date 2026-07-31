package org.sgai.mq;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Data
@Slf4j
public class QueueListener {

    private final MqSendService mqSendService;

    public static final String QUEUE_NAME = "flow_meter_raw_process";

    @Value("${flow.attribute}")
    private String[] attributeKey;

    @RabbitListener(queues = QUEUE_NAME, ackMode = "AUTO")
    public void listener(Message message){
        String body = new String(message.getBody());
        try{
            log.info("mq消息消费成功。queue:{},message:{}", QUEUE_NAME, body);
            DeviceAttributeData data = JSONObject.parseObject(body, DeviceAttributeData.class);
            Map<String,BigDecimal> attributeData = data.getData()
                    .stream()
                    .collect(Collectors.toMap(AttributeData::getUniqueKey, AttributeData::getValue));
            List<AttributeData> list = new ArrayList<>();
            for(String key : attributeKey){
                BigDecimal i = attributeData.get(key);
                BigDecimal d = attributeData.get(key + "-D");
                if(i != null || d != null){
                    i = i == null ? BigDecimal.ZERO : i;
                    d = d == null ? BigDecimal.ZERO : d;
                    list.add(new AttributeData(key, i.add(d).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            data.setData(list);
            data.setEquipmentCode("flow_" + data.getEquipmentCode());
            mqSendService.sendDeviceAttributeDataChange(data);
            mqSendService.sendDeviceLastGatherTime(data.getEquipmentCode(),data.getTimestamp());
        }catch (Exception e){
            log.error("mq消息消费失败。queue:{},message:{}", QUEUE_NAME, body, e);
        }
    }
}
