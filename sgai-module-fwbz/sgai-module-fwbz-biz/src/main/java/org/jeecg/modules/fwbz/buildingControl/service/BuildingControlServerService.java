package org.jeecg.modules.fwbz.buildingControl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunwayland.pspace.entity.Base;
import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.enums.PsDataTypeEnum;
import com.sunwayland.pspace.enums.PsErrorCodeEnum;
import com.sunwayland.pspace.enums.PsQualityEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.alarm.service.IAlarmRecordService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceServerService;
import org.jeecg.modules.fwbz.mdm.constant.DeviceConstant;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.mq.send.MqSendService;
import org.jeecg.modules.fwbz.mqtt.mapper.MDeviceAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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

    @Autowired
    private IDeviceService deviceService;
    @Autowired
    private final MqSendService mqSendService;

    private final IAlarmRecordService alarmRecordService;

    public BuildingControlServerService(MqSendService mqSendService, IAlarmRecordService alarmRecordService) {
        this.mqSendService = mqSendService;
        this.alarmRecordService = alarmRecordService;
    }

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
     * 的记录，更新 value、gather_time（值按 dataType 处理：BOOL 类型统一转换成 0/1）。
     * 楼控数据仅更新设备属性表，不保存历史数据。
     * <p>
     * 全部点位读取完成后统一批量更新，避免逐点 UPDATE 造成大量数据库交互
     * （1029 个点由 2000+ 次 DB 往返降为 2~3 次）。
     *
     * @param updateList   检测点ID列表（对应 device_attribute.acquisition_coding）
     * @param dataTime 采集时间（保留参数，兼容调用方；仅用于更新时间语义）
     * @return true 表示读取流程完成（逐点读取，单个失败仅告警，不影响后续）
     */
    public boolean realReadList(List<DeviceAttribute> updateList, LocalDateTime dataTime) {
        // 全部点位读取完成后统一批量更新，不再分批
        List<DeviceAttribute> newUpdateList = new ArrayList<>();
        int readSuccess = 0;
        for (DeviceAttribute item : updateList)  {
            try {
                PsResult<PsData> result = coldSourceServerService.connect().realRead(Long.valueOf(item.getAcquisitionCoding().trim()));
                // 成功则返回状态码 PSRET_OK
                if (Objects.equals(result.getCode(), PsErrorCodeEnum.PSRET_OK)) {
                    readSuccess++;
                    List<PsData> dataList = result.getData();
                    if (dataList != null && !dataList.isEmpty()) {
                        // 解析值与采集时间，加入更新列表
                        // BOOL 类型统一转换为 0/1，避免 "true"/"false" 字符串参与告警检测 BigDecimal 解析时抛异常
                        PsData psData = dataList.get(0);
                        String value = PsDataTypeEnum.BOOL.equals(psData.getDataType())
                                ? BuildingControlRealPushService.convertBoolValue(psData.getValue())
                                : BuildingControlRealPushService.convertValue(psData.getValue());
                        item.setValue(value);
                        newUpdateList.add(item);
                    }
                }
            } catch (Exception e) {
                log.warn("楼控读点异常: tagId={}", item.getAcquisitionCoding(), e);
            }
        }
        // 全部读取完成后统一批量更新（仅更新，不保存历史）
        if (!newUpdateList.isEmpty()) {
            flushUpdate(newUpdateList);
            // 读点成功后，根据 device_id 更新对应设备运行状态为在线，并更新最后采集时间
            updateDevicesOnline(newUpdateList, dataTime);
        }
        try{
            for(DeviceAttribute item : newUpdateList){
                alarmRecordService.alarmDetection(item.getDeviceId(),item.getId(),item.getValue());
            }
        }catch (Exception e){
            log.error("点位值变化消息发送失败",e);
        }
        log.info("楼控读点完成: 检测点数={}, 读取成功={}, 更新={}", newUpdateList.size(), readSuccess, updateList.size());
        return true;
    }

    /**
     * 读点成功后，根据设备属性关联的 device_id 更新对应设备的运行状态为在线，并同步更新最后采集时间
     * 链路：采集编码 -> device_attribute.device_id（去重）-> device 表 device_code -> 更新 run_state=在线、last_gather_time=采集时间
     *
     * @param updateList 读取成功待更新的属性列表
     * @param dataTime   采集时间（15分钟对齐槽位后的时间）
     */
    private void updateDevicesOnline(List<DeviceAttribute> updateList, LocalDateTime dataTime) {
        try {
            // 1. 取读取成功的采集编码（去重）
            List<String> codings = updateList.stream()
                    .map(DeviceAttribute::getAcquisitionCoding)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (codings.isEmpty()) {
                return;
            }
            // 2. 根据采集编码查询关联的 device_id（去重）
            // 注意：MDeviceAttributeMapper 泛型为 mqtt.entity.DeviceAttribute，与 mdm.entity.DeviceAttribute 同名
            List<org.jeecg.modules.fwbz.mqtt.entity.DeviceAttribute> attrs = mdeviceAttributeMapper.selectList(
                    new LambdaQueryWrapper<org.jeecg.modules.fwbz.mqtt.entity.DeviceAttribute>()
                            .select(org.jeecg.modules.fwbz.mqtt.entity.DeviceAttribute::getDeviceId)
                            .in(org.jeecg.modules.fwbz.mqtt.entity.DeviceAttribute::getAcquisitionCoding, codings)
                            .isNotNull(org.jeecg.modules.fwbz.mqtt.entity.DeviceAttribute::getDeviceId));
            Set<Long> deviceIds = attrs.stream()
                    .map(org.jeecg.modules.fwbz.mqtt.entity.DeviceAttribute::getDeviceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (deviceIds.isEmpty()) {
                return;
            }
            // 3. 根据 device_id 查设备编码
            List<Device> devices = deviceService.list(
                    new LambdaQueryWrapper<Device>()
                            .select(Device::getDeviceCode)
                            .in(Device::getId, deviceIds));
            // 4. 更新运行状态为在线，并同步更新最后采集时间
            for (Device device : devices) {
                if (StringUtils.isNotBlank(device.getDeviceCode())) {
                    deviceService.updateStatus(device.getDeviceCode(), DeviceConstant.DEVICE_RUN_STATA_ONLINE);
                    if (dataTime != null) {
                        deviceService.updateLastGatherTime(device.getDeviceCode(), dataTime);
                    }
                }
            }
            log.info("楼控读点成功，设备在线状态与最后采集时间更新完成: 设备数={}", devices.size());
        } catch (Exception e) {
            log.error("楼控读点成功，设备在线状态更新失败", e);
        }
    }

    /**
     * 批量更新 device_attribute：按采集编码(acquisition_coding) 批量更新值、采集时间，
     * 楼控数据仅更新不存储历史，故不分批、不保存历史。
     *
     * @param updateList 读取成功待更新的属性列表
     */
    /** 批量更新每批最大点数：达梦驱动对单条 SQL 绑定参数数量有限制，超出会报"序列号无效" */
    private static final int UPDATE_BATCH_SIZE = 500;

    private void flushUpdate(List<DeviceAttribute> updateList) {
        try {
            // 达梦驱动对单条 SQL 参数数量有限制，按批次拆分更新避免"序列号无效"异常
            for (int i = 0; i < updateList.size(); i += UPDATE_BATCH_SIZE) {
                List<DeviceAttribute> batch = updateList.subList(i,
                        Math.min(i + UPDATE_BATCH_SIZE, updateList.size()));
                mdeviceAttributeMapper.updateValueByIds(batch);
            }
            log.info("楼控读点批量更新属性成功: 点数={}", updateList.size());
        } catch (Exception e) {
            log.error("楼控读点批量更新属性失败: 点数={}", updateList.size(), e);
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
