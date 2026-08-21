package org.jeecg.modules.fwbz.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.fwbz.mqtt.entity.MqttHistory;

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
            "INSERT INTO table_mqtt_history (dev_keys, time_stamp, unique_key, \"desc\", value) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.devKeys}, #{item.timeStamp}, #{item.uniqueKey}, #{item.desc}, #{item.value})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<MqttHistory> list);
}
