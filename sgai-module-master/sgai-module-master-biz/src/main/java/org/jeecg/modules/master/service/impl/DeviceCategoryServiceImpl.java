package org.jeecg.modules.master.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.master.common.MasterDataChangeEvent;
import org.jeecg.modules.master.common.TreeFullNameHelper;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.mapper.DeviceCategoryMapper;
import org.jeecg.modules.master.mapper.DeviceMapper;
import org.jeecg.modules.master.service.IDeviceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceCategoryServiceImpl
        extends ServiceImpl<DeviceCategoryMapper, DeviceCategory>
        implements IDeviceCategoryService {

    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<DeviceCategory> listAll(String name) {
        LambdaQueryWrapper<DeviceCategory> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            w.like(DeviceCategory::getName, name);
        }
        w.orderByAsc(DeviceCategory::getSort);
        return this.list(w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DeviceCategory entity) {
        String pid = StrUtil.isBlank(entity.getPid()) ? TreeFullNameHelper.ROOT_PID : entity.getPid();
        if (countSameLevel(pid, entity.getName(), null) > 0) {
            throw new JeecgBootException("同级下已存在同名类别");
        }
        entity.setId(TreeFullNameHelper.generateUuid());
        entity.setPid(pid);
        entity.setFullName(resolveFullName(pid, entity.getName()));
        if (entity.getSort() == null) {
            entity.setSort(nextSort(pid));
        }
        this.save(entity);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofCategories(
                MasterDataChangeEvent.Op.CREATE, Collections.singletonList(entity), null));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNode(DeviceCategory entity) {
        DeviceCategory old = this.getById(entity.getId());
        if (old == null) {
            throw new JeecgBootException("类别不存在");
        }
        String newPid = StrUtil.isBlank(entity.getPid()) ? TreeFullNameHelper.ROOT_PID : entity.getPid();
        String newName = entity.getName();

        if (countSameLevel(newPid, newName, entity.getId()) > 0) {
            throw new JeecgBootException("同级下已存在同名类别");
        }

        boolean pidChanged = !newPid.equals(old.getPid());
        boolean nameChanged = !newName.equals(old.getName());

        if (pidChanged) {
            if (entity.getId().equals(newPid)) {
                throw new JeecgBootException("不能移动到自身下");
            }
            Set<String> subtreeIds = collectSubtreeIds(entity.getId());
            TreeFullNameHelper.assertMovable(subtreeIds, newPid);
            if (!TreeFullNameHelper.ROOT_PID.equals(newPid) && this.getById(newPid) == null) {
                throw new JeecgBootException("所选上级类别不存在");
            }
        }

        String newFullName = resolveFullName(newPid, newName);
        entity.setPid(newPid);
        entity.setFullName(newFullName);
        this.updateById(entity);

        List<DeviceCategory> affected = new ArrayList<>();
        affected.add(entity);
        if (pidChanged || nameChanged) {
            affected.addAll(recalcSubtreeFullName(entity.getId()));
        }
        eventPublisher.publishEvent(MasterDataChangeEvent.ofCategories(
                MasterDataChangeEvent.Op.UPDATE, affected, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeNode(String id) {
        long childCnt = this.count(new LambdaQueryWrapper<DeviceCategory>()
                .eq(DeviceCategory::getPid, id));
        if (childCnt > 0) {
            throw new JeecgBootException("存在子级，请先删除子级");
        }
        long refCnt = deviceMapper.selectCount(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, id));
        if (refCnt > 0) {
            throw new JeecgBootException("该类别被设备引用，无法删除");
        }
        this.removeById(id);
        DeviceCategory deleted = new DeviceCategory();
        deleted.setId(id);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofCategories(
                MasterDataChangeEvent.Op.DELETE, Collections.singletonList(deleted), null));
    }

    // ---------- 私有工具 ----------

    /** 同级(pid)下 sort 非 null 的最大值 +1；同 pid 无有效节点则返回 1。 */
    private Integer nextSort(String pid) {
        DeviceCategory max = baseMapper.selectOne(new LambdaQueryWrapper<DeviceCategory>()
                .eq(DeviceCategory::getPid, pid)
                .isNotNull(DeviceCategory::getSort)
                .orderByDesc(DeviceCategory::getSort)
                .last("LIMIT 1"));
        return max == null ? 1 : max.getSort() + 1;
    }

    private long countSameLevel(String pid, String name, String excludeId) {
        LambdaQueryWrapper<DeviceCategory> w = new LambdaQueryWrapper<DeviceCategory>()
                .eq(DeviceCategory::getPid, pid)
                .eq(DeviceCategory::getName, name);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(DeviceCategory::getId, excludeId);
        }
        return this.count(w);
    }

    /** pid="0" → name；否则取 parent.fullName 拼接。 */
    private String resolveFullName(String pid, String name) {
        if (TreeFullNameHelper.ROOT_PID.equals(pid)) {
            return name;
        }
        DeviceCategory parent = this.getById(pid);
        if (parent == null) {
            throw new JeecgBootException("所选上级类别不存在");
        }
        return TreeFullNameHelper.buildFullName(parent.getFullName(), name);
    }

    /** 迭代收集子孙 id（不含 root）：按层 selectList(in(pid, frontier))，循环至空。 */
    private Set<String> collectSubtreeIds(String rootId) {
        Set<String> all = new HashSet<>();
        List<String> frontier = Collections.singletonList(rootId);
        while (!frontier.isEmpty()) {
            List<DeviceCategory> children = this.list(new LambdaQueryWrapper<DeviceCategory>()
                    .in(DeviceCategory::getPid, frontier));
            if (children.isEmpty()) {
                break;
            }
            List<String> childIds = children.stream()
                    .map(DeviceCategory::getId).collect(Collectors.toList());
            all.addAll(childIds);
            frontier = childIds;
        }
        return all;
    }

    /** 迭代重算子树 full_name：内存按 pid 分组，BFS 自顶向下拼接，updateBatchById 批量更新。返回受影响子节点列表。 */
    private List<DeviceCategory> recalcSubtreeFullName(String rootId) {
        Set<String> descIds = collectSubtreeIds(rootId);
        if (descIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<DeviceCategory> descendants = this.listByIds(descIds);
        Map<String, List<DeviceCategory>> byPid = descendants.stream()
                .collect(Collectors.groupingBy(DeviceCategory::getPid));

        DeviceCategory root = this.getById(rootId); // 已含新 fullName
        List<DeviceCategory> toUpdate = new ArrayList<>();
        Deque<DeviceCategory> queue = new ArrayDeque<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            DeviceCategory node = queue.poll();
            List<DeviceCategory> kids = byPid.get(node.getId());
            if (kids != null) {
                for (DeviceCategory k : kids) {
                    k.setFullName(TreeFullNameHelper.buildFullName(node.getFullName(), k.getName()));
                    toUpdate.add(k);
                    queue.offer(k);
                }
            }
        }
        if (!toUpdate.isEmpty()) {
            this.updateBatchById(toUpdate);
        }
        return toUpdate;
    }
}
