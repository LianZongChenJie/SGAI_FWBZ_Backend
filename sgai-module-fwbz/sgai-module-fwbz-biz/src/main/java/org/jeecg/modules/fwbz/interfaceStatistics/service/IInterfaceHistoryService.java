package org.jeecg.modules.fwbz.interfaceStatistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.interfaceStatistics.entity.InterfaceHistory;

import java.util.Date;

/**
 * 接口请求记录 Service
 */
public interface IInterfaceHistoryService extends IService<InterfaceHistory> {

    /**
     * 保存接口请求历史记录
     *
     * @param systemId     所属系统ID
     * @param interfacePath 接口地址
     * @param responseTime 响应时间(ms)
     * @param responseBody 响应体内容（用于计算数据大小，可为null）
     */
    void saveHistory(Long systemId, String interfacePath, Long responseTime, String responseBody);

    /**
     * 统计指定日期的数据量（KB）
     *
     * @param date 日期
     * @return 数据量总和，无记录时返回 0
     */
    Double getTodayDataSize(Date date);
}
