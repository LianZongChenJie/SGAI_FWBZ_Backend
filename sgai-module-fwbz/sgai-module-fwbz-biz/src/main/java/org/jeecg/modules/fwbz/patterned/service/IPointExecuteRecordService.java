package org.jeecg.modules.fwbz.patterned.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.patterned.entity.PointExecuteRecord;

import java.util.List;

public interface IPointExecuteRecordService extends IService<PointExecuteRecord> {

    List<PointExecuteRecord> getByStrategyExecuteId(Long strategyExecuteId);
}
