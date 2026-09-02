package org.jeecg.modules.fwbz.exhibitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.exhibitor.ExhibitorInfo;
import org.jeecg.modules.fwbz.exhibitor.mapper.ExhibitorInfoMapper;
import org.jeecg.modules.fwbz.exhibitor.service.IExhibitorInfoService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 参展厂商信息
 * @Author: jeecg-boot
 * @Date:   2026-09-02
 * @Version: V1.0
 */
@Service
public class ExhibitorInfoServiceImpl extends ServiceImpl<ExhibitorInfoMapper, ExhibitorInfo> implements IExhibitorInfoService {

    @Override
    public List<ExhibitorInfo> getListByVenueId(Long venueId) {
        return list(new LambdaQueryWrapper<ExhibitorInfo>()
                .eq(ExhibitorInfo::getVenueId, venueId));
    }

    @Override
    public Long countByVenueId(Long venueId) {
        return count(new LambdaQueryWrapper<ExhibitorInfo>()
                .eq(ExhibitorInfo::getVenueId, venueId));
    }

    @Override
    public Map<Long, Long> countGroupByVenueId(List<Long> venueIds) {
        if (venueIds == null || venueIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return list(new LambdaQueryWrapper<ExhibitorInfo>()
                .select(ExhibitorInfo::getVenueId)
                .in(ExhibitorInfo::getVenueId, venueIds))
                .stream()
                .collect(Collectors.groupingBy(ExhibitorInfo::getVenueId, Collectors.counting()));
    }
}
