package org.jeecg.modules.fwbz.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.alarm.dto.AlarmRecordDto;
import org.jeecg.modules.fwbz.alarm.dto.TransferEventDto;
import org.jeecg.modules.fwbz.alarm.entity.AlarmRecord;
import org.jeecg.modules.fwbz.alarm.service.IAlarmRecordService;
import org.jeecg.modules.fwbz.alarm.vo.AlarmRecordStatisticsVo;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AlarmRecordStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.AlarmRuleStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.vo.Chat;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 告警记录
 */
@RestController
@RequestMapping("/fwbz/alarm/record")
@AllArgsConstructor
public class AlarmRecordController {

    private final IAlarmRecordService service;

    @GetMapping("/listPage")
    public Result<IPage<AlarmRecord>> listPage(AlarmRecordDto params){
        return Result.ok(service.listPage(params));
    }

    /**
     * 导出告警记录
     * <p>支持按空间、告警级别、告警类别、状态、设备、时间范围过滤，不传则导出全部；导出不分页，
     * 告警类别/告警级别名称联动 alarm_category、alarm_level 表。</p>
     */
    @GetMapping("/export")
    @ApiOperation(value = "导出告警记录", notes = "导出告警记录，支持按空间、告警级别、告警类别、状态、设备、时间范围过滤，不传条件导出全部，不分页，联动告警类别/告警级别表")
    public void export(AlarmRecordDto params, HttpServletResponse response) throws Exception {
        List<AlarmRecord> list = service.listForExport(params);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("告警记录.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("告警记录", "告警记录", ExcelType.XSSF),
                AlarmRecord.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }

    @PostMapping("/elimination")
//    @RequiresPermissions("Fwbz:alarmRecord:elimination")
    @AutoLog(value = "告警记录-消除")
    public Result<String> elimination(@RequestParam(name = "id") Long id){
        service.elimination(id);
        return Result.ok();
    }

    @PostMapping("/eliminations")
    @AutoLog(value = "告警记录-批量消除")
    public Result<String> eliminations(@RequestBody List<Long> ids){
        ids.forEach(service::elimination);
        return Result.ok();
    }
    @PostMapping("/confirm")
    @AutoLog(value = "告警记录-确认")
    public Result<String> confirm(@RequestParam(name = "id") Long id){
        service.confirm(id);
        return Result.ok();
    }

    @PostMapping("/confirms")
    @AutoLog(value = "告警记录-批量确认")
    public Result<String> confirms(@RequestBody List<Long> ids){
        ids.forEach(service::confirm);
        return Result.ok();
    }

    /**
     * 告警级别统计
     */
    @GetMapping("/levelStatistics")
    public Result<List<AlarmRecordStatisticsVo>> levelStatistics(AlarmRecordDto params){
        return Result.ok(service.levelStatistics(params));
    }

    @GetMapping("/test")
    public Result<String> test(DeviceAttribute attribute){
        service.alarmDetection(attribute.getDeviceId(),attribute.getId(),attribute.getValue());
        return Result.ok();
    }

    /**
     * 转事件工单
     * @return
     */
    @PostMapping("/transferEvent")
    public Result<String> transferEvent(@RequestBody TransferEventDto data){
        service.transferEvent(data);
        return Result.ok();
    }



    /**
     * 数据统计
     * @param
     * @return
     */
    @GetMapping("/statistics")
    public Result<AlarmRecordStatisticsDto> overviewStatistics() {
        return Result.ok(service.statistics());
    }


    /**
     * 近七日告警统计
     */
    @GetMapping("/alarmTrendRecently")
    public Result<Chat> alarmTrendRecently(){
        return Result.ok(service.alarmTrendRecently());
    }


}
