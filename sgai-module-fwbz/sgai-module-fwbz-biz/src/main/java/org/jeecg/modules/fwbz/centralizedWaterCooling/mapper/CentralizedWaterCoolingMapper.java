package org.jeecg.modules.fwbz.centralizedWaterCooling.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;

/**
 * 集中水冷系统 Mapper（复用冷源历史表 table_cold_source_history）
 */
@Mapper
public interface CentralizedWaterCoolingMapper {

    /**
     * 查询指定 tagid 最新的一条历史记录
     *
     * @param tagId 采集点id
     * @return 最新一条记录（无数据返回 null）
     */
    @Select("SELECT TOP 1 * FROM \"FWBZ\".\"table_cold_source_history\" "
            + "WHERE tag_id = #{tagId} ORDER BY data_time DESC, id DESC")
    TableColdSourceHistory selectLatestByTagId(@Param("tagId") Long tagId);

    /**
     * 查询指定 tagid 当天最早的一条历史记录（即“今天零点”的基数）
     *
     * @param tagId     采集点id
     * @param dayStart  当天零点时间
     * @return 当天最早一条记录（无数据返回 null）
     */
    @Select("SELECT TOP 1 * FROM \"FWBZ\".\"table_cold_source_history\" "
            + "WHERE tag_id = #{tagId} AND data_time >= #{dayStart} "
            + "ORDER BY data_time ASC, id ASC")
    TableColdSourceHistory selectFirstTodayByTagId(@Param("tagId") Long tagId,
                                                   @Param("dayStart") java.time.LocalDateTime dayStart);
}
