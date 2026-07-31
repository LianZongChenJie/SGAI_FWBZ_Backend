package org.sgai.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.sgai.constant.MqConstant;
import org.sgai.dto.DeviceAttributeData;
import org.sgai.dto.DeviceLastGatherTimeMsg;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Data
public class MqSendService {

    private final RabbitTemplate rabbitTemplate;

    private static final long CACHE_TIME = 1000L * 100L;

    @Value("${water.off-line-interval}")
    private Long offLineInterval;

    final TimedCache<String, String> timedCache = CacheUtil.newTimedCache(CACHE_TIME);

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        Message message = new Message(jsonString.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", MqConstant.QUEUE_DEVICE_ENERGY_GATHER, message);
    }

    /**
     * 设备属性数据变更
     */
    public void sendDeviceAttributeDataChange(DeviceAttributeData msg) {
        String jsonString = JSONObject.toJSONString(msg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        Message message = new Message(jsonString.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", MqConstant.QUEUE_DEVICE_ATTRIBUTE_DATA_CHANGE, message);
    }

    /**
     * 设备最后采集时间
     *
     * @param deviceCode 设备编号
     * @param time       时间，yyyy-MM-dd HH:mm:ss
     */
    public void sendDeviceLastGatherTime(String deviceCode, String time) {
        LocalDateTime parse = LocalDateTime.parse(time, dateTimeFormatter);
        String key = deviceCode + parse.getMinute();
        // 判断是否有缓存，有缓存就不发送
        String lock = timedCache.get(key);
        if (lock != null) {
            return;
        }
        timedCache.put(key, "value");
        DeviceLastGatherTimeMsg msg = new DeviceLastGatherTimeMsg(deviceCode, parse, parse.plusMinutes(offLineInterval));
        Message message = new Message(JSONObject.toJSONString(msg).getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("", MqConstant.QUEUE_DEVICE_LAST_GATHER_TIME, message);
    }

}
