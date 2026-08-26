package org.jeecg.modules.fwbz.mqtt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jeecg.modules.fwbz.mqtt.entity.MqttHistory;
import org.jeecg.modules.fwbz.mqtt.service.IMqttHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MQTT消费者
 * <p>连接MQTT服务，订阅低压配电遥测/遥信/电度数据，解析后直接更新device_attribute设备属性表，
 * 其中电度数据uniqueKey包含"01Wp"的为正向有功电能表底值，触发设备能耗计算（分钟、小时、日、月、年）。</p>
 *
 * <p>接收数据格式（8000主站全量推送，JSON对象）：</p>
 * <pre>
 * {"devKeys":"设备代码","timeStamp":"2025-05-28 10:00:00",
 *   "ycdata":[{"uniqueKey":"唯一遥测代码","desc":"量测详细信息","value":10}],
 *   "yxdata":[{"uniqueKey":"唯一遥信代码","desc":"量测详细信息","value":1}],
 *   "kwhdata":[{"uniqueKey":"唯一电度代码","desc":"量测详细信息","value":10.5}]}
 * </pre>
 * <p>也兼容突发数据（JSON数组/单对象/多对象拼接）：</p>
 * <pre>
 * {"devKeys":"设备代码","timeStamp":"2025-05-28 10:00:05","uniqueKey":"唯一遥测代码","desc":"量测详细信息","value":10}
 * </pre>
 *
 * @author fwbz
 */
@Slf4j
@Component
public class MqttConsumer {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${mqtt.broker:tcp://10.22.163.239:18083}")
    private String brokerUrl;

    @Value("${mqtt.username:admin}")
    private String username;

    @Value("${mqtt.password:zaq!@wsx}")
    private String password;

    /** 客户端ID前缀，实际使用时会拼接随机后缀，避免多实例/多次重启互踢 */
    @Value("${mqtt.client-id:sgai-fwbz-mqtt-consumer}")
    private String clientIdPrefix;

    private String clientId;

    /** 订阅主题，默认占位主题，请按实际主题修改 */
    @Value("${mqtt.topic:low_voltage/#}")
    private String topic;

    @Value("${mqtt.qos:1}")
    private int qos;

    private final IMqttHistoryService mqttHistoryService;

    private MqttClient mqttClient;

    public MqttConsumer(IMqttHistoryService mqttHistoryService) {
        this.mqttHistoryService = mqttHistoryService;
    }

