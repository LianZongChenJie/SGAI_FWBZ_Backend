package org.jeecg.module.maintenance.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.module.maintenance.entity.OperateRecord;
import org.jeecg.module.maintenance.entity.Plan;

import java.util.List;

/**
 * 描述:
 *
 * @author ppliu
 * created in 2021/8/19 15:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PlanDto extends Plan {
    private List<OperateRecord> operateRecordList;
//    private List<DeviceAccountDO> deviceList;
//    private List<SpaceDO> spaceDOList;
//    private List<WorkOrder> workOrderList;
public static PlanDto convert(Plan plan) {
    if (plan == null) {
        return null;
    }

    PlanDto dto = new PlanDto();
    // 复制父类 Plan 的所有属性
    dto.setId(plan.getId());
    dto.setPlanCode(plan.getPlanCode());
    dto.setName(plan.getName());
    dto.setTaskTypeName(plan.getTaskTypeName());
    dto.setPlanState(plan.getPlanState());
    dto.setTaskTypeId(plan.getTaskTypeId());
    dto.setExecutor(plan.getExecutor());
    dto.setPrincipal(plan.getPrincipal());
    dto.setPrincipalGroup(plan.getPrincipalGroup());
    dto.setPrincipalGroupId(plan.getPrincipalGroupId());
    dto.setArea(plan.getArea());
    dto.setCycle(plan.getCycle());
    dto.setCreatorName(plan.getCreatorName());
    dto.setCreatedTime(plan.getCreatedTime());
    dto.setPlanBeginTime(plan.getPlanBeginTime());
    dto.setPlanEndTime(plan.getPlanEndTime());
    dto.setRealBeginTime(plan.getRealBeginTime());
    dto.setRealEndTime(plan.getRealEndTime());
    dto.setRealResponseTime(plan.getRealResponseTime());
    dto.setQuestion(plan.isQuestion());
    dto.setDepartment(plan.getDepartment());
    dto.setDepartmentId(plan.getDepartmentId());
    dto.setWeibaoType(plan.getWeibaoType());
    dto.setSendTime(plan.getSendTime());
    dto.setLabelType(plan.getLabelType());

    // 复制 BaseEntity 的属性
    dto.setUpdatorId(plan.getUpdatorId());
    dto.setUpdatorName(plan.getUpdatorName());
    dto.setUpdateTime(plan.getUpdateTime());
    dto.setVersion(plan.getVersion());
    dto.setCreatorId(plan.getCreatorId());
    dto.setCreatorName(plan.getCreatorName());
    dto.setCreateTime(plan.getCreateTime());
    dto.setPage(plan.getPage());
    dto.setPagesize(plan.getPagesize());

    return dto;
}

}