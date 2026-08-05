package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 海康门禁设备在线状态查询请求（/api/nms/v1/online/acs_device/get）
 * <p>批量查询设备在线状态，最多支持500个设备。</p>
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class AcsDeviceOnlineRequest {

    /** 设备编码列表，最大500 */
    private List<String> indexCodes;
}
