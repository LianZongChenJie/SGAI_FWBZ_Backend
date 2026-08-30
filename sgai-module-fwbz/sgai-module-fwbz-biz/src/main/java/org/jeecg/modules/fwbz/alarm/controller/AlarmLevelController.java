package org.jeecg.modules.fwbz.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.alarm.entity.AlarmLevel;
import org.jeecg.modules.fwbz.alarm.service.IAlarmLevelService;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/fwbz/alarm/level")
@AllArgsConstructor
public class AlarmLevelController {

    private final IAlarmLevelService service;

    @PostMapping("/add")
//    @RequiresPermissions("Fwbz:alarmLevel:add")
    @AutoLog(value = "报警级别-添加")
    public Result<String> add(@RequestBody AlarmLevel param){
        service.save(param);
        return Result.ok();
    }

    @PostMapping("/edit")
//    @RequiresPermissions("Fwbz:alarmLevel:edit")
    @AutoLog(value = "报警级别-编辑")
    public Result<String> edit(@RequestBody AlarmLevel param){
        service.updateById(param);
        return Result.ok();
    }

    @DeleteMapping("/delete")
//    @RequiresPermissions("Fwbz:alarmLevel:delete")
    @AutoLog(value = "报警级别-删除")
    public Result<String> delete(Long id){
        service.removeById(id);
        return Result.ok();
    }

    @GetMapping("/listPage")
    public Result<IPage<AlarmLevel>> listPage(AlarmLevel params){
        return Result.ok(service.listPage(params));
    }

    @GetMapping("/list")
    public Result<List<AlarmLevel>> list(){
        return Result.ok(service.list());
    }

    @PostMapping("/startLevel")
//    @RequiresPermissions("Fwbz:alarmLevel:startLevel")
    @AutoLog(value = "报警级别-启用")
    public Result<String> startLevel(@RequestParam(name = "id") Long id){
        service.startLevel(id);
        return Result.ok();
    }

    @PostMapping("/stopLevel")
//    @RequiresPermissions("Fwbz:alarmLevel:stopLevel")
    @AutoLog(value = "报警级别-禁用")
    public Result<String> stopLevel(@RequestParam(name = "id") Long id){
        service.stopLevel(id);
        return Result.ok();
    }

    /**
     * 导出告警等级
     * <p>导出全部告警等级，不分页。</p>
     */
    @GetMapping("/export")
    @ApiOperation(value = "导出告警等级", notes = "导出全部告警等级，不分页")
    public void export(HttpServletResponse response) throws Exception {
        List<AlarmLevel> list = service.list();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("告警等级.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("告警等级", "告警等级", ExcelType.XSSF),
                AlarmLevel.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }

}
