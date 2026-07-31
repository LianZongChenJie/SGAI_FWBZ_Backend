package org.jeecg.modules.fwbz.patterned.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.patterned.entity.PatterningPoint;

import java.util.List;

public interface IPatterningPointService extends IService<PatterningPoint> {

    void removeByPatterningStrategyId(Long patterningStrategyId);

    void save(Long patterningStrategyId, List<PatterningPoint> patterningPointList);

    List<PatterningPoint> findByPatterningStrategyId(Long patterningStrategyId);
}
