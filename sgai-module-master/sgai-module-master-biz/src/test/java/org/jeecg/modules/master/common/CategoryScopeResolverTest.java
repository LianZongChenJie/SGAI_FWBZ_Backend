package org.jeecg.modules.master.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryScopeResolverTest {

    private Map<String, Set<String>> map(String code, String... cats) {
        Map<String, Set<String>> m = new HashMap<>();
        m.put(code, new HashSet<>(Arrays.asList(cats)));
        return m;
    }

    @Test
    void hit_whenCategoryInSystemScope() {
        Map<String, Set<String>> sys = new HashMap<>();
        sys.put("A", new HashSet<>(Arrays.asList("C1", "C2")));
        sys.put("B", new HashSet<>(Collections.singletonList("C9")));

        Set<String> hit = CategoryScopeResolver.resolveHitSystems(
                sys, new HashSet<>(Collections.singletonList("C1")), null);

        assertEquals(new HashSet<>(Collections.singletonList("A")), hit);
    }

    @Test
    void excludeSystemCode_removed() {
        Map<String, Set<String>> sys = new HashMap<>();
        sys.put("A", new HashSet<>(Arrays.asList("C1", "C2")));
        sys.put("B", new HashSet<>(Collections.singletonList("C1")));

        Set<String> hit = CategoryScopeResolver.resolveHitSystems(
                sys, new HashSet<>(Collections.singletonList("C1")), "A");

        assertEquals(new HashSet<>(Collections.singletonList("B")), hit);
    }

    @Test
    void emptyTarget_returnsEmpty() {
        Map<String, Set<String>> sys = map("A", "C1");
        assertTrue(CategoryScopeResolver.resolveHitSystems(sys, Collections.emptySet(), null).isEmpty());
    }

    @Test
    void nullArgs_returnEmpty() {
        assertTrue(CategoryScopeResolver.resolveHitSystems(null, new HashSet<>(Collections.singletonList("C1")), null).isEmpty());
        Map<String, Set<String>> sys = map("A", "C1");
        assertTrue(CategoryScopeResolver.resolveHitSystems(sys, null, null).isEmpty());
    }

    @Test
    void multipleTargets_multipleHits() {
        Map<String, Set<String>> sys = new HashMap<>();
        sys.put("A", new HashSet<>(Arrays.asList("C1")));
        sys.put("B", new HashSet<>(Arrays.asList("C2")));
        sys.put("C", new HashSet<>(Arrays.asList("C3")));

        Set<String> hit = CategoryScopeResolver.resolveHitSystems(
                sys, new HashSet<>(Arrays.asList("C1", "C2")), null);

        assertEquals(new HashSet<>(Arrays.asList("A", "B")), hit);
    }
}
