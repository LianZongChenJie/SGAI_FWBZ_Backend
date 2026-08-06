package org.jeecg.modules.fwbz.venueVisitorFlow.vo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 各场馆今日分时客流趋势数据
 * <p>用于折线图/柱状图展示，每个场馆一个序列，横轴为 00:00~23:00 整点。</p>
 *
 * @author fwbz
 */
@Data
public class VenueHourlyTrendVO {

    /** 横轴时间标签，如 ["00:00", "01:00", ...] */
    private List<String> date;

    /** 各场馆在场人数序列 key=场馆名称, value=24小时数据 */
    private Map<String, List<Long>> venueData;

    /** 每个时间点的总在场人数 */
    private List<Long> total;

    /** 今日累计进场总人数 */
    private Long todayInTotal;

    /** 今日进出总人数（进场 + 在场） */
    private Long todayInOutTotal;

    /**
     * 将 VO 展平为图表需要的 Map 结构
     * <p>key 包含 date、total、todayInTotal 以及每个场馆名称</p>
     */
    public Map<String, Object> toChartMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);
        if (venueData != null) {
            result.putAll(venueData);
        }
        result.put("total", total);
        result.put("todayInTotal", todayInTotal);
        result.put("todayInOutTotal", todayInOutTotal);
        return result;
    }
}
