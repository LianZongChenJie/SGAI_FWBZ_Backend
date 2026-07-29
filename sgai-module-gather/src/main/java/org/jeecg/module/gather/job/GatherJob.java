package org.jeecg.module.gather.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.module.gather.dto.DeviceCommStatus;
import org.jeecg.module.gather.dto.DeviceData;
import org.jeecg.module.gather.dto.PointData;
import org.jeecg.module.gather.entity.EnergyDataGatherTime;
import org.jeecg.module.gather.entity.FtlDevice;
import org.jeecg.module.gather.mq.MqService;
import org.jeecg.module.gather.service.*;
import org.jeecg.module.gather.service.impl.FtlLeiYouServiceImpl;
import org.jeecg.module.gather.service.impl.LeiYouServiceImpl;
import org.jeecg.modules.fwbz.entity.DeviceEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class GatherJob {

    private final DeviceService deviceService;

    private final LeiYouServiceImpl leiyouService;

    private final FtlLeiYouServiceImpl ftlLeiYouService;

    private final IEnergyDataGatherTimeService gatherTimeService;

    private final IFtlDeviceService ftlDeviceService;

    private final MqService mqService;

    /**
     * 设备通讯状态采集
     */
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void deviceStatusGather(){
        // 获取设备数据
        Set<String> deviceCodes = deviceService.findDevices()
                .stream()
                .map(DeviceEntity::getDeviceCode)
                .collect(Collectors.toSet());
        // 获取负碳楼设备编号
        Set<String> ftlDeviceCodes = ftlDeviceService.list()
                .stream()
                .map(FtlDevice::getDeviceCode)
                .collect(Collectors.toSet());

        deviceCodes.removeAll(ftlDeviceCodes);

        deviceStatusGather(ftlLeiYouService,ftlDeviceCodes);
        deviceStatusGather(leiyouService,deviceCodes);
    }

    private void deviceStatusGather(ILeiYouService service, Collection<String> deviceCodes){
        service.refreshToken();
        List<DeviceCommStatus> onlineData = service.getOnlineData(deviceCodes);
        onlineData.forEach(item -> {
            mqService.sendDeviceRunStatus(item.getDeviceId(),item.getCommStatus());
        });
    }

    /**
     * 设备能源数据采集
     */
    @Scheduled(cron = "0 4 * * * ? ")
    public void deviceDataGather(){
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        List<DeviceEntity> devices = deviceService.findDevices();
        Set<String> ftlDeviceCodes = ftlDeviceService.list()
                .stream()
                .map(FtlDevice::getDeviceCode)
                .collect(Collectors.toSet());

        // 获取上次采集时间
        Map<String,LocalDateTime> gatherTimeMap = gatherTimeService.findAll()
                .stream()
                .collect(Collectors.toMap(EnergyDataGatherTime::getDeviceCode, EnergyDataGatherTime::getTime));

        deviceDataGather(ftlLeiYouService,now,devices.stream().filter(device -> ftlDeviceCodes.contains(device.getDeviceCode())).collect(Collectors.toSet()), gatherTimeMap);
        deviceDataGather(leiyouService,now,devices.stream().filter(device -> !ftlDeviceCodes.contains(device.getDeviceCode())).collect(Collectors.toSet()), gatherTimeMap);
    }

    private void deviceDataGather(ILeiYouService service,LocalDateTime time,Collection<DeviceEntity> devices,Map<String,LocalDateTime> gatherTimeMap){
        service.refreshToken();
        List<String> nowDevices = new ArrayList<>();
        List<DeviceEntity> hisDevices = new ArrayList<>();
        for (DeviceEntity device : devices) {

            LocalDateTime lastGatherTime = gatherTimeMap.get(device.getDeviceCode());
            log.info("正在处理设备：{}，最后采集时间：{}",device.getDeviceCode(),lastGatherTime);
            if(lastGatherTime == null || !lastGatherTime.isEqual(time.minusHours(1).withMinute(0).withSecond(0).withNano(0))){
                hisDevices.add(device);
            }else{
                nowDevices.add(device.getDeviceCode());
            }
        }
        log.info("正在处理实时数据设备：{}",nowDevices);
        deviceNowDataGather(service,time,nowDevices);
        log.info("正在处理历史数据设备：{}",hisDevices);
        for (DeviceEntity hisDevice : hisDevices) {
            LocalDateTime start = gatherTimeMap.get(hisDevice.getDeviceCode());
            if(start != null){
                start = start.plusHours(1);
            }
            deviceHisDataGather(service,hisDevice.getDeviceCode(),hisDevice.getCategoryId(),start,time.plusHours(1));
        }
    }

    /**
     * 实时数据采集
     */
    private void deviceNowDataGather(ILeiYouService service,LocalDateTime dateTime,Collection<String> deviceCodes){
        List<DeviceData> deviceData = service.getDeviceData(deviceCodes);
        deviceData.forEach(item -> {
            // 处理设备数据
            mqService.sendDeviceEnergyData(item.getDeviceId(),dateTime,item.getPointValue());
            gatherTimeService.saveGatherData(item.getDeviceId(),dateTime,item.getPointValue());
        });

    }

    /**
     * 历史数据查询
     * @param service
     * @param deviceCode
     * @param specialtyId
     * @param start 包含
     * @param end 不包含
     */
    private void deviceHisDataGather(ILeiYouService service,String deviceCode,Long specialtyId,LocalDateTime start,LocalDateTime end){
        LocalDateTime lastGatherTime = null;
        BigDecimal value = null;
        try{
            if(start == null ||  ChronoUnit.DAYS.between(start,end) > 7){
                start = end.minusDays(7);
            }
            List<PointData> historyDeviceData = service.getHistoryDeviceData(deviceCode, identifiersMap.get(specialtyId), start, end);
            for(PointData item : historyDeviceData){
                mqService.sendDeviceEnergyData(deviceCode,item.getTime(),item.getValue());
                lastGatherTime = item.getTime();
                value = item.getValue();
            }
        }catch (Exception e){
            log.error("历史数据补充失败，截止时间：{}",lastGatherTime,e);
        }
        if(lastGatherTime != null){
            gatherTimeService.saveGatherData(deviceCode,lastGatherTime,value);
        }

    }

    private static final Map<Long,String> identifiersMap = new HashMap<Long,String>(){{
        put(1L,"12L");// 水表
        put(2L,"11X");// 电表
    }};

}
