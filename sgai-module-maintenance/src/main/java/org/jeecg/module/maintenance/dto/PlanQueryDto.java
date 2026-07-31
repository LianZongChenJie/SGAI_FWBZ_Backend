package org.jeecg.module.maintenance.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class PlanQueryDto {

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planBeginTime;

    private String labelType;

    /**
     * 计划名称
     */
    private String name;

    /**
     * 执行人
     */
    private String executor;

    private Integer page = 1;

    private Integer pagesize = 10;

}
