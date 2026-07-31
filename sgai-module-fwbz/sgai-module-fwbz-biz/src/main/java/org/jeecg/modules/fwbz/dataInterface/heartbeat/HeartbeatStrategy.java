package org.jeecg.modules.fwbz.dataInterface.heartbeat;

/**
 * 心跳检测策略接口
 * <p>
 * 不同协议类型实现各自的检测逻辑
 */
public interface HeartbeatStrategy {

    /**
     * 执行心跳检测
     *
     * @param address 接口地址
     * @return 检测结果
     */
    HeartbeatResult check(String address);
}