    @PostConstruct
    public void init() {
        // clientId拼接随机后缀，避免相同clientId被broker互踢
        clientId = clientIdPrefix;
        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT连接丢失: {}", cause.getMessage(), cause);
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) {
                    handleMessage(topicName, new String(message.getPayload(), StandardCharsets.UTF_8));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 消费者无需处理消息投递完成
                }
            });

            mqttClient.connect(options);
            if (!mqttClient.isConnected()) {
                log.error("MQTT连接未建立成功, broker={}", brokerUrl);
                return;
            }
            mqttClient.subscribe(topic, qos);
        } catch (MqttException e) {
            // e.getReasonCode()为0时多为TCP连接被服务端直接关闭，通常是对端端口不是MQTT端口或IP被限制
            log.error("MQTT消费者启动失败, broker={}, clientId={}, 原因码={}, 原因: {}", brokerUrl, clientId, e.getReasonCode(), e.getMessage(), e);
        }
    }

    /**
     * 处理MQTT消息：解析JSON数组，更新设备属性并触发能耗计算
     *
     * @param topicName 消息主题
     * @param payload   消息内容
     */
    private void handleMessage(String topicName, String payload) {
        if (StringUtils.isBlank(payload)) {
            log.warn("MQTT消息内容为空, topic={}", topicName);
            return;
        }
        try {
            List<MqttHistory> list = parsePayload(payload);
            if (list.isEmpty()) {
                log.warn("MQTT消息解析后无有效数据, topic={}", topicName);
                return;
            }


            // 根据采集编码（uniqueKey）更新设备属性表中的采集值和采集时间
            try {
                mqttHistoryService.updateDeviceAttributeByUniqueKey(list);
            } catch (Exception e) {
                log.error("设备属性采集值更新失败, topic={}", topicName, e);
            }

            // 电度数据uniqueKey包含"01Wp"的为正向有功电能表底值，触发设备能耗计算（分钟、小时、日、月、年）
            try {
                mqttHistoryService.calculateEnergyData(list);
            } catch (Exception e) {
                log.error("MQTT能耗数据计算触发失败, topic={}", topicName, e);
            }
        } catch (Exception e) {
            log.error("MQTT消息消费失败, topic={}, payload={}", topicName, payload, e);
        }
    }

    /**
     * 解析MQTT消息内容，兼容JSON数组、单个对象、多对象拼接三种格式
     * <p>对象支持两种结构：</p>
     * <ul>
     *   <li>扁平格式：{devKeys, timeStamp, uniqueKey, desc, value}</li>
     *   <li>嵌套格式：{devKeys, timeStamp, ycdata: [...], yxdata: [...], kwhdata: [...]}（8000主站全量推送）</li>
     * </ul>
     *
     * @param payload 消息内容
     * @return 解析后的数据列表
     */
    private List<MqttHistory> parsePayload(String payload) {
        List<MqttHistory> list = new ArrayList<>();
        String trim = payload.trim();
        if (trim.startsWith("[")) {
            // 标准JSON数组格式
            JSONArray array = JSON.parseArray(trim);
            for (int i = 0; i < array.size(); i++) {
                parseObject(array.getJSONObject(i), list);
            }
        } else if (trim.startsWith("{")) {
            // 多对象拼接格式（无外层方括号），如 {...},{...},{...}：尝试补括号后按数组解析
            if (trim.startsWith("{\"") && trim.endsWith("}")) {
                int braceCount = 0;
                boolean multiObject = false;
                for (int i = 0; i < trim.length(); i++) {
                    char c = trim.charAt(i);
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0 && i < trim.length() - 1) {
                            multiObject = true;
                            break;
                        }
                    }
                }
                if (multiObject) {
                    JSONArray array = JSON.parseArray("[" + trim + "]");
                    for (int i = 0; i < array.size(); i++) {
                        parseObject(array.getJSONObject(i), list);
                    }
                    return list;
                }
            }
            // 单个JSON对象格式
            parseObject(JSON.parseObject(trim), list);
        } else {
            log.warn("MQTT消息不是合法的JSON数组或对象, payload={}", payload);
        }
        return list;
    }

    /**
     * 解析单个JSON对象，支持扁平格式与嵌套格式（ycdata/yxdata/kwhdata）
     */
    private void parseObject(JSONObject json, List<MqttHistory> list) {
        if (json == null) {
            return;
        }
        boolean nested = false;
        // 嵌套格式：{"devKeys":"XH-01","timeStamp":"...","ycdata":[...],"yxdata":[...],"kwhdata":[...]}
        if (json.containsKey("ycdata") || json.containsKey("yxdata") || json.containsKey("kwhdata")) {
            nested = true;
            parseNestedArray(json, "ycdata", "yc", list);
            parseNestedArray(json, "yxdata", "yx", list);
            parseNestedArray(json, "kwhdata", "kwh", list);
        }
        if (!nested) {
            // 扁平格式：{"devKeys":"...","timeStamp":"...","uniqueKey":"...","desc":"...","value":...}
            MqttHistory history = toHistory(json, json, null);
            if (history != null) {
                list.add(history);
            }
        }
    }

    /**
     * 解析嵌套数组，如 ycdata/yxdata/kwhdata
     *
     * @param json     外层JSON对象（包含devKeys、timeStamp）
     * @param key      嵌套数组字段名
     * @param dataType 数据类型：yc-遥测，yx-遥信，kwh-电度
     */
    private void parseNestedArray(JSONObject json, String key, String dataType, List<MqttHistory> list) {
        JSONArray array = json.getJSONArray(key);
        if (array == null) {
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            MqttHistory history = toHistory(json, array.getJSONObject(i), dataType);
            if (history != null) {
                list.add(history);
            }
        }
    }

    /**
     * JSON对象转换为实体
     *
     * @param base    外层JSON对象（扁平格式时与json为同一对象），提供devKeys、timeStamp
     * @param json    数据对象，提供uniqueKey、desc、value
     * @param dataType 数据类型：yc-遥测，yx-遥信，kwh-电度，null-未知（扁平格式）
     */
    private MqttHistory toHistory(JSONObject base, JSONObject json, String dataType) {
        if (base == null || json == null) {
            return null;
        }
        MqttHistory history = new MqttHistory();
        history.setDevKeys(base.getString("devKeys"));
        history.setUniqueKey(json.getString("uniqueKey"));
        history.setDesc(json.getString("desc"));
        history.setDataType(dataType);
        String timeStampStr = base.getString("timeStamp");
        if (StringUtils.isNotBlank(timeStampStr)) {
            try {
                history.setTimeStamp(LocalDateTime.parse(timeStampStr.trim(), TIME_FORMATTER));
            } catch (Exception e) {
                log.warn("时间戳解析失败, timeStamp={}", timeStampStr);
            }
        }
        Object value = json.get("value");
        if (value != null) {
            history.setValue(value.toString());
        }
        return history;
    }

    @PreDestroy
    public void destroy() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                log.info("MQTT消费者已断开连接");
            } catch (MqttException e) {
                log.error("MQTT消费者断开连接失败", e);
            }
        }
    }
}
