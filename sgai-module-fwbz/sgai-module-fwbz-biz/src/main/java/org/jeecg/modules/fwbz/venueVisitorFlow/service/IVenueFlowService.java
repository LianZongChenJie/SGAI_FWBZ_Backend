package org.jeecg.modules.fwbz.venueVisitorFlow.service;

import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VenueFlowVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 各场馆客流统计 Service 接口
 * <p>
 * 业务逻辑：遍历 table_venue_info 中的每个场馆，分别调用海康 OpenAPI
 * （今日进场 / 当前在场 / 峰值人数 / 峰值时间 / 平均停留），按 (dataDate, venueId) 唯一键
 * 写入 table_venue_flow 表（一天一行/馆）。前端表格从数据库读取后展示，含较昨日对比。
 * </p>
 *
 * @author fwbz
 */
public interface IVenueFlowService {

    /**
     * 同步所有场馆的客流数据到数据库。
     * <p>遍历所有场馆调用海康API（今日进场/当前在场/峰值/平均停留），按 (dataDate, venueId) 唯一键写库。
     * 单个场馆同步失败不影响其他场馆（容错）。</p>
     *
     * @return 本次成功同步的场馆数
     */
    int syncAllVenueFlowFromHikvision();

    /**
     * 手动同步指定场馆的客流数据。
     *
     * @param venueId 场馆id
     * @return 是否同步成功
     */
    boolean syncOneVenueFlowFromHikvision(Long venueId);

    /**
     * 查询所有场馆今日客流列表（前端表格展示用，含较昨日对比）。
     *
     * @return 各场馆客流 VO 列表
     */
    List<VenueFlowVO> queryVenueFlowList();

    /**
     * 查询指定日期所有场馆的客流列表。
     *
     * @param date 日期（为空则取今天）
     * @return 各场馆客流 VO 列表
     */
    List<VenueFlowVO> queryVenueFlowListByDate(LocalDate date);
}