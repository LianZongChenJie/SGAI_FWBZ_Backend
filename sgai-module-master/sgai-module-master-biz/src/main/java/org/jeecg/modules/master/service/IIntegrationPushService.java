package org.jeecg.modules.master.service;

import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.vo.IntegrationPayload;
import org.jeecg.modules.master.vo.PushSnapshotResult;

import java.util.List;

public interface IIntegrationPushService {

    /** 实时增量：向单系统推送一条已组装报文，写 PUSH 日志。 */
    void pushOne(IntegrationSystem system, IntegrationPayload payload);

    /** 手动全量：对该系统发 3 次快照推送（空间/类别/设备），各写日志，返回 3 条结果。 */
    List<PushSnapshotResult> pushSnapshotForSystem(String systemId);
}
