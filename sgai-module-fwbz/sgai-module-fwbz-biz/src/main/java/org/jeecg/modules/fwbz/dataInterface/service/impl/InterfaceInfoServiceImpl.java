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
                .set(InterfaceInfo::getState, InterfaceInfo.STATE_OFFLINE)
                .eq(InterfaceInfo::getId, id));
    }

    @Override
    public void disable(Long id) {
        update(new LambdaUpdateWrapper<InterfaceInfo>()
                .set(InterfaceInfo::getState, InterfaceInfo.STATE_OFFLINE)
                .eq(InterfaceInfo::getId, id));
    }

    @Override
    public boolean save(InterfaceInfo entity) {
        entity.setId(null);
        check(entity);
        entity.setState(InterfaceInfo.STATE_OFFLINE);
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
                .ne(InterfaceInfo::getState, InterfaceInfo.STATE_OFFLINE)
                .orderByDesc(InterfaceInfo::getCreateTime));
    }

    @Override
    public List<InterfaceInfo> listAll() {
        return super.list();
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
     * 状态校验：在线/异常状态的记录不允许编辑/删除
     */
    private void checkStatus(Long id) {
        if (id == null) {
            return;
        }
        InterfaceInfo byId = getById(id);
        if (byId == null) {
            throw new JeecgBootException("接口信息不存在");
        }
        if (InterfaceInfo.STATE_ONLINE.equals(byId.getState())
                || InterfaceInfo.STATE_ABNORMAL.equals(byId.getState())) {
            throw new JeecgBootException("该接口正在监控中，请先停用后再操作");
        }
    }
}
