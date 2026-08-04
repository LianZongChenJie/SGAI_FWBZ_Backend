package org.jeecg.modules.fwbz.personnelManagement.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人员轨迹结果项
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "人员轨迹结果", description = "包含抓拍信息和关联摄像头信息")
public class PersonnelTrajectoryVO {

    // ==================== 抓拍信息 ====================

    /** 抓拍时间（ISO8601标准） */
    @ApiModelProperty(value = "抓拍时间")
    private String captureTime;

    /** 相似度 */
    @ApiModelProperty(value = "相似度")
    private String similarity;

    /** 背景图片URL */
    @ApiModelProperty(value = "背景图片URL")
    private String bkgPicUrl;

    /** 人脸图片URL */
    @ApiModelProperty(value = "人脸图片URL")
    private String facePicUrl;

    // ==================== 摄像头信息（关联数据库） ====================

    /** 摄像头唯一编码 */
    @ApiModelProperty(value = "摄像头唯一编码")
    private String cameraIndexCode;

    /** 摄像头名称 */
    @ApiModelProperty(value = "摄像头名称")
    private String cameraName;

    /** 摄像头安装位置 */
    @ApiModelProperty(value = "摄像头安装位置")
    private String installLocation;

    /** 经度 */
    @ApiModelProperty(value = "经度")
    private BigDecimal longitude;

    /** 纬度 */
    @ApiModelProperty(value = "纬度")
    private BigDecimal latitude;

    // ==================== 人脸分组信息（1:N人脸识别身份匹配结果） ====================

    /** 人脸分组匹配姓名 */
    @ApiModelProperty(value = "人脸分组匹配姓名")
    private String groupName;

    /** 证件类别：111-身份证，OTHER-其它证件 */
    @ApiModelProperty(value = "证件类别")
    private String groupCertificateType;

    /** 证件号码 */
    @ApiModelProperty(value = "证件号码")
    private String groupCertificateNum;

    /** 人脸分组相似度 */
    @ApiModelProperty(value = "人脸分组相似度")
    private String groupSimilarity;

    /** 人脸分组人脸图片URL */
    @ApiModelProperty(value = "人脸分组人脸图片URL")
    private String groupFaceUrl;
}
