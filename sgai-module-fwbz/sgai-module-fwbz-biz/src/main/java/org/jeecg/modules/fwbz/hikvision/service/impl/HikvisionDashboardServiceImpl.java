package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.dto.*;
import org.jeecg.modules.fwbz.hikvision.service.IHikvisionDashboardService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.springframework.stereotype.Service;

/**
 * 海康数据看板服务实现
 * <p>通过调用海康OpenAPI获取今日进场人数、当前在场人数、人员识别记录、异常行为预警。</p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class HikvisionDashboardServiceImpl implements IHikvisionDashboardService {

    /**
     * 海康门禁事件搜索API（用于统计进场人数）
     * 实际API路径以平台运管中心-API管理-门禁管理 中的列表为准，可能需要调整为：
     * /api/acs/v1/event/totalSearch 或 /api/acs/v1/events/search
     */
    private static final String ACS_EVENT_SEARCH_API = "/api/acs/v1/event/totalSearch";

    /**
     * 海康当前在场人数API
     * 实际API路径以平台运管中心-API管理-人员统计 中的列表为准
     */
    private static final String CURRENT_ONSITE_API = "/api/acs/v1/statistic/currentPerson";

    /**
     * 海康人脸识别事件搜索API
     * 实际API路径以平台运管中心-API管理-人脸识别 中的列表为准
     */
    private static final String RECOGNITION_EVENT_API = "/api/frs/v1/event/recognition/search";

    /**
     * 海康事件搜索API（用于异常行为预警）
     * 实际API路径以平台运管中心-API管理-事件服务 中的列表为准
     */
    private static final String ALARM_EVENT_API = "/api/els/v1/event/search";

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 500;

    private final HikvisionUtil hikvisionUtil;

    // ==================== 今日进场人数 ====================

    @Override
    public TodayEntryCountVO getTodayEntryCount() {
        log.info("开始查询今日进场人数");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("pageNo", 1);
            requestBody.put("pageSize", 1);
            // 仅查询总数，不关心具体列表
            requestBody.put("returnTotal", true);

            String responseBody = hikvisionUtil.doPostJson(ACS_EVENT_SEARCH_API, requestBody.toJSONString());
            log.debug("海康今日进场人数查询响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("查询今日进场人数失败, 响应: {}", responseBody);
                JSONObject json = hikvisionUtil.parseResponse(responseBody);
                String code = json.getString("code");
                String msg = json.getString("msg");
                throw new RuntimeException("查询今日进场人数失败, code=" + code + ", msg=" + msg);
            }

            JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
            Integer total = dataJson != null ? dataJson.getInteger("total") : 0;
            log.info("今日进场人数: {}", total);

            return TodayEntryCountVO.of(total != null ? total : 0);

        } catch (Exception e) {
            log.error("查询今日进场人数异常", e);
            throw new RuntimeException("查询今日进场人数失败: " + e.getMessage(), e);
        }
    }

    // ==================== 当前在场人数 ====================

    @Override
    public CurrentOnsiteCountVO getCurrentOnsiteCount() {
        log.info("开始查询当前在场人数");

        try {
            String responseBody = hikvisionUtil.doGetString(CURRENT_ONSITE_API, null);
            log.debug("海康当前在场人数查询响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("查询当前在场人数失败, 响应: {}", responseBody);
                JSONObject json = hikvisionUtil.parseResponse(responseBody);
                String code = json.getString("code");
                String msg = json.getString("msg");
                throw new RuntimeException("查询当前在场人数失败, code=" + code + ", msg=" + msg);
            }

            JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
            Integer count = dataJson != null ? dataJson.getInteger("count") : 0;
            log.info("当前在场人数: {}", count);

            return CurrentOnsiteCountVO.of(count != null ? count : 0);

        } catch (Exception e) {
            log.error("查询当前在场人数异常", e);
            throw new RuntimeException("查询当前在场人数失败: " + e.getMessage(), e);
        }
    }

    // ==================== 人员识别记录 ====================

    @Override
    public RecognitionRecordResponse getRecognitionRecords(RecognitionRecordRequest request) {
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        log.info("开始查询人员识别记录, startTime={}, endTime={}, pageNo={}, pageSize={}",
                startTime, endTime, request.getPageNo(), request.getPageSize());

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("startTime", startTime);
            requestBody.put("endTime", endTime);
            requestBody.put("pageNo", request.getPageNo() != null ? request.getPageNo() : DEFAULT_PAGE_NO);
            requestBody.put("pageSize", request.getPageSize() != null ? request.getPageSize() : DEFAULT_PAGE_SIZE);
            if (request.getCameraIndexCodes() != null && request.getCameraIndexCodes().length > 0) {
                requestBody.put("cameraIndexCodes", request.getCameraIndexCodes());
            }

            String responseBody = hikvisionUtil.doPostJson(RECOGNITION_EVENT_API, requestBody.toJSONString());
            log.debug("海康人员识别记录查询响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("查询人员识别记录失败, 响应: {}", responseBody);
                JSONObject json = hikvisionUtil.parseResponse(responseBody);
                String code = json.getString("code");
                String msg = json.getString("msg");
                throw new RuntimeException("查询人员识别记录失败, code=" + code + ", msg=" + msg);
            }

            JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
            if (dataJson == null) {
                log.warn("人员识别记录返回的data为空");
                return new RecognitionRecordResponse();
            }

            RecognitionRecordResponse response = dataJson.toJavaObject(RecognitionRecordResponse.class);
            int resultCount = response.getList() != null ? response.getList().size() : 0;
            log.info("人员识别记录查询完成, 本页返回{}条, 总数={}", resultCount, response.getTotal());

            return response;

        } catch (Exception e) {
            log.error("查询人员识别记录异常, startTime={}, endTime={}", startTime, endTime, e);
            throw new RuntimeException("查询人员识别记录失败: " + e.getMessage(), e);
        }
    }

    // ==================== 异常行为预警 ====================

    @Override
    public AbnormalBehaviorAlertResponse getAbnormalBehaviorAlerts(AbnormalBehaviorAlertRequest request) {
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        log.info("开始查询异常行为预警, startTime={}, endTime={}, pageNo={}, pageSize={}, eventTypes={}",
                startTime, endTime, request.getPageNo(), request.getPageSize(), request.getEventTypes());

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("startTime", startTime);
            requestBody.put("endTime", endTime);
            requestBody.put("pageNo", request.getPageNo() != null ? request.getPageNo() : DEFAULT_PAGE_NO);
            requestBody.put("pageSize", request.getPageSize() != null ? request.getPageSize() : DEFAULT_PAGE_SIZE);
            if (request.getEventTypes() != null && request.getEventTypes().length > 0) {
                requestBody.put("eventTypes", request.getEventTypes());
            }

            String responseBody = hikvisionUtil.doPostJson(ALARM_EVENT_API, requestBody.toJSONString());
            log.debug("海康异常行为预警查询响应: {}", responseBody);

            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("查询异常行为预警失败, 响应: {}", responseBody);
                JSONObject json = hikvisionUtil.parseResponse(responseBody);
                String code = json.getString("code");
                String msg = json.getString("msg");
                throw new RuntimeException("查询异常行为预警失败, code=" + code + ", msg=" + msg);
            }

            JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
            if (dataJson == null) {
                log.warn("异常行为预警返回的data为空");
                return new AbnormalBehaviorAlertResponse();
            }

            AbnormalBehaviorAlertResponse response = dataJson.toJavaObject(AbnormalBehaviorAlertResponse.class);
            int resultCount = response.getList() != null ? response.getList().size() : 0;
            log.info("异常行为预警查询完成, 本页返回{}条, 总数={}", resultCount, response.getTotal());

            return response;

        } catch (Exception e) {
            log.error("查询异常行为预警异常, startTime={}, endTime={}", startTime, endTime, e);
            throw new RuntimeException("查询异常行为预警失败: " + e.getMessage(), e);
        }
    }
}
