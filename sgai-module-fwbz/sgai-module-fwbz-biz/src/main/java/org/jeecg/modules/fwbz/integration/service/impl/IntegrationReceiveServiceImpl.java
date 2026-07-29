package org.jeecg.modules.fwbz.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.integration.dto.*;
import org.jeecg.modules.fwbz.integration.service.IntegrationReceiveService;
import org.jeecg.modules.fwbz.mdm.entity.Device;
import org.jeecg.modules.fwbz.mdm.entity.EquipmentCategory;
import org.jeecg.modules.fwbz.mdm.entity.Space;
import org.jeecg.modules.fwbz.mdm.mapper.DeviceMapper;
import org.jeecg.modules.fwbz.mdm.mapper.EquipmentCategoryMapper;
import org.jeecg.modules.fwbz.mdm.mapper.SpaceMapper;
import org.jeecg.modules.fwbz.mdm.service.IEquipmentCategoryService;
import org.jeecg.modules.fwbz.mdm.service.ISpaceService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class IntegrationReceiveServiceImpl implements IntegrationReceiveService {

    private IEquipmentCategoryService categoryService;
    private ISpaceService spaceService;
    private DeviceMapper deviceMapper;
    private EquipmentCategoryMapper categoryMapper;
    private SpaceMapper spaceMapper;

    @Override
    public ReceiveResult receive(IntegrationPayload<?> payload, String deviceType) {
        ReceiveResult result = new ReceiveResult(payload.getBatchId());
        String type = payload.getType();
        String op = payload.getOp();
        for (Object raw : payload.getData()) {
            String rejectReason = dispatch(type, op, deviceType, raw);
            if (rejectReason == null) {
                result.setAccepted(result.getAccepted() + 1);
            } else {
                String id = extractId(raw);
                result.getRejected().add(new RejectedItem(id, rejectReason));
            }
        }
        return result;
    }

    /** @return null 表示成功；非空为 reject 原因 */
    @SuppressWarnings("unchecked")
    private String dispatch(String type, String op, String deviceType, Object raw) {
        try {
            if ("CATEGORY".equals(type)) {
                return handleCategory(op, deviceType, (CategoryPushItem) convert(raw, CategoryPushItem.class));
            } else if ("SPACE".equals(type)) {
                return handleSpace(op, (SpacePushItem) convert(raw, SpacePushItem.class));
            } else if ("DEVICE".equals(type)) {
                return handleDevice(op, deviceType, (DevicePushItem) convert(raw, DevicePushItem.class));
            }
            return "不支持的类型:" + type;
        } catch (Exception e) {
            log.error("接收处理异常: {}", raw, e);
            return "处理异常:" + e.getMessage();
        }
    }

    private String handleCategory(String op, String type, CategoryPushItem item) {
        if ("DELETE".equals(op)) {
            categoryMapper.delete(new QueryWrapper<EquipmentCategory>().eq("master_id", item.getId()));
            return null;
        }
        UpsertResult r = categoryService.upsertByMasterId(item.getId(), item.getName(), item.getPid(), type);
        return r.isOk() ? null : r.getReason();
    }

    private String handleSpace(String op, SpacePushItem item) {
        if ("DELETE".equals(op)) {
            spaceMapper.delete(new QueryWrapper<Space>().eq("master_id", item.getId()));
            return null;
        }
        UpsertResult r = spaceService.upsertByMasterId(item.getId(), item.getName(), item.getPid());
        return r.isOk() ? null : r.getReason();
    }

    private String handleDevice(String op, String deviceType, DevicePushItem item) {
        Device exist = deviceMapper.selectOne(new QueryWrapper<Device>().eq("master_id", item.getId()));
        if ("DELETE".equals(op)) {
            if (exist != null) {
                deviceMapper.deleteById(exist.getId());
            }
            return null;
        }
        // UPSERT / SNAPSHOT
        // 1. 引用校验：categoryId / spaceId 必须在本地存在（按 master_id 查）
        Long categoryId = item.getCategoryId() == null ? null
                : toLocalId(categoryMapper, item.getCategoryId());
        if (item.getCategoryId() != null && categoryId == null) return "类别不存在";
        Long spaceId = item.getSpaceId() == null ? null
                : toLocalId(spaceMapper, item.getSpaceId());
        if (item.getSpaceId() != null && spaceId == null) return "空间不存在";
        // 2. 名称冲突（撞别的 master_id）
        Device nameOwner = deviceMapper.selectOne(
                new QueryWrapper<Device>().eq("device_name", item.getName()).last("limit 1"));
        if (nameOwner != null && (nameOwner.getMasterId() == null
                || !nameOwner.getMasterId().equals(item.getId()))) {
            return "设备名称冲突";
        }
        if (exist == null) {
            Device d = new Device();
            d.setMasterId(item.getId());
            d.setDeviceName(item.getName());
            d.setDeviceCode(item.getName());
            d.setDeviceType(deviceType);
            d.setCategoryId(categoryId);
            d.setSpaceId(spaceId);
            d.setRemark(item.getRemark());
            d.setSort(0);
            deviceMapper.insert(d);
        } else {
            exist.setDeviceName(item.getName());
            exist.setDeviceCode(item.getName());
            exist.setDeviceType(deviceType);
            exist.setCategoryId(categoryId);
            exist.setSpaceId(spaceId);
            exist.setRemark(item.getRemark());
            deviceMapper.updateById(exist);
        }
        return null;
    }

    /** 按 master_id 查本地类别 Long id；查不到返回 null */
    private Long toLocalId(EquipmentCategoryMapper mapper, String masterId) {
        EquipmentCategory c = mapper.selectOne(new QueryWrapper<EquipmentCategory>().eq("master_id", masterId));
        return c == null ? null : c.getId();
    }

    /** 按 master_id 查本地空间 Long id；查不到返回 null */
    private Long toLocalId(SpaceMapper mapper, String masterId) {
        Space s = mapper.selectOne(new QueryWrapper<Space>().eq("master_id", masterId));
        return s == null ? null : s.getId();
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper OM =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private Object convert(Object raw, Class<?> clz) {
        // payload.data 经 Jackson 解析为 LinkedHashMap；用 ObjectMapper 转目标类型
        return OM.convertValue(raw, clz);
    }

    private String extractId(Object raw) {
        if (raw instanceof java.util.Map) {
            Object id = ((java.util.Map<?, ?>) raw).get("id");
            return id == null ? null : id.toString();
        }
        return null;
    }
}
