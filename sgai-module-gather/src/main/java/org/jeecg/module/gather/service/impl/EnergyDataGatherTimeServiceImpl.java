package org.jeecg.module.gather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.module.gather.entity.EnergyDataGatherTime;
import org.jeecg.module.gather.mapper.EnergyDataGatherTimeMapper;
import org.jeecg.module.gather.service.IEnergyDataGatherTimeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnergyDataGatherTimeServiceImpl extends ServiceImpl<EnergyDataGatherTimeMapper, EnergyDataGatherTime> implements IEnergyDataGatherTimeService {

    @Override
    public void saveGatherData(String deviceCode, LocalDateTime time, BigDecimal value) {
        // 查询数据是否存在
        EnergyDataGatherTime data = getById(deviceCode);
        if(data == null){
            data = new EnergyDataGatherTime();
            data.setDeviceCode(deviceCode);
        }
        data.setTime(time);
        data.setValue(value);
        super.saveOrUpdate(data);
    }

    @Override
    public List<EnergyDataGatherTime> findAll() {
        return super.list();
    }
}
