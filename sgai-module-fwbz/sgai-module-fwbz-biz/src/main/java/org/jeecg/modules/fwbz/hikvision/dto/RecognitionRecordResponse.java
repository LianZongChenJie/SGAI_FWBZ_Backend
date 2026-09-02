package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 人员识别记录响应
 *
 * @author fwbz
 */
@Data
public class RecognitionRecordResponse {

    /** 结果总数 */
    private Integer total;

    /** 当前页码 */
    private Integer pageNo;

    /** 每页记录数 */
    private Integer pageSize;

    /** 识别记录列表 */
    private List<RecognitionRecordItem> list;

    /**
     * 人员识别记录单条结果
     */
    @Data
    public static class RecognitionRecordItem {

        /** 事件唯一标识 */
        private String eventId;

        /** 识别时间（ISO8601标准） */
        private String eventTime;

        /** 摄像头唯一编码 */
        private String cameraIndexCode;

        /** 摄像头名称 */
        private String cameraName;

        /** 人员姓名 */
        private String personName;

        /** 证件号码 */
        private String certificateNum;

        /** 相似度 */
        private String similarity;

        /** 人脸图片URL */
        private String facePicUrl;

        /** 背景图片URL */
        private String bkgPicUrl;

        /** 年龄 */
        private Integer age;

        /** 性别：male/female */
        private String gender;
    }
}
