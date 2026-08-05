package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康门禁状态查询响应（/api/acs/v1/door/states）
 *
 * @author fwbz
 */
@Data
public class DoorStatusResponse {

    /** 有权限的门禁点状态集合 */
    private List<DoorStatusItem> authDoorList;

    /** 没有权限的门禁点集合 */
    private List<String> noAuthDoorIndexCodeList;

    /**
     * 门禁状态信息
     */
    @Data
    public static class DoorStatusItem {

        /** 门禁点唯一编码 */
        private String doorIndexCode;

        /** 门状态，0-初始状态，1-开门状态，2-关门状态，3-离线状态 */
        private Integer doorState;
    }
}
