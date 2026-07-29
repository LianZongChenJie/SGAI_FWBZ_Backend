package org.jeecg.modules.master.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.master.common.MasterDataChangeEvent;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.mapper.DeviceCategoryMapper;
import org.jeecg.modules.master.mapper.DeviceMapper;
import org.jeecg.modules.master.mapper.SpaceMapper;
import org.jeecg.modules.master.service.impl.DeviceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceUpsertFromIntegrationTest {

    @Mock
    DeviceMapper baseMapper;
    @Mock
    DeviceCategoryMapper deviceCategoryMapper;
    @Mock
    SpaceMapper spaceMapper;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    DeviceServiceImpl service;

    private Device incoming() {
        Device d = new Device();
        d.setId("D1");
        d.setName("设备A");
        d.setCategoryId("C1");
        d.setSpaceId("S1");
        d.setRemark("r");
        return d;
    }

    private void stubRefsExist() {
        when(deviceCategoryMapper.selectById("C1")).thenReturn(new DeviceCategory());
        when(spaceMapper.selectById("S1")).thenReturn(new Space());
    }

    @Test
    void upsert_newId_insertsWithGivenIdAndPublishesCreate() {
        Device d = incoming();
        stubRefsExist();
        when(baseMapper.selectCount(any())).thenReturn(0L); // countName 无冲突
        when(baseMapper.selectById("D1")).thenReturn(null); // 新增
        when(baseMapper.insert(any(Device.class))).thenReturn(1);

        service.upsertFromIntegration(d, "SYS_A");

        verify(baseMapper).insert(d); // 用传入 id，不重新生成
        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.Op.CREATE, cap.getValue().getOp());
        assertEquals("SYS_A", cap.getValue().getExcludeSystemCode());
    }

    @Test
    void upsert_existingId_updatesAndPublishesUpdate() {
        Device d = incoming();
        stubRefsExist();
        when(baseMapper.selectCount(any())).thenReturn(0L);
        when(baseMapper.selectById("D1")).thenReturn(new Device()); // 已存在

        service.upsertFromIntegration(d, null);

        verify(baseMapper).updateById(d);
        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.Op.UPDATE, cap.getValue().getOp());
        verify(baseMapper, never()).insert(any(Device.class));
    }

    @Test
    void upsert_categoryNotExist_throws() {
        Device d = incoming();
        when(deviceCategoryMapper.selectById("C1")).thenReturn(null);
        assertThrows(JeecgBootException.class, () -> service.upsertFromIntegration(d, null));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void upsert_nameConflict_throws() {
        Device d = incoming();
        stubRefsExist();
        when(baseMapper.selectCount(any())).thenReturn(1L); // 名称撞别的 id
        assertThrows(JeecgBootException.class, () -> service.upsertFromIntegration(d, null));
        verify(baseMapper, never()).insert(any(Device.class));
    }

    @Test
    void delete_deletesAndPublishesDeleteWithCategoryId() {
        Device d = incoming();
        when(baseMapper.deleteById("D1")).thenReturn(1);

        service.deleteFromIntegration(d, "SYS_A");

        verify(baseMapper).deleteById("D1");
        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.Op.DELETE, cap.getValue().getOp());
        assertEquals("C1", cap.getValue().getDevices().get(0).getCategoryId());
    }
}
