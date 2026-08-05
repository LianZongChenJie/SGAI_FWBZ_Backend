package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康门禁设备在线状态查询响应（/api/nms/v1/online/acs_device/get）
 *
 * @author fwbz
 */
@Data
public class AcsDeviceOnlineResponse {

    private Integer pageNo;
    private Integer pageSize;
    private Integer totalPage;
    private Integer total;
    private List<OnlineItem> list;

    @Data
    public static class OnlineItem {

        /** 设备型号 */
        private String deviceType;
        /** 设备唯一编码 */
        private String deviceIndexCode;
        /** 区域编码 */
        private String regionIndexCode;
        /** 采集时间 */
        private String collectTime;
        /** 区域名字 */
        private String regionName;
        /** 资源唯一编码 */
        private String indexCode;
        /** 设备名称 */
        private String cn;
        /** 协议类型 */
        private String treatyType;
        /** 厂商 */
        private String manufacturer;
        /** IP地址 */
        private String ip;
        /** 端口 */
        private Integer port;
        /** 在线状态，0-离线，1-在线 */
        private Integer online;
    }
}
