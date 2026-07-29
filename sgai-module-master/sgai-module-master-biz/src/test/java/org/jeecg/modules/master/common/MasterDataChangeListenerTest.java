package org.jeecg.modules.master.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.entity.IntegrationSystemCategory;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.modules.master.mapper.IntegrationSystemMapper;
import org.jeecg.modules.master.service.IIntegrationPushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterDataChangeListenerTest {

    @Mock IntegrationSystemMapper integrationSystemMapper;
    @Mock IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Mock IIntegrationPushService pushService;

    @InjectMocks
    MasterDataChangeListener listener;

    private IntegrationSystem sys(String id, String code) {
        IntegrationSystem s = new IntegrationSystem();
        s.setId(id); s.setCode(code); s.setPushEnabled(1);
        s.setPushUrl("http://x"); s.setToken("T");
        return s;
    }

    private void stubSystemsAndScope() {
        // load() 第 1 次查询：integrationSystemMapper.selectList → 两系统
        when(integrationSystemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(sys("S1", "CODE1"), sys("S2", "CODE2")));
        // load() 第 2 次查询：integrationSystemCategoryMapper.selectList → 类别范围（注意是另一个 mapper）
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(scope("S1", "C1"), scope("S2", "C2")));
    }

    private IntegrationSystemCategory scope(String systemId, String categoryId) {
        IntegrationSystemCategory r = new IntegrationSystemCategory();
        r.setSystemId(systemId); r.setCategoryId(categoryId);
        return r;
    }

    @Test
    void deviceChange_pushesOnlyToScopedSystem() {
        stubSystemsAndScope();
        Device d = new Device();
        d.setId("D1"); d.setCategoryId("C1"); // 命中 CODE1（S1→C1）

        listener.onChange(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Collections.singletonList(d), null));

        verify(pushService, times(1)).pushOne(any(IntegrationSystem.class), any());
        verify(pushService).pushOne(eq(sys("S1", "CODE1")), any());
        verify(pushService, never()).pushOne(eq(sys("S2", "CODE2")), any());
    }

    @Test
    void deviceChange_excludesSourceSystem() {
        // 两系统都含 C1，exclude=CODE1 → 只推 CODE2
        when(integrationSystemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(sys("S1", "CODE1"), sys("S2", "CODE2")));
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(scope("S1", "C1"), scope("S2", "C1")));
        Device d = new Device();
        d.setId("D1"); d.setCategoryId("C1");

        listener.onChange(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Collections.singletonList(d), "CODE1"));

        verify(pushService, times(1)).pushOne(any(IntegrationSystem.class), any());
        verify(pushService).pushOne(eq(sys("S2", "CODE2")), any());
        verify(pushService, never()).pushOne(eq(sys("S1", "CODE1")), any());
    }

    @Test
    void categoryChange_exactMatchOnly() {
        stubSystemsAndScope();
        DeviceCategory c = new DeviceCategory();
        c.setId("C1"); c.setName("电气");

        listener.onChange(MasterDataChangeEvent.ofCategories(
                MasterDataChangeEvent.Op.CREATE, Collections.singletonList(c), null));

        verify(pushService).pushOne(eq(sys("S1", "CODE1")), any());
        verify(pushService, never()).pushOne(eq(sys("S2", "CODE2")), any());
    }

    @Test
    void spaceChange_pushesAllExceptExclude() {
        stubSystemsAndScope();
        Space sp = new Space();
        sp.setId("SP1");

        listener.onChange(MasterDataChangeEvent.ofSpaces(
                MasterDataChangeEvent.Op.UPDATE, Collections.singletonList(sp), "CODE2"));

        verify(pushService).pushOne(eq(sys("S1", "CODE1")), any());
        verify(pushService, never()).pushOne(eq(sys("S2", "CODE2")), any());
    }

    @Test
    void deviceChange_emptyDevices_doesNothing() {
        listener.onChange(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Collections.emptyList(), null));
        verifyNoInteractions(pushService);
    }
}
