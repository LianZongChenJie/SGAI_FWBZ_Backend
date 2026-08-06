package org.jeecg.modules.fwbz.venueVisitorFlow.controller;

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
 * 各场馆客流分时趋势图表 / 热力图 控制器
 * <p>
 * 数据来源：table_venue_flow_hour（各场馆客流分时统计表）
 * </p>
 *
 * @author fwbz
 */
@RestController
@RequestMapping("/fwbz/venueVisitorFlow/hourly")
public class VenueFlowHourController {

    private final IVenueFlowHourService venueFlowHourService;

    public VenueFlowHourController(IVenueFlowHourService venueFlowHourService) {
        this.venueFlowHourService = venueFlowHourService;
    }

    /**
     * 获取今日/本周/本月 各场馆客流趋势图数据。
     * <ul>
     *   <li><b>periodType=0</b>（默认）本日：返回按时分组的各场馆在场人数序列。</li>
     *   <li><b>periodType=1</b> 本周：返回周一到周日每天各场馆最新在场的日度趋势。</li>
     *   <li><b>periodType=2</b> 本月：返回1日到月末每天各场馆最新在场的日度趋势。</li>
     * </ul>
     * <p>
     * date 参数可选，不传默认今天（确定属于哪一天/哪一周/哪一月）。
     * </p>
     *
     * @param periodType 统计周期: 0-本日, 1-本周, 2-本月
     * @param date       参考日期（yyyy-MM-dd），可选，默认今天
     * @return 趋势图数据，包含横轴标签、各场馆序列、合计序列及汇总值
     */
    @GetMapping("/todayTrend")
    public Result<Map<String, Object>> todayTrend(
            @RequestParam(defaultValue = "0") Integer periodType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        VenueHourlyTrendVO vo;
        switch (periodType) {
            case 1:
                vo = venueFlowHourService.queryWeeklyTrend(date);
                break;
            case 2:
                vo = venueFlowHourService.queryMonthlyTrend(date);
                break;
            default:
                vo = venueFlowHourService.queryHourlyTrend(date);
                break;
        }

        return Result.ok(vo.toChartMap());
    }

    /**
     * 获取各场馆热力图数据。
     * <p>
     * 取每个场馆今日最后一条分时记录，联动 table_venue_info 获取名称和经纬度。
     * </p>
     *
     * @return 热力图数据列表，包含 id/name/lng/lat/used/total 等
     */
    @GetMapping("/heatmap")
    public Result<List<VenueHeatmapItemVO>> heatmap() {
        return Result.ok(venueFlowHourService.queryHeatmap());
    }
}
