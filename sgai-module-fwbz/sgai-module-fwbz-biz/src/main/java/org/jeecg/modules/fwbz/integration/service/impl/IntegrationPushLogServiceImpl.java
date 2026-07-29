package org.jeecg.modules.fwbz.integration.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.integration.entity.IntegrationPushLog;
import org.jeecg.modules.fwbz.integration.mapper.IntegrationPushLogMapper;
import org.jeecg.modules.fwbz.integration.service.IIntegrationPushLogService;
import org.springframework.stereotype.Service;

@Service
public class IntegrationPushLogServiceImpl
        extends ServiceImpl<IntegrationPushLogMapper, IntegrationPushLog>
        implements IIntegrationPushLogService {
}
