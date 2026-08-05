package org.jeecg.modules.fwbz.interfaceStatistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 接口请求记录
 */
@Data
@TableName("table_interface_history")
public class InterfaceHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属系统
     */
    private Long systemId;

    /**
     * 接口地址
     */
    private String interfacePath;

    /**
     * 请求日期
     */
    private Date clinetDate;

    /**
     * 请求时间
     */
    private Date clinetTime;

    /**
     * 响应时间ms
     */
    private Long responseTime;

    /**
     * 数据大小kb
     */
    private Double dataSize;
}
