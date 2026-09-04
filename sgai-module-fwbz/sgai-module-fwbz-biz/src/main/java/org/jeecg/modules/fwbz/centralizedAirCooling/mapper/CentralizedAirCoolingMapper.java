package org.jeecg.modules.fwbz.centralizedAirCooling.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;

import java.time.LocalDateTime;

/**
 * 集中风冷系统 Mapper（复用冷源历史表 table_cold_source_history）
 */
@Mapper
public interface CentralizedAirCoolingMapper {

    /**
     * 查询指定 tagid 最新的一条历史记录
     */
    @Select("SELECT TOP 1 * FROM \"FWBZ\".\"table_cold_source_history\" "
            + "WHERE tag_id = #{tagId} ORDER BY data_time DESC, id DESC")
    TableColdSourceHistory selectLatestByTagId(@Param("tagId") Long tagId);

    /**
     * 查询指定 tagid 当天最早的一条历史记录（“今天零点”基数）
     */
    @Select("SELECT TOP 1 * FROM \"FWBZ\".\"table_cold_source_history\" "
            + "WHERE tag_id = #{tagId} AND data_time >= #{dayStart} "
            + "ORDER BY data_time ASC, id ASC")
    TableColdSourceHistory selectFirstTodayByTagId(@Param("tagId") Long tagId,
                                                   @Param("dayStart") LocalDateTime dayStart);
}
