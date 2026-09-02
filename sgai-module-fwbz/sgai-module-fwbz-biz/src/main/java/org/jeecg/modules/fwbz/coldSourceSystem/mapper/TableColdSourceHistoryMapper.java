package org.jeecg.modules.fwbz.coldSourceSystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceHistoryPageDto;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 冷源系统存储数据表 Mapper
 */
@Mapper
public interface TableColdSourceHistoryMapper extends BaseMapper<TableColdSourceHistory> {

    /**
     * 分页查询冷源历史记录（关联 table_tagid_info 取描述）
     *
     * @param page      分页参数
     * @param tagId     采集点id（精确匹配，可为空）
     * @param descName  描述（模糊匹配，可为空）
     * @param startTime 采集时间-开始（可为空）
     * @param endTime   采集时间-结束（可为空）
     * @return tagId / desc / dataTime(采集时间) / value(值)
     */
    @Select("<script>"
            + "SELECT h.tag_id AS tagId, t.\"desc\" AS \"desc\", h.data_time AS dataTime, h.value AS value "
            + "FROM \"FWBZ\".\"table_cold_source_history\" h "
            + "LEFT JOIN \"FWBZ\".\"table_tagid_info\" t ON h.tag_id = t.tag_id "
            + "<where>"
            + "  <if test='tagId != null'> AND h.tag_id = #{tagId} </if>"
            + "  <if test='descName != null and descName != \"\"'> AND t.\"desc\" LIKE CONCAT('%', #{descName}, '%') </if>"
            + "  <if test='startTime != null'> AND h.data_time &gt;= #{startTime} </if>"
            + "  <if test='endTime != null'> AND h.data_time &lt;= #{endTime} </if>"
            + "</where>"
            + "ORDER BY h.data_time DESC"
            + "</script>")
    IPage<ColdSourceHistoryPageDto> selectHistoryPage(Page<ColdSourceHistoryPageDto> page,
                                                      @Param("tagId") Long tagId,
                                                      @Param("descName") String descName,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查询冷源历史记录（不分页，按条件查全部，用于导出）
     *
     * @param tagId     采集点id（精确匹配，可为空）
     * @param descName  描述（模糊匹配，可为空）
     * @param startTime 采集时间-开始（可为空）
     * @param endTime   采集时间-结束（可为空）
     * @return tagId / desc / dataTime(采集时间) / value(值)
     */
    @Select("<script>"
            + "SELECT h.tag_id AS tagId, t.\"desc\" AS \"desc\", h.data_time AS dataTime, h.value AS value "
            + "FROM \"FWBZ\".\"table_cold_source_history\" h "
            + "LEFT JOIN \"FWBZ\".\"table_tagid_info\" t ON h.tag_id = t.tag_id "
            + "<where>"
            + "  <if test='tagId != null'> AND h.tag_id = #{tagId} </if>"
            + "  <if test='descName != null and descName != \"\"'> AND t.\"desc\" LIKE CONCAT('%', #{descName}, '%') </if>"
            + "  <if test='startTime != null'> AND h.data_time &gt;= #{startTime} </if>"
            + "  <if test='endTime != null'> AND h.data_time &lt;= #{endTime} </if>"
            + "</where>"
            + "ORDER BY h.data_time DESC"
            + "</script>")
    List<ColdSourceHistoryPageDto> selectHistoryList(@Param("tagId") Long tagId,
                                                    @Param("descName") String descName,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);
}
