package org.jeecg.modules.fwbz.dataInterface.job;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.dataInterface.entity.InterfaceInfo;
import org.jeecg.modules.fwbz.dataInterface.heartbeat.HeartbeatResult;
import org.jeecg.modules.fwbz.dataInterface.heartbeat.HeartbeatStrategy;
import org.jeecg.modules.fwbz.dataInterface.heartbeat.HeartbeatStrategyFactory;
import org.jeecg.modules.fwbz.dataInterface.heartbeat.TcpHeartbeatStrategy;
import org.jeecg.modules.fwbz.dataInterface.service.IInterfaceInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 接口心跳检测定时任务
 * <p>
 * 定时轮询所有被监控的接口，根据协议类型选择对应策略检测在线/离线/异常状态，
 * 并更新最后心跳时间(requestTime)和响应时间(responseTime)。
 * <p>
 * 协议-策略映射：
 * <ul>
 *   <li>HTTP API(1)  → HTTP GET 请求</li>
 *   <li>MQTT(2)      → TCP Socket :1883</li>
 *   <li>BACnet(3)    → TCP Socket :47808</li>
 *   <li>Modbus TCP(4)→ TCP Socket :502</li>
 *   <li>OPC UA(5)    → TCP Socket :4840</li>
 * </ul>
 */
@Slf4j
@Component
@AllArgsConstructor
public class InterfaceHeartbeatJob {

    private final IInterfaceInfoService interfaceInfoService;
    private final HeartbeatStrategyFactory strategyFactory;

    /**
     * 每分钟执行一次心跳检测
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void execute() {
        log.debug("接口心跳检测定时任务开始执行");

        List<InterfaceInfo> monitorList = interfaceInfoService.listAll();

        if (monitorList.isEmpty()) {
            log.debug("无接口数据");
            return;
        }

        log.info("开始心跳检测，共 {} 个接口", monitorList.size());

        for (InterfaceInfo info : monitorList) {
            try {
                doHeartbeat(info);
            } catch (Exception e) {
                log.error("接口心跳检测异常 - ID: {}, 系统: {}, 地址: {}",
                        info.getId(), info.getSysName(), info.getInterfacePath(), e);
            }
        }

        log.debug("接口心跳检测定时任务执行完毕");
    }

    /**
     * 对单个接口执行心跳检测
     */
    private void doHeartbeat(InterfaceInfo info) {
        String address = info.getInterfacePath();
        if (address == null || address.trim().isEmpty()) {
            log.warn("接口地址为空，跳过 - ID: {}", info.getId());
            return;
        }

        // 根据协议类型选择策略
        HeartbeatStrategy strategy = strategyFactory.getStrategy(info.getProtocolTypeId());
        if (strategy == null) {
            log.debug("无对应心跳策略，跳过 - ID: {}, 协议类型: {}", info.getId(), info.getProtocolTypeId());
            return;
        }

        // 执行心跳检测
        HeartbeatResult result;
        if (strategy instanceof TcpHeartbeatStrategy) {
            result = ((TcpHeartbeatStrategy) strategy).check(address, info.getProtocolTypeId());
        } else {
            result = strategy.check(address);
        }

        // 更新数据库
        Date now = new Date();
        Integer newState = mapToState(result.getStatus());

        updateHeartbeatResult(info.getId(), newState, result.getResponseTime(), now);

        // 状态变更时记录警告日志
        if (!newState.equals(info.getState())) {
            log.warn("接口状态变更 - ID: {}, 系统: {}, 地址: {}, 协议: {}, {} → {}",
                    info.getId(), info.getSysName(), address,
                    info.getProtocolTypeId(),
                    stateName(info.getState()), stateName(newState));
        }
    }

    /**
     * 将 HeartbeatResult.Status 映射为 InterfaceInfo state 值
     */
    private Integer mapToState(HeartbeatResult.Status status) {
        switch (status) {
            case ONLINE:
                return InterfaceInfo.STATE_ONLINE;
            case OFFLINE:
                return InterfaceInfo.STATE_OFFLINE;
            case ABNORMAL:
                return InterfaceInfo.STATE_ABNORMAL;
            default:
                return InterfaceInfo.STATE_OFFLINE;
        }
    }

    /**
     * 状态名称（日志用）
     */
    private String stateName(Integer state) {
        if (InterfaceInfo.STATE_ONLINE.equals(state)) return "在线";
        if (InterfaceInfo.STATE_OFFLINE.equals(state)) return "离线";
        if (InterfaceInfo.STATE_ABNORMAL.equals(state)) return "异常";
        return "未知";
    }

    /**
     * 更新心跳检测结果到数据库
     */
    private void updateHeartbeatResult(Long id, Integer state, long responseTime, Date requestTime) {
        interfaceInfoService.update(new LambdaUpdateWrapper<InterfaceInfo>()
                .set(InterfaceInfo::getState, state)
                .set(InterfaceInfo::getResponseTime, responseTime)
                .set(InterfaceInfo::getRequestTime, requestTime)
                .eq(InterfaceInfo::getId, id));
    }
}
