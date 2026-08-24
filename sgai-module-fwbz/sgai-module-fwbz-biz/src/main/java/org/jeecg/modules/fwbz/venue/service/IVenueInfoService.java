package org.jeecg.modules.fwbz.venue.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.venue.VenueInfo;

import java.util.List;

/**
 * @Description: 场馆基本信息
 * @Author: jeecg-boot
 * @Date:   2026-07-29
 * @Version: V1.0
 */
public interface IVenueInfoService extends IService<VenueInfo> {

    /**
     * 查询所有场馆（下拉列表）
     */
    List<VenueInfo> getAllVenueList();

}
