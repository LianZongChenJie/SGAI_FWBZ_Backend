package org.jeecg.module.maintenance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 维保计划模板
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("device_maintenance_plan_model")
public class PlanModel extends BaseEntity{

    /** 主键. */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("序号")
    private Integer indexNum;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("数量")
    private Integer count;

    @ApiModelProperty("维保周期")
    private String cycle;

    @ApiModelProperty("是否关联设备")
    private Boolean associatedDevice;

    @ApiModelProperty("是否关联空间")
    private Boolean associatedSpace;

    @ApiModelProperty("是否关联设备位置")
    private Boolean associateLocation;

    @ApiModelProperty("厂家")
    private String factory;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("频率")
    private String frequency;

    @ApiModelProperty("持续时间")
    private Integer duration;

    @ApiModelProperty("负责人")
    private String principal;

    @ApiModelProperty("部门")
    private String department;

    @ApiModelProperty("部门id")
    private Long departmentId;

    @ApiModelProperty("年份")
    private Integer year;

    @ApiModelProperty("第1周")
    private String w1;

    @ApiModelProperty("第2周")
    private String w2;

    @ApiModelProperty("第3周")
    private String w3;

    @ApiModelProperty("第4周")
    private String w4;

    @ApiModelProperty("第5周")
    private String w5;

    @ApiModelProperty("第6周")
    private String w6;

    @ApiModelProperty("第7周")
    private String w7;

    @ApiModelProperty("第8周")
    private String w8;

    @ApiModelProperty("第9周")
    private String w9;

    @ApiModelProperty("第10周")
    private String w10;

    @ApiModelProperty("第11周")
    private String w11;

    @ApiModelProperty("第12周")
    private String w12;

    @ApiModelProperty("第13周")
    private String w13;

    @ApiModelProperty("第14周")
    private String w14;

    @ApiModelProperty("第15周")
    private String w15;

    @ApiModelProperty("第16周")
    private String w16;

    @ApiModelProperty("第17周")
    private String w17;

    @ApiModelProperty("第18周")
    private String w18;

    @ApiModelProperty("第19周")
    private String w19;

    @ApiModelProperty("第20周")
    private String w20;

    @ApiModelProperty("第21周")
    private String w21;

    @ApiModelProperty("第22周")
    private String w22;

    @ApiModelProperty("第23周")
    private String w23;

    @ApiModelProperty("第24周")
    private String w24;

    @ApiModelProperty("第25周")
    private String w25;

    @ApiModelProperty("第26周")
    private String w26;

    @ApiModelProperty("第27周")
    private String w27;

    @ApiModelProperty("第28周")
    private String w28;

    @ApiModelProperty("第29周")
    private String w29;

    @ApiModelProperty("第30周")
    private String w30;

    @ApiModelProperty("第31周")
    private String w31;

    @ApiModelProperty("第32周")
    private String w32;

    @ApiModelProperty("第33周")
    private String w33;

    @ApiModelProperty("第34周")
    private String w34;

    @ApiModelProperty("第35周")
    private String w35;

    @ApiModelProperty("第36周")
    private String w36;

    @ApiModelProperty("第37周")
    private String w37;

    @ApiModelProperty("第38周")
    private String w38;

    @ApiModelProperty("第39周")
    private String w39;

    @ApiModelProperty("第40周")
    private String w40;

    @ApiModelProperty("第41周")
    private String w41;

    @ApiModelProperty("第42周")
    private String w42;

    @ApiModelProperty("第43周")
    private String w43;

    @ApiModelProperty("第44周")
    private String w44;

    @ApiModelProperty("第45周")
    private String w45;

    @ApiModelProperty("第46周")
    private String w46;

    @ApiModelProperty("第47周")
    private String w47;

    @ApiModelProperty("第48周")
    private String w48;

    @ApiModelProperty("第49周")
    private String w49;

    @ApiModelProperty("第50周")
    private String w50;

    @ApiModelProperty("第51周")
    private String w51;

    @ApiModelProperty("第52周")
    private String w52;

    @ApiModelProperty("第53周")
    private String w53;

    @ApiModelProperty("维保类型")
    private String weibaoType;
    @ApiModelProperty("标签类型")
    private String labelType;
}
