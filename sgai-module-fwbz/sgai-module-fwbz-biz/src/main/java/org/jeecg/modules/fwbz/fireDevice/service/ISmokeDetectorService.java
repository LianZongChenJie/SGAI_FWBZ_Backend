package org.jeecg.modules.fwbz.fireDevice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.activeMeetStatistics.vo.StatCardVO;
import org.jeecg.modules.fwbz.fireDevice.entity.FireAlarmRecord;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetector;

import java.util.Date;
import java.util.List;

/**
 * 消防设备 Service 接口
 *
 * @author fwbz
 */
public interface ISmokeDetectorService extends IService<SmokeDetector> {

    /**
     * 分页查询消防设备列表，联动返回设备类型名称。
     *
     * @param page         分页参数（当前页/每页大小）
     * @param deviceName   设备名称（模糊查询）
     * @param status       状态
     * @param deviceType   设备类型ID
     * @param venueId      场馆ID
     * @param startTime    最后巡检时间-开始
     * @param endTime      最后巡检时间-结束
     * @param signal       信号强度
     * @param powerLevel   电量
     * @return 分页结果（包含 typeName）
     */
    IPage<SmokeDetector> getSmokeDetectorPage(IPage<SmokeDetector> page,
                                               String deviceName,
                                               String status,
                                               String deviceType,
                                               Long venueId,
                                               Date startTime,
                                               Date endTime,
                                               String signal,
                                               String powerLevel);

    /**
     * 根据消防设备ID分页查询报警记录。
     *
     * @param page     分页参数
     * @param deviceId 消防设备ID
     * @return 报警记录分页结果
     */
    IPage<FireAlarmRecord> getAlarmRecordsByDeviceId(IPage<FireAlarmRecord> page, Long deviceId);

    /**
     * 统计消防设备总数。
     *
     * @return 统计卡片
     */
    StatCardVO countTotal();

    /**
     * 统计设备在线率（排除"离线"和"故障"状态，返回百分比）。
     *
     * @return 统计卡片
     */
    StatCardVO countOnline();

    /**
     * 统计今日巡检完成数量（最后巡检时间为今天的设备数）。
     *
     * @return 统计卡片
     */
    StatCardVO countTodayCheck();

    /**
     * 统计待处理告警数量（handle_status=0 且 status=1）。
     *
     * @return 统计卡片
     */
    StatCardVO countPendingAlarm();

    /**
     * 获取消防设备统计汇总数据。
     *
     * @return 汇总卡片列表
     */
    List<StatCardVO> getSummary();
}
