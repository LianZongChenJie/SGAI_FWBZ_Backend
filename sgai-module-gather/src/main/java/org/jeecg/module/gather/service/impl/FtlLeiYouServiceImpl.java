package org.jeecg.module.gather.service.impl;

import lombok.Data;
import org.jeecg.module.gather.dto.DeviceCommStatus;
import org.jeecg.module.gather.dto.DeviceData;
import org.jeecg.module.gather.dto.PointData;
import org.jeecg.module.gather.service.ILeiYouService;
import org.jeecg.module.gather.util.LeiYouUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@Data
public class FtlLeiYouServiceImpl implements ILeiYouService {
    @Value("${leiyou.ftl.host}")
    private String host;
    @Value("${leiyou.ftl.clientId}")
    private String clientId;
    @Value("${leiyou.ftl.clientSecret}")
    private String clientSecret;

    private static String token = "";

    @Override
    public void refreshToken(){
        token = LeiYouUtil.getToken(host,clientId,clientSecret);
    }

    /**
     * 获取设备最新采集值
     * @param deviceCodeList 设备编号列表
     */
    @Override
    public List<DeviceData> getDeviceData(Collection<String> deviceCodeList) {
        return LeiYouUtil.getDeviceData(host,token,deviceCodeList);
    }

    /**
     * 获取设备状态
     * @param deviceCodeList 设备编号
     */
    @Override
    public List<DeviceCommStatus> getOnlineData(Collection<String> deviceCodeList) {
        return LeiYouUtil.getOnlineData(host,token,deviceCodeList);
    }

    @Override
    public List<PointData> getHistoryDeviceData(String deviceCode, String identifiers, LocalDateTime start, LocalDateTime end){
        return LeiYouUtil.getHistoryDeviceData(host,token,deviceCode,identifiers,start,end);
    }



}
