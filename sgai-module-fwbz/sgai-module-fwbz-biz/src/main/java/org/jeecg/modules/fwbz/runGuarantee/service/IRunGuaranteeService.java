package org.jeecg.modules.fwbz.runGuarantee.service;

import org.jeecg.modules.fwbz.runGuarantee.vo.SystemDeviceStatVO;

import java.util.List;

/**
 * 运行保障服务接口
 */
public interface IRunGuaranteeService {

    /**
     * 获取各系统设备在线统计
     * @return 各系统设备统计列表
     */
    List<SystemDeviceStatVO> getDeviceStat();
}
