package org.jeecg.modules.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("integration_log")
@ApiModel("对接日志")
public class IntegrationLog {

    @TableId(type = IdType.NONE)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("方向 PUSH/RECEIVE")
    private String direction;

    @ApiModelProperty("对接系统id")
    private String systemId;

    @ApiModelProperty("对接系统编码(冗余)")
    private String systemCode;

    @ApiModelProperty("类型 CATEGORY/SPACE/DEVICE")
    private String type;

    @ApiModelProperty("操作 UPSERT/DELETE/SNAPSHOT")
    private String op;

    @ApiModelProperty("批次id")
    private String batchId;

    @ApiModelProperty("数据条数")
    private Integer payloadCount;

    @ApiModelProperty("状态 SUCCESS/PARTIAL/FAIL")
    private String status;

    @ApiModelProperty("原始报文JSON(仅审计)")
    private String payload;

    @ApiModelProperty("失败原因/接收逐条拒绝明细")
    private String error;

    @ApiModelProperty("耗时毫秒")
    private Integer costMs;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
