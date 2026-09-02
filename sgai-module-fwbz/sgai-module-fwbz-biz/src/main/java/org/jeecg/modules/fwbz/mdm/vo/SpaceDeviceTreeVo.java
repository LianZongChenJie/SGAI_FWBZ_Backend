package org.jeecg.modules.fwbz.mdm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.modules.fwbz.mdm.entity.Device;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 空间-设备树形模型（空间树下挂载设备）
 * @Author: jeecg-boot
 * @Date:   2025-02-20
 * @Version: V1.0
 */
@Data
@ApiModel(value="spaceDeviceTree对象", description="空间-设备树形模型")
public class SpaceDeviceTreeVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**空间id*/
    @ApiModelProperty(value = "空间id")
    private Long spaceId;

    /**空间名称*/
    @ApiModelProperty(value = "空间名称")
    private String spaceName;

    /**该空间下的设备*/
    @ApiModelProperty(value = "该空间下的设备")
    private List<Device> device;

    /**子空间节点*/
    @ApiModelProperty(value = "子空间节点")
    private List<SpaceDeviceTreeVo> child;
}
