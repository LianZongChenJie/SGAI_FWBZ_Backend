package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.master.entity.DeviceCategory;

import java.util.List;

public interface IDeviceCategoryService extends IService<DeviceCategory> {

    /** 扁平全量列表（name 可选模糊）。 */
    List<DeviceCategory> listAll(String name);

    /** 新增（生成 uuid、计算 full_name、同层重名校验）。 */
    void create(DeviceCategory entity);

    /** 编辑/移动（pid 变化即移动；pid 或 name 变化触发子树全称重算）。 */
    void updateNode(DeviceCategory entity);

    /** 删除（有子节点或被设备引用时拒绝）。 */
    void removeNode(String id);
}
