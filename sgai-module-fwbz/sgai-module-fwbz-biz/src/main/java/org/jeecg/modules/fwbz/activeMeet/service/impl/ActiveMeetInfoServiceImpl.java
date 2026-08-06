package org.jeecg.modules.fwbz.activeMeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetPreparationInfo;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetsDeviceType;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetInfoMapper;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetPreparationInfoMapper;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetsDeviceTypeMapper;
import org.jeecg.modules.fwbz.activeMeet.service.IActiveMeetInfoService;
import org.jeecg.modules.fwbz.activeMeet.vo.WeekActivityVO;
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActiveMeetInfoServiceImpl extends ServiceImpl<ActiveMeetInfoMapper, ActiveMeetInfo> implements IActiveMeetInfoService {

    private final IVenueInfoService venueInfoService;
    private final ActiveMeetsDeviceTypeMapper activeMeetsDeviceTypeMapper;
    private final ActiveMeetPreparationInfoMapper activeMeetPreparationInfoMapper;

    public ActiveMeetInfoServiceImpl(IVenueInfoService venueInfoService,
                                     ActiveMeetsDeviceTypeMapper activeMeetsDeviceTypeMapper,
                                     ActiveMeetPreparationInfoMapper activeMeetPreparationInfoMapper) {
        this.venueInfoService = venueInfoService;
        this.activeMeetsDeviceTypeMapper = activeMeetsDeviceTypeMapper;
        this.activeMeetPreparationInfoMapper = activeMeetPreparationInfoMapper;
    }

    @Override
    public IPage<ActiveMeetInfo> listPage(ActiveMeetInfo params) {
        LambdaQueryWrapper<ActiveMeetInfo> wrapper = new LambdaQueryWrapper<ActiveMeetInfo>()
                .like(params.getActiveName() != null, ActiveMeetInfo::getActiveName, params.getActiveName())
                .eq(params.getVenueId() != null, ActiveMeetInfo::getVenueId, params.getVenueId());

        // 日期范围过滤：都为空查全部；startDate有值→开始日期之后；endDate有值→开始日期之前；都有→之间
        boolean hasStart = params.getStartDate() != null;
        boolean hasEnd = params.getEndDate() != null;
        if (hasStart && hasEnd) {
            wrapper.ge(ActiveMeetInfo::getStartDate, params.getStartDate())
                   .le(ActiveMeetInfo::getStartDate, params.getEndDate());
        } else if (hasStart) {
            wrapper.ge(ActiveMeetInfo::getStartDate, params.getStartDate());
        } else if (hasEnd) {
            wrapper.le(ActiveMeetInfo::getStartDate, params.getEndDate());
        }

        IPage<ActiveMeetInfo> result = page(
                new Page<ActiveMeetInfo>(params.getPageNo(), params.getPageSize()),
                wrapper.orderByAsc(ActiveMeetInfo::getStartDate)
                        .orderByAsc(ActiveMeetInfo::getStartTime)
        );
        fillVenueName(result.getRecords());
        return result;
    }

    @Override
    public List<ActiveMeetInfo> listAll() {
        List<ActiveMeetInfo> result = list(new LambdaQueryWrapper<ActiveMeetInfo>()
                .orderByDesc(ActiveMeetInfo::getStartDate)
                .orderByAsc(ActiveMeetInfo::getStartTime));
        fillVenueName(result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(ActiveMeetInfo entity) {
        entity.setId(null);
        check(entity);
        boolean result = super.save(entity);
        // 创建会议后，自动将所有设备类型插入会前筹备信息表
        initPreparationInfo(entity.getId());
        return result;
    }

    /**
     * 根据所有设备类型初始化会前筹备信息
     */
    private void initPreparationInfo(Long activeMeetId) {
        List<ActiveMeetsDeviceType> deviceTypes = activeMeetsDeviceTypeMapper.selectList(null);
        if (deviceTypes == null || deviceTypes.isEmpty()) {
            return;
        }
        List<ActiveMeetPreparationInfo> list = new ArrayList<>();
        for (ActiveMeetsDeviceType deviceType : deviceTypes) {
            ActiveMeetPreparationInfo info = new ActiveMeetPreparationInfo();
            info.setActiveMeetId(activeMeetId);
            info.setActiveMeetsDeviceTypeId(deviceType.getId());
            info.setStatus(0);
            list.add(info);
        }
        for (ActiveMeetPreparationInfo info : list) {
            activeMeetPreparationInfoMapper.insert(info);
        }
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        // 删除会议时同时删除关联的筹备信息
        activeMeetPreparationInfoMapper.delete(
                new LambdaQueryWrapper<ActiveMeetPreparationInfo>()
                        .eq(ActiveMeetPreparationInfo::getActiveMeetId, id));
        return super.removeById(id);
    }

    @Override
    public boolean updateById(ActiveMeetInfo entity) {
        check(entity);
        return super.updateById(entity);
    }

    private void check(ActiveMeetInfo entity) {
        checkTimeConflict(entity);
    }

    @Override
    public List<WeekActivityVO> listThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date weekEnd = cal.getTime();

        List<ActiveMeetInfo> all = list(new LambdaQueryWrapper<ActiveMeetInfo>()
                .ge(ActiveMeetInfo::getStartDate, weekStart)
                .le(ActiveMeetInfo::getStartDate, weekEnd)
                .orderByAsc(ActiveMeetInfo::getStartDate)
                .orderByAsc(ActiveMeetInfo::getStartTime));
        fillVenueName(all);

        Map<Date, List<ActiveMeetInfo>> grouped = all.stream()
                .collect(Collectors.groupingBy(ActiveMeetInfo::getStartDate,
                        () -> new TreeMap<>(),
                        Collectors.toList()));

        List<WeekActivityVO> result = new ArrayList<>();
        for (Map.Entry<Date, List<ActiveMeetInfo>> entry : grouped.entrySet()) {
            WeekActivityVO vo = new WeekActivityVO();
            vo.setDate(entry.getKey());
            vo.setList(entry.getValue());
            result.add(vo);
        }
        return result;
    }

    /**
     * 批量填充场馆名称
     */
    private void fillVenueName(List<ActiveMeetInfo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> venueIds = list.stream()
                .map(ActiveMeetInfo::getVenueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (venueIds.isEmpty()) {
            return;
        }
        Map<Long, String> venueNameMap = venueInfoService.listByIds(venueIds).stream()
                .collect(Collectors.toMap(VenueInfo::getId, VenueInfo::getVenueName));
        for (ActiveMeetInfo item : list) {
            item.setVenueName(venueNameMap.get(item.getVenueId()));
        }
    }

    /**
     * 校验时间冲突
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
