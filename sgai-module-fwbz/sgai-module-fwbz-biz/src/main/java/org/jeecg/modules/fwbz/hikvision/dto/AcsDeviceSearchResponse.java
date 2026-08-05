package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康门禁设备查询响应（/api/resource/v2/acsDevice/search）
 *
 * @author fwbz
 */
@Data
public class AcsDeviceSearchResponse {

    private Integer total;
    private Integer pageNo;
    private Integer pageSize;
    private List<AcsDeviceItem> list;

    @Data
    public static class AcsDeviceItem {

        private String indexCode;
        private String resourceType;
        private String name;
        private String parentIndexCode;
        private String devTypeCode;
        private String devTypeDesc;
        private String deviceCode;
        private String manufacturer;
        private String regionIndexCode;
        private String regionPath;
        private String treatyType;
        private Integer cardCapacity;
        private Integer fingerCapacity;
        private Integer veinCapacity;
        private Integer faceCapacity;
        private Integer doorCapacity;
        private String deployId;
        private String netZoneId;
        private String createTime;
        private String updateTime;
        private String description;
        private String acsReaderVerifyModeAbility;
        private String regionName;
        private String regionPathName;
        private String ip;
        private String port;
        private String capability;
        private String devSerialNum;
        private String dataVersion;
    }
}
