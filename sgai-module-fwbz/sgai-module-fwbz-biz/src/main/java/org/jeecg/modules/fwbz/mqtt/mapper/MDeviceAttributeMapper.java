package org.jeecg.modules.fwbz.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.fwbz.mqtt.entity.DeviceAttribute;
import org.jeecg.modules.fwbz.mqtt.entity.MqttHistory;

import java.util.Collection;
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

    /**
     * 按采集编码(acquisition_coding) 批量更新采集值和采集时间
     * 一条 SQL 更新多行，减少数据库交互次数（用于楼控批量读点后统一落库）
     *
     * @param list 待更新数据（需包含 acquisitionCoding、value、gatherTime 字段）
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE device_attribute " +
            "<set>" +
            "value = CASE acquisition_coding " +
            "<foreach collection='list' item='item'>" +
            "WHEN #{item.acquisitionCoding} THEN #{item.value} " +
            "</foreach>" +
            "END, " +
            "gather_time = CASE acquisition_coding " +
            "<foreach collection='list' item='item'>" +
            "WHEN #{item.acquisitionCoding} THEN #{item.gatherTime} " +
            "</foreach>" +
            "END " +
            "</set>" +
            "WHERE acquisition_coding IN " +
            "<foreach collection='list' item='item' open='(' separator=',' close=')'>" +
            "#{item.acquisitionCoding}" +
            "</foreach>" +
            "</script>")
    int updateValueByIds(@Param("list") Collection<org.jeecg.modules.fwbz.mdm.entity.DeviceAttribute> list);
}
