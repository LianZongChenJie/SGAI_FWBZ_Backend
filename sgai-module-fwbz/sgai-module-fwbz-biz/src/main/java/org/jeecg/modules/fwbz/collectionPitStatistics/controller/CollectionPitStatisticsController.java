package org.jeecg.modules.fwbz.collectionPitStatistics.controller;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.collectionPitStatistics.dto.CollectionPitStatisticsDto;
import org.jeecg.modules.fwbz.collectionPitStatistics.service.ICollectionPitStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集水坑统计
 */
@RestController
@RequestMapping("/fwbz/collectionPitStatistics")
@AllArgsConstructor
@Api(tags="集水坑统计")
@Slf4j
public class CollectionPitStatisticsController {

    private final ICollectionPitStatisticsService service;

    /**
     * 集水坑统计（液位告警设备数、故障设备总数）
     * @return
     */
    @GetMapping("/statistics")
    public Result<CollectionPitStatisticsDto> statistics() {
        return Result.ok(service.statistics());
    }

}
