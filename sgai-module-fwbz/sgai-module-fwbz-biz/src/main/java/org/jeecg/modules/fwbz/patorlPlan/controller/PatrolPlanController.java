package org.jeecg.modules.fwbz.patorlPlan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.fwbz.patorlPlan.dto.PatrolPlanDetailVo;
import org.jeecg.modules.fwbz.patorlPlan.dto.PatrolPlanDto;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolPlan;
import org.jeecg.modules.fwbz.patorlPlan.service.IPatrolPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: 巡更计划
 * @Author: jeecg-boot
 * @Date:   2026-07-31
 * @Version: V1.0
 */
@Api(tags="巡更计划")
@RestController
@RequestMapping("/fwbz/patrolPlan")
@Slf4j
public class PatrolPlanController extends JeecgController<PatrolPlan, IPatrolPlanService> {

    @Autowired
    private IPatrolPlanService patrolPlanService;

    /**
     * 分页列表查询
     *
     * @param patrolPlan
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value="巡更计划-分页列表查询", notes="巡更计划-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<PatrolPlan>> queryPageList(PatrolPlan patrolPlan,
                                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                   HttpServletRequest req) {
        QueryWrapper<PatrolPlan> queryWrapper = QueryGenerator.initQueryWrapper(patrolPlan, req.getParameterMap());
        Page<PatrolPlan> page = new Page<>(pageNo, pageSize);
        IPage<PatrolPlan> pageList = patrolPlanService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 查询详情（含关联摄像头列表）
     *
     * @param id
     * @return
     */
    @ApiOperation(value="巡更计划-查询详情", notes="巡更计划-查询详情（含关联摄像头）")
    @GetMapping(value = "/queryById")
    public Result<PatrolPlanDetailVo> queryById(@RequestParam(name="id", required=true) Long id) {
        PatrolPlanDetailVo detail = patrolPlanService.getDetail(id);
        if (detail == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(detail);
    }

    /**
     * 添加巡更计划
     *
     * @param dto
     * @return
     */
    @AutoLog(value = "巡更计划-添加")
    @ApiOperation(value="巡更计划-添加", notes="巡更计划-添加")
    @RequiresPermissions("fwbz:patrol_plan:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody PatrolPlanDto dto) {
        patrolPlanService.saveWithCameras(dto);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑巡更计划
     *
     * @param dto
     * @return
     */
    @AutoLog(value = "巡更计划-编辑")
    @ApiOperation(value="巡更计划-编辑", notes="巡更计划-编辑")
    @RequiresPermissions("fwbz:patrol_plan:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody PatrolPlanDto dto) {
        patrolPlanService.updateWithCameras(dto);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "巡更计划-通过id删除")
    @ApiOperation(value="巡更计划-通过id删除", notes="巡更计划-通过id删除")
    @RequiresPermissions("fwbz:patrol_plan:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id", required=true) Long id) {
        patrolPlanService.deleteWithCameras(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "巡更计划-批量删除")
    @ApiOperation(value="巡更计划-批量删除", notes="巡更计划-批量删除")
    @RequiresPermissions("fwbz:patrol_plan:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name="ids", required=true) String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            patrolPlanService.deleteWithCameras(Long.valueOf(id));
        }
        return Result.OK("批量删除成功!");
    }

}
