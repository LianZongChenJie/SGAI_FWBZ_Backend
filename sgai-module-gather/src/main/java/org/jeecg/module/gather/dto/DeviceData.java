package org.jeecg.module.gather.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DeviceData {
    private String deviceId;
    private List<PointData> functions;

    public BigDecimal getPointValue() {
        return this.getFunctions().get(0).getValue();
    }
}
