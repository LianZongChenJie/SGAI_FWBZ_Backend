package org.jeecg.modules.master.service;

import org.jeecg.modules.master.vo.ReceivePayload;
import org.jeecg.modules.master.vo.ReceiveResult;

public interface IIntegrationReceiveService {

    /** 接收外部设备推送：鉴权 + 类别过滤 + 逐条 upsert/delete + hub。鉴权失败抛 JeecgBootException。 */
    ReceiveResult receive(ReceivePayload payload, String token);
}
