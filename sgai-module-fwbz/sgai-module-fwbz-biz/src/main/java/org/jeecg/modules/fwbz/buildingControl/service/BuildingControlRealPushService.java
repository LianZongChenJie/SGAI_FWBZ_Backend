package org.jeecg.modules.fwbz.buildingControl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsSubRealData;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 楼控系统实时数据采集服务（读点 -> 更新设备属性值表）
 *
 * 通讯方式参考冷源 ColdSourceServerService：
 * 1. 启动时从 device_attribute 表读取采集编码(acquisition_coding)为数字的记录，
 *    该值即楼控检测点ID(tagId)，构建 检测点ID -> 设备属性 索引；
 * 2. 通过楼控 pSpace SDK realReadList 按检测点ID(tagId)读取点位值，
 *    验证是否获取成功；
 * 3. 获取成功后可根据返回的检测点ID(tagId) 更新 device_attribute 表：
 *    采集编码为该检测点ID 的记录，更新 value、gather_time，并触发 MQ 消息与历史保存。
 */
@Slf4j
@Service
public class BuildingControlRealPushService {

    @Autowired
    private BuildingControlServerService buildingControlServerService;

    @Autowired
    private IDeviceAttributeService deviceAttributeService;

    /** 检测点ID(tagId) -> 关联的设备属性列表；buildIndex 后只读 */
    private volatile Map<Long, List<DeviceAttribute>> tag2Attributes = Collections.emptyMap();

    /** 楼控读点定时任务：每分钟执行一次 */
    private ScheduledExecutorService readScheduler;

    @PostConstruct
    public void init() {
        buildIndex();
        // 每分钟读取一次点位信息数据（读点）：按检测点ID(tagId)读取值，并更新 device_attribute 表
        readScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "building-control-read");
            t.setDaemon(true);
            return t;
        });
        readScheduler.scheduleWithFixedDelay(this::readRealDataOnce, 0, 1, TimeUnit.MINUTES);
        log.info("楼控读点定时任务已启动: 每 {} 分钟执行一次", 1);
    }

    /**
     * 读取点位信息数据（读点）：按检测点ID(tagId)读取值
     * 从索引取全部检测点ID，调用楼控读点接口并打印返回结果，验证是否获取成功
     */
    public void readRealDataOnce() {
        List<Long> tagIds = new ArrayList<>(tag2Attributes.keySet());
        if (tagIds.isEmpty()) {
            log.warn("device_attribute 中没有配置数字采集编码(检测点ID)的属性，跳过楼控读点");
            return;
        }
        log.info("楼控读点开始: 检测点数={}, tagIds={}", tagIds.size(), tagIds);
        if (buildingControlServerService.realReadList(tagIds)) {
            log.info("楼控读点获取成功");
        } else {
            log.warn("楼控读点获取失败");
        }
    }

    /**
     * 构建 检测点ID -> 设备属性 索引：
     * device_attribute.acquisition_coding 为数字(=检测点ID) 的属性参与楼控实时订阅。
     */
    private void buildIndex() {
        List<DeviceAttribute> list = deviceAttributeService.list(new LambdaQueryWrapper<DeviceAttribute>()
                .isNotNull(DeviceAttribute::getAcquisitionCoding)
                .ne(DeviceAttribute::getAcquisitionCoding, ""));
        Map<Long, List<DeviceAttribute>> map = new HashMap<>();
        for (DeviceAttribute attr : list) {
            String coding = attr.getAcquisitionCoding();
            if (coding == null || coding.trim().isEmpty()) {
                continue;
            }
            try {
                Long tagId = Long.valueOf(coding.trim());
                map.computeIfAbsent(tagId, k -> new ArrayList<>()).add(attr);
            } catch (NumberFormatException e) {
                // 非数字采集编码（如 gatewayAdr-bacnetAdr 格式）不参与楼控实时订阅
                log.debug("采集编码非数字，跳过楼控订阅: id={}, acquisitionCoding={}", attr.getId(), coding);
            }
        }
        this.tag2Attributes = Collections.unmodifiableMap(map);
        log.info("楼控实时订阅索引构建完成: 订阅检测点数={}, 关联属性数={}", map.size(), list.size());
    }








    /**
     * 值转换：Boolean -> "1"/"0"，整数不带小数，BigDecimal 去尾零，其余 toString
     */
    public static String convertValue(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean) {
            return (Boolean) v ? "1" : "0";
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).stripTrailingZeros().toPlainString();
        }
        if (v instanceof Long || v instanceof Integer || v instanceof Short || v instanceof Byte) {
            return v.toString();
        }
        if (v instanceof Double || v instanceof Float) {
            double d = ((Number) v).doubleValue();
            if (d == Math.rint(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        return v.toString();
    }

    /**
     * BOOL 类型值转换：统一转换为 "0"/"1"
     * 兼容 Boolean、数值（0/1，含 0.0/1.0）、字符串（"true"/"false"、"on"/"off"、"0"/"1"）
     */
    public static String convertBoolValue(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean) {
            return (Boolean) v ? "1" : "0";
        }
        if (v instanceof Number) {
            return ((Number) v).doubleValue() == 0 ? "0" : "1";
        }
        String s = v.toString().trim();
        if ("true".equalsIgnoreCase(s) || "1".equals(s) || "on".equalsIgnoreCase(s)) {
            return "1";
        }
        if ("false".equalsIgnoreCase(s) || "0".equals(s) || "off".equalsIgnoreCase(s)) {
            return "0";
        }
        return s;
    }

    private static int subIdOf(PsSubRealData data) {
        Long subId = data.getSubId();
        return subId == null ? 0 : subId.intValue();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
