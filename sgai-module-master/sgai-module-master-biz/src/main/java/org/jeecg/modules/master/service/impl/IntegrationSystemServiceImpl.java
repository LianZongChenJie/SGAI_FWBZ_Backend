package org.jeecg.modules.master.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.master.common.TreeFullNameHelper;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.entity.IntegrationSystemCategory;
import org.jeecg.modules.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.modules.master.mapper.IntegrationSystemMapper;
import org.jeecg.modules.master.service.IIntegrationSystemService;
import org.jeecg.modules.master.vo.IntegrationSystemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IntegrationSystemServiceImpl
        extends ServiceImpl<IntegrationSystemMapper, IntegrationSystem>
        implements IIntegrationSystemService {

    @Autowired
    private IntegrationSystemCategoryMapper integrationSystemCategoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFromForm(IntegrationSystemForm form) {
        validateForm(form);
        assertCodeUnique(form.getCode(), null);
        assertTokenUnique(form.getToken(), null);

        IntegrationSystem entity = new IntegrationSystem();
        BeanUtil.copyProperties(form, entity);
        entity.setId(TreeFullNameHelper.generateUuid());
        baseMapper.insert(entity);
        overwriteCategories(entity.getId(), form.getCategoryIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFromForm(IntegrationSystemForm form) {
        if (StrUtil.isBlank(form.getId())) {
            throw new JeecgBootException("id不能为空");
        }
        validateForm(form);
        assertCodeUnique(form.getCode(), form.getId());
        assertTokenUnique(form.getToken(), form.getId());

        IntegrationSystem entity = new IntegrationSystem();
        BeanUtil.copyProperties(form, entity);
        baseMapper.updateById(entity);
        overwriteCategories(entity.getId(), form.getCategoryIds());
    }

    @Override
    public IntegrationSystemForm getFormById(String id) {
        IntegrationSystem sys = baseMapper.selectById(id);
        if (sys == null) {
            return null;
        }
        IntegrationSystemForm form = new IntegrationSystemForm();
        BeanUtil.copyProperties(sys, form);
        List<IntegrationSystemCategory> rows = integrationSystemCategoryMapper.selectList(
                new LambdaQueryWrapper<IntegrationSystemCategory>()
                        .eq(IntegrationSystemCategory::getSystemId, id));
        form.setCategoryIds(rows.stream()
                .map(IntegrationSystemCategory::getCategoryId)
                .collect(Collectors.toList()));
        return form;
    }

    @Override
    public IPage<IntegrationSystem> listPage(Page<IntegrationSystem> page, String name, String code) {
        LambdaQueryWrapper<IntegrationSystem> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            w.like(IntegrationSystem::getName, name);
        }
        if (StrUtil.isNotBlank(code)) {
            w.like(IntegrationSystem::getCode, code);
        }
        w.orderByDesc(IntegrationSystem::getCreateTime);
        return baseMapper.selectPage(page, w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIdWithCheck(String id) {
        IntegrationSystem sys = baseMapper.selectById(id);
        if (sys == null) {
            return;
        }
        if (Integer.valueOf(1).equals(sys.getPushEnabled())
                || Integer.valueOf(1).equals(sys.getReceiveEnabled())) {
            throw new JeecgBootException("请先停用该对接系统");
        }
        integrationSystemCategoryMapper.delete(new LambdaQueryWrapper<IntegrationSystemCategory>()
                .eq(IntegrationSystemCategory::getSystemId, id));
        baseMapper.deleteById(id);
    }

    @Override
    public IntegrationSystem findByToken(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return baseMapper.selectOne(new LambdaQueryWrapper<IntegrationSystem>()
                .eq(IntegrationSystem::getToken, token)
                .eq(IntegrationSystem::getReceiveEnabled, 1));
    }

    // ---------- 私有工具 ----------

    private void validateForm(IntegrationSystemForm form) {
        if (form == null || StrUtil.isBlank(form.getName())) {
            throw new JeecgBootException("系统名称不能为空");
        }
        if (StrUtil.isBlank(form.getCode())) {
            throw new JeecgBootException("系统编码不能为空");
        }
        if (form.getCategoryIds() == null || form.getCategoryIds().isEmpty()) {
            throw new JeecgBootException("请选择类别范围");
        }
        if ((Integer.valueOf(1).equals(form.getPushEnabled())
                || Integer.valueOf(1).equals(form.getReceiveEnabled()))
                && StrUtil.isBlank(form.getToken())) {
            throw new JeecgBootException("请填写令牌");
        }
    }

    private void assertCodeUnique(String code, String excludeId) {
        LambdaQueryWrapper<IntegrationSystem> w = new LambdaQueryWrapper<IntegrationSystem>()
                .eq(IntegrationSystem::getCode, code);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(IntegrationSystem::getId, excludeId);
        }
        if (baseMapper.selectCount(w) > 0) {
            throw new JeecgBootException("系统编码已存在");
        }
    }

    private void assertTokenUnique(String token, String excludeId) {
        if (StrUtil.isBlank(token)) {
            return; // 未启用推送/接收，无 token
        }
        LambdaQueryWrapper<IntegrationSystem> w = new LambdaQueryWrapper<IntegrationSystem>()
                .eq(IntegrationSystem::getToken, token);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(IntegrationSystem::getId, excludeId);
        }
        if (baseMapper.selectCount(w) > 0) {
            throw new JeecgBootException("令牌已存在");
        }
    }

    /** 类别范围整体覆盖：先按 system_id 删，再批量 insert。 */
    private void overwriteCategories(String systemId, List<String> categoryIds) {
        integrationSystemCategoryMapper.delete(new LambdaQueryWrapper<IntegrationSystemCategory>()
                .eq(IntegrationSystemCategory::getSystemId, systemId));
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        for (String cid : categoryIds) {
            IntegrationSystemCategory row = new IntegrationSystemCategory();
            row.setId(TreeFullNameHelper.generateUuid());
            row.setSystemId(systemId);
            row.setCategoryId(cid);
            integrationSystemCategoryMapper.insert(row);
        }
    }
}
