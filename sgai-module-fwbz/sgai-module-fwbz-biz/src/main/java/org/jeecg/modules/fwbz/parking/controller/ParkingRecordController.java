package org.jeecg.modules.fwbz.parking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.parking.dto.ParkingRecordDto;
import org.jeecg.modules.fwbz.parking.entity.ParkingRecord;
import org.jeecg.modules.fwbz.parking.service.IParkingRecordService;
import org.jeecg.modules.fwbz.parking.vo.ParkingLotVo;
import org.jeecg.modules.fwbz.parking.vo.ParkTypeVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 停车记录
 */
@RestController
@RequestMapping("/fwbz/parking/record")
@AllArgsConstructor
public class ParkingRecordController {

    private final IParkingRecordService service;

    /**
     * 停车记录分页查询（可按车牌号、停车场、车辆类型检索）
     */
    @GetMapping("/listPage")
    public Result<IPage<ParkingRecord>> listPage(ParkingRecordDto params) {
        return Result.ok(service.listPage(params));
    }

    /**
     * 停车场下拉列表
     */
    @GetMapping("/parkingLotList")
    public Result<List<ParkingLotVo>> parkingLotList() {
        return Result.ok(service.getParkingLotList());
    }

    /**
     * 车辆类型下拉列表
     */
    @GetMapping("/parkTypeList")
    public Result<List<ParkTypeVo>> parkTypeList() {
        return Result.ok(service.getParkTypeList());
    }
}
