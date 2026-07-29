package org.jeecg.modules.fwbz.integration.dto;

import lombok.Data;

@Data
public class CategoryPushItem {
    private String id;
    private String name;
    private String fullName;   // fwbz 端忽略，仅接收
    private String pid;        // uuid 或 "0"
}
