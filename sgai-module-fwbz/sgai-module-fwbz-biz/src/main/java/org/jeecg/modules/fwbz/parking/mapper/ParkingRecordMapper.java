package org.jeecg.modules.fwbz.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.fwbz.parking.entity.ParkingRecord;
import org.jeecg.modules.fwbz.parking.vo.ParkingLotVo;
import org.jeecg.modules.fwbz.parking.vo.ParkTypeVo;

import java.util.List;

/**
 * 停车记录Mapper
 */
public interface ParkingRecordMapper extends BaseMapper<ParkingRecord> {

    /**
     * 查询停车场下拉列表（去重）
     */
    List<ParkingLotVo> selectParkingLotList();

    /**
     * 查询车辆类型下拉列表（去重）
     */
    List<ParkTypeVo> selectParkTypeList();
}
