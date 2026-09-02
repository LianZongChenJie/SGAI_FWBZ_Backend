package org.jeecg.modules.fwbz.personnelManagement.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 人员轨迹查询请求参数
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "人员轨迹查询请求", description = "根据时间范围和人脸照片查询人员轨迹")
public class PersonnelTrajectoryRequest {

    /** 搜图开始时间（ISO8601标准，如 2024-05-03T17:30:08.000+08:00） */
    @ApiModelProperty(value = "搜图开始时间", required = true, example = "2024-05-03T00:00:00.000+08:00")
    private String startTime;

    /** 搜图结束时间（ISO8601标准，必须在startTime之后） */
    @ApiModelProperty(value = "搜图结束时间", required = true, example = "2024-05-03T23:59:59.000+08:00")
    private String endTime;

    /** 人脸照片Base64编码字符串 */
    @ApiModelProperty(value = "人脸照片Base64编码", required = true)
    private String facePhoto;
}
