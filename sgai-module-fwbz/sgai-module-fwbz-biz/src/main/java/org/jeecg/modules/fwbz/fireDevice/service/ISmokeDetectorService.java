package org.jeecg.modules.fwbz.fireDevice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.fireDevice.entity.SmokeDetector;

import java.util.Date;

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
}
