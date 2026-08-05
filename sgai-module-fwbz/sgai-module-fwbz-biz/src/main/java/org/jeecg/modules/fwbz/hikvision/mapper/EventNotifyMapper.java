package org.jeecg.modules.fwbz.hikvision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.fwbz.hikvision.entity.EventNotify;

/**
 * 事件订阅通知表 Mapper
 *
 * @author fwbz
 */
@Mapper
public interface EventNotifyMapper extends BaseMapper<EventNotify> {

}
