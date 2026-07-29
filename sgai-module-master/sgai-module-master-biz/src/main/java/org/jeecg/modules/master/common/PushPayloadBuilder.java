package org.jeecg.modules.master.common;

import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.vo.CategoryPushItem;
import org.jeecg.modules.master.vo.DevicePushItem;
import org.jeecg.modules.master.vo.IntegrationPayload;
import org.jeecg.modules.master.vo.SpacePushItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 组装统一推送/接收报文（纯静态，不依赖 Spring / Mapper）。
 * 实体 → PushItem 转换；DELETE 时仍带完整项（含 categoryId），便于下游过滤/分发判定。
 */
public final class PushPayloadBuilder {

    private PushPayloadBuilder() {
    }

    public static IntegrationPayload devices(String systemCode, IntegrationPayload.Op op,
                                             String batchId, List<Device> devices) {
        IntegrationPayload p = base(systemCode, op, batchId, IntegrationPayload.Type.DEVICE);
        List<Object> data = new ArrayList<>();
        if (devices != null) {
            for (Device d : devices) {
                DevicePushItem item = new DevicePushItem();
                item.setId(d.getId());
                item.setName(d.getName());
                item.setCategoryId(d.getCategoryId());
                item.setSpaceId(d.getSpaceId());
                item.setRemark(d.getRemark());
                data.add(item);
            }
        }
        p.setData(data);
        return p;
    }

    public static IntegrationPayload categories(String systemCode, IntegrationPayload.Op op,
                                                String batchId, List<DeviceCategory> categories) {
        IntegrationPayload p = base(systemCode, op, batchId, IntegrationPayload.Type.CATEGORY);
        List<Object> data = new ArrayList<>();
        if (categories != null) {
            for (DeviceCategory c : categories) {
                CategoryPushItem item = new CategoryPushItem();
                item.setId(c.getId());
                item.setName(c.getName());
                item.setFullName(c.getFullName());
                item.setPid(c.getPid());
                data.add(item);
            }
        }
        p.setData(data);
        return p;
    }

    public static IntegrationPayload spaces(String systemCode, IntegrationPayload.Op op,
                                           String batchId, List<Space> spaces) {
        IntegrationPayload p = base(systemCode, op, batchId, IntegrationPayload.Type.SPACE);
        List<Object> data = new ArrayList<>();
        if (spaces != null) {
            for (Space s : spaces) {
                SpacePushItem item = new SpacePushItem();
                item.setId(s.getId());
                item.setName(s.getName());
                item.setFullName(s.getFullName());
                item.setPid(s.getPid());
                data.add(item);
            }
        }
        p.setData(data);
        return p;
    }

    private static IntegrationPayload base(String systemCode, IntegrationPayload.Op op,
                                          String batchId, IntegrationPayload.Type type) {
        IntegrationPayload p = new IntegrationPayload();
        p.setSystemCode(systemCode);
        p.setOp(op);
        p.setBatchId(batchId);
        p.setType(type);
        p.setData(Collections.emptyList());
        return p;
    }
}
