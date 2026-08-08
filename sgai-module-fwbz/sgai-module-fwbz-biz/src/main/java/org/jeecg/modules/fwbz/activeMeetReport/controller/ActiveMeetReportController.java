package org.jeecg.modules.fwbz.activeMeetReport.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.fwbz.activeMeetReport.entity.ActiveMeetReport;
import org.jeecg.modules.fwbz.activeMeetReport.service.IActiveMeetReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 展会总结报告
 * @Author: jeecg-boot
 * @Date:   2026-08-08
 * @Version: V1.0
 */
@Api(tags="展会总结报告")
@RestController
@RequestMapping("/fwbz/activeMeetReport")
@Slf4j
public class ActiveMeetReportController extends JeecgController<ActiveMeetReport, IActiveMeetReportService> {
    @Autowired
    private IActiveMeetReportService activeMeetReportService;

    /**
     * 分页列表查询
     *
     * @param activeMeetReport
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value="展会总结报告-分页列表查询", notes="展会总结报告-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ActiveMeetReport>> queryPageList(ActiveMeetReport activeMeetReport,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<ActiveMeetReport> queryWrapper = QueryGenerator.initQueryWrapper(activeMeetReport, req.getParameterMap());
        Page<ActiveMeetReport> page = new Page<ActiveMeetReport>(pageNo, pageSize);
        IPage<ActiveMeetReport> pageList = activeMeetReportService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param activeMeetReport
     * @return
     */
    @AutoLog(value = "展会总结报告-添加")
    @ApiOperation(value="展会总结报告-添加", notes="展会总结报告-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ActiveMeetReport activeMeetReport) {
        activeMeetReportService.save(activeMeetReport);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param activeMeetReport
     * @return
     */
    @AutoLog(value = "展会总结报告-编辑")
    @ApiOperation(value="展会总结报告-编辑", notes="展会总结报告-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody ActiveMeetReport activeMeetReport) {
        activeMeetReportService.updateById(activeMeetReport);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "展会总结报告-通过id删除")
    @ApiOperation(value="展会总结报告-通过id删除", notes="展会总结报告-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id",required=true) String id) {
        activeMeetReportService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "展会总结报告-批量删除")
    @ApiOperation(value="展会总结报告-批量删除", notes="展会总结报告-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        this.activeMeetReportService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     * 已总结(status=1)直接返回库数据，未总结则动态从各表计算
     *
     * @param id
     * @return
     */
    @ApiOperation(value="展会总结报告-通过id查询", notes="展会总结报告-通过id查询，已总结返回库数据，未总结动态计算")
    @GetMapping(value = "/queryById")
    public Result<ActiveMeetReport> queryById(@RequestParam(name="id",required=true) String id) {
        ActiveMeetReport report = activeMeetReportService.computeReport(Long.valueOf(id));
        if(report==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(report);
    }

    /**
     * 根据活动名称查询报告
     *
     * @param activeName 活动名称
     * @return
     */
    @ApiOperation(value="展会总结报告-根据活动名称查询", notes="展会总结报告-根据活动名称查询")
    @GetMapping(value = "/queryByActiveName")
    public Result<ActiveMeetReport> queryByActiveName(@RequestParam(name="activeName",required=true) String activeName) {
        ActiveMeetReport report = activeMeetReportService.getByActiveName(activeName);
        if(report==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(report);
    }

    /**
     * 保存总结报告
     *
     * @param activeMeetReport
     * @return
     */
    @AutoLog(value = "展会总结报告-保存总结")
    @ApiOperation(value="展会总结报告-保存总结", notes="根据报告ID，仅更新数据字段并将状态置为已总结")
    @PostMapping(value = "/save")
    public Result<String> saveReport(@RequestBody ActiveMeetReport activeMeetReport) {
        activeMeetReportService.saveReport(activeMeetReport);
        return Result.OK("保存成功！");
    }

    /**
     * 导出excel
     *
     * @param request
     * @param activeMeetReport
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ActiveMeetReport activeMeetReport) {
        return super.exportXls(request, activeMeetReport, ActiveMeetReport.class, "展会总结报告");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ActiveMeetReport.class);
    }
}
