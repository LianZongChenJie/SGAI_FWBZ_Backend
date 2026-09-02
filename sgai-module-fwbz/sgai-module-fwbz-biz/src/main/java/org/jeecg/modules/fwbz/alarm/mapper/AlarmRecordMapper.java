package org.jeecg.modules.fwbz.alarm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.alarm.entity.AlarmRecord;
@Mapper
public interface AlarmRecordMapper extends BaseMapper<AlarmRecord> {
    /**
     * 统计今日告警条数
     *
     * @param date 日期
     * @return 条数
     */
    @Select("SELECT COUNT(*) FROM alarm_record WHERE TRUNC(alarm_time) = TRUNC(#{date})")
    Long countToday(@Param("date") java.util.Date date);
}
