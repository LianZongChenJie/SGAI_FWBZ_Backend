package org.jeecg.modules.fwbz.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.alarm.entity.AlarmRules;
import org.jeecg.modules.fwbz.alarm.service.IAlarmRulesService;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AlarmRuleStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.OverViewStatisticsDto;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 告警规则
 */
@RestController
@RequestMapping("/fwbz/alarm/rule")
@AllArgsConstructor
public class AlarmRuleController {

    private final IAlarmRulesService service;

    @PostMapping("/add")
//    @RequiresPermissions("Fwbz:alarmRule:add")
    @AutoLog(value = "告警规则-新增")
    public Result<String> add(@RequestBody AlarmRules data){
        service.save(data);
        return Result.ok();
    }

    @PostMapping("/edit")
//    @RequiresPermissions("Fwbz:alarmRule:edit")
    @AutoLog(value = "告警规则-编辑")
    public Result<String> edit(@RequestBody AlarmRules data){
        service.updateById(data);
        return Result.ok();
    }

    @DeleteMapping("/delete")
//    @RequiresPermissions("Fwbz:alarmRule:delete")
    @AutoLog(value = "告警规则-删除")
    public Result<String> delete(Long id){
        service.removeById(id);
        return Result.ok();
    }

    @GetMapping("/getDetailById")
    public Result<AlarmRules> getDetailById(Long id){
        return Result.ok(service.getDetailById(id));
    }

    @PostMapping("/startRule")
//    @RequiresPermissions("Fwbz:alarmRule:startRule")
    @AutoLog(value = "告警规则-启用")
    public Result<String> startRule(Long id){
        service.startRule(id);
        return Result.ok();
    }

    @PostMapping("/stopRule")
//    @RequiresPermissions("Fwbz:alarmRule:stopRule")
    @AutoLog(value = "告警规则-禁用")
    public Result<String> stopRule(Long id){
        service.stopRule(id);
        return Result.ok();
    }


    @GetMapping("/listPage")
    public Result<IPage<AlarmRules>> listPage(AlarmRules params){
        return Result.ok(service.listPage(params));
    }

    /**
     * 导出告警规则
     * <p>导出全部告警规则，不分页，联动告警类别/告警等级名称。</p>
     */
    @GetMapping("/export")
    @ApiOperation(value = "导出告警规则", notes = "导出全部告警规则，不分页，联动告警类别/告警等级名称")
    public void export(HttpServletResponse response) throws Exception {
        List<AlarmRules> list = service.list();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("告警规则.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("告警规则", "告警规则", ExcelType.XSSF),
                AlarmRules.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }


    /**
     * 数据统计
     * @param
     * @return
     */
    @GetMapping("/statistics")
    public Result<AlarmRuleStatisticsDto> overviewStatistics() {
        return Result.ok(service.statistics());
    }

}
