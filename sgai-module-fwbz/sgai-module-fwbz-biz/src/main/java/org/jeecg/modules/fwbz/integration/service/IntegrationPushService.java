package org.jeecg.modules.fwbz.integration.service;

import org.jeecg.modules.fwbz.mdm.entity.Device;
import java.util.List;

public interface IntegrationPushService {
    /** 设备增删改后调用；op = UPSERT / DELETE */
    void pushDevices(List<Device> devices, String op);
}
