package org.jeecg.modules.fwbz.buildingControl.service;

import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataResponse;

import java.util.List;

/**
 * 楼控系统实时数据写入服务接口
 */
public interface BuildingControlService {

    /**
     * 向外部系统批量写入实时数据
     *
     * @param items 前端传入的更新项
     * @return 外部系统响应
     */
    String updRealData(UpdRealDataItemDto items);
}
