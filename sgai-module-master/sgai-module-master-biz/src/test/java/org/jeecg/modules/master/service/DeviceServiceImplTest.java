package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    DeviceMapper baseMapper;
    @Mock
    DeviceCategoryMapper deviceCategoryMapper;
    @Mock
    SpaceMapper spaceMapper;
    @Mock
    org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    DeviceServiceImpl service;

    @Test
    void create_blankCategoryId_throws() {
        Device d = new Device();
        d.setName("设备A");
        d.setSpaceId("s1");
        assertThrows(JeecgBootException.class, () -> service.create(d));
    }

    @Test
    void create_duplicateName_throws() {
        Device d = new Device();
        d.setName("设备A");
        d.setCategoryId("c1");
        d.setSpaceId("s1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThrows(JeecgBootException.class, () -> service.create(d));
    }

    @Test
    void create_categoryNotExist_throws() {
        Device d = new Device();
        d.setName("设备A");
        d.setCategoryId("c1");
        d.setSpaceId("s1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deviceCategoryMapper.selectById("c1")).thenReturn(null);
        assertThrows(JeecgBootException.class, () -> service.create(d));
    }

    @Test
    void create_ok() {
        Device d = new Device();
        d.setName("设备A");
        d.setCategoryId("c1");
        d.setSpaceId("s1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deviceCategoryMapper.selectById("c1")).thenReturn(new DeviceCategory());
        when(spaceMapper.selectById("s1")).thenReturn(new Space());
        when(baseMapper.insert(any(Device.class))).thenReturn(1);

        service.create(d);

        assertNotNull(d.getId());
        verify(baseMapper).insert(any(Device.class));
    }

    @Test
    void create_publishesCreateEvent() {
        Device d = new Device();
        d.setName("设备A");
        d.setCategoryId("C1");
        d.setSpaceId("S1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L); // countName 无冲突
        when(deviceCategoryMapper.selectById("C1")).thenReturn(new DeviceCategory());
        when(spaceMapper.selectById("S1")).thenReturn(new Space());
        when(baseMapper.insert(any(Device.class))).thenReturn(1);

        service.create(d);

        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.EntityType.DEVICE, cap.getValue().getEntityType());
        assertEquals(MasterDataChangeEvent.Op.CREATE, cap.getValue().getOp());
    }
}
