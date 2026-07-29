package org.jeecg.modules.master.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 精确匹配命中判定（纯静态，不依赖 Spring / Mapper）。
 *
 * 给定各系统（仅 push_enabled）的类别范围，与本次变更涉及的 categoryId 集合，
 * 返回「类别范围与变更集合有交集且未被排除」的系统 code 集合。
 * 精确匹配——不做子树展开（子树语义已 YAGNI）。
 */
public final class CategoryScopeResolver {

    private CategoryScopeResolver() {
    }

    public static Set<String> resolveHitSystems(Map<String, Set<String>> systemToCategoryIds,
                                                Set<String> targetCategoryIds,
                                                String excludeSystemCode) {
        Set<String> hit = new HashSet<>();
        if (systemToCategoryIds == null || systemToCategoryIds.isEmpty()
                || targetCategoryIds == null || targetCategoryIds.isEmpty()) {
            return hit;
        }
        for (Map.Entry<String, Set<String>> e : systemToCategoryIds.entrySet()) {
            String code = e.getKey();
            if (code != null && code.equals(excludeSystemCode)) {
                continue;
            }
            Set<String> scope = e.getValue();
            if (scope != null && intersects(scope, targetCategoryIds)) {
                hit.add(code);
            }
        }
        return hit;
    }

    private static boolean intersects(Set<String> a, Set<String> b) {
        for (String x : a) {
            if (b.contains(x)) {
                return true;
            }
        }
        return false;
    }
}
