package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.dto.CaptureSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.CaptureSearchResponse;
import org.jeecg.modules.fwbz.hikvision.service.ICaptureSearchService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.springframework.stereotype.Service;

/**
 * 海康以图搜图服务实现
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class CaptureSearchServiceImpl implements ICaptureSearchService {

    /** 海康以图搜图API路径 */
    private static final String CAPTURE_SEARCH_API = "/api/frs/v1/application/captureSearch";

    /** 固定最小相似度 */
    private static final int MIN_SIMILARITY = 50;

    /** 固定页码 */
    private static final int PAGE_NO = 1;

    /** 固定每页条数 */
    private static final int PAGE_SIZE = 1000;

    private final HikvisionUtil hikvisionUtil;

    @Override
    public CaptureSearchResponse searchByImage(String facePicBase64, String startTime, String endTime) {
        log.info("开始海康以图搜图, startTime={}, endTime={}", startTime, endTime);

        // 1. 构建请求参数
        CaptureSearchRequest request = new CaptureSearchRequest()
                .setFacePicBinaryData(facePicBase64)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setMinSimilarity(MIN_SIMILARITY)
                .setPageNo(PAGE_NO)
                .setPageSize(PAGE_SIZE);

        try {
            // 2. 序列化并发送请求
            String requestBody = JSON.toJSONString(request);
            log.debug("海康以图搜图请求: {}", requestBody);

            String responseBody = hikvisionUtil.doPostJson(CAPTURE_SEARCH_API, requestBody);

            // 3. 检查响应是否成功
            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("海康以图搜图失败, 响应: {}", responseBody);
                JSONObject json = hikvisionUtil.parseResponse(responseBody);
                String code = json.getString("code");
                String msg = json.getString("msg");
                throw new RuntimeException("海康以图搜图失败, code=" + code + ", msg=" + msg);
            }

            // 4. 解析返回数据
            JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
            if (dataJson == null) {
                log.warn("海康以图搜图返回的data为空");
                return new CaptureSearchResponse();
            }

            CaptureSearchResponse response = dataJson.toJavaObject(CaptureSearchResponse.class);
            int resultCount = response.getList() != null ? response.getList().size() : 0;
            log.info("海康以图搜图完成, 本页返回{}条, 总数={}", resultCount, response.getTotal());

            return response;

        } catch (Exception e) {
            log.error("海康以图搜图异常, startTime={}, endTime={}", startTime, endTime, e);
            throw new RuntimeException("海康以图搜图失败: " + e.getMessage(), e);
        }
    }
}
