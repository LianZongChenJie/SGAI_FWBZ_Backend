package org.jeecg.modules.fwbz.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.mdm.dto.DeviceAttributeHistoryQueryDto;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttributeHistory;

import java.util.Collection;
import java.util.List;

public interface IDeviceAttributeHistoryService extends IService<DeviceAttributeHistory> {
    List<DeviceAttributeHistory> listByAttributeId(DeviceAttributeHistoryQueryDto param);

    void saveAttributeHistory(Collection<DeviceAttribute> attributes);
}
