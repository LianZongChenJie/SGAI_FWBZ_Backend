package org.jeecg.modules.fwbz.buildingControl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.buildingControl.entity.BuildingControlSendHistory;
import org.jeecg.modules.fwbz.buildingControl.mapper.BuildingControlSendHistoryMapper;
import org.jeecg.modules.fwbz.buildingControl.service.IBuildingControlSendHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 楼控发送控制历史服务实现
 */
@Service
public class BuildingControlSendHistoryServiceImpl
        extends ServiceImpl<BuildingControlSendHistoryMapper, BuildingControlSendHistory>
        implements IBuildingControlSendHistoryService {

    @Override
    public void saveControlHistory(Long attributeId, Long deviceId, String attributeName, String value, String controlBy) {
        BuildingControlSendHistory history = new BuildingControlSendHistory();
        history.setAttributeId(attributeId);
        history.setDeviceId(deviceId);
        history.setAttributeName(attributeName);
        history.setValue(value);
        history.setControlBy(controlBy);
        history.setCollectionTime(LocalDateTime.now());
        super.save(history);
    }
}
