package org.jeecg.modules.master.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("对接系统表单(新增/编辑/详情)")
public class IntegrationSystemForm {

    @ApiModelProperty("id（编辑/详情时有）")
    private String id;

    @ApiModelProperty("系统名称(必填)")
    private String name;

    @ApiModelProperty("系统编码(必填,唯一)")
    private String code;

    @ApiModelProperty("是否启用推送 0否1是")
    private Integer pushEnabled;

    @ApiModelProperty("推送目标URL")
    private String pushUrl;

    @ApiModelProperty("是否启用接收 0否1是")
    private Integer receiveEnabled;

    @ApiModelProperty("共享令牌")
    private String token;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("类别范围 categoryIds(必填,推送/接收共用)")
    private List<String> categoryIds;
}
