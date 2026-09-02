package org.jeecg.modules.fwbz.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.parking.dto.ParkingRecordDto;
import org.jeecg.modules.fwbz.parking.entity.ParkingRecord;
import org.jeecg.modules.fwbz.parking.mapper.ParkingRecordMapper;
import org.jeecg.modules.fwbz.parking.service.IParkingRecordService;
import org.jeecg.modules.fwbz.parking.vo.ParkingLotVo;
import org.jeecg.modules.fwbz.parking.vo.ParkTypeVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 停车记录Service实现
 */
@Service
@AllArgsConstructor
@Slf4j
public class ParkingRecordServiceImpl extends ServiceImpl<ParkingRecordMapper, ParkingRecord> implements IParkingRecordService {

    @Override
    public IPage<ParkingRecord> listPage(ParkingRecordDto params) {
        return page(
                new Page<>(params.getPageNo(), params.getPageSize()),
                getQueryWrapper(params)
                        .orderByDesc(ParkingRecord::getGmtCreate)
        );
    }

    @Override
    public List<ParkingLotVo> getParkingLotList() {
        return baseMapper.selectParkingLotList();
    }

    @Override
    public List<ParkTypeVo> getParkTypeList() {
        return baseMapper.selectParkTypeList();
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ParkingRecord> getQueryWrapper(ParkingRecordDto params) {
        return new LambdaQueryWrapper<ParkingRecord>()
                .like(StringUtils.isNotEmpty(params.getPlateNo()), ParkingRecord::getPlateNo, params.getPlateNo())
                .eq(StringUtils.isNotEmpty(params.getParkingLot()), ParkingRecord::getParkingLot, params.getParkingLot())
                .eq(StringUtils.isNotEmpty(params.getParkType()), ParkingRecord::getParkType, params.getParkType());
    }
}
