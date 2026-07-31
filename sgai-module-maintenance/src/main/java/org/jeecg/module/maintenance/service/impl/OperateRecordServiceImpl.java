package org.jeecg.module.maintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.module.maintenance.entity.OperateRecord;
import org.jeecg.module.maintenance.mapper.OperateRecordMapper;
import org.jeecg.module.maintenance.service.IOperateRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperateRecordServiceImpl extends ServiceImpl<OperateRecordMapper, OperateRecord> implements IOperateRecordService {
    @Override
    public List<OperateRecord> findByOrderId(Long orderId) {
        return super.list(new LambdaQueryWrapper<OperateRecord>().eq(OperateRecord::getOrderId, orderId));
    }
}
