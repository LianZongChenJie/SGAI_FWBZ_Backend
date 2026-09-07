package org.jeecg.modules.fwbz.centralizedWaterCooling.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

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
                                                   @Param("dayStart") LocalDateTime dayStart);

    /**
     * 批量取多个 tagid 的最新一条历史记录
     * 返回 key=tagId, value=该 tagId 最新一条记录
     */
    @MapKey("tagId")
    @Select("<script>SELECT h.* FROM \"FWBZ\".\"table_cold_source_history\" h "
            + "JOIN (SELECT tag_id, MAX(data_time) AS mx FROM \"FWBZ\".\"table_cold_source_history\" "
            + "       WHERE tag_id IN "
            + "         <foreach collection='tagIds' item='tid' open='(' separator=',' close=')'>"
            + "           #{tid}"
            + "         </foreach>"
            + "       GROUP BY tag_id) m "
            + "  ON h.tag_id = m.tag_id AND h.data_time = m.mx</script>")
    Map<Long, TableColdSourceHistory> selectLatestByTagIds(@Param("tagIds") Collection<Long> tagIds);

    /**
     * 批量取多个 tagid 当天最早的一条历史记录（今日零点基数）
     */
    @MapKey("tagId")
    @Select("<script>SELECT h.* FROM \"FWBZ\".\"table_cold_source_history\" h "
            + "JOIN (SELECT tag_id, MIN(data_time) AS mn FROM \"FWBZ\".\"table_cold_source_history\" "
            + "       WHERE data_time >= #{dayStart} AND tag_id IN "
            + "         <foreach collection='tagIds' item='tid' open='(' separator=',' close=')'>"
            + "           #{tid}"
            + "         </foreach>"
            + "       GROUP BY tag_id) m "
            + "  ON h.tag_id = m.tag_id AND h.data_time = m.mn</script>")
    Map<Long, TableColdSourceHistory> selectFirstTodayByTagIds(@Param("tagIds") Collection<Long> tagIds,
                                                               @Param("dayStart") LocalDateTime dayStart);
}
