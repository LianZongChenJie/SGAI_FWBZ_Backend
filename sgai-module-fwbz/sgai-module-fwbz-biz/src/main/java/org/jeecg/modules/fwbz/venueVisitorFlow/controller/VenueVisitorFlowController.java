package org.jeecg.modules.fwbz.venueVisitorFlow.controller;

import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueVisitorFlowService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueFlowVO;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VisitorFlowCardVO;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;

/**
 * 场馆客流统计 Controller
 * <p>
 * 提供两类接口：
 * <ul>
 *     <li>整体四张卡片（今日总客流/当前在场/峰值客流/平均停留）</li>
 *     <li>各场馆客流统计表格</li>
 * </ul>
 * 数据由定时任务每5分钟从 HTTP API 同步到数据库，接口直接读库返回。
 * </p>
 *
 * @author fwbz
 */
@RestController
@RequestMapping("/fwbz/venueVisitorFlow")
@AllArgsConstructor
public class VenueVisitorFlowController {

    private final IVenueVisitorFlowService service;
    private final IVenueFlowService venueFlowService;

    /**
     * 今日总客流（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/todayVisitorCount")
    @AutoLog(value = "场馆客流-今日总客流")
    public Result<VisitorFlowCardVO> todayVisitorCount() {
        return Result.ok(service.queryTodayVisitorCount());
    }

    /**
     * 当前在场（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/currentVisitorCount")
    @AutoLog(value = "场馆客流-当前在场")
    public Result<VisitorFlowCardVO> currentVisitorCount() {
        return Result.ok(service.queryCurrentVisitorCount());
    }

    /**
     * 峰值客流（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/peakVisitorCount")
    @AutoLog(value = "场馆客流-峰值客流")
    public Result<VisitorFlowCardVO> peakVisitorCount() {
        return Result.ok(service.queryPeakVisitorCount());
    }

    /**
     * 平均停留（读库，数据由定时任务每5分钟同步）
     */
    @GetMapping("/averageStopDuration")
    @AutoLog(value = "场馆客流-平均停留")
    public Result<VisitorFlowCardVO> averageStopDuration() {
        return Result.ok(service.queryAverageStopDuration());
    }

    /**
     * 整体四张卡片汇总（读库，数据由定时任务每5分钟同步）。
     */
    @GetMapping("/summary")
    @AutoLog(value = "场馆客流-汇总")
    public Result<List<VisitorFlowCardVO>> summary() {
        return Result.ok(service.querySummary());
    }

    /**
     * 各场馆客流统计表格（读库，数据由定时任务每5分钟同步）。
     * <p>对应前端"各场馆客流统计"表格：场馆 / 今日进场 / 当前在场 / 峰值人数 / 峰值时间 / 平均停留 / 较昨日 / 状态</p>
     */
    @GetMapping("/venueList")
    @AutoLog(value = "各场馆客流统计")
    public Result<List<VenueFlowVO>> venueList(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) {
            return Result.ok(venueFlowService.queryToday());
        }
        return Result.ok(venueFlowService.queryByDate(date));
    }

    /**
     * 导出场馆客流统计（与"各场馆客流统计"表格数据一致）。
     * <p>导出 venueList 查询出的数据，场馆名称联动 table_venue_info 场馆信息表；
     * 状态为 IOC 同步入库的 state（如 宽松/适中/拥挤），较昨日为导出时按今日/昨日进场计算补全。</p>
     */
    @GetMapping("/export")
    @AutoLog(value = "各场馆客流统计-导出")
    public void exportVenueList(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletResponse response) throws Exception {
        List<VenueFlowVO> list = date == null
                ? venueFlowService.queryToday()
                : venueFlowService.queryByDate(date);
        // 仅补全较昨日增减率（查询结果已含状态等其余展示字段）
        for (VenueFlowVO vo : list) {
            vo.setCompareRate(compareRate(nvl(vo.getTodayInCount()), nvl(vo.getYesterdayInCount())));
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("各场馆客流统计.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("各场馆客流统计", "各场馆客流统计", ExcelType.XSSF),
                VenueFlowVO.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 今日较昨日增减率描述（如 ↑18.5%），与卡片模块展示格式保持一致。
     */
    private String compareRate(long today, long yesterday) {
        if (yesterday == 0) {
            return today == 0 ? "—" : "↑100%";
        }
        double rate = (today - yesterday) * 100.0 / yesterday;
        String arrow = rate >= 0 ? "↑" : "↓";
        double abs = Math.abs(rate);
        return arrow + (abs == (long) abs ? String.valueOf((long) abs) : String.format("%.1f", abs)) + "%";
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    /**
     * 手动触发各场馆客流同步（用于运维/调试）。
     */
    @GetMapping("/syncVenueFlow")
    @AutoLog(value = "各场馆客流-手动同步")
    public Result<Integer> syncVenueFlow() {
        return Result.ok(venueFlowService.syncAllVenueFlowFromApi());
    }
}