package org.jeecg.modules.fwbz.runGuarantee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetsDeviceType;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetsDeviceTypeMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.entity.Device;
import org.jeecg.modules.fwbz.activeMeetPreparation.entity.LightingCircuit;
import org.jeecg.modules.fwbz.activeMeetPreparation.entity.SmokeDetector;
import org.jeecg.modules.fwbz.activeMeetPreparation.mapper.DeviceMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.mapper.LightingCircuitMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.mapper.SmokeDetectorMapper;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.complaint.mapper.BuildingControlPointHistoryMapper;
import org.jeecg.modules.fwbz.complaint.mapper.BuildingControlPointSendHistoryMapper;
import org.jeecg.modules.fwbz.complaint.mapper.LightingOperationLogMapper;
import org.jeecg.modules.fwbz.hikvision.entity.AcsDevice;
import org.jeecg.modules.fwbz.hikvision.entity.CameraResource;
import org.jeecg.modules.fwbz.hikvision.entity.DoorResource;
import org.jeecg.modules.fwbz.hikvision.mapper.AcsDeviceMapper;
import org.jeecg.modules.fwbz.hikvision.mapper.CameraResourceMapper;
import org.jeecg.modules.fwbz.hikvision.mapper.DoorResourceMapper;
import org.jeecg.modules.fwbz.runGuarantee.service.IRunGuaranteeService;
import org.jeecg.modules.fwbz.runGuarantee.vo.SystemDeviceStatVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 运行保障服务实现
 */
@Service
public class RunGuaranteeServiceImpl implements IRunGuaranteeService {

    private final ActiveMeetsDeviceTypeMapper activeMeetsDeviceTypeMapper;
    private final DeviceMapper deviceMapper;
    private final CameraResourceMapper cameraResourceMapper;
    private final DoorResourceMapper doorResourceMapper;
    private final AcsDeviceMapper acsDeviceMapper;
    private final SmokeDetectorMapper smokeDetectorMapper;
    private final LightingCircuitMapper lightingCircuitMapper;
    private final BuildingControlPointHistoryMapper buildingControlPointHistoryMapper;
    private final BuildingControlPointSendHistoryMapper buildingControlPointSendHistoryMapper;
    private final LightingOperationLogMapper lightingOperationLogMapper;

    public RunGuaranteeServiceImpl(ActiveMeetsDeviceTypeMapper activeMeetsDeviceTypeMapper,
                                   DeviceMapper deviceMapper,
                                   CameraResourceMapper cameraResourceMapper,
                                   DoorResourceMapper doorResourceMapper,
                                   AcsDeviceMapper acsDeviceMapper,
                                   SmokeDetectorMapper smokeDetectorMapper,
                                   LightingCircuitMapper lightingCircuitMapper,
                                   BuildingControlPointHistoryMapper buildingControlPointHistoryMapper,
                                   BuildingControlPointSendHistoryMapper buildingControlPointSendHistoryMapper,
                                   LightingOperationLogMapper lightingOperationLogMapper) {
        this.activeMeetsDeviceTypeMapper = activeMeetsDeviceTypeMapper;
        this.deviceMapper = deviceMapper;
        this.cameraResourceMapper = cameraResourceMapper;
        this.doorResourceMapper = doorResourceMapper;
        this.acsDeviceMapper = acsDeviceMapper;
        this.smokeDetectorMapper = smokeDetectorMapper;
        this.lightingCircuitMapper = lightingCircuitMapper;
        this.buildingControlPointHistoryMapper = buildingControlPointHistoryMapper;
        this.buildingControlPointSendHistoryMapper = buildingControlPointSendHistoryMapper;
        this.lightingOperationLogMapper = lightingOperationLogMapper;
    }

    @Override
    public List<SystemDeviceStatVO> getDeviceStat() {
        List<ActiveMeetsDeviceType> allDeviceTypes = activeMeetsDeviceTypeMapper.selectList(null);

        List<SystemDeviceStatVO> result = new ArrayList<>();
        for (ActiveMeetsDeviceType dt : allDeviceTypes) {
            CountResult countResult = computeCount(dt);

            SystemDeviceStatVO vo = new SystemDeviceStatVO();
            vo.setSystemName(dt.getDeviceTypeName());
            vo.setDeviceCount(countResult.total);
            vo.setOnline(countResult.online);

            if (countResult.total > 0) {
                vo.setOnlineRate((int) (countResult.online * 100 / countResult.total));
            } else {
                vo.setOnlineRate(0);
            }

            result.add(vo);
        }
        return result;
    }

