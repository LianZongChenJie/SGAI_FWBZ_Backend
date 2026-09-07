package org.jeecg.modules.fwbz.buildingControl.service;

import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataResponse;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdateValueTypeResponseDto;

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

    /**
     * 更新设备属性数据类型：读取 device_attribute 中采集编码为数字的属性，
     * 逐点 realRead 获取返回的 dataType，回写 value_type。
     *
     * @return 更新统计结果（总数/成功/失败及失败明细）
     */
    UpdateValueTypeResponseDto updateAttributeValueType();
}
