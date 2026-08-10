package org.jeecg.modules.fwbz.personnelManagement.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 人员轨迹查询结果
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "人员轨迹查询结果", description = "包含1:N识别信息和轨迹摄像头列表")
public class PersonnelTrajectoryResultVO {

    // ==================== 1:N识别信息 ====================

    /** 人脸分组匹配姓名 */
    @ApiModelProperty(value = "人脸分组匹配姓名")
    private String name;

    /** 证件类别：111-身份证，OTHER-其它证件 */
    @ApiModelProperty(value = "证件类别")
    private String certificateType;

    /** 证件号码 */
    @ApiModelProperty(value = "证件号码")
    private String certificateNum;

    /** 人脸分组相似度 */
    @ApiModelProperty(value = "人脸分组相似度")
    private String similarity;

    /** 人脸分组人脸图片URL */
    @ApiModelProperty(value = "人脸分组人脸图片URL")
    private String faceUrl;

    // ==================== 轨迹摄像头列表 ====================

    /** 轨迹摄像头信息列表 */
    @ApiModelProperty(value = "轨迹摄像头信息列表")
    private List<PersonnelTrajectoryVO> cameraList;
}
