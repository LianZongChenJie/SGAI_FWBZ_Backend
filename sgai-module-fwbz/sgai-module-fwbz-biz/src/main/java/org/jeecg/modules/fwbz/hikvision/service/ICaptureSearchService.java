package org.jeecg.modules.fwbz.hikvision.service;

import org.jeecg.modules.fwbz.hikvision.dto.CaptureSearchResponse;

/**
 * 海康以图搜图服务接口
 *
 * @author fwbz
 */
public interface ICaptureSearchService {

    /**
     * 以图搜图（单页查询）
     * <p>根据上传的人脸图片，在海康平台指定时间范围内搜索匹配的人脸抓拍记录。</p>
     * <p>固定参数：最小相似度=50，页码=1，每页1000条。</p>
     *
     * @param facePicBase64 人脸图片的Base64编码字符串（建议：正面免冠照，jpg格式，10KB~200KB）
     * @param startTime     搜图开始时间（ISO8601标准，如 2024-05-03T17:30:08.000+08:00）
     * @param endTime       搜图结束时间（ISO8601标准，必须在startTime之后）
     * @return 以图搜图结果
     */
    CaptureSearchResponse searchByImage(String facePicBase64, String startTime, String endTime);
}
