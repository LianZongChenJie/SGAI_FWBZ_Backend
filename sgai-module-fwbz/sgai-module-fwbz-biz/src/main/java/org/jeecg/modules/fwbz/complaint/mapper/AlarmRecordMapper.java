package org.jeecg.modules.fwbz.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.complaint.entity.AlarmRecord;

/**
 * @Description: 设备告警记录
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
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
