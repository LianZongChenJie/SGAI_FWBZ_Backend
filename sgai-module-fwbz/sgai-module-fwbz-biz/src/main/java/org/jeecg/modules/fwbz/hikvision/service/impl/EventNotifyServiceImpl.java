package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.entity.EventNotify;
import org.jeecg.modules.fwbz.hikvision.dto.EventNotifyPushRequest;
import org.jeecg.modules.fwbz.hikvision.dto.EventNotifyPushRequest.EventNotifyEvent;
import org.jeecg.modules.fwbz.hikvision.dto.EventNotifyPushRequest.EventNotifyParams;
import org.jeecg.modules.fwbz.hikvision.service.IEventNotifyService;
import org.jeecg.modules.fwbz.mapper.EventNotifyMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 海康事件通知服务实现
 * <p>解析海康推送事件JSON，转换成EventNotify实体入库。</p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class EventNotifyServiceImpl implements IEventNotifyService {

    private final EventNotifyMapper eventNotifyMapper;

    @Override
    public int handleEventNotify(EventNotifyPushRequest pushRequest) {
        if (pushRequest == null) {
            log.warn("接收到空推送事件，跳过处理");
            return 0;
        }

        EventNotifyParams params = pushRequest.getParams();
        if (params == null || params.getEvents() == null || params.getEvents().isEmpty()) {
            log.warn("推送事件中events为空, ability={}, sendTime={}",
                    params != null ? params.getAbility() : null,
                    params != null ? params.getSendTime() : null);
            return 0;
        }

        String ability = params.getAbility();
        String sendTime = params.getSendTime();
        List<EventNotifyEvent> events = params.getEvents();

        log.info("接收到海康事件推送, ability={}, sendTime={}, 事件数量={}", ability, sendTime, events.size());

        List<EventNotify> entityList = new ArrayList<>();
        int skipCount = 0;

        for (EventNotifyEvent event : events) {
            try {
                EventNotify entity = convertToEntity(event, ability, sendTime);
                entityList.add(entity);
            } catch (Exception e) {
                log.error("转换事件失败, eventId={}, eventType={}", event.getEventId(), event.getEventType(), e);
                skipCount++;
            }
        }

        if (!entityList.isEmpty()) {
            // 批量插入（MyBatis-Plus的saveBatch）
            for (EventNotify entity : entityList) {
                eventNotifyMapper.insert(entity);
            }
            log.info("海康事件保存完成, 成功{}条, 跳过{}条", entityList.size(), skipCount);
        }

        if (skipCount > 0) {
            log.warn("海康事件推送有{}条转换失败", skipCount);
        }

        return entityList.size();
    }

    /**
     * 将推送事件转换为数据库实体
     */
    private EventNotify convertToEntity(EventNotifyEvent event, String ability, String sendTime) {
        EventNotify entity = new EventNotify()
                .setSendTime(sendTime)
                .setAbility(ability)
                .setEventId(event.getEventId())
                .setEventType(event.getEventType())
                .setHappenTime(event.getHappenTime())
                .setSrcIndex(event.getSrcIndex())
                .setSrcName(event.getSrcName())
                .setSrcType(event.getSrcType())
                .setStatus(event.getStatus() != null ? event.getStatus() : 0)
                .setEventLvl(event.getEventLvl() != null ? event.getEventLvl() : 0)
                .setTimeout(event.getTimeout() != null ? event.getTimeout() : 0);

        // 提取父设备编码（优先使用事件自带的，其次从data中提取）
        String parentIndex = event.getSrcParentIndex();
        if (parentIndex == null || parentIndex.isEmpty()) {
            parentIndex = extractParentIndex(event.getData());
        }
        entity.setSrcParentIndex(parentIndex);

        // 将data对象序列化为JSON字符串存入event_data字段
        JSONObject data = event.getData();
        if (data != null && !data.isEmpty()) {
            entity.setEventData(data.toJSONString());
        }

        return entity;
    }

    /**
     * 从事件data中尝试提取父设备编码
     * <p>不同事件类型的data结构不同，优先从常见路径提取：</p>
     * <ul>
     *   <li>data.fielddetection[0].targetAttrs.deviceIndexCode</li>
     *   <li>data.targetAttrs.deviceIndexCode</li>
     * </ul>
     */
    private String extractParentIndex(JSONObject data) {
        if (data == null) {
            return null;
        }

        try {
            // 尝试从行为分析事件的fielddetection中提取
            String eventTypeName = data.getString("eventType");
            if (eventTypeName != null && !eventTypeName.isEmpty()) {
                // 尝试通过事件类型名找到对应的数组字段
                Object eventArray = data.get(eventTypeName);
                if (eventArray instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) eventArray;
                    if (!list.isEmpty() && list.get(0) instanceof JSONObject) {
                        JSONObject firstItem = (JSONObject) list.get(0);
                        JSONObject targetAttrs = firstItem.getJSONObject("targetAttrs");
                        if (targetAttrs != null) {
                            String deviceIndexCode = targetAttrs.getString("deviceIndexCode");
                            if (deviceIndexCode != null && !deviceIndexCode.isEmpty()) {
                                return deviceIndexCode;
                            }
                        }
                    }
                }
            }

            // 兼容：直接从data.targetAttrs提取
            JSONObject targetAttrs = data.getJSONObject("targetAttrs");
            if (targetAttrs != null) {
                String parentIndex = targetAttrs.getString("deviceIndexCode");
                if (parentIndex != null && !parentIndex.isEmpty()) {
                    return parentIndex;
                }
            }
        } catch (Exception e) {
            log.debug("提取父设备编码失败, dataType={}", data.getString("dataType"));
        }

        return null;
    }
}
