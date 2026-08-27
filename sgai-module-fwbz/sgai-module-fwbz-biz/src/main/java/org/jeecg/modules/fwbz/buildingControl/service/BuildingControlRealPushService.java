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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 楼控系统实时数据采集服务（读点 -> 更新设备属性值表 + 保存点位历史）
 *
 * 通讯方式参考冷源 ColdSourceServerService：
 * 1. 启动时从 device_attribute 表读取采集编码(acquisition_coding)为数字的记录，
 *    该值即楼控检测点ID(tagId)，构建 检测点ID -> 设备属性 索引；
 * 2. 每 15 分钟通过楼控 pSpace SDK realReadList 按检测点ID(tagId)读取点位值，
 *    验证是否获取成功；
 * 3. 获取成功后根据返回的检测点ID(tagId) 更新 device_attribute 表：
 *    采集编码为该检测点ID 的记录，更新 value、gather_time；
 *    同时将点位值写入 device_attribute_history 历史表，采集时间对齐到
 *    当前整十五分钟槽位（如 08:00:05 执行 -> collection_time = 08:00:00），
 *    同一属性同一槽位已有历史数据则更新。
 */
@Slf4j
@Service
public class BuildingControlRealPushService {

    @Autowired
    private BuildingControlServerService buildingControlServerService;

    @Autowired
    private IDeviceAttributeService deviceAttributeService;

    /** 楼控读点定时任务：每15分钟执行一次 */
    private ScheduledExecutorService readScheduler;

    @PostConstruct
    public void init() {

        // 每15分钟读取一次点位信息数据（读点）：按检测点ID(tagId)读取值，更新 device_attribute 表并保存点位历史
        readScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "building-control-read");
            t.setDaemon(true);
            return t;
        });
        readScheduler.scheduleWithFixedDelay(this::readRealDataOnce, 0, 15, TimeUnit.MINUTES);
        log.info("楼控读点定时任务已启动: 每 {} 分钟执行一次", 15);
    }

    /**
     * 读取点位信息数据（读点）：按检测点ID(tagId)读取值
     * 每次从数据中取全部检测点ID，调用楼控读点接口并打印返回结果，验证是否获取成功；
     * 采集时间对齐到当前整十五分钟槽位（如 08:00:05 执行 -> 08:00:00），
     * 更新设备属性表的同时写入 device_attribute_history 历史表（有则更新）。
     */
    public void readRealDataOnce() {
        List<Long> tagIds = getTagIds();
        if (tagIds.isEmpty()) {
            log.warn("device_attribute 中没有配置数字采集编码(检测点ID)的属性，跳过楼控读点");
            return;
        }
        // 时间对齐到当前整十五分钟槽位（如 08:00:05 执行 -> dataTime = 08:00:00）
        LocalDateTime dataTime = alignTo15MinuteSlot(LocalDateTime.now());
        if (!buildingControlServerService.realReadList(tagIds, dataTime)) {
            log.warn("楼控读点获取失败");
        }
    }

    /**
     * 将时间对齐到当前整十五分钟槽位：分钟向下取整到 15 的倍数，秒与纳秒清零
     * 如 08:00:05 -> 08:00:00，08:16:59 -> 08:15:00
     *
     * @param time 原始时间
     * @return 对齐后的时间；入参为 null 时返回 null
     */
    public static LocalDateTime alignTo15MinuteSlot(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        int slotMinute = (time.getMinute() / 15) * 15;
        return time.withMinute(slotMinute).withSecond(0).withNano(0);
    }

    /**
     * 获取全部楼控检测点ID(tagId)：每次直接从 device_attribute 表查询，
     * 取采集编码(acquisition_coding)为数字的记录，该值即楼控检测点ID。
     *
     * @return 去重后的检测点ID列表（保持查询顺序）
     */
    public List<Long> getTagIds() {
        List<DeviceAttribute> list = deviceAttributeService.list(new LambdaQueryWrapper<DeviceAttribute>()
                .isNotNull(DeviceAttribute::getAcquisitionCoding)
                .ne(DeviceAttribute::getAcquisitionCoding, ""));
        Set<Long> tagIds = new LinkedHashSet<>();
        for (DeviceAttribute attr : list) {
            String coding = attr.getAcquisitionCoding();
            if (coding == null || coding.trim().isEmpty()) {
                continue;
            }
            try {
                tagIds.add(Long.valueOf(coding.trim()));
            } catch (NumberFormatException e) {
                // 非数字采集编码（如 gatewayAdr-bacnetAdr 格式）不参与楼控实时订阅
                log.debug("采集编码非数字，跳过楼控订阅: id={}, acquisitionCoding={}", attr.getId(), coding);
            }
        }
        return new ArrayList<>(tagIds);
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
