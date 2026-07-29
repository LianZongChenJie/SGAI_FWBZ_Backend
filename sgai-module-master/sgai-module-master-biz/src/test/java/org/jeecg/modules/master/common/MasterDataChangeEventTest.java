package org.jeecg.modules.master.common;

import org.jeecg.modules.master.entity.Device;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MasterDataChangeEventTest {

    @Test
    void ofDevices_setsFieldsAndExclude() {
        Device d = new Device();
        d.setId("D1");
        d.setCategoryId("C1");
        List<Device> devices = Arrays.asList(d);

        MasterDataChangeEvent event = MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.CREATE, devices, "SYS_A");

        assertEquals(MasterDataChangeEvent.EntityType.DEVICE, event.getEntityType());
        assertEquals(MasterDataChangeEvent.Op.CREATE, event.getOp());
        assertSame(devices, event.getDevices());
        assertEquals("SYS_A", event.getExcludeSystemCode());
        assertTrue(event.getCategories().isEmpty());
        assertTrue(event.getSpaces().isEmpty());
    }

    @Test
    void ofDevices_nullExclude_allowed() {
        MasterDataChangeEvent event = MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Arrays.asList(), null);
        assertNull(event.getExcludeSystemCode());
    }
}
