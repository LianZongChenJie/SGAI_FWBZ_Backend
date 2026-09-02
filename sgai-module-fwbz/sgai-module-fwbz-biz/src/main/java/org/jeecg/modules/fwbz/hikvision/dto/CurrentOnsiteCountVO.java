package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

/**
 * 当前在场人数响应VO
 *
 * @author fwbz
 */
@Data
public class CurrentOnsiteCountVO {

    /** 当前在场总人数 */
    private Integer onsiteCount;

    /** 峰值客流 */
    private Long maxCount;

    /** 平均时长（小时） */
    private Double averageStopDuration;

    public static CurrentOnsiteCountVO of(Integer count) {
        CurrentOnsiteCountVO vo = new CurrentOnsiteCountVO();
        vo.setOnsiteCount(count);
        return vo;
    }
}
