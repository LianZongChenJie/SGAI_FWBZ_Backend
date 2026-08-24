package org.jeecg.modules.fwbz.venue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.mapper.VenueInfoMapper;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 场馆基本信息
 * @Author: jeecg-boot
 * @Date:   2026-07-29
 * @Version: V1.0
 */
@Service
public class VenueInfoServiceImpl extends ServiceImpl<VenueInfoMapper, VenueInfo> implements IVenueInfoService {

    @Override
    public boolean save(VenueInfo entity) {
        // 校验场馆名称是否存在
        if (count(new LambdaQueryWrapper<VenueInfo>().eq(VenueInfo::getVenueName, entity.getVenueName())) > 0) {
            throw new JeecgBootException("场馆名称重复！");
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(VenueInfo entity) {
        // 校验场馆名称是否存在
        if (count(new LambdaQueryWrapper<VenueInfo>()
                .eq(VenueInfo::getVenueName, entity.getVenueName())
                .ne(VenueInfo::getId, entity.getId())) > 0) {
            throw new JeecgBootException("场馆名称重复！");
        }
        return super.updateById(entity);
    }

    @Override
    public List<VenueInfo> getAllVenueList() {
        return list();
    }
}
