package org.jeecg.modules.fwbz.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.mdm.dto.DeviceStaticDataDto;
import org.jeecg.modules.fwbz.mdm.entity.DeviceStaticData;
import org.jeecg.modules.fwbz.main.vo.DeviceStaticDataVo;

import java.util.List;

public interface IDeviceStaticDataService extends IService<DeviceStaticData> {

    List<DeviceStaticDataVo> list(String type, Long deviceId);

    boolean save(DeviceStaticDataDto data);
}
