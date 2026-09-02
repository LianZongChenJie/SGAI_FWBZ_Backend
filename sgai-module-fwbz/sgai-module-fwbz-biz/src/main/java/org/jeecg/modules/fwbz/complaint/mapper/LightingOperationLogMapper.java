package org.jeecg.modules.fwbz.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.complaint.entity.LightingOperationLog;

/**
 * @Description: 照明操作日志
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Mapper
public interface LightingOperationLogMapper extends BaseMapper<LightingOperationLog> {

    /**
     * 统计今日操作条数
     *
     * @param date 日期
     * @return 条数
     */
    @Select("SELECT COUNT(*) FROM lighting_operation_log WHERE TRUNC(operation_time) = TRUNC(#{date})")
    Long countToday(@Param("date") java.util.Date date);
}
