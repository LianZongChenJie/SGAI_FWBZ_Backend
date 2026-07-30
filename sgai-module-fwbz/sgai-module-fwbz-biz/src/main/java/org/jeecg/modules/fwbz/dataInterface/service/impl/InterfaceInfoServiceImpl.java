package org.jeecg.modules.fwbz.dataInterface.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;
import org.jeecg.modules.fwbz.dataInterface.mapper.InterfaceInfoMapper;
import org.jeecg.modules.fwbz.dataInterface.service.IInterfaceInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 接口信息 Service 实现
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements IInterfaceInfoService {

    @Override
    public IPage<InterfaceInfo> listPage(InterfaceInfo params) {
        return page(
                new Page<>(params.getPageNo(), params.getPageSize()),
                new LambdaQueryWrapper<InterfaceInfo>()
                        .like(params.getSysName() != null, InterfaceInfo::getSysName, params.getSysName())
                        .like(params.getInterfacePath() != null, InterfaceInfo::getInterfacePath, params.getInterfacePath())
                        .eq(params.getProtocolTypeId() != null, InterfaceInfo::getProtocolTypeId, params.getProtocolTypeId())
                        .eq(params.getState() != null, InterfaceInfo::getState, params.getState())
                        .orderByDesc(InterfaceInfo::getCreateTime)
        );
    }

    @Override
    public void enable(Long id) {
        update(new LambdaUpdateWrapper<InterfaceInfo>()
                .set(InterfaceInfo::getState, InterfaceInfo.STATE_ENABLE)
                .eq(InterfaceInfo::getId, id));
    }

    @Override
    public void disable(Long id) {
        update(new LambdaUpdateWrapper<InterfaceInfo>()
                .set(InterfaceInfo::getState, InterfaceInfo.STATE_DISABLE)
                .eq(InterfaceInfo::getId, id));
    }

    @Override
    public boolean save(InterfaceInfo entity) {
        entity.setId(null);
        check(entity);
        entity.setState(Integer.valueOf(InterfaceInfo.STATE_ENABLE));
        return super.save(entity);
    }

    @Override
    public boolean updateById(InterfaceInfo entity) {
        check(entity);
        entity.setState(null);
        return super.updateById(entity);
    }

    @Override
    public List<InterfaceInfo> list() {
        return list(new LambdaQueryWrapper<InterfaceInfo>()
                .eq(InterfaceInfo::getState, InterfaceInfo.STATE_ENABLE)
                .orderByDesc(InterfaceInfo::getCreateTime));
    }

    /**
     * 业务校验
     */
    private void check(InterfaceInfo entity) {
        checkStatus(entity.getId());
        if (count(new LambdaQueryWrapper<InterfaceInfo>()
                .eq(InterfaceInfo::getInterfacePath, entity.getInterfacePath())
                .ne(entity.getId() != null, InterfaceInfo::getId, entity.getId())) > 0) {
            throw new JeecgBootException("已存在相同接口地址");
        }
    }

    /**
     * 状态校验：已启用的记录不允许编辑/删除
     */
    private void checkStatus(Long id) {
        if (id == null) {
            return;
        }
        InterfaceInfo byId = getById(id);
        if (byId == null) {
            throw new JeecgBootException("接口信息不存在");
        }
        if (InterfaceInfo.STATE_ENABLE.equals(String.valueOf(byId.getState()))) {
            throw new JeecgBootException("该记录已启用，禁止操作");
        }
    }
}
