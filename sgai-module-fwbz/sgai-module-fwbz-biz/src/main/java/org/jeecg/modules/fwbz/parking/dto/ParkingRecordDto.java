package org.jeecg.modules.fwbz.parking.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.parking.entity.ParkingRecord;

/**
 * 停车记录查询DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ParkingRecordDto extends ParkingRecord {
}
