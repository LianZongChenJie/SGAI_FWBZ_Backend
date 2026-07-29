package org.jeecg.modules.fwbz.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.energyAnalysis.service.IMeteringPointDataService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class MeteringPointDataJob {

    private final IMeteringPointDataService service;

    @Scheduled(cron = "0 15 * * * ?")
    public void calculationMeteringPointData(){
        // TODO，应该由设备计量数据更新来触发计算
//        service.calculateValue(LocalDateTime.now());
    }

}
