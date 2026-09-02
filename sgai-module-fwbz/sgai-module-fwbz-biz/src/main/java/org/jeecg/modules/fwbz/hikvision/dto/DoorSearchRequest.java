package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 海康门禁点查询请求参数（/api/resource/v2/door/search）
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class DoorSearchRequest {

    /** 当前页码 */
    private Integer pageNo;

    /** 分页大小 */
    private Integer pageSize;
}
