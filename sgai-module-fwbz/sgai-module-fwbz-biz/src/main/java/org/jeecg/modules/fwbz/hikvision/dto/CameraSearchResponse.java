package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康摄像头查询响应
 *
 * @author fwbz
 */
@Data
public class CameraSearchResponse {

    /** 总数 */
    private Integer total;

    /** 当前页码 */
    private Integer pageNo;

    /** 每页条数 */
    private Integer pageSize;

    /** 摄像头列表 */
    private List<CameraItem> list;

    /**
     * 海康返回的摄像头信息
     */
    @Data
    public static class CameraItem {

        /** 唯一编码 */
        private String indexCode;

        /** 资源类型 */
        private String resourceType;

        /** 监控点国标编号 */
        private String externalIndexCode;

        /** 资源名称 */
        private String name;

        /** 通道号 */
        private Integer chanNum;

        /** 级联编号 */
        private String cascadeCode;

        /** 父级资源编号 */
        private String parentIndexCode;

        /** 经度 */
        private String longitude;

        /** 纬度 */
        private String latitude;

        /** 海拔高度 */
        private String elevation;

        /** 监控点类型 */
        private Integer cameraType;

        /** 能力集 */
        private String capability;

        /** 录像存储位置 */
        private String recordLocation;

        /** 通道子类型 */
        private String channelType;

        /** 所属区域 */
        private String regionIndexCode;

        /** 所属区域目录 */
        private String regionPath;

        /** 传输协议 */
        private Integer transType;

        /** 接入协议 */
        private String treatyType;

        /** 安装位置 */
        private String installLocation;

        /** 创建时间 */
        private String createTime;

        /** 更新时间 */
        private String updateTime;

        /** 显示顺序 */
        private Integer disOrder;

        /** 资源唯一编码 */
        private String resourceIndexCode;

        /** 解码模式 */
        private String decodeTag;

        /** 监控点关联对讲唯一标志 */
        private String cameraRelateTalk;

        /** 区域名称 */
        private String regionName;

        /** 区域目录名称 */
        private String regionPathName;
    }
}
