package org.jeecg.modules.fwbz.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.alarm.dto.AlarmRecordDto;
import org.jeecg.modules.fwbz.alarm.dto.TransferEventDto;
import org.jeecg.modules.fwbz.alarm.entity.AlarmRecord;
import org.jeecg.modules.fwbz.alarm.service.IAlarmRecordService;
import org.jeecg.modules.fwbz.alarm.vo.AlarmRecordStatisticsVo;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警记录
 */
@RestController
@RequestMapping("/Fwbz/alarm/record")
@AllArgsConstructor
public class AlarmRecordController {

    private final IAlarmRecordService service;

    @GetMapping("/listPage")
    public Result<IPage<AlarmRecord>> listPage(AlarmRecordDto params){
        return Result.ok(service.listPage(params));
    }

    @PostMapping("/elimination")
    @RequiresPermissions("Fwbz:alarmRecord:elimination")
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
}
