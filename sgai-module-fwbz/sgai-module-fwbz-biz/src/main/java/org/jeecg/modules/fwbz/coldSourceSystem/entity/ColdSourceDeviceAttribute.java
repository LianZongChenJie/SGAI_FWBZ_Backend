package org.jeecg.modules.fwbz.coldSourceSystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 冷源设备属性表（FWBZ.cold_source_device_attribute）
 */
@Data
@TableName("\"FWBZ\".\"cold_source_device_attribute\"")
public class ColdSourceDeviceAttribute {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 所属部门编码 */
    private String sysOrgCode;

    /** 关联 cold_source_device.id */
    private Long deviceId;

    /** 属性名 */
    private String attrName;

    /** 点位短名(object-name末段) */
    private String attrCode;

    /** pSpace 通讯点位ID */
    private Long tagid;

    /** 点位键名 */
    private String keyname;

    /** 数据类型 */
    private String dataType;

    /** 单位 */
    private String unit;

    /** 枚举JSON */
    private String valueEnum;

    /** 枚举JSON(CLOB) */
    private String objectDef;

    /** 是否枚举 */
    private Integer isEnum;

    /** 属性类型 */
    private String attrType;

    /** 排序 */
    private Integer sortOrder;

    /** 采集值 */
    private String value;

    /** 采集时间 */
    private LocalDateTime gatherTime;
}
