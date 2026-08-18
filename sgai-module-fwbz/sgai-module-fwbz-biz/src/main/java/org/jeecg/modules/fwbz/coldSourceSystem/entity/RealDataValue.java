package org.jeecg.modules.fwbz.coldSourceSystem.entity;

import lombok.Data;

/**
 * pSpace WebApi /RealData 响应中的单点实时值
 *
 * 对应文档 RealData 响应 data.values[]：
 * pid: 点ID, name: 点长名, pv: 值, tm: 时间, qy: 质量戳
 */
@Data
public class RealDataValue {

    /** 点ID */
    private Long pid;

    /** 点长名 */
    private String name;

    /** 值（布尔/数字/字符串） */
    private Object pv;

    /** 时间（字符串时间或时间戳，取决于请求 timetype） */
    private Object tm;

    /** 质量戳（参考 pSpace 质量戳描述） */
    private Integer qy;
}
