package org.jeecg.modules.fwbz.coldSourceSystem.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceRawPushService;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冷源系统「原始数据」观察 WebSocket 端点（独立调试用）
 *
 * 与 {@link ColdSourceWsEndpoint} 隔离，仅供测试页面实时查看冷源系统推送的原始数据
 * （不做 FIELD_MAP 映射、聚合、写库等处理）。
 */
@Slf4j
@Component
@ServerEndpoint("/fwbz/coldSource/rawTest/ws")
public class ColdSourceRawWsEndpoint {

    /** 所有已连接的前端会话 */
    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        log.info("冷源原始数据客户端接入: session={}, 当前连接数={}", session.getId(), SESSIONS.size());
        // 首次连接推送最近一次的全量原始数据（订阅获取的初值）
        sendSnapshot(session);
    }

    /**
     * 向指定 session 推送冷源原始数据全量快照（最近一次已缓存的数据）。
     */
    private void sendSnapshot(Session session) {
        try {
            Map<String, Object> snapshotData = ColdSourceRawPushService.buildSnapshotData();
            if (snapshotData == null || snapshotData.isEmpty()) {
                return;
            }
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", "RAW_DATA");
            message.put("data", snapshotData);
            synchronized (session) {
                session.getBasicRemote().sendText(OBJECT_MAPPER.writeValueAsString(message));
            }
        } catch (Exception e) {
            log.warn("冷源原始数据全量快照推送失败: session={}, err={}", session.getId(), e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
        log.info("冷源原始数据客户端断开: session={}, 当前连接数={}", session.getId(), SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSIONS.remove(session);
        log.warn("冷源原始数据客户端异常: session={}, err={}", session == null ? "null" : session.getId(), error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 测试页面无需处理客户端上行消息
    }

    /**
     * 广播原始数据给所有已连接的前端。
     */
    public static void broadcast(String json) {
        if (json == null || SESSIONS.isEmpty()) {
            return;
        }
        for (Session session : SESSIONS) {
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(json);
                    }
                }
            } catch (IOException e) {
                log.warn("冷源原始数据推送失败: session={}, err={}", session.getId(), e.getMessage());
            }
        }
    }
}
