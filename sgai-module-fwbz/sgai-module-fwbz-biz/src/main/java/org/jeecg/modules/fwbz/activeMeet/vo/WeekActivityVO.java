package org.jeecg.modules.fwbz.activeMeet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.modules.fwbz.activeMeet.entity.ActiveMeetInfo;

import java.util.Date;
import java.util.List;

/**
 * 本周活动（按日期分组）
 */
@Data
public class WeekActivityVO {

    /**
     * 活动日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date date;

    /**
     * 当天活动列表
     */
    private List<ActiveMeetInfo> list;
}
