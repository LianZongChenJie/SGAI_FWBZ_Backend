package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 人员识别记录请求参数
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class RecognitionRecordRequest {

    /** 查询开始时间（ISO8601标准，如 2024-05-03T00:00:00.000+08:00） */
    private String startTime;

    /** 查询结束时间（ISO8601标准） */
    private String endTime;

    /** 页码，从1开始 */
    private Integer pageNo;

    /** 每页条数 */
    private Integer pageSize;

    /** 摄像头唯一标识列表（可选），不传则查询全部 */
    private String[] cameraIndexCodes;
}
