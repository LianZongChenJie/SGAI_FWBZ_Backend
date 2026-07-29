package org.jeecg.modules.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("integration_system_category")
@ApiModel("对接系统-类别范围")
public class IntegrationSystemCategory {

    @TableId(type = IdType.NONE)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("对接系统id")
    private String systemId;

    @ApiModelProperty("类别id")
    private String categoryId;
}
