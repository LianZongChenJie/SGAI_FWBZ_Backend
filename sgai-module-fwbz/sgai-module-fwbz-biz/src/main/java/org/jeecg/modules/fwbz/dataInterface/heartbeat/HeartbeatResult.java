package org.jeecg.modules.fwbz.dataInterface.heartbeat;

/**
 * 心跳检测结果
 */
public class HeartbeatResult {

    public enum Status {
        /**
         * 在线：检测正常响应
         */
        ONLINE,
        /**
         * 离线：连接失败或不可达
         */
        OFFLINE,
        /**
         * 异常：有响应但不正常（超时、错误码等）
         */
        ABNORMAL
    }

    private final Status status;
    private final long responseTime;

    private HeartbeatResult(Status status, long responseTime) {
        this.status = status;
        this.responseTime = responseTime;
    }

    public static HeartbeatResult online(long responseTime) {
        return new HeartbeatResult(Status.ONLINE, responseTime);
    }

    public static HeartbeatResult offline(long responseTime) {
        return new HeartbeatResult(Status.OFFLINE, responseTime);
    }

    public static HeartbeatResult abnormal(long responseTime) {
        return new HeartbeatResult(Status.ABNORMAL, responseTime);
    }

    public Status getStatus() {
        return status;
    }

    public long getResponseTime() {
        return responseTime;
    }
}
