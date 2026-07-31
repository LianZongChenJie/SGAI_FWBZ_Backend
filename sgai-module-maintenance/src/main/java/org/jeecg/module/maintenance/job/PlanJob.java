package org.jeecg.module.maintenance.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.module.maintenance.dto.PlanParam;
import org.jeecg.module.maintenance.entity.Plan;
import org.jeecg.module.maintenance.entity.PlanModelDetail;
import org.jeecg.module.maintenance.service.IPlanModelDetailService;
import org.jeecg.module.maintenance.service.IPlanService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
@AllArgsConstructor
public class PlanJob {

    private final IPlanModelDetailService planModelDetailService;

    private final IPlanService planService;

    /**
     * 每月1号凌晨两点执行
     * 创建当月计划
     */
    @Scheduled(cron = "0 0 2 1 * ? ")
    public void createPlan(){

        LocalDate now = LocalDate.now();

        LocalDate lastDay =now.with(TemporalAdjusters.lastDayOfMonth());

        if(now.getDayOfMonth() != lastDay.getDayOfMonth()){

            return;
        }
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        user.setRealname("系统生成");
        user.setId("0");
        LocalDate from = now.withDayOfMonth(1);
        LocalDate to = now.with(TemporalAdjusters.lastDayOfMonth());
        List<PlanModelDetail> detailList = planModelDetailService.queryByStartRangeAndEnableFlag(from, to, false);

        planService.createPlan(detailList);
    }

    @Scheduled(cron = "0 0 5 * * ? ")
    public void planStatusJob(){
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        user.setRealname("系统生成");
        user.setId("0");

        // 获取当天未派发的计划
        List<Plan> planList = planService.list(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getPlanState,"待派发")
                .isNotNull(Plan::getDepartmentId)
                .likeRight(Plan::getPlanBeginTime, LocalDate.now()+"%")
        );

        if(planList != null){
            planList.forEach(item ->{
                PlanParam planParm = new PlanParam();
                item.setPlanState("已派发");
                planParm.setPlan(item);
                item.setSendTime(LocalDateTime.now());
                planParm.setPlanId(item.getId());
                planService.transferData(planParm);
            });
        }
    }

}
