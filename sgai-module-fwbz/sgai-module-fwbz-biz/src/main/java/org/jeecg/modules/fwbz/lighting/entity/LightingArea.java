package org.jeecg.modules.fwbz.lighting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.modules.fwbz.permission.annotation.DataPermissionField;
import org.jeecg.modules.fwbz.permission.entity.RoleDataPermission;

import java.time.LocalDateTime;

/**
 * 照明区域
 */
@Data
@TableName("lighting_area")
public class LightingArea {

    /**
     * 主键
     */
    @TableId(type= IdType.AUTO)
    private Long id;

    /**
     * 空间。金安桥：1；一高炉：2；
     */
    @DataPermissionField(type = RoleDataPermission.TYPE_LIGHTING, value = "space")
    private String space;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 区域编码
     */
    private String areaCode;

    /**
     * 状态：
     */
    private String status;

    /**
     * 上次操作时间
     */
    @TableField(exist = false)
    private LocalDateTime lastOperationTime;

    /**
     * 上次操作人
     */
    @TableField(exist = false)
    private String lastOperationBy;

    /**
     * 1:区域回路、2:建筑回路
     */
    private String type;
    /**
     * 位置信息
     */
    private String location;
    /**
     * 监控地址
     */
    private String monitorAdr;
    /**
     * 备注
     */
    private String remark;

    /**
     * 场景启动时间，单位：秒
     */
    private Long allDuration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closingTime;

    /**
     * 场景开启码
     */
    private String openCode;

    /**
     * 场景关闭码
     */
    private String closeCode;

    /**
     * 关联名称
     */
    private String relName;

    /**
     * 排序字段，升序排列
     */
    private Long sort;
}
