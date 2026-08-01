package org.jeecg.modules.fwbz.energyAnalysis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeasureRuleDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointDto;
import org.jeecg.modules.fwbz.energyAnalysis.dto.MeteringPointStatisticsDto;
import org.jeecg.modules.fwbz.energyAnalysis.entity.MeteringPoint;
import org.jeecg.modules.fwbz.energyAnalysis.vo.MeteringPointTreeVo;
import org.jeecg.modules.fwbz.energyAnalysis.vo.MeteringPointVo;
import org.jeecg.modules.fwbz.energyAnalysis.vo.PermissionMeteringPointTreeModel;
import org.jeecg.modules.fwbz.mdm.entity.Device;

import java.util.List;

public interface IEenergyMeteringService {

    IPage<Device> deviceMeterDataList(Device device);

}
