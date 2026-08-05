package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 海康门禁设备查询请求参数（/api/resource/v2/acsDevice/search）
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class AcsDeviceSearchRequest {

    /** 当前页码 */
    private Integer pageNo;

    /** 分页大小 */
    private Integer pageSize;
}
