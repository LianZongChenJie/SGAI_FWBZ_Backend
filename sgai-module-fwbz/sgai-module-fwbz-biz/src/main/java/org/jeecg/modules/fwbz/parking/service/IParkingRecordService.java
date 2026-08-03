package org.jeecg.modules.fwbz.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.parking.dto.ParkingRecordDto;
import org.jeecg.modules.fwbz.parking.entity.ParkingRecord;
import org.jeecg.modules.fwbz.parking.vo.ParkingLotVo;
import org.jeecg.modules.fwbz.parking.vo.ParkTypeVo;

import java.util.List;

/**
 * 停车记录Service接口
 */
public interface IParkingRecordService extends IService<ParkingRecord> {

    /**
     * 分页查询停车记录（可按车牌号、停车场、车辆类型检索）
     */
    IPage<ParkingRecord> listPage(ParkingRecordDto params);

    /**
     * 查询停车场下拉列表
     */
    List<ParkingLotVo> getParkingLotList();

    /**
     * 查询车辆类型下拉列表
     */
    List<ParkTypeVo> getParkTypeList();
}
