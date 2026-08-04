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

    public static CurrentOnsiteCountVO of(Integer count) {
        CurrentOnsiteCountVO vo = new CurrentOnsiteCountVO();
        vo.setOnsiteCount(count);
        return vo;
    }
}
