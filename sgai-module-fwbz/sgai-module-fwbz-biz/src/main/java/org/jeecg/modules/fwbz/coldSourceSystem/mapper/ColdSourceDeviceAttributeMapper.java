package org.jeecg.modules.fwbz.coldSourceSystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceDeviceAttribute;

import java.util.List;

/**
 * 冷源设备属性表 Mapper
 */
@Mapper
public interface ColdSourceDeviceAttributeMapper extends BaseMapper<ColdSourceDeviceAttribute> {

    /**
     * 根据设备id查询属性列表
     *
     * @param deviceId 设备id
     * @return 属性列表
     */
    @Select("SELECT id, device_id AS deviceId, attr_name AS attrName, attr_code AS attrCode, "
            + "       tagid, keyname, data_type AS dataType, unit, value_enum AS valueEnum, "
            + "       object_def AS objectDef, is_enum AS isEnum, attr_type AS attrType, "
            + "       sort_order AS sortOrder, value, gather_time AS gatherTime "
            + "FROM \"FWBZ\".\"cold_source_device_attribute\" "
            + "WHERE device_id = #{deviceId} "
            + "ORDER BY sort_order ASC, id ASC")
    List<ColdSourceDeviceAttribute> selectByDeviceId(Long deviceId);

    /**
     * 按 tagid 更新属性采集值及采集时间（定时采集任务用）
     *
     * @param attribute 仅需设置 tagid/value/gatherTime
     * @return 受影响行数
     */
    @Update("UPDATE \"FWBZ\".\"cold_source_device_attribute\" "
            + "SET value = #{value}, gather_time = #{gatherTime}, update_time = CURRENT_TIMESTAMP "
            + "WHERE tagid = #{tagid}")
    int updateByTagId(ColdSourceDeviceAttribute attribute);
}
