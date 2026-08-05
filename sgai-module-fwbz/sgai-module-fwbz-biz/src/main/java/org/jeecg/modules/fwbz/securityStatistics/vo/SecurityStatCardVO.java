package org.jeecg.modules.fwbz.securityStatistics.vo;

import lombok.Data;

/**
 * 安防统计卡片VO
 */
@Data
public class SecurityStatCardVO {

    /**
     * 标题
     */
    private String title;

    /**
     * 数值（支持数字、"24/24" 等字符串形式）
     */
    private String value;

    /**
     * 上下文描述（如 ↑3、↓2、98.9% 在线率、100% 完成等）
     */
    private String context;
}
