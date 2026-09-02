package org.jeecg.modules.fwbz.venueVisitorFlow.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 区域热力图响应
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "区域热力图响应", description = "区域热力图返回数据")
public class AreaHeatResponseVO {

    @ApiModelProperty("最大权重")
    private Integer maxweight;

    @ApiModelProperty("热力图数据列表")
    private List<AreaHeatDataItemVO> peopleHeatmapDataList;
}
