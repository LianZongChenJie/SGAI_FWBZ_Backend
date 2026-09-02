package org.jeecg.modules.fwbz.history.service;

import java.time.LocalDateTime;

/**
 * 设备属性历史数据清理服务
 *
 * @author fwbz
 */
public interface IDeviceAttributeHistoryCleanService {

    /**
     * 清理指定时间之前的历史数据（分批删除，避免大事务锁表）
     *
     * @param cutoff 保留截止时间，早于该时间的记录将被删除
     * @return 本次删除的总条数
     */
    int cleanHistoryBefore(LocalDateTime cutoff);
}
