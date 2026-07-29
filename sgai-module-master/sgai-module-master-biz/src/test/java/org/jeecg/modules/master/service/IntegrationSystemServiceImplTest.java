package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.entity.IntegrationSystemCategory;
import org.jeecg.modules.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.modules.master.mapper.IntegrationSystemMapper;
import org.jeecg.modules.master.service.impl.IntegrationSystemServiceImpl;
import org.jeecg.modules.master.vo.IntegrationSystemForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationSystemServiceImplTest {

    @Mock
    IntegrationSystemMapper baseMapper;
    @Mock
    IntegrationSystemCategoryMapper integrationSystemCategoryMapper;

    @InjectMocks
    IntegrationSystemServiceImpl service;

    private IntegrationSystemForm form(String name, String code, List<String> catIds) {
        IntegrationSystemForm f = new IntegrationSystemForm();
        f.setName(name);
        f.setCode(code);
        f.setPushEnabled(1);
        f.setReceiveEnabled(0);
        f.setToken("TOK");
        f.setCategoryIds(catIds);
        return f;
    }

    @Test
    void save_duplicateCode_throws() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThrows(JeecgBootException.class, () -> service.saveFromForm(form("A", "CODE1", Arrays.asList("C1"))));
    }

    @Test
    void save_ok_insertsSystemAndCategoryRows() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(IntegrationSystem.class))).thenReturn(1);
        when(integrationSystemCategoryMapper.insert(any(IntegrationSystemCategory.class))).thenReturn(1);

        service.saveFromForm(form("A", "CODE1", Arrays.asList("C1", "C2")));

        ArgumentCaptor<IntegrationSystem> sysCap = ArgumentCaptor.forClass(IntegrationSystem.class);
        verify(baseMapper).insert(sysCap.capture());
        assertNotNull(sysCap.getValue().getId());
        assertEquals("CODE1", sysCap.getValue().getCode());
        // 类别子表：先删（无）+ 插 2 行
        verify(integrationSystemCategoryMapper).delete(any(LambdaQueryWrapper.class));
        verify(integrationSystemCategoryMapper, times(2)).insert(any(IntegrationSystemCategory.class));
    }

    @Test
    void save_emptyCategoryIds_throws() {
        IntegrationSystemForm f = form("A", "CODE1", Collections.emptyList());
        // validateForm 在唯一性查询之前即校验 categoryIds 非空，此处不触发 selectCount
        assertThrows(JeecgBootException.class, () -> service.saveFromForm(f));
    }

    @Test
    void update_overwritesCategories_deleteThenInsert() {
        IntegrationSystemForm f = form("A", "CODE1", Arrays.asList("C3"));
        f.setId("S1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.updateById(any(IntegrationSystem.class))).thenReturn(1);
        when(integrationSystemCategoryMapper.insert(any(IntegrationSystemCategory.class))).thenReturn(1);

        service.updateFromForm(f);

        verify(integrationSystemCategoryMapper).delete(any(LambdaQueryWrapper.class));
        verify(integrationSystemCategoryMapper, times(1)).insert(any(IntegrationSystemCategory.class));
        verify(baseMapper).updateById(any(IntegrationSystem.class));
    }

    @Test
    void remove_pushEnabled_throws() {
        IntegrationSystem sys = new IntegrationSystem();
        sys.setId("S1"); sys.setPushEnabled(1); sys.setReceiveEnabled(0);
        when(baseMapper.selectById("S1")).thenReturn(sys);
        assertThrows(JeecgBootException.class, () -> service.removeByIdWithCheck("S1"));
    }

    @Test
    void remove_disabled_deletesCategoriesAndSystem() {
        IntegrationSystem sys = new IntegrationSystem();
        sys.setId("S1"); sys.setPushEnabled(0); sys.setReceiveEnabled(0);
        when(baseMapper.selectById("S1")).thenReturn(sys);
        when(integrationSystemCategoryMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(baseMapper.deleteById("S1")).thenReturn(1);

        service.removeByIdWithCheck("S1");

        verify(integrationSystemCategoryMapper).delete(any(LambdaQueryWrapper.class));
        verify(baseMapper).deleteById("S1");
    }

    @Test
    void getFormById_returnsFormWithCategoryIds() {
        IntegrationSystem sys = new IntegrationSystem();
        sys.setId("S1"); sys.setName("A"); sys.setCode("CODE1");
        when(baseMapper.selectById("S1")).thenReturn(sys);
        IntegrationSystemCategory row = new IntegrationSystemCategory();
        row.setSystemId("S1"); row.setCategoryId("C1");
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(row));

        IntegrationSystemForm form = service.getFormById("S1");

        assertEquals("S1", form.getId());
        assertEquals(Arrays.asList("C1"), form.getCategoryIds());
    }

    @Test
    void findByToken_enabled_matches() {
        IntegrationSystem sys = new IntegrationSystem();
        sys.setCode("CODE1"); sys.setReceiveEnabled(1);
        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sys);

        IntegrationSystem found = service.findByToken("TOK");

        assertNotNull(found);
        assertEquals("CODE1", found.getCode());
    }
}
