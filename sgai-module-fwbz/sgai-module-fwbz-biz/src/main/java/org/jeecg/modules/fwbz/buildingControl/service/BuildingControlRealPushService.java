package org.jeecg.modules.fwbz.buildingControl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 楼控系统实时数据采集服务（读点 -> 更新设备属性值表）
 *
 * 通讯方式参考冷源 ColdSourceServerService：
 * 1. 从 device_attribute 表读取采集编码(acquisition_coding)为数字的记录，
 *    该值即楼控检测点ID(tagId)，构建 检测点ID -> 设备属性 索引；
 * 2. 通过楼控 pSpace SDK realReadList 按检测点ID(tagId)读取点位值，验证是否获取成功；
 * 3. 获取成功后根据返回的检测点ID(tagId) 更新 device_attribute 表：
 *    采集编码为该检测点ID 的记录，更新 value、gather_time；
 *    同时根据 device_id 更新对应设备运行状态为在线，并同步更新最后采集时间；
 *    其中 is_save=1 的属性按时间槽位保存到 device_attribute_history 历史表。
 *
 * 定时调度入口：{@link org.jeecg.modules.fwbz.buildingControl.job.BuildingControlRealPushJob}（每 15 分钟，cron 错峰执行）
 */
@Slf4j
@Service
public class BuildingControlRealPushService {

    @Autowired
    private BuildingControlServerService buildingControlServerService;

    @Autowired
    private IDeviceAttributeService deviceAttributeService;

    /**
     * 读取点位信息数据（读点）：按检测点ID(tagId)读取值
     * 每次从数据中取全部检测点ID，调用楼控读点接口并打印返回结果，验证是否获取成功；
     * 采集时间对齐到当前整十五分钟槽位（如 08:00:05 执行 -> 08:00:00），
     * 更新设备属性表，并根据 device_id 更新设备在线状态与最后采集时间；
     * 其中 is_save=1 的属性按时间槽位保存到 device_attribute_history 历史表。
     */
    public void readRealDataOnce() {
        List<DeviceAttribute> tagIds = getTagIds();
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
    private List<DeviceAttribute> getTagIds() {
        // 仅查询采集编码、设备id、属性id、是否存储历史(is_save)四列，避免全表返回完整实体对象（含大字段）导致内存峰值过高
        // 说明：属性id与设备id供告警检测(alarmDetection)使用，is_save 供历史存储过滤使用，必须一并查出
        List<DeviceAttribute> list = deviceAttributeService.list(new LambdaQueryWrapper<DeviceAttribute>()
                .select(DeviceAttribute::getAcquisitionCoding, DeviceAttribute::getId, DeviceAttribute::getDeviceId, DeviceAttribute::getIsSave)
                .isNotNull(DeviceAttribute::getAcquisitionCoding)
                .ne(DeviceAttribute::getAcquisitionCoding, ""));
        List<DeviceAttribute> tagIds = new ArrayList<>();
        for (DeviceAttribute attr : list) {
            String coding = attr.getAcquisitionCoding();
            if (coding == null || coding.trim().isEmpty()) {
                continue;
            }
            // 仅保留采集编码为数字的记录（该值即楼控检测点ID）
            try {
                Long.parseLong(coding.trim());
                tagIds.add(attr);
            } catch (NumberFormatException e) {
                // 非数字采集编码（如 gatewayAdr-bacnetAdr 格式）不参与楼控实时订阅
                log.debug("采集编码非数字，跳过楼控订阅: id={}, acquisitionCoding={}", attr.getId(), coding);
            }
        }
        return tagIds;
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
}
