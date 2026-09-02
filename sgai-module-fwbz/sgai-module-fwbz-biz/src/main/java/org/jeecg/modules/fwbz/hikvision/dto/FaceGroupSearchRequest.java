package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 海康人脸分组检索请求参数（/api/frs/v1/application/oneToMany）
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class FaceGroupSearchRequest {

    /** 页码，从1开始 */
    private Integer pageNo;

    /** 每页条数，范围 (0, 1000] */
    private Integer pageSize;

    /** 单个识别资源最大搜索张数，最大100 */
    private Integer searchNum;

    /** 最小相似度，范围[1, 100] */
    private Integer minSimilarity;

    /** 检索图片的URL */
    private String facePicUrl;

    /** 检索图片的二进制数据Base64编码后的字符串 */
    private String facePicBinaryData;

    /** 人脸分组唯一标志列表 */
    private String[] faceGroupIndexCodes;

    /** 人脸名称模糊查询 */
    private String name;

    /** 证件类别：111-身份证，OTHER-其它证件 */
    private String certificateType;

    /** 证件号码模糊查询 */
    private String certificateNum;
}
