package org.jeecg.modules.fwbz.main.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dm.jdbc.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.fwbz.bc.entity.BuildingControlPoint;
import org.jeecg.modules.fwbz.bc.service.IBuildingControlPointSendHistoryService;
import org.jeecg.modules.fwbz.bc.service.IBuildingControlPointService;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.jeecg.modules.fwbz.mdm.service.IDeviceService;
import org.jeecg.modules.fwbz.mq.send.MqSendService;
import org.jeecg.modules.fwbz.main.dto.DeviceAttributeOperationDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备属性操作服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class DeviceAttributeOperationService {

    private final IDeviceService deviceService;

    private final IDeviceAttributeService deviceAttributeService;

    private final MqSendService mqSendService;
    private final IBuildingControlPointSendHistoryService buildingControlPointSendHistoryService;
    private final IBuildingControlPointService buildingControlPointService;
    private final RedisUtil redisUtil;

    /**
     * 设备属性操作
     * @param list deviceId,pointId,value
     */
    public void operationDeviceAttribute(Collection<DeviceAttributeOperationDto> list){
        if(CollectionUtil.isEmpty(list)){
            return;
        }
        // 获取设备信息
        Set<Long> deviceIds = list.stream().map(DeviceAttributeOperationDto::getDeviceId).collect(Collectors.toSet());
        Map<Long,Device> deviceMap = deviceService.listByIds(deviceIds)
                .stream()
                .collect(Collectors.toMap(Device::getId, Function.identity()));
        // 获取点位信息
        Set<Long> pointIds = list.stream().map(DeviceAttributeOperationDto::getPointId).collect(Collectors.toSet());
        Map<Long,DeviceAttribute> pointMap = deviceAttributeService.findByIds(pointIds)
                .stream()
                .collect(Collectors.toMap(DeviceAttribute::getId, Function.identity()));
        for (DeviceAttributeOperationDto item : list) {
            Device device = deviceMap.get(item.getDeviceId());
            DeviceAttribute deviceAttribute = pointMap.get(item.getPointId());
            if(device != null && deviceAttribute != null && StringUtil.isNotEmpty(item.getValue())){
                operationDeviceAttribute(device,deviceAttribute,item.getValue());
            }
        }
    }

    public void operationDeviceAttribute(Device device,DeviceAttribute attribute,String value){
        // TODO 设备属性操作
        // 楼控属性操作
        String acquisitionCoding = attribute.getAcquisitionCoding();
        if(StrUtil.isEmpty(acquisitionCoding)){
            return;
        }
        // 获取楼控点位信息
        String[] split = acquisitionCoding.split("-");
        if(split.length != 2){
            return;
        }
        String gatewayAdr = split[0];
        String bacnetAdr = split[1];
        //查询楼控信息
        BuildingControlPoint one = getByGatewayAdrAndBacnetAdr(gatewayAdr,bacnetAdr);
        //保存楼控发送历史
        buildingControlPointSendHistoryService.save(one.getId(),one.getValue(),one.getCollectionTime());

        // 发送消息
        mqSendService.sendBuildingControlOperation(gatewayAdr,bacnetAdr,value);
    }

    public void operationDeviceAttribute(Long attributeId,String value){
        DeviceAttribute attribute = deviceAttributeService.getById(attributeId);
        if(attribute == null){
            return;
        }
        String acquisitionCoding = attribute.getAcquisitionCoding();
        if(StrUtil.isEmpty(acquisitionCoding)){
            return;
        }
        // 获取楼控点位信息
        String[] split = acquisitionCoding.split("-");
        if(split.length != 2){
            return;
        }
        String gatewayAdr = split[0];
        String bacnetAdr = split[1];
        //查询楼控信息
        BuildingControlPoint one = getByGatewayAdrAndBacnetAdr(gatewayAdr,bacnetAdr);
        //保存楼控发送历史
        buildingControlPointSendHistoryService.save(one.getId(),one.getValue(),one.getCollectionTime());

        mqSendService.sendBuildingControlOperation(gatewayAdr, bacnetAdr,value);
    }


    private BuildingControlPoint getByGatewayAdrAndBacnetAdr(String gatewayAdr,String bacnetAdr){
        // 缓存
        Object o = redisUtil.get(getRedisKey(gatewayAdr, bacnetAdr));
        if(o != null){
            return (BuildingControlPoint) o;
        }
        return buildingControlPointService.getOne(new LambdaQueryWrapper<BuildingControlPoint>().eq(BuildingControlPoint::getGatewayAdr, gatewayAdr).eq(BuildingControlPoint::getBacnetAdr, bacnetAdr));
    }

    private String getRedisKey(String gatewayAdr,String bacnetAdr){
        return "fwbz:bc:"+gatewayAdr+"-"+bacnetAdr;
    }




}
