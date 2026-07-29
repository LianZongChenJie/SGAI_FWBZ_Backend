package org.jeecg.modules.master.common;

import org.jeecg.common.exception.JeecgBootException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class TreeFullNameHelperTest {

    @Test
    void buildFullName_root_returnsName() {
        assertEquals("楼", TreeFullNameHelper.buildFullName(null, "楼"));
        assertEquals("楼", TreeFullNameHelper.buildFullName("", "楼"));
        assertEquals("楼", TreeFullNameHelper.buildFullName("0", "楼"));
    }

    @Test
    void buildFullName_child_concatenatesWithDash() {
        assertEquals("楼-1F", TreeFullNameHelper.buildFullName("楼", "1F"));
        assertEquals("楼-1F-会议室", TreeFullNameHelper.buildFullName("楼-1F", "会议室"));
    }

    @Test
    void assertMovable_intoSubtree_throws() {
        HashSet<String> subtree = new HashSet<>(Arrays.asList("B", "C", "D"));
        assertThrows(JeecgBootException.class, () -> TreeFullNameHelper.assertMovable(subtree, "B"));
        assertThrows(JeecgBootException.class, () -> TreeFullNameHelper.assertMovable(subtree, "D"));
    }

    @Test
    void assertMovable_outsideSubtree_ok() {
        HashSet<String> subtree = new HashSet<>(Arrays.asList("B", "C", "D"));
        assertDoesNotThrow(() -> TreeFullNameHelper.assertMovable(subtree, "Z"));
        assertDoesNotThrow(() -> TreeFullNameHelper.assertMovable(subtree, null));
    }

    @Test
    void generateUuid_is32CharNoDash() {
        String id = TreeFullNameHelper.generateUuid();
        assertNotNull(id);
        assertEquals(32, id.length());
        assertFalse(id.contains("-"));
    }
}
