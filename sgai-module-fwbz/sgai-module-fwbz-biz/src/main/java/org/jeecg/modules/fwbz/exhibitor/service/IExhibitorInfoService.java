package org.jeecg.modules.fwbz.exhibitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.exhibitor.ExhibitorInfo;

import java.util.List;
import java.util.Map;

/**
 * @Description: 参展厂商信息
 * @Author: jeecg-boot
 * @Date:   2026-09-02
 * @Version: V1.0
 */
public interface IExhibitorInfoService extends IService<ExhibitorInfo> {

    /**
     * 根据场馆id查询参展厂商列表
     *
     * @param venueId 场馆id
     */
    List<ExhibitorInfo> getListByVenueId(Long venueId);

    /**
     * 根据场馆id统计参展厂商数量
     *
     * @param venueId 场馆id
     * @return 厂商数量
     */
    Long countByVenueId(Long venueId);

    /**
     * 根据场馆id列表批量统计参展厂商数量
     *
     * @param venueIds 场馆id列表
     * @return 场馆id -> 厂商数量
     */
    Map<Long, Long> countGroupByVenueId(List<Long> venueIds);

    /**
     * 根据展会总结报告id查询参展厂商列表
     * <p>
     * 展会总结报告id -> 报告活动名称 -> 该活动名称下所有会展活动 -> 所有活动所在场馆id -> 参展厂商列表
     *
     * @param reportId 展会总结报告id
     * @return 参展厂商列表
     */
    List<ExhibitorInfo> getListByReportId(Long reportId);

}
