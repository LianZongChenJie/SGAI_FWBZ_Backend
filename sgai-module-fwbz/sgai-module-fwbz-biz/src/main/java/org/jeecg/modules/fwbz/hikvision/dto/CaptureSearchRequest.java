package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 海康以图搜图请求参数（/api/frs/v1/application/captureSearch）
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class CaptureSearchRequest {

    /** 页码，从1开始，为空时等价于1 */
    private Integer pageNo;

    /** 每页条数，范围 (0, 1000]，为空时等价于1000 */
    private Integer pageSize;

    /** 搜图图片的二进制数据经Base64编码后的字符串 */
    private String facePicBinaryData;

    /** 搜图图片的URL（与facePicBinaryData至少有一个存在） */
    private String facePicUrl;

    /** 抓拍机唯一标识列表，为空则搜索全部 */
    private String[] cameraIndexCodes;

    /** 单个识别资源最大搜索张数，最大100，为空时等价于100 */
    private Integer searchNum;

    /** 搜图开始时间（ISO8601标准） */
    private String startTime;

    /** 搜图结束时间（ISO8601标准，必须在startTime之后） */
    private String endTime;

    /** 最小相似度，范围[1, 100] */
    private Integer minSimilarity;

    /** 最大相似度，范围[1, 100] */
    private Integer maxSimilarity;
}
