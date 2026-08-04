package org.jeecg.modules.fwbz.hikvision.service;

import org.jeecg.modules.fwbz.hikvision.dto.EventNotifyPushRequest;

/**
 * 海康事件通知服务接口
 * <p>接收海康平台推送的事件，解析并存入数据库。</p>
 *
 * @author fwbz
 */
public interface IEventNotifyService {

    /**
     * 处理海康推送事件
     * <p>解析推送JSON，将每个事件转换为EventNotify实体并批量存入数据库。</p>
     *
     * @param pushRequest 海康事件推送请求体
     * @return 成功保存的事件数量
     */
    int handleEventNotify(EventNotifyPushRequest pushRequest);
}
