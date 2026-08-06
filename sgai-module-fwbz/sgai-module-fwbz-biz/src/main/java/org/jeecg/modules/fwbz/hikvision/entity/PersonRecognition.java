package org.jeecg.modules.fwbz.hikvision.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人员识别记录表
 *
 * @author fwbz
 */
@Data
@TableName("table_person_recognition")
public class PersonRecognition implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 识别时间 */
    @TableField("recognize_time")
    private LocalDateTime recognizeTime;

    /** 人员类型（员工/访客/VIP/临时人员/黑名单等） */
    @TableField("person_type")
    private String personType;

    /** 姓名 */
    @TableField("person_name")
    private String personName;

    /** 识别位置 */
    @TableField("recognize_location")
    private String recognizeLocation;

    /** 置信度 */
    @TableField("confidence")
    private BigDecimal confidence;

    /** 进出方向（进/出/未知） */
    @TableField("direction")
    private String direction;

    /** 所属场馆 */
    @TableField("venue")
    private String venue;

    /** 员工号 */
    @TableField("employee_no")
    private String employeeNo;

    /** 记录创建时间 */
    @TableField("gmt_create")
    private LocalDateTime gmtCreate;

    /** 记录更新时间 */
    @TableField("gmt_modified")
    private LocalDateTime gmtModified;
}
