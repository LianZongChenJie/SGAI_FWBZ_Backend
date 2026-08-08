package org.jeecg.modules.fwbz.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.complaint.entity.BuildingControlPointSendHistory;

/**
 * @Description: 楼控点位下发历史
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Mapper
public interface BuildingControlPointSendHistoryMapper extends BaseMapper<BuildingControlPointSendHistory> {

    /**
     * 统计今日下发条数
     *
     * @param date 日期
     * @return 条数
     */
    @Select("SELECT COUNT(*) FROM building_control_point_send_history WHERE TRUNC(collection_time) = TRUNC(#{date})")
    Long countToday(@Param("date") java.util.Date date);
}
