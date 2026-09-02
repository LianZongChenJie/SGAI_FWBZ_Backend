package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 海康门禁状态查询请求参数（/api/acs/v1/door/states）
 * <p>批量查询门禁状态，最多支持200个门禁点。</p>
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class DoorStatusRequest {

    /** 门禁点唯一标识列表，最多200个 */
    private List<String> doorIndexCodes;
}