    @Override
    public StatCardVO getLinkTotal() {
        long count = activeMeetsDeviceTypeMapper.selectCount(null);
        StatCardVO vo = new StatCardVO();
        vo.setTitle("链路总数");
        vo.setValue(count);
        vo.setContext(count + "系统");
        return vo;
    }

    @Override
    public StatCardVO getNormalLink() {
        long count = buildingControlPointHistoryMapper.countToday(getToday());
        StatCardVO vo = new StatCardVO();
        vo.setTitle("数据接收数");
        vo.setValue(count);
        vo.setContext(count + "消息");
        return vo;
    }

    @Override
    public StatCardVO getCollectionStatus() {
        long count = buildingControlPointSendHistoryMapper.countToday(getToday());
        StatCardVO vo = new StatCardVO();
        vo.setTitle("设备控制数");
        vo.setValue(count);
        vo.setContext(count + "控制");
        return vo;
    }

    @Override
    public StatCardVO getProcessingStatus() {
        long count = lightingOperationLogMapper.countToday(getToday());
        StatCardVO vo = new StatCardVO();
        vo.setTitle("照明控制数");
        vo.setValue(count);
        vo.setContext(count + "照明");
        return vo;
    }

    @Override
    public List<StatCardVO> getSummary() {
        return Arrays.asList(getLinkTotal(), getNormalLink(), getCollectionStatus(), getProcessingStatus());
    }

    private Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private CountResult computeCount(ActiveMeetsDeviceType dt) {
        if (dt.getDeviceTypeId() != null) {
            long total = deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getCategoryId, dt.getDeviceTypeId()));
            long online = deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getCategoryId, dt.getDeviceTypeId())
                            .eq(Device::getRunState, "在线"));
            return new CountResult(total, online);
        } else {
            String name = dt.getDeviceTypeName();
            if (name == null) {
                return CountResult.ZERO;
            }
            switch (name) {
                case "摄像头":
                    return countCamera();
                case "门禁点位":
                    return countDoor();
                case "门禁设备":
                    return countAcsDevice();
                case "烟感设备":
                    return countSmokeDetector("1");
                case "温感设备":
                    return countSmokeDetector("2");
                case "照明设备":
                    return countLighting();
                default:
                    return CountResult.ZERO;
            }
        }
    }

    private CountResult countCamera() {
        long total = cameraResourceMapper.selectCount(null);
        long online = cameraResourceMapper.selectCount(
                new LambdaQueryWrapper<CameraResource>()
                        .eq(CameraResource::getOnline, 1));
        return new CountResult(total, online);
    }

    private CountResult countDoor() {
        long total = doorResourceMapper.selectCount(null);
        long online = doorResourceMapper.selectCount(
                new LambdaQueryWrapper<DoorResource>()
                        .ne(DoorResource::getDoorState, "3"));
        return new CountResult(total, online);
    }

    private CountResult countAcsDevice() {
        long total = acsDeviceMapper.selectCount(null);
        long online = acsDeviceMapper.selectCount(
                new LambdaQueryWrapper<AcsDevice>()
                        .eq(AcsDevice::getOnline, "1"));
        return new CountResult(total, online);
    }

    private CountResult countSmokeDetector(String deviceType) {
        long total = smokeDetectorMapper.selectCount(
                new LambdaQueryWrapper<SmokeDetector>()
                        .eq(SmokeDetector::getDeviceType, deviceType));
        long online = smokeDetectorMapper.selectCount(
                new LambdaQueryWrapper<SmokeDetector>()
                        .eq(SmokeDetector::getDeviceType, deviceType)
                        .ne(SmokeDetector::getStatus, "离线")
                        .ne(SmokeDetector::getStatus, "故障"));
        return new CountResult(total, online);
    }

    private CountResult countLighting() {
        long total = lightingCircuitMapper.selectCount(null);
        long online = lightingCircuitMapper.selectCount(
                new LambdaQueryWrapper<LightingCircuit>()
                        .eq(LightingCircuit::getComstat, "1"));
        return new CountResult(total, online);
    }

    private static class CountResult {
        static final CountResult ZERO = new CountResult(0L, 0L);

        final long total;
        final long online;

        CountResult(long total, long online) {
            this.total = total;
            this.online = online;
        }
    }
}
