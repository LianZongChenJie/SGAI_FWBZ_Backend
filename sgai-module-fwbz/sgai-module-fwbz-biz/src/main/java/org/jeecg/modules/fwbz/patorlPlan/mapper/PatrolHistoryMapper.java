package org.jeecg.modules.fwbz.patorlPlan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.fwbz.patorlPlan.entity.PatrolHistory;

/**
 * @Description: 巡更历史
 * @Author: jeecg-boot
 * @Date:   2026-08-03
 * @Version: V1.0
 */
@Mapper
public interface PatrolHistoryMapper extends BaseMapper<PatrolHistory> {

    /**
     * 查询指定计划今天的记录数量
     */
    int countTodayByPlanId(@Param("patrolId") Long patrolId);

    /**
     * 插入巡更历史记录
     */
    int insertHistory(@Param("patrolId") Long patrolId);
}
