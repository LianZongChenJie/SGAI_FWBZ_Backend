package org.jeecg.modules.fwbz.coldSourceSystem.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceRealPushService;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冷源系统实时数据推送 WebSocket 端点
 *
 * 前端连接：ws://{host}:{port}/fwbz/coldSource/ws
 * 服务端推送消息格式：
 * <pre>
 * {
 *   "type": "REAL_DATA",
 *   "data": {
 *     "station.supplyTemp": {"value": 7.1, "timestamp": 1786925522068, "quality": "GOOD", "dataType": "DOUBLE"},
 *     "station.totalPower": {"value": 1234.56, "timestamp": 1786925522068, "quality": "GOOD", "dataType": "DOUBLE"}
 *   }
 * }
 * </pre>
 * 其中 data 的 key 即 FIELD_MAP 中的前端字段 key；聚合字段(映射多个测点)的 value 为各测点求和值。
 */
@Slf4j
@Component
@ServerEndpoint("/fwbz/coldSource/ws")
public class ColdSourceWsEndpoint {

    /** 所有已连接的前端会话 */
    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        log.info("冷源实时数据客户端接入: session={}, 当前连接数={}", session.getId(), SESSIONS.size());
        // 首次推送全量快照（含无映射测点，其 value 为 "???"）
        sendSnapshot(session);
    }

    /**
     * 向指定 session 推送冷源实时数据全量快照。
     * 全量数据来自测点值缓存；无 FIELD_MAP 映射的测点 value 传 "???"。
     */
    private void sendSnapshot(Session session) {
        try {
            Map<String, Object> snapshotData = ColdSourceRealPushService.buildSnapshotData();
            if (snapshotData == null || snapshotData.isEmpty()) {
                return;
            }
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", "REAL_DATA");
            message.put("data", snapshotData);
            synchronized (session) {
                session.getBasicRemote().sendText(OBJECT_MAPPER.writeValueAsString(message));
            }
        } catch (Exception e) {
            log.warn("冷源实时数据全量快照推送失败: session={}, err={}", session.getId(), e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
        log.info("冷源实时数据客户端断开: session={}, 当前连接数={}", session.getId(), SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        if (session != null) {
            SESSIONS.remove(session);
        }
        log.warn("冷源实时数据 WebSocket 异常: session={}, err={}", session == null ? "null" : session.getId(), error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 暂未定义客户端 -> 服务端消息，仅记录
        log.debug("收到客户端消息: session={}, msg={}", session.getId(), message);
    }

    /**
     * 向所有已连接的前端广播文本消息（线程安全，逐会话加锁发送）
     */
    public static void broadcast(String json) {
        if (SESSIONS.isEmpty()) {
            return;
        }
        for (Session session : SESSIONS) {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.getBasicRemote().sendText(json);
                    }
                } else {
                    SESSIONS.remove(session);
                }
            } catch (Exception e) {
                log.warn("冷源实时数据推送失败: session={}, err={}", session.getId(), e.getMessage());
                SESSIONS.remove(session);
            }
        }
    }
}
