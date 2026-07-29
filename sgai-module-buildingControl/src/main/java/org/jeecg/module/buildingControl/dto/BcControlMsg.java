package org.jeecg.module.buildingControl.dto;

import lombok.Data;

/**
 * 楼控设备控制消息
 */
@Data
public class BcControlMsg {

    private String path;

    private String value;
}
