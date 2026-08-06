package org.jeecg.modules.fwbz.dataCollection.service;

import org.jeecg.modules.fwbz.dataCollection.vo.InterfaceListVO;

import java.util.List;

public interface IDataCollectionService {

    /**
     * 获取系统接口列表（含采集量、完整率、最后采集时间）
     */
    List<InterfaceListVO> getInterfaceList();
}
