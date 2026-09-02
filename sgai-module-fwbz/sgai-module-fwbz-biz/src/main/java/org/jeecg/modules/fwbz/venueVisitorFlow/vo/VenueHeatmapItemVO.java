package org.jeecg.modules.fwbz.venueVisitorFlow.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 各场馆热力图数据项
 *
 * @author fwbz
 */
@Data
public class VenueHeatmapItemVO {

    /** 场馆ID */
    private Long id;

    /** 场馆名称 */
    private String name;

    /** 经度 */
    private BigDecimal lng;

    /** 纬度 */
    private BigDecimal lat;

    /** 当前在场人数（today_now_count） */
    private Long used;

    /** 今日峰值（max_count），作为容量参考 */
    private Long total;

    /** 剩余容量 */
    private Long shengyu;

    /** 拥挤状态：宽松/适中/拥挤 */
    private String state;

    /** 剩余比例 = shengyu/total */
    private BigDecimal saturation;

    /** 使用率百分比 = used/total * 100 */
    private BigDecimal usageRate;

    /** 使用比例 = used/total */
    private BigDecimal usedRate;
}
