package org.jeecg.modules.fwbz.parking.vo;

import lombok.Data;

/**
 * 车辆类型下拉列表VO
 */
@Data
public class ParkTypeVo {

    /**
     * 车辆类型，如进场、出场
     */
    private String parkType;
}
