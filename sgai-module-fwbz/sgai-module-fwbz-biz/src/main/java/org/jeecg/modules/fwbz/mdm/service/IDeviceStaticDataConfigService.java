package org.jeecg.modules.fwbz.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.mdm.entity.DeviceStaticDataConfig;

import java.util.List;

public interface IDeviceStaticDataConfigService extends IService<DeviceStaticDataConfig> {

    List<DeviceStaticDataConfig> list(DeviceStaticDataConfig param);

    List<DeviceStaticDataConfig> findByType(String type);
}
