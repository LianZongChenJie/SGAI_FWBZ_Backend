package org.jeecg.modules.fwbz.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.complaint.entity.BuildingControlPointHistory;

import java.util.Date;

/**
 * @Description: 楼控点位采集历史
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Mapper
public interface CBuildingControlPointHistoryMapper extends BaseMapper<BuildingControlPointHistory> {

    /**
     * 统计今日采集条数
     *
     * @param date 日期
     * @return 条数
     */
    @Select("SELECT COUNT(*) FROM building_control_point_history WHERE TRUNC(collection_time) = TRUNC(#{date})")
    Long countToday(@Param("date") Date date);
}
