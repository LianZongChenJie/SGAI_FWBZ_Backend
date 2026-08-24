package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.energyAnalysis.entity.EnergyPricingConfig;

public interface IEnergyPricingConfigService extends IService<EnergyPricingConfig> {
    boolean save(EnergyPricingConfig data);

    void add(EnergyPricingConfig data);

    void update(EnergyPricingConfig data);

    void enable(Long id);

    void disable(Long id);

    Page<EnergyPricingConfig> listPage(EnergyPricingConfig params);

    EnergyPricingConfig getByCategory(String category);

    EnergyPricingConfig getByCategoryId(Long categoryId);
}
