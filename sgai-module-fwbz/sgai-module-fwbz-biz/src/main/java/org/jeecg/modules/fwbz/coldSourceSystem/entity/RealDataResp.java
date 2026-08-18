package org.jeecg.modules.fwbz.coldSourceSystem.entity;

import lombok.Data;

import java.util.List;

/**
 * pSpace WebApi /RealData 响应体
 *
 * 对应文档 RealData 响应：
 * <pre>
 * {
 *   "code": 0,
 *   "mesg": "succeed",
 *   "data": {
 *     "count": 2,
 *     "values": [ { "pid":.., "name":.., "pv":.., "tm":.., "qy":.. } ]
 *   }
 * }
 * </pre>
 */
@Data
public class RealDataResp {

    /** 错误码（0 为成功） */
    private Integer code;

    /** 错误描述 */
    private String mesg;

    /** 响应数据 */
    private BodyData data;

    @lombok.Data
    public static class BodyData {

        /** 数据总数量 */
        private Integer count;

        /** 实时值数组 */
        private List<RealDataValue> values;
    }

    /**
     * 是否调用成功
     */
    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
