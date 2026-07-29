package org.jeecg.modules.master.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.master.common.TreeFullNameHelper;
import org.jeecg.modules.master.entity.IntegrationLog;
import org.jeecg.modules.master.mapper.IntegrationLogMapper;
import org.jeecg.modules.master.service.IIntegrationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class IntegrationLogServiceImpl
        extends ServiceImpl<IntegrationLogMapper, IntegrationLog>
        implements IIntegrationLogService {

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void writeLog(IntegrationLog log) {
        if (log.getId() == null) {
            log.setId(TreeFullNameHelper.generateUuid());
        }
        if (log.getCreateTime() == null) {
            log.setCreateTime(new Date());
        }
        baseMapper.insert(log);
    }
}
