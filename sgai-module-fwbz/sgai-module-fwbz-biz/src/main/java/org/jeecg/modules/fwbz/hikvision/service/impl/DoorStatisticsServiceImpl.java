package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.dto.StatCardVO;
import org.jeecg.modules.fwbz.hikvision.entity.AcsDevice;
import org.jeecg.modules.fwbz.hikvision.entity.DoorResource;
import org.jeecg.modules.fwbz.hikvision.service.IAcsDeviceService;
import org.jeecg.modules.fwbz.hikvision.service.IDoorResourceService;
import org.jeecg.modules.fwbz.hikvision.service.IDoorStatisticsService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 门禁统计服务实现
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class DoorStatisticsServiceImpl implements IDoorStatisticsService {

    private final IDoorResourceService doorResourceService;
    private final IAcsDeviceService acsDeviceService;

    @Override
    public StatCardVO countTotalDoorPoints() {
        long total = doorResourceService.count();
        // 在线点位 = 总点位 - 离线点位
        long online = doorResourceService.count(new LambdaQueryWrapper<DoorResource>()
                .ne(DoorResource::getDoorState, "3"));
        String onlineRate = total > 0 ? String.format("%.1f%%", (double) online / total * 100) : "0%";

        StatCardVO vo = new StatCardVO();
        vo.setTitle("门禁点位总数");
        vo.setValue(total);
        vo.setContext("在线率" + onlineRate);
        return vo;
    }

    @Override
    public StatCardVO countOnlineDoorPoints() {
        long online = doorResourceService.count(new LambdaQueryWrapper<DoorResource>()
                .ne(DoorResource::getDoorState, "3"));
        long total = doorResourceService.count();
        String onlineRate = total > 0 ? String.format("%.1f%%", (double) online / total * 100) : "0%";

        StatCardVO vo = new StatCardVO();
        vo.setTitle("在线门禁点位");
        vo.setValue(online);
        vo.setContext("在线率" + onlineRate);
        return vo;
    }

    @Override
    public StatCardVO countTotalDevices() {
        long total = acsDeviceService.count();
        long online = acsDeviceService.count(new LambdaQueryWrapper<AcsDevice>()
                .eq(AcsDevice::getOnline, "1"));
        String onlineRate = total > 0 ? String.format("%.1f%%", (double) online / total * 100) : "0%";

        StatCardVO vo = new StatCardVO();
        vo.setTitle("门禁设备总数");
        vo.setValue(total);
        vo.setContext("在线率" + onlineRate);
        return vo;
    }

    @Override
    public StatCardVO countOnlineDevices() {
        long online = acsDeviceService.count(new LambdaQueryWrapper<AcsDevice>()
                .eq(AcsDevice::getOnline, "1"));
        long total = acsDeviceService.count();
        String onlineRate = total > 0 ? String.format("%.1f%%", (double) online / total * 100) : "0%";

        StatCardVO vo = new StatCardVO();
        vo.setTitle("在线门禁设备");
        vo.setValue(online);
        vo.setContext("在线率" + onlineRate);
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        return Arrays.asList(
                countTotalDoorPoints(),
                countOnlineDoorPoints(),
                countTotalDevices(),
                countOnlineDevices()
        );
    }
}
