package org.jeecg.modules.fwbz.interfaceStatistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.interfaceStatistics.entity.InterfaceHistory;

import java.util.Date;

public interface InterfaceHistoryMapper extends BaseMapper<InterfaceHistory> {

    /**
     * 统计指定时间范围内的数据量总和（KB）
     */
    @Select("SELECT SUM(data_size) FROM table_interface_history WHERE clinet_date >= #{startTime} AND clinet_date < #{endTime}")
    Double selectDataSizeSum(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
