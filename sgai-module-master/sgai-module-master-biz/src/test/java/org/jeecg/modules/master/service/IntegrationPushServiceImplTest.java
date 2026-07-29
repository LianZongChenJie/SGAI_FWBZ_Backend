package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.master.common.IntegrationHttpExecutor;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.IntegrationLog;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.entity.IntegrationSystemCategory;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.mapper.DeviceCategoryMapper;
import org.jeecg.modules.master.mapper.DeviceMapper;
import org.jeecg.modules.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.modules.master.mapper.IntegrationSystemMapper;
import org.jeecg.modules.master.mapper.SpaceMapper;
import org.jeecg.modules.master.service.impl.IntegrationPushServiceImpl;
import org.jeecg.modules.master.vo.IntegrationPayload;
import org.jeecg.modules.master.vo.PushSnapshotResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationPushServiceImplTest {

    @Mock IntegrationSystemMapper integrationSystemMapper;
    @Mock IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Mock DeviceCategoryMapper deviceCategoryMapper;
    @Mock SpaceMapper spaceMapper;
    @Mock DeviceMapper deviceMapper;
    @Mock IIntegrationLogService logService;
    @Mock IntegrationHttpExecutor httpExecutor;

    @InjectMocks
    IntegrationPushServiceImpl service;

    private IntegrationSystem pushSystem() {
        IntegrationSystem s = new IntegrationSystem();
        s.setId("S1"); s.setCode("CODE1"); s.setPushEnabled(1);
        s.setPushUrl("http://x"); s.setToken("TOK");
        return s;
    }

    private IntegrationPayload devicePayload() {
        Device d = new Device();
        d.setId("D1"); d.setCategoryId("C1");
        return org.jeecg.modules.master.common.PushPayloadBuilder.devices(
                "CODE1", IntegrationPayload.Op.UPSERT, "B1", Collections.singletonList(d));
    }

    @Test
    void pushOne_2xx_writesSuccessLog() {
        when(httpExecutor.post(anyString(), anyString(), anyString())).thenReturn(200);
        service.pushOne(pushSystem(), devicePayload());
        verify(logService).writeLog(argThat((IntegrationLog l) -> "SUCCESS".equals(l.getStatus())
                && "PUSH".equals(l.getDirection()) && "DEVICE".equals(l.getType())));
    }

    @Test
    void pushOne_non2xx_writesFailLogWithError() {
        when(httpExecutor.post(anyString(), anyString(), anyString())).thenReturn(500);
        service.pushOne(pushSystem(), devicePayload());
        verify(logService).writeLog(argThat((IntegrationLog l) -> "FAIL".equals(l.getStatus())
                && l.getError() != null));
    }

    @Test
    void pushOne_exception_writesFailLog() {
        when(httpExecutor.post(anyString(), anyString(), anyString())).thenReturn(-1);
        service.pushOne(pushSystem(), devicePayload());
        verify(logService).writeLog(argThat((IntegrationLog l) -> "FAIL".equals(l.getStatus())));
    }

    @Test
    void pushSnapshot_disabled_throws() {
        IntegrationSystem s = pushSystem();
        s.setPushEnabled(0);
        when(integrationSystemMapper.selectById("S1")).thenReturn(s);
        assertThrows(org.jeecg.common.exception.JeecgBootException.class,
                () -> service.pushSnapshotForSystem("S1"));
    }

    @Test
    void pushSnapshot_returns3ResultsAndWrites3Logs() {
        when(integrationSystemMapper.selectById("S1")).thenReturn(pushSystem());
        // 类别范围
        IntegrationSystemCategory row = new IntegrationSystemCategory();
        row.setSystemId("S1"); row.setCategoryId("C1");
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(row));
        // 空间全量
        Space sp = new Space(); sp.setId("SP1");
        when(spaceMapper.selectList(any())).thenReturn(Collections.singletonList(sp));
        // 类别（类别集内）
        org.jeecg.modules.master.entity.DeviceCategory cat = new org.jeecg.modules.master.entity.DeviceCategory();
        cat.setId("C1");
        when(deviceCategoryMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(cat));
        // 设备（category_id ∈ 集合）
        Device d = new Device(); d.setId("D1"); d.setCategoryId("C1");
        when(deviceMapper.selectList(any())).thenReturn(Collections.singletonList(d));
        when(httpExecutor.post(anyString(), anyString(), anyString())).thenReturn(200);

        List<PushSnapshotResult> results = service.pushSnapshotForSystem("S1");

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(r -> "SUCCESS".equals(r.getStatus())));
        verify(logService, times(3)).writeLog(any(IntegrationLog.class));
    }

    private static <T> T argThat(org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
