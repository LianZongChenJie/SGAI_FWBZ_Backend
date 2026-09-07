package org.jeecg.modules.fwbz.buildingControl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunwayland.pspace.entity.Base;
import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.enums.PsErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdateValueTypeResponseDto;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlService;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlRealPushService;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlServerService;
import org.jeecg.modules.fwbz.buildingControl.service.IBuildingControlSendHistoryService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceServerService;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 楼控系统实时数据写入服务实现
 *
 * 连接复用 BuildingControlServerService 管理的长连接（幂等），
 * 避免每次写点都 connect/disconnect 干扰实时订阅链路。
 */
@Slf4j
@Service
public class BuildingControlServiceImpl implements BuildingControlService {

    @Autowired
    private BuildingControlServerService buildingControlServerService;

    @Autowired
    private ColdSourceServerService coldSourceServerService;

    @Autowired
    private IDeviceAttributeService deviceAttributeService;

    @Autowired
    private IBuildingControlSendHistoryService buildingControlSendHistoryService;

    /**
     * 向楼控系统(pSpace)写入实时数据（写点/控制）
     *
     * 无论写点成功与否，都记录发送控制历史并标记是否成功：
     * 按 tagId（device_attribute.acquisition_coding）关联设备属性，
     * 落一条 building_control_point_send_history（含属性id/设备id/属性名称/控制值/控制时间/控制人/是否成功）。
     *
     * @param items 前端传入的更新项（tagid + 设定值）
     * @return 楼控系统返回结果
     */
    @Override
    public String updRealData(UpdRealDataItemDto items) {
        if (items == null) {
            throw new IllegalArgumentException("更新数据不能为空");
        }
        PsResult<Base> result = buildingControlServerService.realWrite(items.getTagid(), items.getPv());
        PsErrorCodeEnum code = result.getCode();
        boolean isOk = Objects.equals(code, PsErrorCodeEnum.PSRET_OK);
        if (!isOk) {
            log.warn("楼控写点失败: tagid={}, value={}, code={}", items.getTagid(), items.getPv(), code);
        } else {
            // 写点成功：根据 tagId 回写设备属性采集值（写入值即采集值）与采集时间
            updateAttributeValue(items.getTagid(), items.getPv());
        }
        // 写点成功与否都记录发送控制历史（历史记录失败仅告警，不影响主流程）
        saveSendHistory(items.getTagid(), items.getPv(), isOk);
        if (!isOk) {
            // 写点失败：抛出异常，由调用方返回失败结果（历史已在上面记录）
            throw new RuntimeException("楼控写点失败: tagid=" + items.getTagid() + ", value=" + items.getPv() + ", code=" + code);
        }
        if (result.getData() == null || result.getData().isEmpty()) {
            return "写点完成: " + code;
        }
        return result.getData().get(0).toString();
    }

    /**
     * 更新设备属性数据类型：
     * 1. 查询 device_attribute 中采集编码(acquisition_coding)非空的属性，过滤出纯数字编码；
     * 2. 以编码作为 tagId 逐点 realRead，取返回 PsData 的 dataType；
     * 3. 按采集编码回写 value_type（仅更新该字段，不覆盖采集值/采集时间）。
     *
     * @return 更新统计结果（总数/成功/失败及失败明细）
     */
    @Override
    public UpdateValueTypeResponseDto updateAttributeValueType() {
        UpdateValueTypeResponseDto response = new UpdateValueTypeResponseDto();
        List<DeviceAttribute> attributes = deviceAttributeService.list(
                new LambdaQueryWrapper<DeviceAttribute>()
                        .isNotNull(DeviceAttribute::getAcquisitionCoding)
                        .ne(DeviceAttribute::getAcquisitionCoding, ""));
        int total = 0;
        int success = 0;
        int failed = 0;
        for (DeviceAttribute attr : attributes) {
            String coding = attr.getAcquisitionCoding() == null ? "" : attr.getAcquisitionCoding().trim();
            if (!StringUtils.isNumeric(coding)) {
                continue;
            }
            total++;
            Long tagId;
            try {
                tagId = Long.valueOf(coding);
            } catch (NumberFormatException e) {
                failed++;
                response.getFailDetails().add("tagId=" + coding + " 非法数字");
                continue;
            }
            try {
                PsResult<PsData> result = coldSourceServerService.connect().realRead(tagId);
                if (result.isSuccess() && result.getData() != null && !result.getData().isEmpty()) {
                    PsData psData = result.getData().get(0);
                    String dataType = psData.getDataType() == null ? null : psData.getDataType().name();
                    if (StringUtils.isNotBlank(dataType)) {
                        attr.setValueType(dataType);
                        deviceAttributeService.updateById(attr);
                        success++;
                    } else {
                        failed++;
                        response.getFailDetails().add("tagId=" + tagId + " dataType为空");
                    }
                } else {
                    failed++;
                    response.getFailDetails().add("tagId=" + tagId + " code=" + result.getCode());
                }
            } catch (Exception e) {
                failed++;
                response.getFailDetails().add("tagId=" + tagId + " 异常: " + e.getMessage());
                log.warn("更新属性数据类型读点失败: tagId={}", tagId, e);
            }
        }
        response.setTotal(total);
        response.setSuccess(success);
        response.setFailed(failed);
        log.info("更新设备属性数据类型完成: 总数={}, 成功={}, 失败={}", total, success, failed);
        return response;
    }

