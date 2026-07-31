package org.jeecg.module.maintenance.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.module.maintenance.dto.PlanQueryDto;
import org.jeecg.module.maintenance.entity.Plan;
import org.jeecg.module.maintenance.entity.PlanModelDetail;
import org.jeecg.module.maintenance.service.IPlanModelDetailService;
import org.jeecg.module.maintenance.service.IPlanService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/maintenance/plan")
@AllArgsConstructor
@Slf4j
public class PlanController {

    private final IPlanService service;

    private final IPlanModelDetailService planModelDetailService;

    /**
     * 计划列表分页查询
     */
    @GetMapping("/queryPage")
    public Result<?> queryPage(PlanQueryDto param) {
        return Result.ok(
                service.page(new Page<>(param.getPage(), param.getPagesize()),
                        new LambdaQueryWrapper<Plan>()
                                .like(StrUtil.isNotEmpty(param.getName()), Plan::getName, param.getName())
                                .like(StrUtil.isNotEmpty(param.getExecutor()), Plan::getExecutor, param.getExecutor())
                                .ge(param.getPlanBeginTime() != null, Plan::getPlanBeginTime, param.getPlanBeginTime())
                ));
    }

    /**
     * 查询计划
     */
    @GetMapping("/queryAll")
    public Result<?> queryAll(PlanQueryDto param) {
        return Result.ok(service.queryByBeginTimeRangeAndLabelType(param.getStartTime(), param.getEndTime(), param.getLabelType()));
    }

    /**
     * 启动计划
     */
    @GetMapping("/createPlan")
    public Result<?> createPlan(@RequestParam(required = false) String date) {
        LocalDate now = StrUtil.isEmpty(date) ? LocalDate.now() : LocalDate.parse(date);

        // TODO 创建人改为系统生成
//        AdminUserSubject userSubject = new AdminUserSubject();
//        userSubject.setRealName("系统生成");
//        userSubject.setUserId(0L);
//        UserLocalContext.setUserSubject(userSubject);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        user.setRealname("系统生成");
        user.setId("0");
        LocalDate from = now.plusMonths(1).withDayOfMonth(1);
        LocalDate to = now.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        List<PlanModelDetail> detailList = planModelDetailService.queryByStartRangeAndEnableFlag(from, to, false);
        service.createPlan(detailList);
        return Result.ok();
    }

    /**
     * 计划详情
     *
     * @param planId 计划id
     */
    @GetMapping("/findDetail")
    public Result<?> findDetail(@RequestParam Long planId, @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int pagesize, @RequestParam(required = false) String name) {
        return Result.ok(service.findDetail(planId, page, pagesize, name));
    }

    /**
     * 及时生效
     *
     * @param planId 计划id
     */
    @GetMapping("/executeNow")
    public Result<String> executeNow(Long planId) {
        try {
            //调用接口将当前计划派发
            service.excuteNow(planId);
            return Result.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error("错误失败");
        }
    }

    @PostMapping(value = "/removeByIds")
    public Result<String> removeByIds(@RequestBody List<Long> ids) {
        service.removeByIds(ids);
        return Result.ok();
    }

    @PostMapping("/saveOrUpdateOne")
    public Result<?> saveOrUpdateOne(@RequestBody Plan entity){
        service.saveOrUpdate(entity);
        return Result.ok();
    }
}
