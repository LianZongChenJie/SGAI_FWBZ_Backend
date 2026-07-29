package org.jeecg.modules.fwbz.patterned.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.patterned.entity.RearPoint;

import java.util.Collection;
import java.util.List;

public interface IRearPointService extends IService<RearPoint> {

    void removeByLinkageStrategyId(Long linkageStrategyId);

    List<RearPoint> getListByLinkageStrategyId(Long linkageStrategyId);

    List<RearPoint> getListByLinkageStrategyIds(Collection<Long> linkageStrategyIds);
}
