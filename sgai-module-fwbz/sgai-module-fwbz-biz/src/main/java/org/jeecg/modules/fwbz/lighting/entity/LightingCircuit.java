package org.jeecg.modules.fwbz.lighting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 照明回路信息
 */
@Data
@TableName("lighting_circuit")
public class LightingCircuit {

    public static final String COMSTAT_ONLINE = "在线";

    public static final String COMSTAT_OFFLINE = "离线";

    public static final String STATUS_ON = "开启";

    public static final String STATUS_OFF = "关闭";

    public static final Map<String,String> STATUS_MAP = Map.of(
            "100", "开启",
            "0", "关闭"
    );

    @TableId(type= IdType.AUTO)
    private Long id;

    /**
     * 回路名称
     */
    private String circuitName;

    /**
     * 回路编号
     */
    private String circuitCode;

    /**
     * 状态
     */
    private String status;

    /**
     * 所在区域
     */
    private Long areaId;

    /**
     * 区域编码
     */
    private String areaCode;

    /**
     * 回路开启时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 回路关闭时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closingTime;

    /**
     * 开启总时长
     */
    private Long allDuration;

    /**
     * 操作人
     */
    private String operatorBy;

    /**
     * 操作时间
     */
    private LocalDateTime operatorTime;

    /**
     * 区域名称
     */
    @TableField(exist = false)
    private String areaName;

    /**
     * 通讯状态，在线、离线
     */
    private String comstat;

    /**
     * 空间名称
     */
    @TableField(exist = false)
    private String spaceName;

}
