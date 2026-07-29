package org.jeecg.modules.master.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
@ApiModel("设备导入DTO")
public class DeviceImportDTO {

    @Excel(name = "设备名称*")
    private String name;

    @Excel(name = "类别全称*")
    private String categoryFullName;

    @Excel(name = "空间全称*")
    private String spaceFullName;

    @Excel(name = "备注")
    private String remark;
}