    /**
     * 写点成功后，根据 tagId（对应 device_attribute.acquisition_coding）回写该设备属性的采集值 value 与采集时间 gather_time。
     * 写入值即采集值（Boolean 转 0/1、整数去小数、BigDecimal 去尾零，与读点链路一致）；
     * 属性查不到或更新失败仅告警，不影响写点主流程返回。
     *
     * @param tagId 检测点ID（对应 device_attribute.acquisition_coding）
     * @param pv    写点设定值（即采集值 value）
     */
    private void updateAttributeValue(Long tagId, Object pv) {
        try {
            DeviceAttribute attribute = deviceAttributeService.getOne(
                    new LambdaQueryWrapper<DeviceAttribute>()
                            .eq(DeviceAttribute::getAcquisitionCoding, String.valueOf(tagId)),
                    false);
            if (attribute == null) {
                log.warn("楼控写点成功但未找到对应设备属性, 无法回写采集值: tagid={}, value={}", tagId, pv);
                return;
            }
            attribute.setValue(BuildingControlRealPushService.convertValue(pv));
            attribute.setGatherTime(LocalDateTime.now());
            deviceAttributeService.updateById(attribute);
            log.info("楼控写点成功，已回写设备属性采集值: tagid={}, attributeId={}, value={}",
                    tagId, attribute.getId(), pv);
        } catch (Exception e) {
            log.error("楼控写点成功，回写设备属性采集值失败: tagid={}, value={}", tagId, pv, e);
        }
    }

    /**
     * 保存楼控发送控制历史：按 tagId 查 device_attribute（acquisition_coding=tagId），
     * 取属性id/设备id/属性名称与控制值、控制人、是否成功一起写入 building_control_point_send_history。
     * 属性查不到时仅告警（不影响主流程返回）。
     *
     * @param tagId 检测点ID（对应 device_attribute.acquisition_coding）
     * @param pv    控制值
     * @param isOk  写点是否成功（true-成功 false-失败）
     */
    private void saveSendHistory(Long tagId, Object pv, boolean isOk) {
        try {
            DeviceAttribute attribute = deviceAttributeService.getOne(
                    new LambdaQueryWrapper<DeviceAttribute>()
                            .eq(DeviceAttribute::getAcquisitionCoding, String.valueOf(tagId)),
                    false);
            if (attribute == null) {
                log.warn("楼控写点{}但未找到对应设备属性, 不记录历史: tagid={}, value={}",
                        isOk ? "成功" : "失败", tagId, pv);
                return;
            }
            buildingControlSendHistoryService.saveControlHistory(
                    attribute.getId(),
                    attribute.getDeviceId(),
                    attribute.getAttributeName(),
                    String.valueOf(pv),
                    getCurrentUsername(),
                    isOk ? "1" : "0");
            log.info("楼控写点历史记录成功: tagid={}, attributeId={}, value={}, isOk={}", tagId, attribute.getId(), pv, isOk);
        } catch (Exception e) {
            log.error("楼控写点历史记录失败: tagid={}, value={}", tagId, pv, e);
        }
    }

    /**
     * 获取当前登录用户；无登录上下文（如系统间调用/匿名）返回 null
     */
    private String getCurrentUsername() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                return ((LoginUser) principal).getUsername();
            }
        } catch (Exception e) {
            log.debug("获取当前登录用户失败，controlBy 置空", e);
        }
        return null;
    }
}
