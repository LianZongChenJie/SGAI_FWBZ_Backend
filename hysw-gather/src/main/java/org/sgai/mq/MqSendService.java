package org.sgai.mq;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sgai.dto.DeviceAttributeData;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class MqSendService {

    public static final String QUEUE_DEVICE_ENERGY_GATHER = "device_energy-data_day_gather";
    public static final String QUEUE_DEVICE_LAST_GATHER_TIME = "device_last_gather_time";
    public static final String QUEUE_DEVICE_ATTRIBUTE_DATA_CHANGE = "device_attribute_data_gather";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RabbitTemplate rabbitTemplate;

    public static final MessageProperties messageProperties = MessagePropertiesBuilder.newInstance()
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .build();

    /**
     * 设备能耗数据
     *
     * @param deviceCode 设备编号
     * @param time       采集时间
     * @param value      采集值
     */
    public void sendDeviceEnergyDataGather(String deviceCode, String time, String value) {
        Map<String, String> msg = new HashMap<>();
        msg.put("deviceCode", deviceCode);
        msg.put("time", time);
        msg.put("value", value);
        String jsonString = JSONObject.toJSONString(msg);
        Message message = new Message(jsonString.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", QUEUE_DEVICE_ENERGY_GATHER, message);
    }

    /**
     * 设备最后采集时间
     *
     * @param deviceCode 设备编号
     * @param time       时间，yyyy-MM-dd HH:mm:ss
     */
    public void sendDeviceLastGatherTime(String deviceCode, String time) {
        LocalDateTime parse = LocalDateTime.parse(time, dateTimeFormatter);
        DeviceLastGatherTimeMsg msg = new DeviceLastGatherTimeMsg(deviceCode, parse, parse.plusHours(24));
        Message message = new Message(JSONObject.toJSONString(msg).getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", QUEUE_DEVICE_LAST_GATHER_TIME, message);
    }

    /**
     * 设备属性数据变更
     */
    public void sendDeviceAttributeDataChange(DeviceAttributeData msg) {
        String jsonString = JSONObject.toJSONString(msg);
        log.info("设备属性数据变更：{}", jsonString);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        Message message = new Message(jsonString.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", QUEUE_DEVICE_ATTRIBUTE_DATA_CHANGE, message);
    }
}
