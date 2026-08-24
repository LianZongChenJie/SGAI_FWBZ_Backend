package org.jeecg.modules.fwbz.coldSourceSystem.service;

import com.sunwayland.pspace.entity.PsSubRealData;
import com.sunwayland.pspace.enums.PsDataTypeEnum;
import com.sunwayland.pspace.enums.PsQualityEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 【临时模拟数据源】模拟冷源系统测点实时推送，用于无冷源网络环境下的全链路联调测试。
 *
 * <p>由 {@link ColdSourceRealPushService} 在 fwbz.cold-source.mock=true 时启动：
 * 启动时全量推送一次初值（模拟 SDK 订阅返回全量初值），之后每 2 秒增量推送一批测点变化，
 * 数据走与真实 SDK 回调完全相同的 {@code onRealData} 处理链路（测点缓存 -> 聚合求和 -> 前端 WebSocket 广播）。
 * 为保证聚合逻辑可验证，聚合 key（如 station.totalPower）涉及的测点每轮都会更新。
 *
 * <p>临时文件：正式接入真实冷源后删除。
 */
@Slf4j
public class MockColdSourceDataGenerator {

    /** 推送间隔（毫秒） */
    private static final long TICK_INTERVAL_MS = 2_000L;

    /** tagId -> 字段语义（如 supplyTemp/running/totalPower），决定生成数值的量纲与类型 */
    private final Map<Long, String> idSemantic;

    /** 聚合 key 涉及的全部测点 id（每轮必更，便于验证聚合实时求和） */
    private final Set<Long> aggregateIds;

    /** 数据消费回调（即 ColdSourceRealPushService.onRealData） */
    private final Consumer<List<PsSubRealData>> dataConsumer;

    /** tagId -> 当前模拟值状态 */
    private final Map<Long, MockValue> values = new ConcurrentHashMap<>();

