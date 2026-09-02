package org.jeecg.modules.fwbz.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.alarm.entity.AlarmCategory;
import org.jeecg.modules.fwbz.alarm.service.IAlarmCategoryService;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/fwbz/alarm/category")
@AllArgsConstructor
public class AlarmCategoryController {

    private final IAlarmCategoryService service;

    @PostMapping("/add")
//    @RequiresPermissions("Fwbz:alarmCategory:add")
    @AutoLog(value = "告警类别-添加")
    public Result<String> add(@RequestBody AlarmCategory param) {
        service.save(param);
        return Result.ok();
    }

    @PostMapping("/edit")
//    @RequiresPermissions("Fwbz:alarmCategory:edit")
    @AutoLog(value = "告警类别-编辑")
    public Result<String> edit(@RequestBody AlarmCategory param) {
        service.updateById(param);
        return Result.ok();
    }

    @DeleteMapping("/delete")
//    @RequiresPermissions("Fwbz:alarmCategory:delete")
    @AutoLog(value = "告警类别-删除")
    public Result<String> delete(@RequestParam(name = "id") Long id) {
        service.removeById(id);
        return Result.ok();
    }

    @PostMapping("/startCategory")
//    @RequiresPermissions("Fwbz:alarmCategory:startCategory")
    @AutoLog(value = "告警类别-启用")
    public Result<String> startCategory(@RequestParam(name = "id") Long id) {
        service.startCategory(id);
        return Result.ok();
    }

    @PostMapping("/stopCategory")
//    @RequiresPermissions("Fwbz:alarmCategory:stopCategory")
    @AutoLog(value = "告警类别-停用")
    public Result<String> stopCategory(@RequestParam(name = "id") Long id) {
        service.stopCategory(id);
        return Result.ok();
    }

    @GetMapping("/listPage")
    public Result<IPage<AlarmCategory>> listPage(AlarmCategory params) {
        return Result.ok(service.listPage(params));
    }

    @GetMapping("/list")
    public Result<List<AlarmCategory>> list() {
        return Result.ok(service.list());
    }

    /**
     * 导出告警类别
     * <p>导出全部告警类别，不分页。</p>
     */
    @GetMapping("/export")
    @ApiOperation(value = "导出告警类别", notes = "导出全部告警类别，不分页")
    public void export(HttpServletResponse response) throws Exception {
        List<AlarmCategory> list = service.list();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("告警类别.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("告警类别", "告警类别", ExcelType.XSSF),
                AlarmCategory.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }
}
