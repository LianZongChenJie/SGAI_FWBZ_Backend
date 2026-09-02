package org.jeecg.modules.fwbz.activeMeetReport.vo;

import lombok.Data;

/**
 * 活动报告统计卡片VO
 */
@Data
public class StatCardVO {

    /**
     * 标题
     */
    private String title;

    /**
     * 数值（支持数字和字符串，如 "AI"）
     */
    private Object value;

    /**
     * 上下文描述（如 ↑3、↓2、需出具报告、自动+人工）
     */
    private String context;
}
