package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.hikvision.dto.EventNotifyPushRequest;
import org.jeecg.modules.fwbz.hikvision.entity.EventNotify;

/**
 * 海康事件通知服务接口
 * <p>接收海康平台推送的事件，解析并存入数据库；支持分页查询。</p>
 *
 * @author fwbz
 */
public interface IEventNotifyService extends IService<EventNotify> {

    /**
     * 处理海康推送事件
     * <p>解析推送JSON，将每个事件转换为EventNotify实体并批量存入数据库。</p>
     *
     * @param pushRequest 海康事件推送请求体
     * @return 成功保存的事件数量
     */
    int handleEventNotify(EventNotifyPushRequest pushRequest);

    /**
     * 分页查询事件通知记录，支持按事件类型、状态、等级、事件源、时间范围等筛选
     *
     * @param pageNo        页码，从1开始
     * @param pageSize      每页条数
     * @param ability       事件类别（如：视频事件），为空查全部
     * @param eventType     事件类型，数值编码
     * @param status        事件状态：0-瞬时 1-开始 2-停止
     * @param eventLvl      事件等级：0-未配置 1-低 2-中 3-高
     * @param srcIndex      事件源编号，精确匹配
     * @param srcName       事件源名称，模糊匹配
     * @param srcType       事件源类型
     * @param happenTimeStart 事件发生开始时间
     * @param happenTimeEnd   事件发生结束时间
     * @return 事件通知分页列表
     */
    IPage<EventNotify> getEventNotifyList(int pageNo, int pageSize,
                                           String ability, Integer eventType,
                                           Integer status, Integer eventLvl,
                                           String srcIndex, String srcName, String srcType,
                                           String happenTimeStart, String happenTimeEnd);
}
