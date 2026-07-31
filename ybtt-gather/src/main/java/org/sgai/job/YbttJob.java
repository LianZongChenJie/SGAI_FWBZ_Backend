package org.sgai.job;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sgai.dto.YbttAttribute;
import org.sgai.dto.YbttDto;
import org.sgai.mq.AttributeData;
import org.sgai.mq.DeviceAttributeData;
import org.sgai.mq.MqSendService;
import org.sgai.service.YbttService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class YbttJob implements CommandLineRunner {

    private final YbttService service;

    private final MqSendService mqSendService;

    @Scheduled(cron = "0 0/15 * * * ? ")
    public void deviceDetailGather(){
        log.info("开始处理数据");
        List<YbttDto> ybttDtos = service.queryDeviceDetail();
        for (YbttDto item : ybttDtos) {
            DeviceAttributeData attributeData = convert(item);
            mqSendService.sendDeviceAttributeDataChange(attributeData);
            mqSendService.sendDeviceLastGatherTime(attributeData.getEquipmentCode(),attributeData.getTimestamp());
        }
        log.info("数据处理成功");
    }

    private DeviceAttributeData convert(YbttDto dto){
        DeviceAttributeData data = new DeviceAttributeData();
        data.setEquipmentCode(dto.getDeviceNum());
        List<AttributeData> dataList = new ArrayList<>();
        Long time = null;
        for(YbttAttribute item : dto.getDataList()){
            AttributeData attributeData = new AttributeData();
            attributeData.setUniqueKey(item.getDataName());
            attributeData.setValue(item.getDataValue());
            dataList.add(attributeData);
            if(time == null || time < item.getDataLogTime()){
                time = item.getDataLogTime();
            }
        }
        if(time == null){
            time = System.currentTimeMillis();
        }
        data.setData(dataList);
        data.setTimestamp(LocalDateTimeUtil.of(time));
        return data;
    }

    @Override
    public void run(String... args) throws Exception {
        deviceDetailGather();
    }
}
