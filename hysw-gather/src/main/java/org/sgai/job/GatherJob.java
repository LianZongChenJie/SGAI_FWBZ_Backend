package org.sgai.job;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sgai.dto.AttributeData;
import org.sgai.dto.DeviceAttributeData;
import org.sgai.mq.MqSendService;
import org.sgai.util.HyswService;
import org.sgai.util.WaterMeter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class GatherJob {

    private final MqSendService mqSendService;

    private final HyswService hyswService;

    @Scheduled(cron = "0 0 * * * ? ")
    public void gather() {
        List<WaterMeter> waterMeters = hyswService.waterList();
        for (WaterMeter meter : waterMeters) {
            if(StrUtil.isAllEmpty(meter.getMeterReading(),meter.getReportingTime())){
                log.info("{}数据为空",meter.getMeterCode());
                continue;
            }
            gatherAttributeData(meter);
            mqSendService.sendDeviceEnergyDataGather(meter.getMeterCode(), meter.getReportingTime(), meter.getMeterReading());
            mqSendService.sendDeviceLastGatherTime(meter.getMeterCode(), meter.getReportingTime());
        }
    }

    private void gatherAttributeData(WaterMeter meter){
        DeviceAttributeData data = new DeviceAttributeData();
        data.setEquipmentCode(meter.getMeterCode());
        data.setTimestamp(meter.getReportingTime());
        List<AttributeData> dataList = new ArrayList<>();
        dataList.add(new AttributeData("ColVoltage",new BigDecimal(meter.getBatteryVoltage())));
        data.setData(dataList);
        mqSendService.sendDeviceAttributeDataChange(data);
    }
}
