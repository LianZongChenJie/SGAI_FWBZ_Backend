package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.entity.IntegrationSystemCategory;
import org.jeecg.modules.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.modules.master.service.impl.IntegrationReceiveServiceImpl;
import org.jeecg.modules.master.vo.DevicePushItem;
import org.jeecg.modules.master.vo.IntegrationPayload;
import org.jeecg.modules.master.vo.ReceivePayload;
import org.jeecg.modules.master.vo.ReceiveResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationReceiveServiceImplTest {

    @Mock IIntegrationSystemService integrationSystemService;
    @Mock IDeviceService deviceService;
    @Mock IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Mock IIntegrationLogService logService;

    @InjectMocks
    IntegrationReceiveServiceImpl service;

    private IntegrationSystem sys() {
        IntegrationSystem s = new IntegrationSystem();
        s.setId("S1"); s.setCode("CODE1"); s.setReceiveEnabled(1);
        return s;
    }

    private DevicePushItem item(String id, String catId) {
        DevicePushItem d = new DevicePushItem();
        d.setId(id); d.setCategoryId(catId); d.setName(id); d.setSpaceId("SP1");
        return d;
    }

    private ReceivePayload payload(String op, DevicePushItem... items) {
        ReceivePayload p = new ReceivePayload();
        p.setType(IntegrationPayload.Type.DEVICE);
        p.setOp("DELETE".equals(op) ? IntegrationPayload.Op.DELETE : IntegrationPayload.Op.UPSERT);
        p.setBatchId("B1");
        p.setData(Arrays.asList(items));
        return p;
    }

    private void stubScope(String catId) {
        IntegrationSystemCategory row = new IntegrationSystemCategory();
        row.setSystemId("S1"); row.setCategoryId(catId);
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(row));
    }

    @Test
    void receive_authFail_throwsAndLogsFail() {
        when(integrationSystemService.findByToken("BAD")).thenReturn(null);
        assertThrows(JeecgBootException.class, () -> service.receive(payload("UPSERT", item("D1", "C1")), "BAD"));
        verify(logService).writeLog(argThat((org.jeecg.modules.master.entity.IntegrationLog l) ->
                "FAIL".equals(l.getStatus()) && "RECEIVE".equals(l.getDirection())));
    }

    @Test
    void receive_mixedAcceptedRejected() {
        when(integrationSystemService.findByToken("TOK")).thenReturn(sys());
        stubScope("C1"); // 仅 C1 在范围
        ReceivePayload p = payload("UPSERT", item("D1", "C1"), item("D2", "C9"));

        ReceiveResult r = service.receive(p, "TOK");

        assertEquals(1, r.getAccepted());
        assertEquals(1, r.getRejected().size());
        assertEquals("D2", r.getRejected().get(0).getId());
        verify(logService).writeLog(argThat((org.jeecg.modules.master.entity.IntegrationLog l) ->
                "PARTIAL".equals(l.getStatus())));
    }

    @Test
    void receive_upsertPassesExcludeSystemCode() {
        when(integrationSystemService.findByToken("TOK")).thenReturn(sys());
        stubScope("C1");
        service.receive(payload("UPSERT", item("D1", "C1")), "TOK");
        verify(deviceService).upsertFromIntegration(any(), eq("CODE1"));
    }

    @Test
    void receive_delete_callsDeleteWithExclude() {
        when(integrationSystemService.findByToken("TOK")).thenReturn(sys());
        stubScope("C1");
        service.receive(payload("DELETE", item("D1", "C1")), "TOK");
        verify(deviceService).deleteFromIntegration(any(), eq("CODE1"));
        verify(deviceService, never()).upsertFromIntegration(any(), anyString());
    }

    @Test
    void receive_allOk_logsSuccess() {
        when(integrationSystemService.findByToken("TOK")).thenReturn(sys());
        stubScope("C1");
        ReceiveResult r = service.receive(payload("UPSERT", item("D1", "C1")), "TOK");
        assertEquals(1, r.getAccepted());
        assertTrue(r.getRejected().isEmpty());
        verify(logService).writeLog(argThat((org.jeecg.modules.master.entity.IntegrationLog l) ->
                "SUCCESS".equals(l.getStatus())));
    }

    private static <T> T argThat(java.util.function.Predicate<T> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
