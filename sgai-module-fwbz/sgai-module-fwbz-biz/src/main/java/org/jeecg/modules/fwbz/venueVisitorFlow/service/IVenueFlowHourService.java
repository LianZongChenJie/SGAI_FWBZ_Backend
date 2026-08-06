package org.jeecg.modules.fwbz.venueVisitorFlow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.venueVisitorFlow.entity.VenueFlowHour;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueHeatmapItemVO;
import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueHourlyTrendVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 各场馆客流分时统计 Service 接口
 *
 * @author fwbz
 */
public interface IVenueFlowHourService extends IService<VenueFlowHour> {

    /**
     * 查询指定日期各场馆分时客流趋势（用于图表展示）。
     * <p>返回横轴时间标签、每个场馆在场人数序列、合计序列及今日进出汇总。</p>
     *
     * @param date 数据日期，为空默认今天
     * @return 分时趋势数据
     */
    VenueHourlyTrendVO queryHourlyTrend(LocalDate date);

    /**
     * 查询各场馆今日最新在场人数（用于热力图展示）。
     * <p>取每个场馆今天最后一条分时记录，联动 table_venue_info 获取名称和经纬度。</p>
     *
     * @return 热力图数据列表
     */
    List<VenueHeatmapItemVO> queryHeatmap();
}
