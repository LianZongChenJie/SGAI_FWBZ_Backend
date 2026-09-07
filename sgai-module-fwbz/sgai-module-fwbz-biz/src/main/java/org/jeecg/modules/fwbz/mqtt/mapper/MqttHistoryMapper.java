package org.jeecg.modules.fwbz.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.fwbz.mqtt.entity.MqttHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MQTT低压配电数据 Mapper
 * <p>注意：desc 为达梦数据库保留字，批量插入时使用双引号转义列名。</p>
 *
 * @author fwbz
 */
@Mapper
public interface MqttHistoryMapper extends BaseMapper<MqttHistory> {

    /**
     * 批量插入MQTT低压配电数据
     *
     * @param list 数据列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO table_mqtt_history (device_id, time_stamp, attribute_id, \"desc\", value) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.deviceId}, #{item.timeStamp}, #{item.attributeId}, #{item.desc}, #{item.value})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<MqttHistory> list);

    /**
     * 批量查询指定设备属性在15分钟槽位时间段 [timeStamp, slotEnd) 内已存在的历史记录
     * <p>注意：desc 为达梦保留字，必须转义；时间范围使用半开区间，避免槽位边界重叠。</p>
     *
     * @param list 数据列表（须含 deviceId、attributeId、timeStamp、slotEnd）
     * @return 已存在的历史记录
     */
    @Select("<script>" +
            "SELECT id, device_id, time_stamp, attribute_id, \"desc\", value FROM table_mqtt_history WHERE " +
            "<foreach collection='list' item='item' separator=' OR '>" +
            "(device_id = #{item.deviceId} AND attribute_id = #{item.attributeId} " +
            "AND time_stamp &gt;= #{item.timeStamp} AND time_stamp &lt; #{item.slotEnd})" +
            "</foreach>" +
            "</script>")
    List<MqttHistory> selectBySlotList(@Param("list") List<MqttHistory> list);

    /**
     * 按主键批量更新MQTT低压配电数据的测点描述与遥测值（同一槽位已有历史时覆盖）
     *
     * @param list 数据列表（须含 id）
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE table_mqtt_history " +
            "<set>" +
            "\"desc\" = CASE id " +
            "<foreach collection='list' item='item'>" +
            "WHEN #{item.id} THEN #{item.desc} " +
            "</foreach>" +
            "END, " +
            "value = CASE id " +
            "<foreach collection='list' item='item'>" +
            "WHEN #{item.id} THEN #{item.value} " +
            "</foreach>" +
            "END " +
            "</set>" +
            "WHERE id IN " +
            "<foreach collection='list' item='item' open='(' separator=',' close=')'>" +
            "#{item.id}" +
            "</foreach>" +
            "</script>")
    int updateBatch(@Param("list") List<MqttHistory> list);

    /**
     * 按设备ID列表 + desc 模糊匹配 + 时间范围查询遥测历史（用于趋势图）
     * <p>desc 为达梦保留字，必须转义；desc 模糊搜索用于匹配"总有功功率"等测点含义。</p>
     *
     * @param deviceIds   设备ID列表（可为空，为空时不限制设备）
     * @param descKeyword 测点含义关键字（模糊匹配）
     * @param startTime   起始时间（可为空）
     * @param endTime     结束时间（可为空）
     * @return 遥测历史记录（按时间升序）
     */
    @Select("<script>" +
            "SELECT id, device_id, time_stamp, attribute_id, \"desc\", value FROM table_mqtt_history " +
            "<where>" +
            "  <if test='descKeyword != null and descKeyword != \"\"'> AND \"desc\" LIKE CONCAT('%', #{descKeyword}, '%') </if>" +
            "  <if test='deviceIds != null and deviceIds.size() > 0'>" +
            "    AND device_id IN " +
            "    <foreach collection='deviceIds' item='did' open='(' separator=',' close=')'>#{did}</foreach>" +
            "  </if>" +
            "  <if test='startTime != null'> AND time_stamp &gt;= #{startTime} </if>" +
            "  <if test='endTime != null'> AND time_stamp &lt;= #{endTime} </if>" +
            "</where>" +
            "ORDER BY time_stamp ASC" +
            "</script>")
    List<MqttHistory> selectActivePowerHistory(@Param("deviceIds") List<Long> deviceIds,
                                               @Param("descKeyword") String descKeyword,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
