package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康人脸分组检索响应（/api/frs/v1/application/oneToMany）
 *
 * @author fwbz
 */
@Data
public class FaceGroupSearchResponse {

    /** 结果总数 */
    private Integer total;

    /** 当前页码 */
    private Integer pageNo;

    /** 每页记录数 */
    private Integer pageSize;

    /** 1:N识别结果列表 */
    private List<FaceGroupSearchItem> list;

    /**
     * 人脸分组检索单条结果
     */
    @Data
    public static class FaceGroupSearchItem {

        /** 该人脸和上传人脸的相似度 */
        private String similarity;

        /** 人脸的唯一标识 */
        private String indexCode;

        /** 人脸信息 */
        private FaceInfo faceInfo;

        /** 人脸图片 */
        private FacePic facePic;
    }

    /**
     * 人脸信息
     */
    @Data
    public static class FaceInfo {

        /** 人脸名称 */
        private String name;

        /** 证件类别：111-身份证，OTHER-其它证件 */
        private String certificateType;

        /** 证件号码 */
        private String certificateNum;
    }

    /**
     * 人脸图片
     */
    @Data
    public static class FacePic {

        /** 人脸图片绝对地址 */
        private String faceUrl;
    }
}
