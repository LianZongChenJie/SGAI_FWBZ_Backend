package org.jeecg.modules.fwbz.buildingControl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 外部系统 UpdRealData 接口请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdRealDataRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 更新数量 */
    private Integer count;

    /** 数据数组 */
    private List<UpdRealDataValue> values;

    /**
     * 单条数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdRealDataValue implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long tagid;
        private Object pv;
        private String tm;
        private Integer qy;
    }
}
