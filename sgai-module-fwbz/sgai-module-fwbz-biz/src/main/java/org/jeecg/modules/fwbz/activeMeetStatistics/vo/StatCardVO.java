package org.jeecg.modules.fwbz.activeMeetStatistics.vo;

import lombok.Data;

/**
 * 统计卡片VO
 */
@Data
public class StatCardVO {

    /**
     * 标题
     */
    private String title;

    /**
     * 数值
     */
    private Number value;

    /**
     * 上下文描述（如 ↑3、↓2、进行中、下周开始）
     */
    private String context;
}
