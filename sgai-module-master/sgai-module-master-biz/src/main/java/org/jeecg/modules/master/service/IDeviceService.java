package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.vo.DeviceImportDTO;
import org.jeecg.modules.master.vo.DeviceVO;

import java.util.List;

public interface IDeviceService extends IService<Device> {

    IPage<DeviceVO> pageVO(Page<Device> page, String name, String categoryId, String spaceId);

    List<DeviceVO> listForExport(String name, String categoryId, String spaceId);

    void create(Device entity);

    void updateNode(Device entity);

    void removeBatch(List<String> ids);

    /** 导入：按全称解析类别/空间，校验唯一与存在，返回失败行信息。 */
    List<String> batchImport(List<DeviceImportDTO> rows);

    /** 接收：按传入 id upsert（不复用 create/update，避免重复发普通事件+重生成 id）。校验引用存在 + 名称冲突，成功后发 hub 事件。 */
    void upsertFromIntegration(Device incoming, String excludeSystemCode);

    /** 接收：按 id 物理删（本地不存在也发，幂等），发 hub DELETE 事件（incoming.categoryId 供过滤）。 */
    void deleteFromIntegration(Device incoming, String excludeSystemCode);
}
