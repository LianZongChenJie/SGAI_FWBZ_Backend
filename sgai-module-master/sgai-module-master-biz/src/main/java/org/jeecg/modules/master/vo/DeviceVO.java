package org.jeecg.modules.master.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.Date;

@Data
@ApiModel("设备列表/导出VO")
public class DeviceVO {

    @ApiModelProperty("id")
    private String id;

    @Excel(name = "设备名称", width = 20)
    @ApiModelProperty("设备名称")
    private String name;

    @ApiModelProperty("类别id")
    private String categoryId;

    @Excel(name = "类别", width = 25)
    @ApiModelProperty("类别全称")
    private String categoryFullName;

    @ApiModelProperty("类别名称")
    private String categoryName;

    @ApiModelProperty("空间id")
    private String spaceId;

    @Excel(name = "空间", width = 25)
    @ApiModelProperty("空间全称")
    private String spaceFullName;

    @ApiModelProperty("空间名称")
    private String spaceName;

    @Excel(name = "备注", width = 30)
    @ApiModelProperty("备注")
    private String remark;

    @Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private Date createTime;
}
