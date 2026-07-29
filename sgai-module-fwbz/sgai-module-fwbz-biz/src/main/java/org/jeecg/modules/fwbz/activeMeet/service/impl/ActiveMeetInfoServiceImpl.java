package org.jeecg.modules.fwbz.activeMeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetInfoMapper;
import org.jeecg.modules.fwbz.activeMeet.service.IActiveMeetInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActiveMeetInfoServiceImpl extends ServiceImpl<ActiveMeetInfoMapper, ActiveMeetInfo> implements IActiveMeetInfoService {

    @Override
    public IPage<ActiveMeetInfo> listPage(ActiveMeetInfo params) {
        return page(
                new Page<ActiveMeetInfo>(params.getPageNo(), params.getPageSize()),
                new LambdaQueryWrapper<ActiveMeetInfo>()
                        .like(params.getActiveName() != null, ActiveMeetInfo::getActiveName, params.getActiveName())
                        .eq(params.getVenueId() != null, ActiveMeetInfo::getVenueId, params.getVenueId())
                        .orderByDesc(ActiveMeetInfo::getStartDate)
                        .orderByAsc(ActiveMeetInfo::getStartTime)
        );
    }

    @Override
    public List<ActiveMeetInfo> listAll() {
        return list(new LambdaQueryWrapper<ActiveMeetInfo>()
                .orderByDesc(ActiveMeetInfo::getStartDate)
                .orderByAsc(ActiveMeetInfo::getStartTime));
    }

    @Override
    public boolean save(ActiveMeetInfo entity) {
        entity.setId(null);
        check(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(ActiveMeetInfo entity) {
        check(entity);
        return super.updateById(entity);
    }

    private void check(ActiveMeetInfo entity) {
        if (entity.getId() != null) {
            ActiveMeetInfo byId = getById(entity.getId());
            if (byId == null) {
                throw new JeecgBootException("活动信息不存在");
            }
        }
        if (count(new LambdaQueryWrapper<ActiveMeetInfo>()
                .eq(ActiveMeetInfo::getActiveName, entity.getActiveName())
                .ne(entity.getId() != null, ActiveMeetInfo::getId, entity.getId())) > 0) {
            throw new JeecgBootException("已存在相同名称的活动");
        }
        checkTimeConflict(entity);
    }

    /**
     * 校验时间冲突：同一场馆同一天，新增活动的开始时间不能落在已有活动的开始时间~结束时间之间
     */
    private void checkTimeConflict(ActiveMeetInfo entity) {
        if (entity.getVenueId() == null || entity.getStartDate() == null || entity.getStartTime() == null) {
            return;
        }
        LambdaQueryWrapper<ActiveMeetInfo> wrapper = new LambdaQueryWrapper<ActiveMeetInfo>()
                .eq(ActiveMeetInfo::getVenueId, entity.getVenueId())
                .eq(ActiveMeetInfo::getStartDate, entity.getStartDate());
        if (entity.getId() != null) {
            wrapper.ne(ActiveMeetInfo::getId, entity.getId());
        }
        List<ActiveMeetInfo> existingList = list(wrapper);
        for (ActiveMeetInfo existing : existingList) {
            if (existing.getStartTime() != null && existing.getEndTime() != null) {
                // 新活动的开始时间在已有活动的 [startTime, endTime) 之间
                if (!entity.getStartTime().before(existing.getStartTime())
                        && entity.getStartTime().before(existing.getEndTime())) {
                    throw new JeecgBootException("时间冲突：该场馆当天已存在时间重叠的活动");
                }
            }
        }
    }
}
