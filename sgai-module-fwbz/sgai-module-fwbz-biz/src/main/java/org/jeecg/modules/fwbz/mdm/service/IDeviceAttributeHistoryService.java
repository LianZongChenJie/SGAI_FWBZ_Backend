package org.jeecg.modules.fwbz.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.mdm.dto.DeviceAttributeHistoryQueryDto;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttributeHistory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface IDeviceAttributeHistoryService extends IService<DeviceAttributeHistory> {
    List<DeviceAttributeHistory> listByAttributeId(DeviceAttributeHistoryQueryDto param);
    List<DeviceAttributeHistory> listByAttributeIds(DeviceAttributeHistoryQueryDto param);

    void saveAttributeHistory(Collection<DeviceAttribute> attributes);

    /**
     * 保存设备属性历史（按时间槽位对齐，存在则更新）
     * 采集时间使用传入的 dataTime（已对齐到整十五分钟槽位，如 08:00:05 执行 -> dataTime = 08:00:00），
     * 同一属性(attributeId) 同一槽位(collectionTime) 已有历史数据则更新 value，否则新增。
     *
     * @param attributes 设备属性集合（需含 id、deviceId、value）
     * @param dataTime   采集时间（已对齐到当前整十五分钟槽位）
     */
    void saveAttributeHistory(Collection<DeviceAttribute> attributes, LocalDateTime dataTime);
}
