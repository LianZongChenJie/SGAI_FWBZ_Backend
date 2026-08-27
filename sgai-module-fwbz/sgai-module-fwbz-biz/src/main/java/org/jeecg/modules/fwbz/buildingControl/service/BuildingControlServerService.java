package org.jeecg.modules.fwbz.buildingControl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunwayland.pspace.entity.Base;
import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.enums.PsDataTypeEnum;
import com.sunwayland.pspace.enums.PsErrorCodeEnum;
import com.sunwayland.pspace.enums.PsQualityEnum;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceServerService;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeHistoryService;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.jeecg.modules.fwbz.mqtt.mapper.MDeviceAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 楼控系统(pSpace) Server API 服务
 * <p>
 * 连接流程（参考冷源 ColdSourceServerService）：
 * 1. PSpaceClient.getInstance(host, port, username, password) 获取客户端实例
 * 2. client.connect() 建立连接（幂等：已连接则直接返回，供订阅/写点复用同一长连接）
 * 3. 调用业务接口（返回 PsResult&lt;T&gt;，成功判断 result.isSuccess()，数据在 getData()）
 * <p>
 * 说明：服务启动时后台异步连接（失败仅告警不影响主系统），
 * 连接参数在 application.yml 的 fwbz.building-control 下配置。
 */
@Slf4j
@Service
public class BuildingControlServerService {

    @Autowired
    private ColdSourceServerService coldSourceServerService;

    @Autowired
    private  MDeviceAttributeMapper mdeviceAttributeMapper;

    /** 设备属性服务：用于回查属性 id、设备 id */
    @Autowired
    private IDeviceAttributeService deviceAttributeService;

    /** 设备属性历史服务：更新属性表的同时保存点位历史 */
    @Autowired
    private IDeviceAttributeHistoryService deviceAttributeHistoryService;


    /**
     * 更新点位信息数据（写点/控制），按检测点ID(tagId)写入值
     * 质量戳使用 232(WRITE_BY_CONTROL，通过下置组件写的值 GOOD)
     *
     * @param tagId 检测点ID（对应 device_attribute.acquisition_coding）
     * @param value 要写入的值（字符串按点位类型转换；其他类型原样透传，如 Boolean/Number）
     * @return PsResult：成功时 isSuccess()=true
     */
    public PsResult<Base> realWrite(Long tagId, Object value) {
        if (tagId == null) {
            throw new IllegalArgumentException("tagId 不能为空");
        }
        if (value == null) {
            throw new IllegalArgumentException("value 不能为空");
        }
        PsResult<Base> result;
        if (value instanceof String) {
            result = coldSourceServerService.connect().realWrite(tagId, (String) value, PsQualityEnum.WRITE_BY_CONTROL, System.currentTimeMillis());
        } else {
            result = coldSourceServerService.connect().realWrite(tagId, new PsData(value, PsQualityEnum.WRITE_BY_CONTROL));
        }
        if (result.isSuccess()) {
            log.info("楼控写点成功: tagId={}, value={}, quality=232(WRITE_BY_CONTROL)", tagId, value);
        } else {
            log.error("楼控写点失败: tagId={}, value={}, code={}", tagId, value, result.getCode());
        }
        return result;
    }

    /**
     * 读取点位信息数据（读点），按检测点ID(tagId)读取值
     * <p>
     * 读取成功后更新 device_attribute 表：采集编码(acquisition_coding) 为该检测点ID(tagId)
     * 的记录，更新 value、gather_time（值按 dataType 处理：BOOL 类型统一转换成 0/1）；
     * 同时将点位值写入 device_attribute_history 历史表（采集时间对齐到当前整十五分钟槽位，
     * 同一属性同一槽位已有历史数据则更新）。
     * <p>
     * 全部点位读取完成后统一批量更新，避免逐点 UPDATE 造成大量数据库交互
     * （1029 个点由 2000+ 次 DB 往返降为 2~3 次）。
     *
     * @param tagIds 检测点ID列表（对应 device_attribute.acquisition_coding）
     * @return true 表示读取流程完成（逐点读取，单个失败仅告警，不影响后续）
     */
    public boolean realReadList(List<Long> tagIds) {
        return realReadList(tagIds, BuildingControlRealPushService.alignTo15MinuteSlot(LocalDateTime.now()));
    }

