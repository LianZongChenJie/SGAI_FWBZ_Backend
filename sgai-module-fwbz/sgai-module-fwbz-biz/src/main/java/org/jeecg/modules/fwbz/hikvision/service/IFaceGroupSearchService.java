package org.jeecg.modules.fwbz.hikvision.service;

import org.jeecg.modules.fwbz.hikvision.dto.FaceGroupSearchResponse;

/**
 * 海康人脸分组检索服务接口
 *
 * @author fwbz
 */
public interface IFaceGroupSearchService {

    /**
     * 人脸分组检索（1:N）
     * <p>根据上传的人脸图片，在指定人脸分组中检索匹配的人员信息。</p>
     *
     * @param facePicBase64      人脸图片的Base64编码字符串（建议：正面免冠照，jpg格式，10KB~200KB）
     * @param faceGroupIndexCodes 人脸分组唯一标志列表
     * @return 人脸分组检索结果
     */
    FaceGroupSearchResponse oneToMany(String facePicBase64, String[] faceGroupIndexCodes);
}
