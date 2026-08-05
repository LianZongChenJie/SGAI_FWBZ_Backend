package org.jeecg.modules.fwbz.hikvision.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventListVO;
import org.jeecg.modules.fwbz.hikvision.dto.DoorEventPageDto;
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

    /**
     * 分页查询门禁点事件列表，支持按人员姓名、门禁点名称、门禁点编码、事件类型、进出类型、卡号、时间范围检索，为空查全部
     *
     * @param dto 分页及查询条件
     * @return 门禁点事件分页列表
     */
    IPage<DoorEventListVO> getEventList(DoorEventPageDto dto);
}
