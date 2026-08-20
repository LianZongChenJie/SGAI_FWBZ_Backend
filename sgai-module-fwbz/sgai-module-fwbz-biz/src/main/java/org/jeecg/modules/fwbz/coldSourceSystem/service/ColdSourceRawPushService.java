package org.jeecg.modules.fwbz.coldSourceSystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunwayland.pspace.PSpaceClient;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsSubRealData;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.config.ColdSourceProperties;
import org.jeecg.modules.fwbz.coldSourceSystem.websocket.ColdSourceRawWsEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冷源系统「原始数据」订阅观察服务（独立调试用）
 *
 * 与 {@link ColdSourceRealPushService} 的区别：
 * - 这里不做任何 FIELD_MAP 映射、聚合求和、设备属性写库等业务处理；
 * - 只订阅 FIELD_MAP 中全部测点，把冷源系统每次推送的原始数据（tagId / value / timestamp / quality / dataType）
 *   原样通过独立 WebSocket 端点 {@link ColdSourceRawWsEndpoint} 推送给测试页面，方便观察冷源系统真实推送内容。
 *
 * 数据链路：冷源 SDK 订阅(FIELD_MAP 全部测点) -> 回调原始数据 -> 组装 {tagId:{...}} -> 独立 WS 推送。
 */
@Slf4j
@Service
public class ColdSourceRawPushService {

    /** 订阅失败后的重试间隔（毫秒） */
    private static final long RETRY_INTERVAL_MS = 30_000L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private ColdSourceServerService coldSourceServerService;

    @Autowired
    private ColdSourceOverviewService coldSourceOverviewService;

    @Autowired
    private ColdSourceProperties properties;

    /** 订阅的测点 id 集合（FIELD_MAP 全部 id），buildPointIds 后只读 */
    private volatile List<Long> tagIds = Collections.emptyList();

    @PostConstruct
    public void init() {
        buildPointIds();
        if (properties.isMock()) {
            log.warn("【模拟模式】冷源原始数据观察：不连接真实冷源，跳过订阅（无原始数据可推）");
            return;
        }
        Thread subscribeThread = new Thread(this::subscribeLoop, "cold-source-raw-subscribe");
        subscribeThread.setDaemon(true);
        subscribeThread.start();
    }

    /**
     * 从 FIELD_MAP 收集全部配置了 id 的测点，作为订阅点集。
     */
    private void buildPointIds() {
        List<Long> ids = new ArrayList<>();
        for (List<Long> list : coldSourceOverviewService.getFieldMap().values()) {
            if (list == null || list.isEmpty()) {
                continue;
            }
            for (Long id : list) {
                if (id != null && !ids.contains(id)) {
                    ids.add(id);
                }
            }
        }
        this.tagIds = Collections.unmodifiableList(ids);
        log.info("冷源原始数据观察：待订阅测点数={}", tagIds.size());
    }

    /**
     * 订阅循环：启动即订阅，失败后按固定间隔重试。
     */
    private void subscribeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (subscribeOnce()) {
                    return;
                }
            } catch (Exception e) {
                log.warn("冷源原始数据订阅异常，{}ms 后重试: {}", RETRY_INTERVAL_MS, e.getMessage());
            }
            sleep(RETRY_INTERVAL_MS);
        }
    }

    /**
     * 建立一次订阅并获取初值。
     *
     * @return true 订阅成功（或无测点可订阅）；false 失败需重试
     */
    private boolean subscribeOnce() throws Exception {
        PSpaceClient client = coldSourceServerService.connect();
        if (tagIds.isEmpty()) {
            log.warn("FIELD_MAP 中没有可订阅的测点，跳过冷源原始数据订阅");
            return true;
        }
        // 订阅全部测点并一次性获取初值；回调在订阅期间持续触发（只推原始数据，不做映射）
        PsResult<PsSubRealData> result = client.realNewSubscribeAndRead(tagIds, Collections.singletonList(this::onRawData));
        if (!result.isSuccess() || result.getData() == null || result.getData().isEmpty()) {
            log.warn("冷源原始数据订阅失败: code={}，{}ms 后重试", result.getCode(), RETRY_INTERVAL_MS);
            return false;
        }
        log.info("冷源原始数据订阅成功: 测点数={}", tagIds.size());
        onRawData(0, result.getData());
        return true;
    }

    /**
     * 冷源 SDK 原始数据回调：不做任何映射/聚合，直接组装 {tagId: {value,timestamp,quality,dataType}} 推送。
     */
    void onRawData(int subId, List<PsSubRealData> subRealDataList) {
        if (subRealDataList == null || subRealDataList.isEmpty()) {
            return;
        }
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            for (PsSubRealData realData : subRealDataList) {
                Long tagId = realData.getTagId();
                if (tagId == null) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("value", realData.getValue());
                item.put("timestamp", realData.getTimestamp());
                item.put("quality", enumName(realData.getQuality()));
                item.put("dataType", enumName(realData.getDataType()));
                data.put(String.valueOf(tagId), item);
            }
            if (!data.isEmpty()) {
                Map<String, Object> message = new LinkedHashMap<>();
                message.put("type", "RAW_DATA");
                message.put("data", data);
                ColdSourceRawWsEndpoint.broadcast(OBJECT_MAPPER.writeValueAsString(message));
            }
        } catch (Exception e) {
            log.warn("冷源原始数据处理/推送异常(subId={}): {}", subId, e.getMessage());
        }
    }

    private static String enumName(Enum<?> e) {
        return e == null ? null : e.name();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
