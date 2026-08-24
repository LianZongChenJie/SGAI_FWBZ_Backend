package org.jeecg.modules.fwbz.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.fwbz.mqtt.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mqtt.entity.MqttHistory;

import java.util.List;

/**
 * 设备属性 Mapper
 *
 * @author fwbz
 */
@Mapper
public interface MDeviceAttributeMapper extends BaseMapper<DeviceAttribute> {

    /**
     * 根据采集编码（acquisition_coding 对应 MQTT uniqueKey）批量更新采集值和采集时间
     *
     * @param list MQTT数据列表
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE device_attribute " +
            "SET value = CASE acquisition_coding " +
            "<foreach collection='list' item='item'>" +
            "WHEN #{item.uniqueKey} THEN #{item.value} " +
            "</foreach>" +
            "END, " +
            "gather_time = CASE acquisition_coding " +
            "<foreach collection='list' item='item'>" +
            "WHEN #{item.uniqueKey} THEN #{item.timeStamp} " +
            "</foreach>" +
            "END " +
            "WHERE acquisition_coding IN " +
            "<foreach collection='list' item='item' open='(' separator=',' close=')'>" +
            "#{item.uniqueKey}" +
            "</foreach>" +
            "</script>")
    int updateValueByUniqueKeys(@Param("list") List<MqttHistory> list);
}
