package org.jeecg.modules.fwbz.patorlPlan.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;
import org.jeecg.modules.fwbz.patorlPlan.entity.PlanCamera;

import java.util.List;

/**
 * @Description: 巡更计划详情VO
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="巡更计划详情VO", description="巡更计划详情返回")
public class PatrolPlanDetailVo extends PatrolPlan {

    /**关联摄像头列表*/
    @ApiModelProperty(value = "关联摄像头列表")
    private List<PlanCamera> cameras;
}
