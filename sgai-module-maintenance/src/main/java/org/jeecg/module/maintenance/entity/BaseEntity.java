package org.jeecg.module.maintenance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class BaseEntity {

    /**
     * 更新人id
     */
    private String updatorId;
    /**
     * 更新人名称
     */
    private String updatorName;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private Integer version;
    /**
     * 创建人id
     */
    private String creatorId;
    /**
     * 创建人名称
     */
    private String creatorName;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(exist = false)
    private Integer page = 1;

    @TableField(exist = false)
    private Integer pagesize = 10;

}
