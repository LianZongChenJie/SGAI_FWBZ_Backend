package org.jeecg.modules.fwbz.interfaceStatistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.interfaceStatistics.entity.InterfaceHistory;

import java.util.Date;

public interface InterfaceHistoryMapper extends BaseMapper<InterfaceHistory> {

    /**
     * 统计指定日期的数据量总和（KB），使用 TRUNC 做纯日期比较，避免 Dameng DATE 类型时间部分不匹配
     */
    @Select("SELECT NVL(SUM(data_size), 0) FROM table_interface_history WHERE TRUNC(clinet_date) = TRUNC(#{date})")
    Double selectDataSizeSum(@Param("date") Date date);

    /**
     * 查询指定接口的最新一条历史记录
     */
    @Select("SELECT * FROM table_interface_history WHERE system_id = #{systemId} ORDER BY clinet_date DESC, clinet_time DESC LIMIT 1")
    InterfaceHistory selectLatestBySystemId(@Param("systemId") Long systemId);

    /**
     * 统计所有历史数据量总和（KB）
     */
    @Select("SELECT NVL(SUM(data_size), 0) FROM table_interface_history")
    Double selectTotalDataSizeSum();

    /**
     * 统计指定日期范围内的数据量总和（KB）
     */
    @Select("SELECT NVL(SUM(data_size), 0) FROM table_interface_history WHERE TRUNC(clinet_date) >= TRUNC(#{start}) AND TRUNC(clinet_date) <= TRUNC(#{end})")
    Double selectDataSizeSumByRange(@Param("start") Date start, @Param("end") Date end);
}
