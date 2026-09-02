package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.dto.FaceGroupSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.FaceGroupSearchResponse;
import org.jeecg.modules.fwbz.hikvision.service.IFaceGroupSearchService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.springframework.stereotype.Service;

/**
 * 海康人脸分组检索服务实现
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class FaceGroupSearchServiceImpl implements IFaceGroupSearchService {

    /** 海康人脸分组检索API路径 */
    private static final String ONE_TO_MANY_API = "/api/frs/v1/application/oneToMany";

    /** 固定最小相似度 */
    private static final int MIN_SIMILARITY = 90;

    /** 固定页码 */
    private static final int PAGE_NO = 1;

    /** 固定每页条数 */
    private static final int PAGE_SIZE = 100;

    private final HikvisionUtil hikvisionUtil;

    @Override
    public FaceGroupSearchResponse oneToMany(String facePicBase64, String[] faceGroupIndexCodes) {
        log.info("开始海康人脸分组检索, faceGroupIndexCodes={}", (Object) faceGroupIndexCodes);

        // 1. 构建请求参数（只传固定参数+必传参数）
        FaceGroupSearchRequest request = new FaceGroupSearchRequest()
                .setFacePicBinaryData(facePicBase64)
                .setFaceGroupIndexCodes(faceGroupIndexCodes)
                .setMinSimilarity(MIN_SIMILARITY)
                .setPageNo(PAGE_NO)
                .setPageSize(PAGE_SIZE);

        try {
            // 2. 序列化并发送请求
            String requestBody = JSON.toJSONString(request);
            log.debug("海康人脸分组检索请求: {}", requestBody);

            String responseBody = hikvisionUtil.doPostJson(ONE_TO_MANY_API, requestBody);

            // 3. 检查响应是否成功
            if (!hikvisionUtil.isSuccess(responseBody)) {
                log.error("海康人脸分组检索失败, 响应: {}", responseBody);
                JSONObject json = hikvisionUtil.parseResponse(responseBody);
                String code = json.getString("code");
                String msg = json.getString("msg");
                throw new RuntimeException("海康人脸分组检索失败, code=" + code + ", msg=" + msg);
            }

            // 4. 解析返回数据
            JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
            if (dataJson == null) {
                log.warn("海康人脸分组检索返回的data为空");
                return new FaceGroupSearchResponse();
            }

            FaceGroupSearchResponse response = dataJson.toJavaObject(FaceGroupSearchResponse.class);
            int resultCount = response.getList() != null ? response.getList().size() : 0;
            log.info("海康人脸分组检索完成, 本页返回{}条, 总数={}", resultCount, response.getTotal());

            return response;

        } catch (Exception e) {
            log.error("海康人脸分组检索异常, faceGroupIndexCodes={}", (Object) faceGroupIndexCodes, e);
            throw new RuntimeException("海康人脸分组检索失败: " + e.getMessage(), e);
        }
    }
}
