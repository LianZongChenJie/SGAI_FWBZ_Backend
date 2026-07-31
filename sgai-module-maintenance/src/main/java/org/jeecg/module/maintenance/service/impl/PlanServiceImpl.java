package org.jeecg.module.maintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.module.maintenance.dto.PlanDto;
import org.jeecg.module.maintenance.dto.PlanParam;
import org.jeecg.module.maintenance.entity.OperateRecord;
import org.jeecg.module.maintenance.entity.Plan;
import org.jeecg.module.maintenance.entity.PlanModel;
import org.jeecg.module.maintenance.entity.PlanModelDetail;
import org.jeecg.module.maintenance.mapper.PlanMapper;
import org.jeecg.module.maintenance.service.IOperateRecordService;
import org.jeecg.module.maintenance.service.IPlanModelDetailService;
import org.jeecg.module.maintenance.service.IPlanModelService;
import org.jeecg.module.maintenance.service.IPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.jeecg.module.maintenance.consts.WorkTypeState.待派发;

@Service
@AllArgsConstructor
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements IPlanService {

    private final IPlanModelService planModelService;

    private final IPlanModelDetailService planModelDetailService;

    private final IOperateRecordService operateRecordService;

    @Override
    public List<Plan> queryByBeginTimeRangeAndLabelType(LocalDateTime startTime, LocalDateTime endTime, String labelType) {
        return super.list(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getLabelType, labelType)
                .between(Plan::getPlanBeginTime, startTime, endTime));
    }

    @Transactional
    @Override
    public void createPlan(PlanModelDetail detail) {
        PlanModel planModel = planModelService.getById(detail.getPlanModelId());
        planModelDetailService.updateEnableFlag(detail.getId(), true);
        //新建计划
        Plan plan = new Plan();
        //todo plancode生成规则
        //plan.setPlanCode();
        plan.setName(planModel.getName() + (detail.getStart().toString()).replaceAll("-", ""));
        //plan.setTaskTypeName();
        //plan.setTaskTypeId();
        //plan.setExecutor();
        //plan.setPrincipal();
        //plan.setPrincipalGroup();
        //plan.setArea();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        plan.setCycle(planModel.getCycle());
        plan.setCreatorName(sysUser.getRealname());
        plan.setCreatedTime(LocalDateTime.now());
        plan.setPlanBeginTime(detail.getStart());
        plan.setPlanEndTime(plan.getPlanBeginTime().plusDays(planModel.getDuration()));
        plan.setPlanState(待派发);
        plan.setLabelType(planModel.getLabelType());

        //新增科组信息 科组名称，科组id
        plan.setDepartment(planModel.getDepartment());
        plan.setDepartmentId(planModel.getDepartmentId());
        plan.setPrincipalGroup(planModel.getDepartment());

        plan.setQuestion(false);

        //增加维保类型
        plan.setWeibaoType(planModel.getWeibaoType());

        //        增加任务id
        plan.setTaskTypeId(planModel.getId());

        //      增加循环周期
        plan.setCycle(planModel.getDuration() + planModel.getUnit());

        super.save(plan);

        //操作记录
        OperateRecord operateRecord = new OperateRecord();

        setPlanRecordInfo(plan, operateRecord);

        operateRecordService.save(operateRecord);

        // TODO 暂时去掉关联设备
//        //新建计划设备数据
//        for (Long aLong : planModelDeviceManager.deviceIdList(planModel.getId())) {
//            PlanDevice planDevice = new PlanDevice();
//            planDevice.setPlanId(plan.getId());
//            planDevice.setDeviceId(aLong);
//            planDevice.setCheckStatus("0");
//            planDeviceManager.insertSelective(planDevice);
//        }
//        //新建计划空间数据
//        for (Long aLong : planModelSpaceManager.spaceIdList(planModel.getId())) {
//            PlanSpace planSpace = new PlanSpace();
//            planSpace.setPlanId(plan.getId());
//            planSpace.setSpaceId(aLong);
//            planSpaceManager.insertSelective(planSpace);
//        }
    }

    @Transactional
    @Override
    public void createPlan(List<PlanModelDetail> details) {
        details.forEach(this::createPlan);
    }

    @Override
    public PlanDto findDetail(Long planId, Integer page, Integer pagesize, String name) {

        PlanDto planInfo = PlanDto.convert(super.getById(planId));
        List<OperateRecord> byOrderId = operateRecordService.findByOrderId(planId);
        planInfo.setOperateRecordList(byOrderId);
//        List<DeviceAccountDO> deviceInfo = planMapper.selectDeviceInfoById(planId, name, rowBounds);

//        if(!deviceInfo.isEmpty()){
//            planInfo.setDeviceList(deviceInfo);
//        }
        return planInfo;

    }

    @Override
    public void excuteNow(Long planId) {
        //通过id获取维保计划详情

        //维保计划更新状态为已派发
        super.update(new LambdaUpdateWrapper<Plan>()
                .eq(Plan::getId, planId)
                .set(Plan::getPlanState, "已派发")
                .set(Plan::getSendTime, LocalDateTime.now()));
        Plan planInfo = super.getById(planId);
        //增加派发记录
        OperateRecord record = new OperateRecord();
        setPlanRecordInfo(planInfo, record);
        operateRecordService.save(record);
    }

    @Override
    public void transferData(PlanParam planParam) {

        //判断是否存在维保计划
        if (planParam.getPlanId() == null) {
//
            return;
        }
        Plan origenInfo = super.getById(planParam.getPlanId());

        //未找到计划时，直接返回
        if (origenInfo == null) {

            return;
        }
        Plan currInfo = planParam.getPlan();

        currInfo.setId(planParam.getPlanId());

        //开始结束时间
        setPlanStatus(currInfo, origenInfo);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        if (!"系统生成".equals(user.getRealname()) && origenInfo.getExecutor() == null) {

            //操作人更新为执行人
            currInfo.setExecutor(user.getRealname());
        }


        //修改状态
        super.updateById(currInfo);

        //插入操作记录
        OperateRecord operateRecord = planParam.getOperateRecord() == null ? new OperateRecord() : planParam.getOperateRecord();

        // 操作记录赋值
        setPlanRecordInfo(currInfo, operateRecord);

        operateRecordService.save(operateRecord);
    }


    /**
     * @param plan          计划信息
     * @param operateRecord 记录信息
     */
    public void setPlanRecordInfo(Plan plan, OperateRecord operateRecord) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        operateRecord.setOrderId(plan.getId());
        operateRecord.setCreatorId(sysUser.getId());
        operateRecord.setCreatorName(sysUser.getRealname());
        operateRecord.setCreateTime(new Date());
        operateRecord.setSourceStatusName(plan.getPlanState());
        operateRecord.setSourceStatusCode(plan.getPlanState());
        operateRecord.setOperationName(plan.getPlanState());
        operateRecord.setOperationTime(LocalDateTime.now());
        operateRecord.setOperatorName(sysUser.getRealname());
        operateRecord.setOperatorId(sysUser.getId());
        operateRecord.setFlowCode("plan");

    }

    /**
     *
     * @param plan 计划信息
     */
    public void setPlanStatus(Plan plan,Plan originInfo){


        String status = plan.getPlanState();

        if("维保开始".equals(status)){
            plan.setRealBeginTime(LocalDateTime.now());

            if(originInfo.getExecutor() == null ||"".equals(originInfo.getExecutor())){
                LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                plan.setExecutor(user.getRealname());

            }
            if(originInfo.getRealResponseTime() == null){
                plan.setRealResponseTime(LocalDateTime.now());
            }
        }

        if("维保结束".equals(status)){
            plan.setRealEndTime(LocalDateTime.now());
        }

    }

}
