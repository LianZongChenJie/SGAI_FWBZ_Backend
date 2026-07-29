package org.jeecg.modules.master.service;

import org.jeecg.modules.master.entity.IntegrationLog;
import org.jeecg.modules.master.mapper.IntegrationLogMapper;
import org.jeecg.modules.master.service.impl.IntegrationLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationLogServiceImplTest {

    @Mock
    IntegrationLogMapper baseMapper;

    @InjectMocks
    IntegrationLogServiceImpl service;

    @Test
    void writeLog_fillsIdAndCreateTime_thenInsert() {
        IntegrationLog log = new IntegrationLog();
        log.setDirection("PUSH");
        log.setStatus("SUCCESS");
        when(baseMapper.insert(any(IntegrationLog.class))).thenReturn(1);

        service.writeLog(log);

        assertNotNull(log.getId());
        assertNotNull(log.getCreateTime());
        verify(baseMapper).insert(log);
    }

    @Test
    void writeLog_nullError_keepsNull() {
        IntegrationLog log = new IntegrationLog();
        log.setDirection("RECEIVE");
        log.setStatus("PARTIAL");
        when(baseMapper.insert(any(IntegrationLog.class))).thenReturn(1);

        service.writeLog(log);

        ArgumentCaptor<IntegrationLog> cap = ArgumentCaptor.forClass(IntegrationLog.class);
        verify(baseMapper).insert(cap.capture());
        assertNull(cap.getValue().getError());
    }
}
