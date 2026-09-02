package org.jeecg.modules.fwbz.venueVisitorFlow.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.venueVisitorFlow.service.IVenueFlowHourService;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.AreaHeatDataItemVO;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.AreaHeatResponseVO;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueHeatmapItemVO;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueHourlyTrendVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
@Slf4j
@RestController
@RequestMapping("/fwbz/venueVisitorFlow/hourly")
public class VenueFlowHourController {

    /** 区域热力图外部API地址 */
    private static final String AREA_HEAT_API_URL = "http://10.168.47.26:9999/sgai-api/openApi/tengxun/getAreaHeat";

    /** 外部API认证头 */
    private static final String APP_KEY = "R6VOSNoijW3o4WA5eFjW5l2bO";
    private static final String APP_SECRET = "GDg18aNuWaKsIX33euL0maXbSVqZSp";

    private final IVenueFlowHourService venueFlowHourService;
    private final RestTemplate restTemplate;

    public VenueFlowHourController(IVenueFlowHourService venueFlowHourService) {
        this.venueFlowHourService = venueFlowHourService;
        this.restTemplate = new RestTemplate();
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

    /**
     * 获取区域热力图数据（代理外部API）。
     * <p>
     * 调用腾讯区域热力API，根据areaId获取人员热力分布数据并直接返回。
     * </p>
     *
     * @param areaId 区域ID
     * @return 热力图响应，包含 maxweight 和 peopleHeatmapDataList
     */
    @GetMapping("/areaHeat")
    public Result<AreaHeatResponseVO> areaHeat(@RequestParam String areaId) {
        log.info("请求区域热力图数据, areaId={}", areaId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("appKey", APP_KEY);
            headers.set("appSecret", APP_SECRET);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = AREA_HEAT_API_URL + "?areaId=" + "4166450493835339248";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String body = response.getBody();
            log.debug("区域热力图API响应: {}", body);

            JSONObject json = JSON.parseObject(body);
            JSONObject resultJson = json.getJSONObject("result");
            if (resultJson == null) {
                log.error("区域热力图API返回异常: {}", body);
                return Result.error("获取区域热力图数据失败");
            }

            AreaHeatResponseVO vo = new AreaHeatResponseVO();
            vo.setMaxweight(resultJson.getInteger("maxweight"));

            JSONArray dataList = resultJson.getJSONArray("peopleHeatmapDataList");
            if (dataList != null) {
                List<AreaHeatDataItemVO> items = dataList.toJavaList(AreaHeatDataItemVO.class);
                vo.setPeopleHeatmapDataList(items);
            }

            return Result.ok(vo);
        } catch (Exception e) {
            log.error("请求区域热力图API异常", e);
            return Result.error("请求区域热力图服务异常: " + e.getMessage());
        }
    }
}
