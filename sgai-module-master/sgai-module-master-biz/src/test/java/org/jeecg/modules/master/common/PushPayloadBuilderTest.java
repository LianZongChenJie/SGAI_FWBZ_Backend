package org.jeecg.modules.master.common;

import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.vo.IntegrationPayload;
import org.jeecg.modules.master.vo.DevicePushItem;
import org.jeecg.modules.master.vo.CategoryPushItem;
import org.jeecg.modules.master.vo.SpacePushItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PushPayloadBuilderTest {

    @Test
    void devices_mapsFields() {
        Device d = new Device();
        d.setId("D1"); d.setName("设备A"); d.setCategoryId("C1"); d.setSpaceId("S1"); d.setRemark("r");

        IntegrationPayload p = PushPayloadBuilder.devices(
                "SYS_A", IntegrationPayload.Op.UPSERT, "BATCH1", Collections.singletonList(d));

        assertEquals("sgai-master", p.getSource());
        assertEquals("SYS_A", p.getSystemCode());
        assertEquals(IntegrationPayload.Type.DEVICE, p.getType());
        assertEquals(IntegrationPayload.Op.UPSERT, p.getOp());
        assertEquals("BATCH1", p.getBatchId());
        assertEquals(1, p.dataCount());
        DevicePushItem item = (DevicePushItem) p.getData().get(0);
        assertEquals("D1", item.getId());
        assertEquals("设备A", item.getName());
        assertEquals("C1", item.getCategoryId());
        assertEquals("S1", item.getSpaceId());
        assertEquals("r", item.getRemark());
    }

    @Test
    void categories_mapsFields() {
        DeviceCategory c = new DeviceCategory();
        c.setId("C1"); c.setName("电气"); c.setFullName("建筑-电气"); c.setPid("C0");

        IntegrationPayload p = PushPayloadBuilder.categories(
                "SYS_A", IntegrationPayload.Op.UPSERT, "B1", Collections.singletonList(c));

        assertEquals(IntegrationPayload.Type.CATEGORY, p.getType());
        CategoryPushItem item = (CategoryPushItem) p.getData().get(0);
        assertEquals("C1", item.getId());
        assertEquals("电气", item.getName());
        assertEquals("建筑-电气", item.getFullName());
        assertEquals("C0", item.getPid());
    }

    @Test
    void spaces_mapsFields() {
        Space s = new Space();
        s.setId("S1"); s.setName("一楼"); s.setFullName("园区-一楼"); s.setPid("S0");

        IntegrationPayload p = PushPayloadBuilder.spaces(
                "SYS_A", IntegrationPayload.Op.SNAPSHOT, "B1", Collections.singletonList(s));

        assertEquals(IntegrationPayload.Type.SPACE, p.getType());
        assertEquals(IntegrationPayload.Op.SNAPSHOT, p.getOp());
        SpacePushItem item = (SpacePushItem) p.getData().get(0);
        assertEquals("S1", item.getId());
        assertEquals("园区-一楼", item.getFullName());
    }

    @Test
    void delete_carriesFullItemWithCategoryId() {
        Device d = new Device();
        d.setId("D1"); d.setCategoryId("C1");
        IntegrationPayload p = PushPayloadBuilder.devices(
                "SYS_A", IntegrationPayload.Op.DELETE, "B1", Collections.singletonList(d));
        DevicePushItem item = (DevicePushItem) p.getData().get(0);
        assertEquals("C1", item.getCategoryId());
    }
}
