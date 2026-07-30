package org.jeecg.modules.fwbz.dataInterface.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.entity.BaseEntity;

import java.util.Date;

/**
 * 接口信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("table_interface_info")
public class InterfaceInfo extends BaseEntity {

    /**
     * 状态。启用
     */
    public static final String STATE_ENABLE = "1";
    /**
     * 状态。禁用
     */
    public static final String STATE_DISABLE = "0";

    /**
     * 系统名称
     */
    private String sysName;

    /**
     * 接口地址
     */
    private String interfacePath;

    /**
     * 协议类型ID
     */
    private Long protocolTypeId;

    /**
     * 状态。启用：1；禁用：0
     */
    private Integer state;

    /**
     * 最后心跳时间
     */
    private Date requestTime;

    /**
     * 响应时间(ms)
     */
    private Long responseTime;
}
