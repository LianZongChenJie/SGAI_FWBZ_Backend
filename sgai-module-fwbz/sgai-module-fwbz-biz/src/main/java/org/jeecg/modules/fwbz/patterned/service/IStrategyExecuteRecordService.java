package org.jeecg.modules.fwbz.patterned.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.patterned.dto.StrategyExecuteRecordDto;
import org.jeecg.modules.fwbz.patterned.entity.PatterningStrategy;
import org.jeecg.modules.fwbz.patterned.entity.StrategyExecuteRecord;

public interface IStrategyExecuteRecordService extends IService<StrategyExecuteRecord> {
    void addLog(PatterningStrategy strategy);

    Page<StrategyExecuteRecord> listPage(StrategyExecuteRecordDto params);
}
