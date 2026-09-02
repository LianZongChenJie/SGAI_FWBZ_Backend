package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康以图搜图响应（/api/frs/v1/application/captureSearch）
 *
 * @author fwbz
 */
@Data
public class CaptureSearchResponse {

    /** 结果总数 */
    private Integer total;

    /** 当前页码 */
    private Integer pageNo;

    /** 每页结果数 */
    private Integer pageSize;

    /** 人脸检索结果列表 */
    private List<CaptureSearchItem> list;

    /**
     * 人脸检索单条结果
     */
    @Data
    public static class CaptureSearchItem {

        /** 抓拍到人脸的通道唯一标识 */
        private String cameraIndexCode;

        /** 抓拍到人脸的绝对时标（ISO8601标准） */
        private String captureTime;

        /** 抓拍到的人脸和上传人脸的相似度 */
        private String similarity;

        /** 抓拍到的人脸的背景图片URL */
        private String bkgPicUrl;

        /** 抓拍到的人脸的人脸图片URL */
        private String facePicUrl;

        /** 超脑抓拍图的坐标（height,width,x,y），可用于人脸抠图 */
        private String rect;
    }
}
