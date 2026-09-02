package org.jeecg.modules.fwbz.buildingControl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunwayland.pspace.entity.Base;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.enums.PsErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlService;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlServerService;
import org.jeecg.modules.fwbz.buildingControl.service.IBuildingControlSendHistoryService;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private IDeviceAttributeService deviceAttributeService;

    @Autowired
    private IBuildingControlSendHistoryService buildingControlSendHistoryService;

    /**
     * 向楼控系统(pSpace)写入实时数据（写点/控制）
     *
     * 写点成功后（返回码 PSRET_OK）记录发送控制历史：
     * 按 tagId（device_attribute.acquisition_coding）关联设备属性，
     * 落一条 building_control_point_send_history（含属性id/设备id/属性名称/控制值/控制时间/控制人）。
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
        if (!Objects.equals(code, PsErrorCodeEnum.PSRET_OK)) {
            log.warn("楼控写点失败: tagid={}, value={}, code={}", items.getTagid(), items.getPv(), code);
        } else {
            // 写点成功：记录发送控制历史（失败仅告警，不影响主流程）
            saveSendHistory(items.getTagid(), items.getPv());
        }
        if (result.getData() == null || result.getData().isEmpty()) {
            return "写点完成: " + code;
        }
        return result.getData().get(0).toString();
    }

    /**
     * 保存楼控发送控制历史：按 tagId 查 device_attribute（acquisition_coding=tagId），
     * 取属性id/设备id/属性名称与控制值、控制人一起写入 building_control_point_send_history。
     * 属性查不到时仅告警（写点本身已成功，不影响返回）。
     *
     * @param tagId 检测点ID（对应 device_attribute.acquisition_coding）
     * @param pv    控制值
     */
    private void saveSendHistory(Long tagId, Object pv) {
        try {
            DeviceAttribute attribute = deviceAttributeService.getOne(
                    new LambdaQueryWrapper<DeviceAttribute>()
                            .eq(DeviceAttribute::getAcquisitionCoding, String.valueOf(tagId)),
                    false);
            if (attribute == null) {
                log.warn("楼控写点成功但未找到对应设备属性, 不记录历史: tagid={}, value={}", tagId, pv);
                return;
            }
            buildingControlSendHistoryService.saveControlHistory(
                    attribute.getId(),
                    attribute.getDeviceId(),
                    attribute.getAttributeName(),
                    String.valueOf(pv),
                    getCurrentUsername());
            log.info("楼控写点历史记录成功: tagid={}, attributeId={}, value={}", tagId, attribute.getId(), pv);
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
