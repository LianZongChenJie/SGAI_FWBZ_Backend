package org.jeecg.modules.fwbz.buildingControl.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 外部系统 UpdRealData 接口响应
 */
@Data
public class UpdRealDataResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private Integer code;

    /** 错误描述 */
    private String mesg;

    /** 响应数据 */
    private UpdRealDataResult data;

    /**
     * 响应数据详情
     */
    @Data
    public static class UpdRealDataResult implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 数量 */
        private Integer count;

        /** 更新错误数组 */
        private List<UpdRealDataErrorItem> values;
    }

    /**
     * 更新错误项
     */
    @Data
    public static class UpdRealDataErrorItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 点ID */
        private Long id;

        /** 错误码 */
        private Integer code;

        /** 错误消息 */
        private String mesg;
    }
}
