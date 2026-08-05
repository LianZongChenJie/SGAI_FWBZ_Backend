package org.jeecg.modules.fwbz.venueVisitorFlow.service;

import org.jeecg.modules.fwbz.venueVisitorFlow.vo.VisitorFlowCardVO;

import java.util.List;

/**
 * 场馆客流统计 Service 接口
 * <p>
 * 业务逻辑：调用 HTTP API（今日总客流/当前在场/峰值客流/平均停留）
 * → 数据入库（一天一行）→ 从数据库读取后构建卡片返回前端，含较昨日对比。
 * </p>
 *
 * @author fwbz
 */
public interface IVenueVisitorFlowService {

    /**
     * 同步客流数据到数据库（今日记录存在则更新，不存在则新增）。
     * 单个 API 任意一项失败不会影响其他项的同步（容错）。
     * <p>由定时任务 VenueVisitorFlowJob 每5分钟自动调用，也可通过 /syncFromApi 接口手动触发。</p>
     *
     * @return 本次同步成功的项数
     */
    int syncFromApi();

    /**
     * 从数据库读取今日总客流（不含同步，仅读库，含较昨日对比）
     */
    VisitorFlowCardVO queryTodayVisitorCount();

    /**
     * 从数据库读取当前在场（不含同步，仅读库，含较昨日对比）
     */
    VisitorFlowCardVO queryCurrentVisitorCount();

    /**
     * 从数据库读取峰值客流（不含同步，仅读库，含较昨日对比）
     */
    VisitorFlowCardVO queryPeakVisitorCount();

    /**
     * 从数据库读取平均停留（不含同步，仅读库，含较昨日对比）
     */
    VisitorFlowCardVO queryAverageStopDuration();

    /**
     * 从数据库读取全部四张卡片（不含同步，仅读库）。
     */
    List<VisitorFlowCardVO> querySummary();
}
