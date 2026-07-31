package org.jeecg.module.maintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.maintenance.entity.OperateRecord;

import java.util.List;

public interface IOperateRecordService extends IService<OperateRecord> {

    List<OperateRecord> findByOrderId(Long orderId);
}
