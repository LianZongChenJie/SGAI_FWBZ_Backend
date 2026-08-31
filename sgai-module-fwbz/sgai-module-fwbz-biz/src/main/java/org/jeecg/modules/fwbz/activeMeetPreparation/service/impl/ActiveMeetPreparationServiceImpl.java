package org.jeecg.modules.fwbz.activeMeetPreparation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetPreparationInfo;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetsDeviceType;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetInfoMapper;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetPreparationInfoMapper;
import org.jeecg.modules.fwbz.activeMeet.mapper.ActiveMeetsDeviceTypeMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.entity.ActiveMeetPreparationType;
import org.jeecg.modules.fwbz.activeMeetPreparation.entity.Device;
import org.jeecg.modules.fwbz.activeMeetPreparation.entity.LightingCircuit;
import org.jeecg.modules.fwbz.activeMeetPreparation.entity.SmokeDetector;
import org.jeecg.modules.fwbz.activeMeetPreparation.mapper.ActiveMeetPreparationTypeMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.mapper.ADeviceMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.mapper.LightingCircuitMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.mapper.SmokeDetectorMapper;
import org.jeecg.modules.fwbz.activeMeetPreparation.service.IActiveMeetPreparationService;
import org.jeecg.modules.fwbz.activeMeetPreparation.vo.DeviceTypeGroupVO;
import org.jeecg.modules.fwbz.activeMeetPreparation.vo.PreparationChecklistVO;
import org.jeecg.modules.fwbz.activeMeetPreparation.vo.PreparationDetailVO;
import org.jeecg.modules.fwbz.hikvision.entity.AcsDevice;
import org.jeecg.modules.fwbz.hikvision.entity.CameraGroup;
import org.jeecg.modules.fwbz.hikvision.entity.CameraInfo;
import org.jeecg.modules.fwbz.hikvision.entity.DoorResource;
import org.jeecg.modules.fwbz.hikvision.mapper.AcsDeviceMapper;
import org.jeecg.modules.fwbz.hikvision.mapper.CameraGroupMapper;
import org.jeecg.modules.fwbz.hikvision.mapper.CameraInfoMapper;
import org.jeecg.modules.fwbz.hikvision.mapper.DoorResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActiveMeetPreparationServiceImpl implements IActiveMeetPreparationService {

    private final ActiveMeetInfoMapper activeMeetInfoMapper;
    private final ActiveMeetPreparationInfoMapper activeMeetPreparationInfoMapper;
    private final ActiveMeetsDeviceTypeMapper activeMeetsDeviceTypeMapper;
    private final ActiveMeetPreparationTypeMapper activeMeetPreparationTypeMapper;
    private final ADeviceMapper deviceMapper;
    private final CameraInfoMapper cameraInfoMapper;
    private final CameraGroupMapper cameraGroupMapper;
    private final DoorResourceMapper doorResourceMapper;
    private final AcsDeviceMapper acsDeviceMapper;
    private final SmokeDetectorMapper smokeDetectorMapper;
    private final LightingCircuitMapper lightingCircuitMapper;

    public ActiveMeetPreparationServiceImpl(ActiveMeetInfoMapper activeMeetInfoMapper,
                                            ActiveMeetPreparationInfoMapper activeMeetPreparationInfoMapper,
                                            ActiveMeetsDeviceTypeMapper activeMeetsDeviceTypeMapper,
                                            ActiveMeetPreparationTypeMapper activeMeetPreparationTypeMapper,
                                            ADeviceMapper deviceMapper,
                                            CameraInfoMapper cameraInfoMapper,
                                            CameraGroupMapper cameraGroupMapper,
                                            DoorResourceMapper doorResourceMapper,
                                            AcsDeviceMapper acsDeviceMapper,
                                            SmokeDetectorMapper smokeDetectorMapper,
                                            LightingCircuitMapper lightingCircuitMapper) {
        this.activeMeetInfoMapper = activeMeetInfoMapper;
        this.activeMeetPreparationInfoMapper = activeMeetPreparationInfoMapper;
        this.activeMeetsDeviceTypeMapper = activeMeetsDeviceTypeMapper;
        this.activeMeetPreparationTypeMapper = activeMeetPreparationTypeMapper;
        this.deviceMapper = deviceMapper;
        this.cameraInfoMapper = cameraInfoMapper;
        this.cameraGroupMapper = cameraGroupMapper;
        this.doorResourceMapper = doorResourceMapper;
        this.acsDeviceMapper = acsDeviceMapper;
        this.smokeDetectorMapper = smokeDetectorMapper;
        this.lightingCircuitMapper = lightingCircuitMapper;
    }

    @Override
    public PreparationChecklistVO getChecklist(Long activeMeetId) {
        // 1. 获取会议信息
        ActiveMeetInfo meetInfo = activeMeetInfoMapper.selectById(activeMeetId);
        if (meetInfo == null) {
            return null;
        }

        // 2. 获取该会议的所有筹备信息
        List<ActiveMeetPreparationInfo> prepInfoList = activeMeetPreparationInfoMapper.selectList(
                new LambdaQueryWrapper<ActiveMeetPreparationInfo>()
                        .eq(ActiveMeetPreparationInfo::getActiveMeetId, activeMeetId));

        // 3. 获取所有设备类型
        List<ActiveMeetsDeviceType> allDeviceTypes = activeMeetsDeviceTypeMapper.selectList(null);

        // 4. 构建设备类型id -> 筹备信息的映射
        Map<Long, ActiveMeetPreparationInfo> infoMap = new HashMap<>();
        if (prepInfoList != null) {
            for (ActiveMeetPreparationInfo info : prepInfoList) {
                infoMap.put(info.getActiveMeetsDeviceTypeId(), info);
            }
        }

        // 5. 获取所有筹备类型
        List<ActiveMeetPreparationType> prepTypes = activeMeetPreparationTypeMapper.selectList(null);

        // 6. 按筹备类型分组构建数据
        List<DeviceTypeGroupVO> data = new ArrayList<>();
        Map<Long, List<ActiveMeetsDeviceType>> deviceTypeMap = new HashMap<>();
        for (ActiveMeetsDeviceType dt : allDeviceTypes) {
            deviceTypeMap.computeIfAbsent(dt.getTypeId(), k -> new ArrayList<>()).add(dt);
        }

        for (ActiveMeetPreparationType prepType : prepTypes) {
            DeviceTypeGroupVO group = new DeviceTypeGroupVO();
            group.setTypeId(prepType.getId());
            group.setTypeName(prepType.getTypeName());

            List<ActiveMeetsDeviceType> typeDevices = deviceTypeMap.get(prepType.getId());
            if (typeDevices == null) {
                group.setTypeData(Collections.emptyList());
                group.setPreparationProgress("0%");
            } else {
                List<PreparationDetailVO> details = new ArrayList<>();
                int completedCount = 0;
                for (ActiveMeetsDeviceType dt : typeDevices) {
                    ActiveMeetPreparationInfo info = infoMap.get(dt.getId());
                    PreparationDetailVO detail = new PreparationDetailVO();
                    detail.setPreparationInfoId(info != null ? info.getId() : null);
                    detail.setPreparationInfoName(dt.getDeviceTypeName());
                    detail.setStatus(info != null ? info.getStatus() : 0);
                    detail.setCompleteTime(info != null ? info.getCompleteTime() : null);
                    if (info != null && info.getStatus() != null && info.getStatus() == 1) {
                        completedCount++;
                    }

                    // 计算设备数量：已完成直接用库数据，未完成则查各表
                    if (info != null && info.getStatus() != null && info.getStatus() == 1) {
                        detail.setPreparationValue(info.getPreparationValue() != null ? info.getPreparationValue() : 0L);
                        detail.setRealValue(info.getRealValue() != null ? info.getRealValue() : 0L);
                    } else {
                        CountResult countResult = computeCount(dt);
                        detail.setPreparationValue(countResult.total);
                        detail.setRealValue(countResult.online);
                    }
                    details.add(detail);
                }
                group.setTypeData(details);
                group.setPreparationProgress(calcGroupProgress(completedCount, details.size()));
            }
            data.add(group);
        }

        // 7. 计算总体进度（四大项各占25%）
        String progress = calcOverallProgress(data);

        PreparationChecklistVO result = new PreparationChecklistVO();
        result.setActiveMeetId(activeMeetId);
        result.setActiveName(meetInfo.getActiveName());
        result.setPreparationProgress(progress);
        result.setData(data);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePreparation(Long preparationInfoId, Long preparationValue, Long realValue) {
        // 1. 查询并更新筹备信息
        ActiveMeetPreparationInfo info = activeMeetPreparationInfoMapper.selectById(preparationInfoId);
        if (info == null) {
            throw new RuntimeException("筹备信息不存在");
        }
        info.setPreparationValue(preparationValue);
        info.setRealValue(realValue);
        info.setStatus(1);
        info.setCompleteTime(new Date());
        activeMeetPreparationInfoMapper.updateById(info);

        // 2. 重算并更新活动总体进度
        Double progress = calcActiveProgress(info.getActiveMeetId());
        ActiveMeetInfo meetInfo = new ActiveMeetInfo();
        meetInfo.setId(info.getActiveMeetId());
        meetInfo.setActiveProgress(progress);
        activeMeetInfoMapper.updateById(meetInfo);
    }

    /**
     * 重新计算活动的筹备总体进度，并返回百分比数值（如 50.0）
     */
    private Double calcActiveProgress(Long activeMeetId) {
        // 查询该会议的所有筹备信息
        List<ActiveMeetPreparationInfo> prepInfoList = activeMeetPreparationInfoMapper.selectList(
                new LambdaQueryWrapper<ActiveMeetPreparationInfo>()
                        .eq(ActiveMeetPreparationInfo::getActiveMeetId, activeMeetId));

        // 获取所有设备类型及其所属的筹备类型
        List<ActiveMeetsDeviceType> allDeviceTypes = activeMeetsDeviceTypeMapper.selectList(null);
        Map<Long, Long> deviceTypeToPrepType = new HashMap<>();
        Map<Long, List<Long>> prepTypeToDeviceTypes = new HashMap<>();
        for (ActiveMeetsDeviceType dt : allDeviceTypes) {
            deviceTypeToPrepType.put(dt.getId(), dt.getTypeId());
            prepTypeToDeviceTypes.computeIfAbsent(dt.getTypeId(), k -> new ArrayList<>()).add(dt.getId());
        }

        // 构建 deviceTypeId -> prepInfo 映射
        Map<Long, ActiveMeetPreparationInfo> infoMap = new HashMap<>();
        if (prepInfoList != null) {
            for (ActiveMeetPreparationInfo info : prepInfoList) {
                infoMap.put(info.getActiveMeetsDeviceTypeId(), info);
            }
        }

        // 按筹备类型计算每组进度
        double totalProgress = 0;
        List<ActiveMeetPreparationType> prepTypes = activeMeetPreparationTypeMapper.selectList(null);
        for (ActiveMeetPreparationType pt : prepTypes) {
            List<Long> dtIds = prepTypeToDeviceTypes.get(pt.getId());
            if (dtIds == null || dtIds.isEmpty()) {
                continue;
            }
            int total = dtIds.size();
            int completed = 0;
            for (Long dtId : dtIds) {
                ActiveMeetPreparationInfo info = infoMap.get(dtId);
                if (info != null && info.getStatus() != null && info.getStatus() == 1) {
                    completed++;
                }
            }
            double groupPercent = total == 0 ? 0 : (completed * 100.0 / total);
            // 每个大项权重25%
            totalProgress += groupPercent * 0.25;
        }

        return Math.round(totalProgress * 100.0) / 100.0;
    }

    /**
     * 根据设备类型计算设备总数和在线数
     */
    private CountResult computeCount(ActiveMeetsDeviceType dt) {
        if (dt.getDeviceTypeId() != null) {
            // 从device表统计（category_id = device_type_id）
            long total = deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getCategoryId, dt.getDeviceTypeId()));
            long online = deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getCategoryId, dt.getDeviceTypeId())
                            .eq(Device::getRunState, "在线"));
            return new CountResult(total, online);
        } else {
            // device_type_id为空，根据device_type_name判断数据来源
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

    /**
     * 摄像头设备统计：从 camera_info 表统计，结合 table_camera_group 分组表，
     * 只统计分组名称包含"服贸会"或"园区高点"的分组及其全部子孙分组下的摄像头。
     */
    private CountResult countCamera() {
        List<CameraGroup> allGroups = cameraGroupMapper.selectList(null);
        if (allGroups == null || allGroups.isEmpty()) {
            return CountResult.ZERO;
        }
        // 收集名称含"服贸会"/"园区高点"的分组（含其全部子孙分组）id
        Map<Long, List<CameraGroup>> childrenMap = allGroups.stream()
                .filter(g -> g.getParentId() != null)
                .collect(Collectors.groupingBy(CameraGroup::getParentId));
        Set<Long> matchedIds = new HashSet<>();
        for (CameraGroup group : allGroups) {
            if (isPackageGroup(group.getName())) {
                matchedIds.add(group.getId());
                collectPackageChildren(group.getId(), childrenMap, matchedIds);
            }
        }
        if (matchedIds.isEmpty()) {
            return CountResult.ZERO;
        }
        List<Long> groupIds = new ArrayList<>(matchedIds);
        long total = cameraInfoMapper.selectCount(
                new LambdaQueryWrapper<CameraInfo>().in(CameraInfo::getGroupId, groupIds));
        long online = cameraInfoMapper.selectCount(
                new LambdaQueryWrapper<CameraInfo>()
                        .in(CameraInfo::getGroupId, groupIds)
                        .eq(CameraInfo::getOnline, 1));
        return new CountResult(total, online);
    }

    /**
     * 判断分组名称是否属于需要统计的分组（名称包含"服贸会"或"园区高点"）
     */
    private boolean isPackageGroup(String name) {
        return StringUtils.isNotBlank(name)
                && (name.contains("服贸会") || name.contains("园区高点"));
    }

    /**
     * 递归收集指定分组的全部子孙分组 id
     */
    private void collectPackageChildren(Long parentId, Map<Long, List<CameraGroup>> childrenMap, Set<Long> matchedIds) {
        List<CameraGroup> children = childrenMap.get(parentId);
        if (children == null) {
            return;
        }
        for (CameraGroup child : children) {
            if (matchedIds.add(child.getId())) {
                collectPackageChildren(child.getId(), childrenMap, matchedIds);
            }
        }
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
        // 除了"离线"和"故障"的，都算在线
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

    /**
     * 计算单个分组内的筹备进度（每项均分）
     */
    private String calcGroupProgress(int completedCount, int totalCount) {
        if (totalCount == 0) {
            return "0%";
        }
        return (completedCount * 100 / totalCount) + "%";
    }

    /**
     * 计算总体进度（四大项各占25%）
     */
    private String calcOverallProgress(List<DeviceTypeGroupVO> groups) {
        if (groups == null || groups.isEmpty()) {
            return "0%";
        }
        double totalProgress = 0;
        for (DeviceTypeGroupVO group : groups) {
            String gp = group.getPreparationProgress();
            if (gp != null && gp.endsWith("%")) {
                double groupPercent = Double.parseDouble(gp.replace("%", ""));
                // 每个大项权重25%
                totalProgress += groupPercent * 0.25;
            }
        }
        return Math.round(totalProgress) + "%";
    }

    /**
     * 设备计数结果
     */
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
