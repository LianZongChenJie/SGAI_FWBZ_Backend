package org.jeecg.modules.fwbz.bc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.bc.dto.BuildingControlPointDto;
import org.jeecg.modules.fwbz.bc.entity.BuildingControlPointHistory;
import org.jeecg.modules.fwbz.bc.entity.BuildingControlPointSendHistory;

import java.time.LocalDateTime;

public interface IBuildingControlPointSendHistoryService extends IService<BuildingControlPointSendHistory> {

    Page<BuildingControlPointSendHistory> listPage(BuildingControlPointDto params);

    void save(Long pointId, String value, LocalDateTime collectionTime);
}
