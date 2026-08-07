package org.jeecg.modules.fwbz.fireDevice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetector;
import org.jeecg.modules.fwbz.fireDevice.vo.StatusCountVO;

import java.util.List;

/**
 * 消防设备 Mapper
 *
 * @author fwbz
 */
@Mapper
public interface FireSmokeDetectorMapper extends BaseMapper<SmokeDetector> {

    /**
     * 按设备状态分组统计数量。
     *
     * @return 各状态设备数量列表
     */
    @Select("SELECT status AS status, COUNT(*) AS count FROM table_smoke_detector WHERE status IS NOT NULL GROUP BY status")
    List<StatusCountVO> countByStatus();
}
