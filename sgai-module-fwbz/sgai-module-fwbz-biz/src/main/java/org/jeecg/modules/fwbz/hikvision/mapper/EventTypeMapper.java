package org.jeecg.modules.fwbz.hikvision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.fwbz.hikvision.entity.EventType;

/**
 * 海康事件类型字典表 Mapper
 *
 * @author fwbz
 */
@Mapper
public interface EventTypeMapper extends BaseMapper<EventType> {

}
