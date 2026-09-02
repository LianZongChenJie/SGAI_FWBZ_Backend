package org.jeecg.modules.fwbz.interfaceStatistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.interfaceStatistics.entity.InterfaceHistory;
import org.jeecg.modules.fwbz.interfaceStatistics.mapper.InterfaceHistoryMapper;
import org.jeecg.modules.fwbz.interfaceStatistics.service.IInterfaceHistoryService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 接口请求记录 Service 实现
 */
@Slf4j
@Service
public class InterfaceHistoryServiceImpl extends ServiceImpl<InterfaceHistoryMapper, InterfaceHistory>
        implements IInterfaceHistoryService {

    @Override
    public void saveHistory(Long systemId, String interfacePath, Long responseTime, String responseBody) {
        try {
            InterfaceHistory history = new InterfaceHistory();
            history.setSystemId(systemId);
            history.setInterfacePath(interfacePath);
            history.setResponseTime(responseTime);

            Date now = new Date();
            history.setClinetDate(now);
            history.setClinetTime(now);

            if (responseBody != null) {
                long bytes = responseBody.getBytes(StandardCharsets.UTF_8).length;
                history.setDataSize(bytes / 1024.0);
            }

            save(history);
        } catch (Exception e) {
            log.error("保存接口请求历史失败: systemId={}, path={}", systemId, interfacePath, e);
        }
    }

    @Override
    public Double getTodayDataSize(Date date) {
        Double result = getBaseMapper().selectDataSizeSum(date);
        return result == null ? 0.0 : result;
    }
}
