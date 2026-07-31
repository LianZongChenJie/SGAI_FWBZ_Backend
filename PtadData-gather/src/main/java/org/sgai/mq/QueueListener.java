package org.sgai.mq;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sgai.constant.MqConstant;
import org.sgai.dto.AttributeData;
import org.sgai.dto.DeviceAttributeData;
import org.sgai.service.MqSendService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@Slf4j
public class QueueListener {

    @Value("${ptad.meterData.key}")
    private String attributeKey;

    private final MqSendService mqSendService;

    @RabbitListener(queues = MqConstant.QUEUE_NAME, ackMode = "AUTO")
    public void listener(Message message){
        String body = new String(message.getBody());
        try {
            List<DeviceAttributeData> list = JSONArray.parseArray(body, DeviceAttributeData.class);
            for(DeviceAttributeData data : list){
                List<AttributeData> item = data.getData();
                for(AttributeData attributeData : item){
                    if(StrUtil.isEmpty(attributeData.getUniqueKey())){
                        continue;
                    }
                    if(this.attributeKey.equals(attributeData.getUniqueKey())){
                        mqSendService.sendDeviceEnergyDataGather(data.getEquipmentCode(),data.getTimestamp(),attributeData.getValue());
                    }
                }
                mqSendService.sendDeviceAttributeDataChange(data);
                // 发送采集时间更新消息
                mqSendService.sendDeviceLastGatherTime(data.getEquipmentCode(),data.getTimestamp());
            }
        } catch (Exception e) {
            log.error("mq消息消费失败。queue:{},message:{}",MqConstant.QUEUE_NAME, body, e);
        }
    }
}
