package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 海康门禁点事件查询请求参数（/api/acs/v2/door/events）
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class DoorEventSearchRequest {

    /** 当前页码（pageNo>0） */
    private Integer pageNo;

    /** 每页展示数目（0<pageSize<=1000） */
    private Integer pageSize;

    /** 开始时间（事件开始时间，ISO8601格式，与endTime配对使用） */
    private String startTime;

    /** 结束时间（事件结束时间，ISO8601格式，与startTime配对使用，最大跨度3个月） */
    private String endTime;
}
