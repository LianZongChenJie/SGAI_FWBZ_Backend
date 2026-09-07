package org.jeecg.modules.fwbz.operationSupport.vo;

import lombok.Data;

/**
 * 设备分类统计VO
 */
@Data
public class EquipmentCategoryStatisticsVo {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 总数
     */
    private Long count;

    /**
     * 在线
     */
    private Long online;

    /**
     * 离线
     */
    private Long offline;

    /**
     * 在线率（百分比整数，如100表示100%）
     */
    private Integer onlineRate;
}
