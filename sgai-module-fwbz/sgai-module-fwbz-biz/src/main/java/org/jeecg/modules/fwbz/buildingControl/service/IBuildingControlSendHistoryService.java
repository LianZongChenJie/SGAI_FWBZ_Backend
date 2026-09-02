package org.jeecg.modules.fwbz.buildingControl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.buildingControl.entity.BuildingControlSendHistory;

/**
 * 楼控发送控制历史服务
 */
public interface IBuildingControlSendHistoryService extends IService<BuildingControlSendHistory> {

    /**
     * 保存楼控写点控制历史
     *
     * @param attributeId   属性id（device_attribute.id）
     * @param deviceId      设备id（device_attribute.device_id）
     * @param attributeName 属性名称（device_attribute.attribute_name）
     * @param value         控制值（写点下发值）
     * @param controlBy     控制人
     */
    void saveControlHistory(Long attributeId, Long deviceId, String attributeName, String value, String controlBy);
}
