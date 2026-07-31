package org.jeecg.modules.fwbz.patterned.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.patterned.dto.StrategyExecuteRecordDto;
import org.jeecg.modules.fwbz.patterned.entity.StrategyExecuteRecord;
import org.jeecg.modules.fwbz.patterned.service.IStrategyExecuteRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fwbz/strategyExecuteRecord")
@AllArgsConstructor
public class StrategyExecuteRecordController {
    private final IStrategyExecuteRecordService service;

    @GetMapping("/listPage")
    public Result<Page<StrategyExecuteRecord>> listPage(StrategyExecuteRecordDto params){
        return Result.ok(service.listPage(params));
    }

}
