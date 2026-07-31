package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 海康监控点在线状态查询请求参数（/api/nms/v1/online/camera/get）
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class CameraOnlineRequest {

    /** 页码，范围 (0, ~)，不填默认为1 */
    private Integer pageNo;

    /** 页大小，范围 (0, 1000]，不填默认为10 */
    private Integer pageSize;
}
