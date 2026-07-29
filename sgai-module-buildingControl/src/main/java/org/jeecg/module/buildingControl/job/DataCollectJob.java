package org.jeecg.module.buildingControl.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.module.buildingControl.mq.MqSendService;
import org.jeecg.module.buildingControl.service.DeviceAttributeService;
import org.jeecg.module.buildingControl.service.EnteliWebService;
import org.jeecg.module.buildingControl.util.BacnetPropertyResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@AllArgsConstructor
public class DataCollectJob {

    private final MqSendService mqSendService;

    private final EnteliWebService enteliWebService;

    private final DeviceAttributeService deviceAttributeService;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5, 10, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Scheduled(fixedRate = 10000)
    public void collect() {
        log.info("数据采集任务开始");
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();
        List<String> acquisitionCoding = deviceAttributeService.getAcquisitionCoding();
        log.info("acquisitionCoding:{}", acquisitionCoding);
        Map<String, List<String>> groupedByGateway = acquisitionCoding.stream()
                .filter(path -> path.split("/").length > 4)
                .collect(Collectors.groupingBy(path -> path.split("/")[4]));
        List<CompletableFuture<Void>> futures = groupedByGateway.values().stream()
                .map(paths -> CompletableFuture.runAsync(() -> {
                    for (String path : paths) {
                        try {
                            BacnetPropertyResult result = enteliWebService.getPropertyWithType(path);
                            mqSendService.sendMsg(path, result.getValue(), LocalDateTime.now());
                            success.incrementAndGet();
                        } catch (Exception e) {
                            log.error("采集失败 - path:{}, error:{}", path, e.getMessage());
                            fail.incrementAndGet();
                        }
                    }
                }, executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("数据采集任务完成, 总数:{}, 成功:{}, 失败:{}", acquisitionCoding.size(), success.get(), fail.get());
    }
}
