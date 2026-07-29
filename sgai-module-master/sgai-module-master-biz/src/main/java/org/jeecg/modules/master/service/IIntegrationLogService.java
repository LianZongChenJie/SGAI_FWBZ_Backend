package org.jeecg.modules.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.master.entity.IntegrationLog;

public interface IIntegrationLogService extends IService<IntegrationLog> {

    /** 写一条对接日志（独立小事务 REQUIRES_NEW，与主数据写入隔离）。 */
    void writeLog(IntegrationLog log);
}
