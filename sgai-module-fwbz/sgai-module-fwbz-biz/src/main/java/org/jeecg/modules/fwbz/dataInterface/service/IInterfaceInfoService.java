package org.jeecg.modules.fwbz.dataInterface.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;

import java.util.List;

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

    /**
     * 查询全部接口（含离线），供心跳检测使用
     */
    List<InterfaceInfo> listAll();
}
