package org.jeecg.modules.fwbz.patorlPlan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.patorlPlan.dto.PatrolPlanDetailVo;
import org.jeecg.modules.fwbz.patorlPlan.dto.PatrolPlanDto;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;
import org.jeecg.modules.fwbz.patorlPlan.entity.PlanCamera;
import org.jeecg.modules.fwbz.patorlPlan.mapper.PatrolPlanMapper;
import org.jeecg.modules.fwbz.patorlPlan.service.IPatrolPlanService;
import org.jeecg.modules.fwbz.patorlPlan.service.IPlanCameraService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * @Description: 巡更计划
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Service
public class PatrolPlanServiceImpl extends ServiceImpl<PatrolPlanMapper, PatrolPlan> implements IPatrolPlanService {

    @Autowired
    private IPlanCameraService planCameraService;

    private static final DateTimeFormatter NEXT_EXECUTION_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithCameras(PatrolPlanDto dto) {
        // 1. 校验执行周期是否与已有非停用计划冲突
        validateExecutionCycle(dto.getExecutionCycle(), null);

        // 2. 设置默认下次执行时间（明天 + 执行周期时间）
        if (dto.getNextExecution() == null || dto.getNextExecution().isEmpty()) {
            dto.setNextExecution(calculateNextExecution(dto.getExecutionCycle()));
        }

        // 3. 保存巡更计划基本信息
        PatrolPlan patrolPlan = new PatrolPlan();
        BeanUtils.copyProperties(dto, patrolPlan);
        this.save(patrolPlan);

        // 4. 保存关联摄像头
        if (dto.getIndexCodes() != null && !dto.getIndexCodes().isEmpty()) {
            planCameraService.batchSave(patrolPlan.getId(), dto.getIndexCodes());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithCameras(PatrolPlanDto dto) {
        // 1. 校验执行周期是否与已有非停用计划冲突（排除自身）
        validateExecutionCycle(dto.getExecutionCycle(), dto.getId());

        // 2. 修改巡更计划基本信息
        PatrolPlan patrolPlan = new PatrolPlan();
        BeanUtils.copyProperties(dto, patrolPlan);
        this.updateById(patrolPlan);

        // 3. 关联摄像头：先删除后添加
        planCameraService.deleteByPlanId(dto.getId());
        if (dto.getIndexCodes() != null && !dto.getIndexCodes().isEmpty()) {
            planCameraService.batchSave(dto.getId(), dto.getIndexCodes());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithCameras(Long id) {
        // 1. 删除关联摄像头
        planCameraService.deleteByPlanId(id);
        // 2. 删除巡更计划
        this.removeById(id);
    }

    @Override
    public PatrolPlanDetailVo getDetail(Long id) {
        PatrolPlan patrolPlan = this.getById(id);
        if (patrolPlan == null) {
            return null;
        }
        PatrolPlanDetailVo vo = new PatrolPlanDetailVo();
        BeanUtils.copyProperties(patrolPlan, vo);

        // 查询关联摄像头
        List<PlanCamera> cameras = planCameraService.selectByPlanId(id);
        vo.setCameras(cameras);

        return vo;
    }

    @Override
    public PatrolPlanDetailVo getRunningPlanDetail() {
        // 查询运行中状态(staus=2)的巡更计划
        PatrolPlan plan = this.baseMapper.selectRunningPlan();
        if (plan == null) {
            return null;
        }
        PatrolPlanDetailVo vo = new PatrolPlanDetailVo();
        BeanUtils.copyProperties(plan, vo);
        // 查询关联摄像头
        List<PlanCamera> cameras = planCameraService.selectByPlanId(plan.getId());
        vo.setCameras(cameras);
        return vo;
    }

    /**
     * 校验执行周期是否与非停用计划冲突
     */
    private void validateExecutionCycle(String executionCycle, Long excludeId) {
        if (executionCycle == null || executionCycle.isEmpty()) {
            return;
        }
        int count = this.baseMapper.countByExecutionCycle(executionCycle, excludeId);
        if (count > 0) {
            throw new JeecgBootException("该执行时间已存在巡更计划，请选择其他时间");
        }
    }

    /**
     * 计算默认下次执行时间（明天 + 执行周期时间）
     */
    private String calculateNextExecution(String executionCycle) {
        if (executionCycle == null || executionCycle.isEmpty()) {
            return null;
        }
        try {
            LocalTime time = parseExecutionTime(executionCycle);
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalDateTime nextExecution = LocalDateTime.of(tomorrow, time);
            return nextExecution.format(NEXT_EXECUTION_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析执行周期时间字符串，支持 HH:mm:ss 和 HH:mm 格式
     */
    private LocalTime parseExecutionTime(String executionCycle) {
        String trimmed = executionCycle.trim();
        if (trimmed.length() <= 5) {
            // HH:mm 格式
            return LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("HH:mm"));
        }
        // HH:mm:ss 格式
        return LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
