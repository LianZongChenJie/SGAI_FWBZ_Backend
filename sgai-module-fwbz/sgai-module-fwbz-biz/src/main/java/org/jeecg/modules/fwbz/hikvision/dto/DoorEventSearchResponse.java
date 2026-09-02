package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康门禁点事件查询响应（/api/acs/v2/door/events）
 *
 * @author fwbz
 */
@Data
public class DoorEventSearchResponse {

    private Integer total;
    private Integer totalPage;
    private Integer pageNo;
    private Integer pageSize;
    private List<DoorEventItem> list;

    @Data
    public static class DoorEventItem {

        private String eventId;
        private String eventName;
        private String eventTime;
        private String personId;
        private String cardNo;
        private String personName;
        private String orgIndexCode;
        private String orgName;
        private String doorName;
        private String doorIndexCode;
        private String doorRegionIndexCode;
        private String picUri;
        private String svrIndexCode;
        private Integer eventType;
        private Integer inAndOutType;
        private String readerDevIndexCode;
        private String readerDevName;
        private String devIndexCode;
        private String devName;
        private String identityCardUri;
        private String receiveTime;
        private String jobNo;
        private String studentId;
        private String certNo;
    }
}
