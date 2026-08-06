package org.jeecg.modules.fwbz.venueVisitorFlow.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueFlowHourService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueHeatmapItemVO;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueHourlyTrendVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 各场馆分时客流趋势接口
 *
 * @author fwbz
 */
@Api(tags = "场馆分时客流趋势")
@RestController
@RequestMapping("/fwbz/venueVisitorFlow/hourly")
@AllArgsConstructor
public class VenueFlowHourController {

    private final IVenueFlowHourService venueFlowHourService;

    /**
     * 获取今日（或指定日期）各场馆客流分时趋势图数据。
     * <p>返回横轴时间标签、每个场馆在场人数序列、合计序列及今日进出汇总。</p>
     */
    @GetMapping("/todayTrend")
    @ApiOperation(value = "今日场馆客流分时趋势", notes = "用于各场馆客流趋势折线图，返回24小时在场人数序列")
    public Result<Map<String, Object>> todayTrend(
            @ApiParam(value = "日期，默认今天")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        VenueHourlyTrendVO vo = venueFlowHourService.queryHourlyTrend(date);
        return Result.ok(vo.toChartMap());
    }

    /**
     * 获取各场馆热力图数据。
     * <p>取每个场馆今天最新一条的 today_now_count 作为在场人数，联动 table_venue_info 获取名称和经纬度。</p>
     */
    @GetMapping("/heatmap")
    @ApiOperation(value = "各场馆热力图数据", notes = "用于热力图展示，返回各场馆今日在场人数及经纬度")
    public Result<List<VenueHeatmapItemVO>> heatmap() {
        return Result.ok(venueFlowHourService.queryHeatmap());
    }
}
