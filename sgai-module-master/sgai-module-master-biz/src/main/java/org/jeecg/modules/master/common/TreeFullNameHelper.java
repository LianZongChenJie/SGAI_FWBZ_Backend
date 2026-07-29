package org.jeecg.modules.master.common;

import cn.hutool.core.util.IdUtil;
import org.jeecg.common.exception.JeecgBootException;

import java.util.Set;

/**
 * 树形主数据全称算法（纯静态，不依赖 Spring / Mapper）。
 */
public final class TreeFullNameHelper {

    public static final String ROOT_PID = "0";
    public static final String SEPARATOR = "-";

    private TreeFullNameHelper() {
    }

    /** 根(parent 为空 / "0") 返回 name；否则 parentFullName + "-" + name。 */
    public static String buildFullName(String parentFullName, String name) {
        if (parentFullName == null || parentFullName.isEmpty() || ROOT_PID.equals(parentFullName)) {
            return name;
        }
        return parentFullName + SEPARATOR + name;
    }

    /** 若 newPid 落在子树 id 集合内，抛异常（防环：不能移到自身/子级下）。 */
    public static void assertMovable(Set<String> subtreeIds, String newPid) {
        if (newPid != null && subtreeIds.contains(newPid)) {
            throw new JeecgBootException("不能移动到自身或其子级下");
        }
    }

    /** 32 位无横线 uuid。 */
    public static String generateUuid() {
        return IdUtil.simpleUUID();
    }
}
