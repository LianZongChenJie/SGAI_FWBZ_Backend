package org.jeecg.module.maintenance.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 描述:
 *
 * @author ppliu
 * created in 2021/8/13 17:29
 */
@Data
public class WeekOfMonth {
    private LocalDate localDate;
    private int index;
}
