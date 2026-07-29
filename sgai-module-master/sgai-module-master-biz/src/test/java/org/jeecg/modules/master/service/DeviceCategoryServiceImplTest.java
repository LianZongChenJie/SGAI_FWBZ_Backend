package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.master.common.MasterDataChangeEvent;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.mapper.DeviceCategoryMapper;
import org.jeecg.modules.master.mapper.DeviceMapper;
import org.jeecg.modules.master.service.impl.DeviceCategoryServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
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
class DeviceCategoryServiceImplTest {

    @Mock
    DeviceCategoryMapper baseMapper;
    @Mock
    DeviceMapper deviceMapper;
    @Mock
    org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    DeviceCategoryServiceImpl service;

    @Test
    void create_duplicateNameInSameLevel_throws() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThrows(JeecgBootException.class, () -> service.create(c));
    }

    @Test
    void create_root_buildsFullNameAsName() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        assertEquals("电气", c.getFullName());
        assertNotNull(c.getId());
        verify(baseMapper).insert(any(DeviceCategory.class));
    }

    @Test
    void remove_hasChildren_throws() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class)))
            .thenReturn(1L); // 第一次调用：count by pid = id
        assertThrows(JeecgBootException.class, () -> service.removeNode("X"));
    }

    @Test
    void remove_referencedByDevice_throws() {
        // 第一次 selectCount（子节点）=0；第二次（设备引用）=1
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deviceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThrows(JeecgBootException.class, () -> service.removeNode("X"));
    }

    @Test
    void update_moveToSelf_throws() {
        DeviceCategory old = new DeviceCategory();
        old.setId("A"); old.setPid("0"); old.setName("电气");
        DeviceCategory entity = new DeviceCategory();
        entity.setId("A"); entity.setPid("A"); entity.setName("电气"); // pid 设成自己
        when(baseMapper.selectById("A")).thenReturn(old);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        assertThrows(JeecgBootException.class, () -> service.updateNode(entity));
    }

    @Test
    void update_moveIntoSubtree_throws() {
        DeviceCategory old = new DeviceCategory();
        old.setId("A"); old.setPid("0"); old.setName("电气");
        DeviceCategory entity = new DeviceCategory();
        entity.setId("A"); entity.setPid("B"); entity.setName("电气"); // 移到 B 下，B 是 A 的子
        DeviceCategory childB = new DeviceCategory();
        childB.setId("B"); childB.setPid("A");
        when(baseMapper.selectById("A")).thenReturn(old);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        // collectSubtreeIds：第一次 in(pid,[A]) → [B]；第二次 in(pid,[B]) → []
        when(baseMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(java.util.Collections.singletonList(childB))
            .thenReturn(java.util.Collections.emptyList());
        assertThrows(JeecgBootException.class, () -> service.updateNode(entity));
    }

    @Test
    void create_sortNull_noSibling_assigns1() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        // 实现前 create 不调 selectOne；实现后 selectOne 默认返回 null → nextSort 返回 1
        assertEquals(Integer.valueOf(1), c.getSort());
    }

    @Test
    void create_sortNull_siblingMax5_assigns6() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        DeviceCategory max = new DeviceCategory();
        max.setSort(5);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(max);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        assertEquals(Integer.valueOf(6), c.getSort());
    }

    @Test
    void create_sortProvided_keepsProvided() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        c.setSort(3);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        assertEquals(Integer.valueOf(3), c.getSort());
        verify(baseMapper, never()).selectOne(any());
    }

    @Test
    void listAll_ordersBySort() {
        when(baseMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(java.util.Collections.emptyList());

        service.listAll(null);

        ArgumentCaptor<LambdaQueryWrapper> cap = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(baseMapper).selectList(cap.capture());
        String sql = cap.getValue().getSqlSegment();
        assertTrue(sql.contains("sort"));
    }

    @Test
    void create_publishesCreateEvent() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.EntityType.CATEGORY, cap.getValue().getEntityType());
        assertEquals(MasterDataChangeEvent.Op.CREATE, cap.getValue().getOp());
    }
}
