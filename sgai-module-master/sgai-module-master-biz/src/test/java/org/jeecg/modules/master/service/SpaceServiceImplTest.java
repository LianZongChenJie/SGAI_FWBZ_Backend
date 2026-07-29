package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.master.common.MasterDataChangeEvent;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.mapper.DeviceMapper;
import org.jeecg.modules.master.mapper.SpaceMapper;
import org.jeecg.modules.master.service.impl.SpaceServiceImpl;
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
class SpaceServiceImplTest {

    @Mock
    SpaceMapper baseMapper;
    @Mock
    DeviceMapper deviceMapper;
    @Mock
    org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    SpaceServiceImpl service;

    @Test
    void create_sortNull_noSibling_assigns1() {
        Space s = new Space();
        s.setName("一层");
        s.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(Space.class))).thenReturn(1);

        service.create(s);

        assertEquals(Integer.valueOf(1), s.getSort());
    }

    @Test
    void create_sortNull_siblingMax5_assigns6() {
        Space s = new Space();
        s.setName("一层");
        s.setPid("0");
        Space max = new Space();
        max.setSort(5);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(max);
        when(baseMapper.insert(any(Space.class))).thenReturn(1);

        service.create(s);

        assertEquals(Integer.valueOf(6), s.getSort());
    }

    @Test
    void create_sortProvided_keepsProvided() {
        Space s = new Space();
        s.setName("一层");
        s.setPid("0");
        s.setSort(3);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(Space.class))).thenReturn(1);

        service.create(s);

        assertEquals(Integer.valueOf(3), s.getSort());
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
        Space s = new Space();
        s.setName("一楼");
        s.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(Space.class))).thenReturn(1);

        service.create(s);

        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.EntityType.SPACE, cap.getValue().getEntityType());
        assertEquals(MasterDataChangeEvent.Op.CREATE, cap.getValue().getOp());
    }
}
