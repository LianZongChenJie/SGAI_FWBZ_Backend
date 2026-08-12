package org.jeecg.modules.fwbz.bc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.bc.entity.BuildingControlPoint;

import java.time.LocalDateTime;

public interface IBuildingControlPointService extends IService<BuildingControlPoint> {

    Page<BuildingControlPoint> listPage(BuildingControlPoint params);

    void save(String gatewayAdr, String bacnetAdr, String value,String remark, LocalDateTime collectionTime);


    BuildingControlPoint getByGatewayAdrAndBacnetAdr(String gatewayAdr,String bacnetAdr);
}
