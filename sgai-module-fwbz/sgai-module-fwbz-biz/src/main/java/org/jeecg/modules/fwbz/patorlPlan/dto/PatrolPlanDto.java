package org.jeecg.modules.fwbz.patorlPlan.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;

import java.util.List;

/**
 * @Description: 巡更计划DTO（含关联摄像头编码列表）
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="巡更计划DTO", description="巡更计划新增/编辑传参")
public class PatrolPlanDto extends PatrolPlan {

    /**摄像头唯一编码列表*/
    @ApiModelProperty(value = "摄像头唯一编码列表")
    private List<String> indexCodes;
}
