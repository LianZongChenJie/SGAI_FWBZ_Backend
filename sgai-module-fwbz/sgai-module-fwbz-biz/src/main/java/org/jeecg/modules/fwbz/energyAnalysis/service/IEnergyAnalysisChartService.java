package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.energyAnalysis.entity.EnergyAnalysisChart;

import java.util.List;

public interface IEnergyAnalysisChartService extends IService<EnergyAnalysisChart> {
    void add(EnergyAnalysisChart data);
    void update(EnergyAnalysisChart data);
    void delete(Long id);
    List<EnergyAnalysisChart> list(EnergyAnalysisChart params);
}
