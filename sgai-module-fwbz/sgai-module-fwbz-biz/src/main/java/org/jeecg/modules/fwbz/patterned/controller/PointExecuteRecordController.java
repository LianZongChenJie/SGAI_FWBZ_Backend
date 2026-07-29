package org.jeecg.modules.fwbz.patterned.controller;

import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.patterned.entity.PointExecuteRecord;
import org.jeecg.modules.fwbz.patterned.service.IPointExecuteRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fwbz/pointExecuteRecord")
@AllArgsConstructor
public class PointExecuteRecordController {
    private final IPointExecuteRecordService service;

    @GetMapping("/getByStrategyExecuteId")
    public Result<List<PointExecuteRecord>> getByStrategyExecuteId(Long id){
        return Result.ok(service.getByStrategyExecuteId(id));
    }

}
