package org.jeecg.modules.fwbz.bc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.fwbz.bc.dto.BuildingControlPointDto;
import org.jeecg.modules.fwbz.bc.entity.BuildingControlPointHistory;
import org.jeecg.modules.fwbz.bc.entity.BuildingControlPointSendHistory;
import org.jeecg.modules.fwbz.bc.mapper.BuildingControlPointHistoryMapper;
import org.jeecg.modules.fwbz.bc.mapper.BuildingControlPointSendHistoryMapper;
import org.jeecg.modules.fwbz.bc.service.IBuildingControlPointHistoryService;
import org.jeecg.modules.fwbz.bc.service.IBuildingControlPointSendHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BuildingControlPointSendHistoryServiceImpl extends ServiceImpl<BuildingControlPointSendHistoryMapper, BuildingControlPointSendHistory> implements IBuildingControlPointSendHistoryService {
    @Override
    public Page<BuildingControlPointSendHistory> listPage(BuildingControlPointDto params) {
        return super.page(
                new Page<>(params.getPageNo(), params.getPageSize()),
                new LambdaQueryWrapper<BuildingControlPointSendHistory>()
                        .eq(BuildingControlPointSendHistory::getPointId, params.getPointId())
                        .orderByDesc(BuildingControlPointSendHistory::getCollectionTime)
                );
    }

    @Override
    public void save(Long pointId, String value, LocalDateTime collectionTime) {
        BuildingControlPointSendHistory history = new BuildingControlPointSendHistory();
        history.setPointId(pointId);
        history.setValue(value);
        history.setCollectionTime(collectionTime);
        super.save(history);
    }
}
