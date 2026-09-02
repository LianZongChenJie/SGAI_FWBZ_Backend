package org.jeecg.modules.fwbz.activeMeet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.service.IActiveMeetInfoService;
import org.jeecg.modules.fwbz.activeMeet.vo.WeekActivityVO;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * 活动排期信息管理控制器
 *
 * @author fwbz
 */
@RestController
@RequestMapping("/fwbz/activeMeet/info")
@AllArgsConstructor
@Api(tags = "活动排期信息管理")
public class ActiveMeetInfoController {

    private final IActiveMeetInfoService service;

    @PostMapping("/add")
    //@RequiresPermissions("fwbz:activeMeetInfo:add")
    @AutoLog(value = "活动信息-添加")
    @ApiOperation(value = "添加活动信息", notes = "新增一条活动排期记录")
    public Result<String> add(@RequestBody ActiveMeetInfo param) {
        service.save(param);
        return Result.ok();
    }

    @PostMapping("/edit")
//    @RequiresPermissions("fwbz:activeMeetInfo:edit")
    @AutoLog(value = "活动信息-编辑")
    @ApiOperation(value = "编辑活动信息", notes = "按id修改活动排期记录")
    public Result<String> edit(@RequestBody ActiveMeetInfo param) {
        service.updateById(param);
        return Result.ok();
    }

    @DeleteMapping("/delete")
//    @RequiresPermissions("fwbz:activeMeetInfo:delete")
    @AutoLog(value = "活动信息-删除")
    @ApiOperation(value = "删除活动信息", notes = "按id删除活动排期记录")
    public Result<String> delete(@RequestParam(name = "id") Long id) {
        service.removeById(id);
        return Result.ok();
    }

    @GetMapping("/listPage")
    @ApiOperation(value = "分页查询活动排期", notes = "分页查询table_activeMeet_info表活动数据，场馆名称联动table_venue_info场馆信息表")
    public Result<IPage<ActiveMeetInfo>> listPage(ActiveMeetInfo params) {
        return Result.ok(service.listPage(params));
    }

    @GetMapping("/list")
    @ApiOperation(value = "查询全部活动排期", notes = "查询table_activeMeet_info表全部活动数据，场馆名称联动table_venue_info场馆信息表")
    public Result<List<ActiveMeetInfo>> list() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/thisWeek")
    @ApiOperation(value = "查询本周活动", notes = "按日期分组返回本周活动数据")
    public Result<List<WeekActivityVO>> thisWeek() {
        return Result.ok(service.listThisWeek());
    }

    /**
     * 导出活动排期
     * <p>按时间范围导出活动，场馆名称联动 table_venue_info 场馆信息表。
     * startDate、endDate 可空：只传开始日期导出其之后的；只传结束日期导出其之前的；都不传导出全部。</p>
     */
    @GetMapping("/export")
    @AutoLog(value = "活动信息-导出")
    @ApiOperation(value = "导出活动排期", notes = "按时间范围导出活动，场馆名称联动table_venue_info场馆信息表。startDate、endDate可空：只传开始日期导出其之后的；只传结束日期导出其之前的；都不传导出全部")
    public void export(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletResponse response) throws Exception {
        List<ActiveMeetInfo> list = service.listByDateRange(startDate, endDate);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("活动排期.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("活动排期", "活动排期", ExcelType.XSSF),
                ActiveMeetInfo.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }
}
