package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.hikvision.entity.DoorEvent;

/**
 * 门禁点事件同步服务接口
 *
 * @author fwbz
 */
public interface IDoorEventService extends IService<DoorEvent> {

    /**
     * 从海康平台增量拉取门禁点事件并同步到数据库
     * <p>以数据库中最新事件时间为 startTime，当前时间为 endTime，逐页拉取。
     * 已有事件（按 event_id 判断）不会重复插入。</p>
     *
     * @return 新增的事件记录数
     */
    int syncFromHikvision();
}
