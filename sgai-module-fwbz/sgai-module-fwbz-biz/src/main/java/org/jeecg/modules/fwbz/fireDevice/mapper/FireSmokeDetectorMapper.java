package org.jeecg.modules.fwbz.fireDevice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetector;
import org.jeecg.modules.fwbz.fireDevice.vo.StatusCountVO;
import org.jeecg.modules.fwbz.fireDevice.vo.VenueDeviceCountVO;

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

    /**
     * 按场馆统计消防设备数量，联动返回场馆经纬度。
     *
     * @return 场馆设备数量统计列表
     */
    @Select("SELECT v.venue_name AS venueName, v.longitude AS longitude, v.latitude AS latitude, " +
            "COUNT(s.id) AS deviceCount " +
            "FROM table_smoke_detector s " +
            "LEFT JOIN table_venue_info v ON s.venue_id = v.id " +
            "WHERE s.venue_id IS NOT NULL " +
            "GROUP BY s.venue_id, v.venue_name, v.longitude, v.latitude " +
            "ORDER BY deviceCount DESC")
    List<VenueDeviceCountVO> countByVenue();
}
