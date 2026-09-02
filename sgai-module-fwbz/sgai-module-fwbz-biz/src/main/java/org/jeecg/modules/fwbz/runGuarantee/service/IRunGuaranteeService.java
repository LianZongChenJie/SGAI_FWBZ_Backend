package org.jeecg.modules.fwbz.runGuarantee.service;

import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
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

    /**
     * 获取链路总数（系统总数）
     * @return 统计卡片
     */
    StatCardVO getLinkTotal();

    /**
     * 获取正常链路（消息总数）
     * @return 统计卡片
     */
    StatCardVO getNormalLink();

    /**
     * 获取数据采集状态（控制总数）
     * @return 统计卡片
     */
    StatCardVO getCollectionStatus();

    /**
     * 获取数据处理状态（照明控制数）
     * @return 统计卡片
     */
    StatCardVO getProcessingStatus();

    /**
     * 获取运行保障汇总统计
     * @return 统计卡片列表
     */
    List<StatCardVO> getSummary();
}
