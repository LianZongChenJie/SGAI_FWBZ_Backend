package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康反向控制门禁点响应（/api/acs/v1/door/doControl）
 * <p>data 为反控结果信息数组，逐项对应请求中的门禁点。</p>
 *
 * @author fwbz
 */
@Data
public class DoorControlResponse {

    /** 反控结果信息数组 */
    private List<DoorControlItem> data;

    /**
     * 反控结果信息
     */
    @Data
    public static class DoorControlItem {

        /** 门禁点唯一标识 */
        private String doorIndexCode;

        /** 反控结果码，0标识反控成功，其他表示失败 */
        private Integer controlResultCode;

        /** 与controlResultCode对应的描述 */
        private String controlResultDesc;
    }
}