    /**
     * 读取点位信息数据（读点），按检测点ID(tagId)读取值
     * <p>
     * 读取成功后更新 device_attribute 表：采集编码(acquisition_coding) 为该检测点ID(tagId)
     * 的记录，更新 value、gather_time（值按 dataType 处理：BOOL 类型统一转换成 0/1）；
     * 同时将点位值写入 device_attribute_history 历史表，采集时间使用传入的 dataTime
     * （已对齐到整十五分钟槽位，如 08:00:05 执行 -> dataTime = 08:00:00，
     * 同一属性同一槽位已有历史数据则更新）。
     * <p>
     * 全部点位读取完成后统一批量更新，避免逐点 UPDATE 造成大量数据库交互
     * （1029 个点由 2000+ 次 DB 往返降为 2~3 次）。
     *
     * @param tagIds   检测点ID列表（对应 device_attribute.acquisition_coding）
     * @param dataTime 采集时间（已对齐到当前整十五分钟槽位），用于写入历史表
     * @return true 表示读取流程完成（逐点读取，单个失败仅告警，不影响后续）
     */
    public boolean realReadList(List<Long> tagIds, LocalDateTime dataTime) {
        // 收集读取成功的数据，读完统一批量更新
        List<DeviceAttribute> updateList = new ArrayList<>();
        int readSuccess = 0;
        for (Long tagId : tagIds) {
            try {
                PsResult<PsData> result = coldSourceServerService.connect().realRead(tagId);
                // 成功则返回状态码 PSRET_OK
                if (Objects.equals(result.getCode(), PsErrorCodeEnum.PSRET_OK)) {
                    readSuccess++;
                    List<PsData> dataList = result.getData();
                    if (dataList != null && !dataList.isEmpty()) {
                        // 解析值与采集时间，加入批量更新列表
                        DeviceAttribute attr = buildAttributeValue(tagId, dataList.get(0));
                        if (attr != null) {
                            updateList.add(attr);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("楼控读点异常: tagId={}", tagId, e);
            }
        }
        // 统一批量更新 device_attribute 对应的值(value) 与采集时间(gather_time)
        if (!updateList.isEmpty()) {
            try {
                // updateValueByIds 按采集编码(acquisition_coding) 批量更新
                mdeviceAttributeMapper.updateValueByIds(updateList);
                log.info("楼控读点批量更新属性成功: 更新点数={}", updateList.size());
            } catch (Exception e) {
                log.error("楼控读点批量更新属性失败: 更新点数={}", updateList.size(), e);
            }
            // 同步保存点位历史：按十五分钟槽位对齐的采集时间写入 device_attribute_history（有则更新）
            try {
                saveHistory(updateList, dataTime);
            } catch (Exception e) {
                log.error("楼控读点保存历史失败: 更新点数={}", updateList.size(), e);
            }
        }
        log.info("楼控读点完成: 检测点数={}, 读取成功={}, 更新={}", tagIds.size(), readSuccess, updateList.size());
        return true;
    }

    /**
     * 保存点位历史：读取返回的属性仅含采集编码/值/采集时间（无 id、deviceId），
     * 需回查 device_attribute 补充属性 id、设备 id 后，写入 device_attribute_history
     * （采集时间对齐到十五分钟槽位，同一属性同一槽位已有历史数据则更新）。
     *
     * @param updateList 读取成功待更新的属性列表（仅含采集编码、值、采集时间）
     * @param dataTime   采集时间（已对齐到当前整十五分钟槽位）
     */
    private void saveHistory(List<DeviceAttribute> updateList, LocalDateTime dataTime) {
        if (updateList == null || updateList.isEmpty() || dataTime == null) {
            return;
        }
        Set<String> codings = updateList.stream()
                .map(DeviceAttribute::getAcquisitionCoding)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (codings.isEmpty()) {
            return;
        }
        // 回查 device_attribute 获取属性 id、设备 id（按采集编码索引）
        Map<String, DeviceAttribute> attrMap = deviceAttributeService.list(
                        new LambdaQueryWrapper<DeviceAttribute>()
                                .in(DeviceAttribute::getAcquisitionCoding, codings))
                .stream()
                .collect(Collectors.toMap(DeviceAttribute::getAcquisitionCoding, Function.identity(), (a, b) -> a));
        List<DeviceAttribute> historyAttrs = new ArrayList<>();
        for (DeviceAttribute item : updateList) {
            DeviceAttribute attr = attrMap.get(item.getAcquisitionCoding());
            if (attr == null || attr.getId() == null || attr.getDeviceId() == null) {
                continue;
            }
            attr.setValue(item.getValue());
            historyAttrs.add(attr);
        }
        if (!historyAttrs.isEmpty()) {
            deviceAttributeHistoryService.saveAttributeHistory(historyAttrs, dataTime);
        }
    }

    /**
     * 将读取返回的测点数据解析为待更新的设备属性（仅填充采集编码、值、采集时间）。
     * 值根据 dataType 处理：dataType=BOOL 时统一转换成 0/1，其余走通用转换 {@link BuildingControlRealPushService#convertValue}。
     *
     * @param tagId 检测点ID（对应 device_attribute.acquisition_coding）
     * @param data  读取返回的测点数据
     * @return 待更新的属性数据；解析失败返回 null
     */
    private DeviceAttribute buildAttributeValue(Long tagId, PsData data) {
        try {
            // 根据 dataType 处理：BOOL 类型统一转换成 0/1，其余走通用转换
            String value = PsDataTypeEnum.BOOL.equals(data.getDataType())
                    ? BuildingControlRealPushService.convertBoolValue(data.getValue())
                    : BuildingControlRealPushService.convertValue(data.getValue());
            // timestamp 为毫秒时间戳，转换为采集时间
            Long timestamp = data.getTimestamp();
            LocalDateTime collectionTime = timestamp == null ? null
                    : LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            DeviceAttribute attr = new DeviceAttribute();
            attr.setAcquisitionCoding(String.valueOf(tagId));
            attr.setValue(value);
            attr.setGatherTime(collectionTime);
            return attr;
        } catch (Exception e) {
            log.warn("楼控读点解析数据失败: tagId={}, data={}", tagId, data, e);
            return null;
        }
    }
}
