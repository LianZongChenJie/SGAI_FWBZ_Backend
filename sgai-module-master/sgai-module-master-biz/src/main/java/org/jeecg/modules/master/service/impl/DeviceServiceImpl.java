package org.jeecg.modules.master.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.master.common.MasterDataChangeEvent;
import org.jeecg.modules.master.common.TreeFullNameHelper;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.mapper.DeviceCategoryMapper;
import org.jeecg.modules.master.mapper.DeviceMapper;
import org.jeecg.modules.master.mapper.SpaceMapper;
import org.jeecg.modules.master.service.IDeviceService;
import org.jeecg.modules.master.vo.DeviceImportDTO;
import org.jeecg.modules.master.vo.DeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {

    @Autowired
    private DeviceCategoryMapper deviceCategoryMapper;
    @Autowired
    private SpaceMapper spaceMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public IPage<DeviceVO> pageVO(Page<Device> page, String name, String categoryId, String spaceId) {
        IPage<Device> p = this.page(page, buildWrapper(name, categoryId, spaceId));
        return toVOPage(p);
    }

    @Override
    public List<DeviceVO> listForExport(String name, String categoryId, String spaceId) {
        List<Device> list = this.list(buildWrapper(name, categoryId, spaceId));
        Page<Device> wrap = new Page<>();
        wrap.setRecords(list);
        wrap.setTotal(list.size());
        return toVOPage(wrap).getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Device entity) {
        validate(entity, null);
        entity.setId(TreeFullNameHelper.generateUuid());
        this.save(entity);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.CREATE, Collections.singletonList(entity), null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNode(Device entity) {
        if (StrUtil.isBlank(entity.getId())) {
            throw new JeecgBootException("设备id不能为空");
        }
        validate(entity, entity.getId());
        this.updateById(entity);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Collections.singletonList(entity), null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new JeecgBootException("未选择删除数据");
        }
        List<Device> affected = this.listByIds(ids);
        this.removeByIds(ids);
        if (affected != null && !affected.isEmpty()) {
            eventPublisher.publishEvent(MasterDataChangeEvent.ofDevices(
                    MasterDataChangeEvent.Op.DELETE, affected, null));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertFromIntegration(Device incoming, String excludeSystemCode) {
        if (StrUtil.isBlank(incoming.getId())) {
            throw new JeecgBootException("设备id不能为空");
        }
        if (StrUtil.isBlank(incoming.getCategoryId())) {
            throw new JeecgBootException("类别不能为空");
        }
        if (StrUtil.isBlank(incoming.getSpaceId())) {
            throw new JeecgBootException("空间不能为空");
        }
        if (deviceCategoryMapper.selectById(incoming.getCategoryId()) == null) {
            throw new JeecgBootException("类别不存在");
        }
        if (spaceMapper.selectById(incoming.getSpaceId()) == null) {
            throw new JeecgBootException("空间不存在");
        }
        if (countName(incoming.getName(), incoming.getId()) > 0) {
            throw new JeecgBootException("设备名称冲突");
        }
        boolean exists = baseMapper.selectById(incoming.getId()) != null;
        if (exists) {
            baseMapper.updateById(incoming);
        } else {
            baseMapper.insert(incoming); // 用传入 id，不重新生成
        }
        MasterDataChangeEvent.Op op = exists
                ? MasterDataChangeEvent.Op.UPDATE
                : MasterDataChangeEvent.Op.CREATE;
        eventPublisher.publishEvent(
                MasterDataChangeEvent.ofDevices(op, Collections.singletonList(incoming), excludeSystemCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFromIntegration(Device incoming, String excludeSystemCode) {
        baseMapper.deleteById(incoming.getId());
        eventPublisher.publishEvent(
                MasterDataChangeEvent.ofDevices(MasterDataChangeEvent.Op.DELETE,
                        Collections.singletonList(incoming), excludeSystemCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> batchImport(List<DeviceImportDTO> rows) {
        List<String> errors = new ArrayList<>();
        List<Device> imported = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return errors;
        }
        // 预载类别/空间全称映射（按全称定位 id）
        Map<String, String> catMap = loadFullNameToIdMap(
                deviceCategoryMapper.selectList(null), DeviceCategory::getFullName, DeviceCategory::getId);
        Map<String, String> spcMap = loadFullNameToIdMap(
                spaceMapper.selectList(null), Space::getFullName, Space::getId);

        int line = 1;
        for (DeviceImportDTO row : rows) {
            line++;
            try {
                if (StrUtil.isBlank(row.getName())) {
                    throw new JeecgBootException("设备名称不能为空");
                }
                if (countName(row.getName(), null) > 0) {
                    throw new JeecgBootException("设备名称已存在：" + row.getName());
                }
                String categoryId = catMap.get(row.getCategoryFullName());
                if (categoryId == null) {
                    throw new JeecgBootException("类别不存在：" + row.getCategoryFullName());
                }
                String spaceId = spcMap.get(row.getSpaceFullName());
                if (spaceId == null) {
                    throw new JeecgBootException("空间不存在：" + row.getSpaceFullName());
                }
                Device d = new Device();
                d.setId(TreeFullNameHelper.generateUuid());
                d.setName(row.getName());
                d.setCategoryId(categoryId);
                d.setSpaceId(spaceId);
                d.setRemark(row.getRemark());
                baseMapper.insert(d);
                imported.add(d);
            } catch (Exception e) {
                errors.add("第" + line + "行：" + e.getMessage());
            }
        }
        if (!imported.isEmpty()) {
            eventPublisher.publishEvent(MasterDataChangeEvent.ofDevices(
                    MasterDataChangeEvent.Op.CREATE, imported, null));
        }
        return errors;
    }

    // ---------- 私有工具 ----------

    private LambdaQueryWrapper<Device> buildWrapper(String name, String categoryId, String spaceId) {
        LambdaQueryWrapper<Device> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            w.like(Device::getName, name);
        }
        if (StrUtil.isNotBlank(categoryId)) {
            w.eq(Device::getCategoryId, categoryId);
        }
        if (StrUtil.isNotBlank(spaceId)) {
            w.eq(Device::getSpaceId, spaceId);
        }
        w.orderByDesc(Device::getCreateTime);
        return w;
    }

    private void validate(Device entity, String excludeId) {
        if (StrUtil.isBlank(entity.getName())) {
            throw new JeecgBootException("设备名称不能为空");
        }
        if (StrUtil.isBlank(entity.getCategoryId())) {
            throw new JeecgBootException("请选择类别");
        }
        if (StrUtil.isBlank(entity.getSpaceId())) {
            throw new JeecgBootException("请选择空间");
        }
        if (countName(entity.getName(), excludeId) > 0) {
            throw new JeecgBootException("设备名称已存在");
        }
        if (deviceCategoryMapper.selectById(entity.getCategoryId()) == null) {
            throw new JeecgBootException("所选类别不存在");
        }
        if (spaceMapper.selectById(entity.getSpaceId()) == null) {
            throw new JeecgBootException("所选空间不存在");
        }
    }

    private long countName(String name, String excludeId) {
        LambdaQueryWrapper<Device> w = new LambdaQueryWrapper<Device>().eq(Device::getName, name);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(Device::getId, excludeId);
        }
        return this.count(w);
    }

    /** 设备分页 → DeviceVO（内存拼类别/空间名称）。 */
    private IPage<DeviceVO> toVOPage(IPage<Device> p) {
        List<Device> records = p.getRecords();
        List<DeviceVO> voList = records.stream().map(d -> {
            DeviceVO vo = new DeviceVO();
            BeanUtil.copyProperties(d, vo);
            return vo;
        }).collect(Collectors.toList());

        Set<String> catIds = records.stream().map(Device::getCategoryId)
                .filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        Set<String> spcIds = records.stream().map(Device::getSpaceId)
                .filter(StrUtil::isNotBlank).collect(Collectors.toSet());

        Map<String, DeviceCategory> catMap = catIds.isEmpty() ? Collections.emptyMap()
                : deviceCategoryMapper.selectBatchIds(catIds).stream()
                .collect(Collectors.toMap(DeviceCategory::getId, c -> c));
        Map<String, Space> spcMap = spcIds.isEmpty() ? Collections.emptyMap()
                : spaceMapper.selectBatchIds(spcIds).stream()
                .collect(Collectors.toMap(Space::getId, s -> s));

        for (DeviceVO vo : voList) {
            DeviceCategory c = catMap.get(vo.getCategoryId());
            if (c != null) {
                vo.setCategoryName(c.getName());
                vo.setCategoryFullName(c.getFullName());
            }
            Space s = spcMap.get(vo.getSpaceId());
            if (s != null) {
                vo.setSpaceName(s.getName());
                vo.setSpaceFullName(s.getFullName());
            }
        }

        IPage<DeviceVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(voList);
        return result;
    }

    private <T> Map<String, String> loadFullNameToIdMap(List<T> list,
                                                        Function<T, String> fullNameFn,
                                                        Function<T, String> idFn) {
        Map<String, String> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (T t : list) {
            map.put(fullNameFn.apply(t), idFn.apply(t));
        }
        return map;
    }
}
