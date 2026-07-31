package org.jeecg.modules.fwbz.patterned.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.patterned.service.IPatterningExecutionTimeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 场景控制定时任务
 */
@Component
@AllArgsConstructor
@Slf4j
public class PatterningStrategyJob {

    private final IPatterningExecutionTimeService patterningExecutionTimeService;

    @Scheduled(cron = "0 0 22 * * ?")
    public void execute(){
        patterningExecutionTimeService.getNextExecution(LocalDate.now().plusDays(1));
    }

}
