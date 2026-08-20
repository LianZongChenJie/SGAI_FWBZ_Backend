package org.jeecg.modules.fwbz.coldSourceSystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunwayland.pspace.PSpaceClient;
import com.sunwayland.pspace.callback.IRealCallback;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsSubRealData;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.config.ColdSourceProperties;
import org.jeecg.modules.fwbz.coldSourceSystem.websocket.ColdSourceWsEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 冷源系统实时数据订阅推送服务（冷源 SDK 订阅 -> 前端 WebSocket）
 *
 * 数据链路（SDK 的 realNewSubscribeAndRead 即冷源 WebSocket 长连接订阅，替代原 HTTP /RealData 轮询）：
 * 1. 启动后台守护线程连接冷源系统(pSpace SDK)，用 realNewSubscribeAndRead 一次性订阅
 *    FIELD_MAP 中全部测点：返回值携带全量初值，立即推送一次，前端可展示全量数据；
 * 2. 冷源系统持续推送更新，SDK 回调 {@link IRealCallback#realDataCallBack(int, List)}
 *    收到 PsSubRealData 列表（真实字段为 value，而非 pv）；
 * 3. 每条数据按 tagId 反查 FIELD_MAP 中的 key：
 *    - 单测点 key：直接推送该测点数据（value + timestamp + quality + dataType）；
 *    - 多测点聚合 key：将新值写入测点值缓存，用缓存中该 key 全部测点值求和后推送
 *      （即"新值 + 未变更测点的旧值"）；
 * 4. 组装 {key: {value,...}} 通过 {@link ColdSourceWsEndpoint} 广播给前端。
 *
 * 说明：全程后台线程执行，不阻塞 Spring 容器启动；订阅失败按固定间隔自动重试，
 * 冷源系统恢复后无需重启即可自动续订。
 */
@Slf4j
@Service
public class ColdSourceRealPushService {

    /** 订阅失败后的重试间隔（毫秒） */
    private static final long RETRY_INTERVAL_MS = 30_000L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 全局单例引用：供 WebSocket 端点（非 Spring 单例）在连接建立时构建全量快照 */
    private static volatile ColdSourceRealPushService instance;

    /** 无 FIELD_MAP 映射测点的 value 占位值 */
    private static final String UNMAPPED_VALUE = "???";

    @Autowired
    private ColdSourceServerService coldSourceServerService;

    @Autowired
    private ColdSourceOverviewService coldSourceOverviewService;

    @Autowired
    private ColdSourceProperties properties;

    /** tagId -> 该测点关联的 key 列表（一个测点可能映射多个 key）；buildIndex 后只读 */
    private volatile Map<Long, List<String>> id2Keys = Collections.emptyMap();

    /** 映射 key -> 该 key 关联的全部测点 id（单测点 key 仅 1 个，聚合 key 多个）；buildIndex 后只读 */
    private volatile Map<String, List<Long>> key2Ids = Collections.emptyMap();

    /** 全部映射 key（FIELD_MAP 中配置了测点的 key），保持 FIELD_MAP 顺序；buildIndex 后只读 */
    private volatile List<String> allKeys = Collections.emptyList();

    /** 测点最新值缓存：tagId -> 最新订阅数据（供全量推送时取最新值） */
    private final Map<Long, PsSubRealData> tagValueCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        instance = this;
        buildIndex();
        if (properties.isMock()) {
            log.warn("【模拟模式】fwbz.cold-source.mock=true：不连接真实冷源系统，改用内置模拟数据源测试全链路");
            startMockGenerator();
            return;
        }
        Thread subscribeThread = new Thread(this::subscribeLoop, "cold-source-subscribe");
        // 守护线程：订阅慢/失败都不影响应用启动与退出
        subscribeThread.setDaemon(true);
        subscribeThread.start();
    }

    /**
     * 【模拟模式】启动内置模拟数据源：模拟冷源测点实时推送，数据走与真实 SDK 回调完全相同的
     * onRealData 处理链路（测点缓存 -> 聚合求和 -> 前端 WebSocket 广播），
     * 用于无冷源网络环境下的全链路联调测试。
     */
    private void startMockGenerator() {
        Set<Long> aggregateIds = key2Ids.values().stream()
                .filter(ids -> ids.size() > 1)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        MockColdSourceDataGenerator generator = new MockColdSourceDataGenerator(
                buildIdSemantic(), aggregateIds, list -> onRealData(0, list));
        generator.start();
    }

    /**
     * 构建 tagId -> 字段语义（取该测点关联的首个 key 的末段字段名，如 supplyTemp/running/totalPower），
     * 供模拟数据源按字段量纲生成合理数值。
     */
    private Map<Long, String> buildIdSemantic() {
        Map<Long, String> semantic = new LinkedHashMap<>();
        Map<String, List<Long>> fieldMap = coldSourceOverviewService.getFieldMap();
        for (Map.Entry<String, List<Long>> entry : fieldMap.entrySet()) {
            List<Long> ids = entry.getValue();
            if (ids == null || ids.isEmpty()) {
                continue;
            }
            String key = entry.getKey();
            String field = key.substring(key.lastIndexOf('.') + 1);
            for (Long id : ids) {
                semantic.putIfAbsent(id, field);
            }
        }
        return semantic;
    }

    /**
     * 根据 FIELD_MAP 构建索引，构建完成后以不可变视图暴露，只读安全。
     * allKeys：FIELD_MAP 中全部 key（含未配置测点 id 的 key，如 makeup.pressure），保持顺序；
     * key2Ids：映射 key -> 该 key 关联的测点 id（单测点 1 个，聚合 key 多个）；
     *          未配置 id 的 key 不放入，即 key2Ids.get(key) 为 null；
     * id2Keys：tagId -> 关联的 key 列表。
     */
    private void buildIndex() {
        Map<String, List<Long>> fieldMap = coldSourceOverviewService.getFieldMap();
        Map<Long, List<String>> id2KeysMap = new HashMap<>();
        Map<String, List<Long>> key2IdsMap = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, List<Long>> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            List<Long> ids = entry.getValue();
            // 全部 key 都进入 allKeys（未配置测点 id 的 key 也推送，全量时 value 为 "???"）
            keys.add(key);
            if (ids == null || ids.isEmpty()) {
                continue;
            }
            key2IdsMap.put(key, new ArrayList<>(ids));
            for (Long id : ids) {
                id2KeysMap.computeIfAbsent(id, k -> new ArrayList<>()).add(key);
            }
        }
        this.id2Keys = Collections.unmodifiableMap(id2KeysMap);
        this.key2Ids = Collections.unmodifiableMap(key2IdsMap);
        this.allKeys = Collections.unmodifiableList(keys);
        log.info("冷源实时订阅索引构建完成: 订阅测点数={}, 映射key数={}", id2Keys.size(), allKeys.size());
    }

    /**
     * 订阅循环：启动即订阅，失败/异常后按固定间隔自动重试，
     * 冷源系统恢复后无需人工干预即可自动续订。
     */
    private void subscribeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (subscribeOnce()) {
                    // 订阅成功：后续由 SDK 回调持续推送增量，本线程退出
                    return;
                }
            } catch (Exception e) {
                log.warn("冷源实时订阅异常，{}ms 后重试: {}", RETRY_INTERVAL_MS, e.getMessage());
            }
            sleep(RETRY_INTERVAL_MS);
        }
    }

    /**
     * 建立一次订阅并推送初值。
     *
     * @return true 表示订阅成功（或无测点可订阅）；false 表示失败需重试
     */
    private boolean subscribeOnce() throws Exception {
        PSpaceClient client = coldSourceServerService.connect();
        List<Long> tagIds = new ArrayList<>(id2Keys.keySet());
        if (tagIds.isEmpty()) {
            log.warn("FIELD_MAP 中没有可订阅的测点，跳过冷源实时订阅");
            return true;
        }
        // 订阅全部测点并一次性获取初值；回调在订阅期间持续触发
        PsResult<PsSubRealData> result = client.realNewSubscribeAndRead(tagIds, Collections.singletonList(this::onRealData));
        if (!result.isSuccess() || result.getData() == null || result.getData().isEmpty()) {
            log.warn("冷源实时订阅失败: code={}，{}ms 后重试", result.getCode(), RETRY_INTERVAL_MS);
            return false;
        }
        log.info("冷源实时订阅成功: 测点数={}, subId={}", tagIds.size(), subIdOf(result.getData().get(0)));
        // 初值全量推送一次，前端可立即展示
        onRealData(subIdOf(result.getData().get(0)), result.getData());
        return true;
    }

    /**
     * 实时数据回调：冷源 SDK 推送（或模拟数据源产生）的增量数据。
     * 更新测点值缓存后，仅推送本次发生变化的测点对应的 key（增量推送）：
     *  - 单测点 key：透传该测点最新值；
     *  - 聚合 key：用缓存中该 key 全部测点值求和；
     *  - 无 FIELD_MAP 映射的测点：跳过，不推送（首次连接时全量数据已由 WS 端点推送）。
     * 包内可见：模拟数据源 {@link MockColdSourceDataGenerator} 直接调用，与真实 SDK 回调走同一处理链路。
     */
    void onRealData(int subId, List<PsSubRealData> subRealDataList) {
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
                tagValueCache.put(tagId, realData);
                List<String> keys = id2Keys.get(tagId);
                if (keys == null) {
                    continue;
                }
                for (String key : keys) {
                    data.put(key, key2Ids.get(key).size() > 1
                            ? buildAggregateValue(key)
                            : buildValue(realData));
                }
            }
            if (!data.isEmpty()) {
                Map<String, Object> message = new LinkedHashMap<>();
                message.put("type", "REAL_DATA");
                message.put("data", data);
                ColdSourceWsEndpoint.broadcast(OBJECT_MAPPER.writeValueAsString(message));
            }
        } catch (Exception e) {
            log.warn("冷源实时数据处理/推送异常(subId={}): {}", subId, e.getMessage());
        }
    }

    /**
     * 按映射集合中全部 key 组装全量数据：
     * 遍历 {@link #allKeys}，对每个映射 key 从测点值缓存取最新值组装；
     * 若该 key 没有对应的 id 关系（关联测点均无缓存值），则该 key 的 value 推 {@link #UNMAPPED_VALUE}（"???"）。
     *
     * @return data 部分（不含 type 包裹）
     */
    private Map<String, Object> buildAllData() {
        Map<String, Object> data = new LinkedHashMap<>();
        for (String key : allKeys) {
            List<Long> ids = key2Ids.get(key);
            if (ids == null || ids.isEmpty()) {
                // 未配置测点 id 的映射 key（如 makeup.pressure）：value 推 "???"
                data.put(key, UNMAPPED_VALUE);
                continue;
            }
            if (ids.size() > 1) {
                // 聚合 key：对全部测点缓存值求和；无缓存值时 value 为 "???"
                Map<String, Object> agg = buildAggregateValue(key);
                data.put(key, agg);
            } else {
                // 单测点 key：取该测点缓存值；无缓存值时 value 为 "???"
                PsSubRealData realData = tagValueCache.get(ids.get(0));
                data.put(key, realData == null ? UNMAPPED_VALUE : buildValue(realData));
            }
        }
        return data;
    }

    /** 单测点 key：透传 value，附带时间戳/质量/类型等其他字段 */
    private Map<String, Object> buildValue(PsSubRealData realData) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("value", realData.getValue());
        m.put("timestamp", realData.getTimestamp());
        m.put("quality", enumName(realData.getQuality()));
        m.put("dataType", enumName(realData.getDataType()));
        return m;
    }

    /**
     * 聚合 key：用该 key 全部测点缓存值求和。
     * 无任何缓存值（无对应 id 关系）时，value 返回 {@link #UNMAPPED_VALUE}（"???"），
     * 其余字段取第一个有缓存值的测点。
     */
    private Map<String, Object> buildAggregateValue(String key) {
        List<Long> ids = key2Ids.get(key);
        double sum = 0;
        boolean hasNumber = false;
        Object firstNonNull = null;
        PsSubRealData any = null;
        if (ids != null) {
            for (Long id : ids) {
                PsSubRealData d = tagValueCache.get(id);
                if (d == null) {
                    continue;
                }
                if (any == null) {
                    any = d;
                }
                Object v = d.getValue();
                if (v instanceof Number) {
                    sum += ((Number) v).doubleValue();
                    hasNumber = true;
                } else if (v != null && firstNonNull == null) {
                    firstNonNull = v;
                }
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        if (any == null) {
            // 无对应 id 关系：value 推 "???"
            m.put("value", UNMAPPED_VALUE);
            return m;
        }
        m.put("value", hasNumber ? sum : firstNonNull);
        m.put("timestamp", any.getTimestamp());
        m.put("quality", enumName(any.getQuality()));
        m.put("dataType", enumName(any.getDataType()));
        return m;
    }

    /**
     * 构建冷源实时数据全量快照（供前端 WebSocket 连接建立时首次推送）。
     * 与每次增量推送使用同一构建逻辑：按映射集合中全部 key 组装，
     * 无对应 id 关系的 key value 推 {@link #UNMAPPED_VALUE}（"???"）。
     *
     * @return data 部分（不含 type 包裹）
     */
    public static Map<String, Object> buildSnapshotData() {
        ColdSourceRealPushService svc = instance;
        if (svc == null) {
            return Collections.emptyMap();
        }
        return svc.buildAllData();
    }

    private static int subIdOf(PsSubRealData data) {
        Long subId = data.getSubId();
        return subId == null ? 0 : subId.intValue();
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
