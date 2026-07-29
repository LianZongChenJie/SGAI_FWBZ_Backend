package org.jeecg.modules.master.common;

import lombok.Getter;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.entity.Space;

import java.util.Collections;
import java.util.List;

/**
 * 主数据变更事件（通用载体）。在主数据增删改事务内发布，
 * 由 @TransactionalEventListener(AFTER_COMMIT) + @Async 监听器做实时增量推送。
 *
 * affected* 列表携带完整实体字段（设备 categoryId、类别/空间 id 等），
 * 监听器据此做命中判定，无需回查库。
 * excludeSystemCode：仅 hub 接收分发时填（=来源系统 code），本地写入场景为 null。
 */
@Getter
public class MasterDataChangeEvent {

    public enum EntityType { CATEGORY, SPACE, DEVICE }
    public enum Op { CREATE, UPDATE, DELETE }

    private final EntityType entityType;
    private final Op op;
    private final List<Device> devices;
    private final List<DeviceCategory> categories;
    private final List<Space> spaces;
    private final String excludeSystemCode;

    private MasterDataChangeEvent(EntityType entityType, Op op,
                                  List<Device> devices,
                                  List<DeviceCategory> categories,
                                  List<Space> spaces,
                                  String excludeSystemCode) {
        this.entityType = entityType;
        this.op = op;
        this.devices = devices == null ? Collections.emptyList() : devices;
        this.categories = categories == null ? Collections.emptyList() : categories;
        this.spaces = spaces == null ? Collections.emptyList() : spaces;
        this.excludeSystemCode = excludeSystemCode;
    }

    public static MasterDataChangeEvent ofDevices(Op op, List<Device> devices, String excludeSystemCode) {
        return new MasterDataChangeEvent(EntityType.DEVICE, op, devices, null, null, excludeSystemCode);
    }

    public static MasterDataChangeEvent ofCategories(Op op, List<DeviceCategory> categories, String excludeSystemCode) {
        return new MasterDataChangeEvent(EntityType.CATEGORY, op, null, categories, null, excludeSystemCode);
    }

    public static MasterDataChangeEvent ofSpaces(Op op, List<Space> spaces, String excludeSystemCode) {
        return new MasterDataChangeEvent(EntityType.SPACE, op, null, null, spaces, excludeSystemCode);
    }
}
