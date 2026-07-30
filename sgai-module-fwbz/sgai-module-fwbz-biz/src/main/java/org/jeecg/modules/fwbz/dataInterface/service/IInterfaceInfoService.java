package org.jeecg.modules.fwbz.dataInterface.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;

public interface IInterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 分页查询
     */
    IPage<InterfaceInfo> listPage(InterfaceInfo params);

    /**
     * 启用
     */
    void enable(Long id);

    /**
     * 停用
     */
    void disable(Long id);
}
