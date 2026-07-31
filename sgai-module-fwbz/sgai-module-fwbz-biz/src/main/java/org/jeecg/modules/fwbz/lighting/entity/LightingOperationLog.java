package org.jeecg.modules.fwbz.lighting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 照明-控制记录
 */
@Data
@TableName("lighting_operation_log")
public class LightingOperationLog {

    /**
     * 关联类型：区域
     */
    public static final String REL_TYPE_AREA = "区域";
    /**
     * 关联类型：回路
     */
    public static final String REL_TYPE_CIRCUIT = "回路";

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 关联类型，区域：1、回路：2
     */
    private String relType;
    /**
     * 关联id
     */
    private Long relId;
    /**
     * 名称
     */
    private String name;
    /**
     * 操作类型
     */
    private String operationType;
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作人
     */
    private String operationBy;

}