    private final Random random = new Random();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "cold-source-mock-tick");
                t.setDaemon(true);
                return t;
            });

    public MockColdSourceDataGenerator(Map<Long, String> idSemantic, Set<Long> aggregateIds,
                                       Consumer<List<PsSubRealData>> dataConsumer) {
        this.idSemantic = idSemantic;
        this.aggregateIds = aggregateIds == null ? Collections.emptySet() : aggregateIds;
        this.dataConsumer = dataConsumer;
    }

    /** 启动：全量推送初值，随后定时增量推送 */
    public void start() {
        // 初值全量推送一次（模拟 SDK 订阅返回全量初值），前端可立即展示全量数据
        long now = System.currentTimeMillis();
        List<PsSubRealData> initList = new ArrayList<>(idSemantic.size());
        for (Long id : idSemantic.keySet()) {
            initList.add(toData(id, valueOf(id), now));
        }
        dataConsumer.accept(initList);
        log.info("【模拟模式】冷源模拟数据源已启动: 测点数={}, 聚合测点数={}, 推送间隔={}ms",
                idSemantic.size(), aggregateIds.size(), TICK_INTERVAL_MS);
        scheduler.scheduleWithFixedDelay(this::tick, TICK_INTERVAL_MS, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /** 停用（进程退出时由守护线程自然结束） */
    public void stop() {
        scheduler.shutdownNow();
    }

    /** 一轮增量推送：聚合测点全量更新 + 随机挑一部分其他测点更新 */
    private void tick() {
        long now = System.currentTimeMillis();
        List<PsSubRealData> list = new ArrayList<>(aggregateIds.size() + 8);
        // 聚合测点每轮必更（验证 station.totalPower 等聚合实时求和）
        for (Long id : aggregateIds) {
            list.add(toData(id, valueOf(id).next(), now));
        }
        // 其余测点随机抽样更新（模拟真实增量推送：某一时刻只有部分测点变化）
        List<Long> others = new ArrayList<>(idSemantic.keySet());
        others.removeAll(aggregateIds);
        Collections.shuffle(others, random);
        int sample = Math.min(8, others.size());
        for (int i = 0; i < sample; i++) {
            list.add(toData(others.get(i), valueOf(others.get(i)).next(), now));
        }
        dataConsumer.accept(list);
    }

    private MockValue valueOf(Long id) {
        return values.computeIfAbsent(id, k -> new MockValue(idSemantic.getOrDefault(k, "default")));
    }

    private PsSubRealData toData(Long id, MockValue mv, long now) {
        PsSubRealData d = new PsSubRealData();
        d.setTagId(id);
        d.setValue(mv.isBoolean() ? mv.booleanValue() : mv.numberValue());
        d.setTimestamp(now);
        d.setQuality(PsQualityEnum.GOOD);
        d.setDataType(mv.isBoolean() ? PsDataTypeEnum.BOOL : PsDataTypeEnum.DOUBLE);
        return d;
    }

    /**
     * 单个测点的模拟值状态：根据字段语义确定布尔/数值、量程与步长，采用随机游走模拟实时波动。
     */
    private static class MockValue {
        private static final Random RANDOM = new Random();

        private final boolean booleanField;
        private final boolean increasing;
        private final double min;
        private final double max;
        private final double step;
        private double value;

        MockValue(String field) {
            this.booleanField = isBooleanField(field);
            this.increasing = isIncreasingField(field);
            double[] range = rangeOf(field);
            this.min = range[0];
            this.max = range[1];
            this.step = range[2];
            // 初值：布尔量随机，递增量取区间起点，其余取区间中段
            if (booleanField) {
                this.value = RANDOM.nextInt(2);
            } else if (increasing) {
                this.value = min;
            } else {
                this.value = min + (max - min) * (0.3 + 0.4 * RANDOM.nextDouble());
            }
        }

        MockValue next() {
            if (booleanField) {
                // 状态量：小概率翻转（模拟运行/故障状态偶发变化）
                if (RANDOM.nextDouble() < 0.2) {
                    value = 1.0 - value;
                }
                return this;
            }
            if (increasing) {
                value += step * (0.5 + RANDOM.nextDouble());
                return this;
            }
            double delta = (RANDOM.nextDouble() * 2 - 1) * step;
            value = Math.max(min, Math.min(max, value + delta));
            return this;
        }

        boolean isBoolean() {
            return booleanField;
        }

        boolean booleanValue() {
            return value >= 0.5;
        }

        double numberValue() {
            return booleanField ? 0 : value;
        }
    }

    /** 状态量字段名 */
    private static boolean isBooleanField(String field) {
        switch (field) {
            case "running":
            case "fault":
            case "softFault":
            case "pumpRunning":
            case "softenerRunning":
            case "systemEnabled":
            case "autoMode":
                return true;
            default:
                return false;
        }
    }

    /** 单调递增字段名（运行时长、日累计冷量） */
    private static boolean isIncreasingField(String field) {
        return "hours".equals(field) || "dailyEnergy".equals(field);
    }

    /** 字段名 -> {min, max, step} */
    private static double[] rangeOf(String field) {
        switch (field) {
            case "supplyTemp":        return new double[]{7, 11, 0.1};     // 供水温度 ℃
            case "returnTemp":        return new double[]{12, 17, 0.1};    // 回水温度 ℃
            case "supplyPressure":    return new double[]{0.3, 0.6, 0.005};// 供水压力 MPa
            case "returnPressure":    return new double[]{0.2, 0.4, 0.005};// 回水压力 MPa
            case "pressure":          return new double[]{0.3, 0.6, 0.005};// 补水压力 MPa
            case "flow":              return new double[]{80, 300, 2};     // 瞬时流量 m³/h
            case "frequency":         return new double[]{20, 50, 0.5};    // 频率反馈 Hz
            case "fanFrequency":      return new double[]{20, 50, 0.5};    // 风机频率 Hz
            case "power":             return new double[]{50, 600, 5};     // 功率 kW
            case "totalPower":        return new double[]{50, 600, 5};     // 总功率分量 kW
            case "coolingCapacity":   return new double[]{300, 2000, 20};  // 制冷量 kW
            case "load":              return new double[]{30, 100, 1};     // 负荷 %
            case "loadRate":          return new double[]{30, 100, 1};     // 负荷率 %
            case "level":             return new double[]{20, 80, 1};      // 液位 %
            case "tankLevel":         return new double[]{20, 80, 1};      // 水箱液位 %
            case "cop":               return new double[]{3.5, 6.5, 0.05}; // COP
            case "powerSavingRate":   return new double[]{-10, 30, 0.5};   // 节能率 %
            case "copImprovement":    return new double[]{-5, 20, 0.5};    // COP 提升率 %
            case "forecastEnergy":    return new double[]{1000, 5000, 50}; // 预测能耗 kWh
            case "approachSetpoint":  return new double[]{3, 8, 0.1};      // 趋近温度设定 ℃
            case "hours":             return new double[]{100, 100000, 0.05}; // 运行时长 h（递增）
            case "dailyEnergy":       return new double[]{0, 100000, 0.8}; // 日累计冷量 kWh（递增）
            case "alarmCount":        return new double[]{0, 3, 0.2};      // 告警数（取整展示）
            case "controlMode":
            case "controlSource":
            case "startMode":
            case "forceCommand":
            case "forceCount":
            case "outputCount":
            case "startOrder":
            case "pumpOrder":         return new double[]{0, 10, 0.2};     // 模式/命令类（取整展示）
            default:                  return new double[]{0, 100, 1};
        }
    }
}
