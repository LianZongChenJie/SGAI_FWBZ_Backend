package org.jeecg.modules.fwbz.hikvision.service;

import org.jeecg.modules.fwbz.hikvision.dto.*;

/**
 * 海康数据看板服务接口
 * <p>提供今日进场人数、当前在场人数、人员识别记录、异常行为预警四个数据看板方法。</p>
 *
 * @author fwbz
 */
public interface IHikvisionDashboardService {

    /**
     * 获取今日进场人数
     * <p>查询今日ACS（门禁）进场事件总数。</p>
     *
     * @return 今日进场人数
     */
    TodayEntryCountVO getTodayEntryCount();

    /**
     * 获取当前在场人数
     * <p>查询当前各区域/场所内的实时在场人数。</p>
     *
     * @return 当前在场人数
     */
    CurrentOnsiteCountVO getCurrentOnsiteCount();

    /**
     * 查询人员识别记录
     * <p>根据时间范围分页查询人脸识别事件记录。</p>
     *
     * @param request 查询参数（startTime, endTime, pageNo, pageSize）
     * @return 人员识别记录列表
     */
    RecognitionRecordResponse getRecognitionRecords(RecognitionRecordRequest request);

    /**
     * 查询异常行为预警
     * <p>根据时间范围分页查询异常行为告警事件。</p>
     *
     * @param request 查询参数（startTime, endTime, pageNo, pageSize, eventTypes）
     * @return 异常行为预警列表
     */
    AbnormalBehaviorAlertResponse getAbnormalBehaviorAlerts(AbnormalBehaviorAlertRequest request);
}
