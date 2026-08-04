package org.jeecg.modules.fwbz.venueVisitorFlow.vo;

import lombok.Data;

/**
 * 场馆客流统计卡片 VO
 * <p>对应前端四张卡片：今日总客流 / 当前在场 / 峰值客流 / 平均停留</p>
 *
 * @author fwbz
 */
@Data
public class VisitorFlowCardVO {

    /**
     * 标题
     */
    private String title;

    /**
     * 数值
     */
    private Number value;

    /**
     * 单位/后缀（如 h、%）
     */
    private String unit;

    /**
     * 上下文描述（如 ↑5.3% 较昨日、进行中）
     */
    private String context;
}
