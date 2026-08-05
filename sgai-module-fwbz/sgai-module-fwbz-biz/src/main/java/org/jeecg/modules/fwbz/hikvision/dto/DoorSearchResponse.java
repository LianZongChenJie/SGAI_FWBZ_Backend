package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康门禁点查询响应（/api/resource/v2/door/search）
 *
 * @author fwbz
 */
@Data
public class DoorSearchResponse {

    /** 记录总数 */
    private Integer total;

    /** 当前页码 */
    private Integer pageNo;

    /** 分页大小 */
    private Integer pageSize;

    /** 门禁点列表 */
    private List<DoorItem> list;

    /**
     * 海康返回的门禁点信息
     */
    @Data
    public static class DoorItem {

        /** 资源唯一编码 */
        private String indexCode;

        /** 资源类型 */
        private String resourceType;

        /** 资源名称 */
        private String name;

        /** 门禁点编号 */
        private String doorNo;

        /** 通道号 */
        private String channelNo;

        /** 父级资源编号 */
        private String parentIndexCode;

        /** 一级控制器id */
        private String controlOneId;

        /** 二级控制器id */
        private String controlTwoId;

        /** 读卡器1 */
        private String readerInId;

        /** 读卡器2 */
        private String readerOutId;

        /** 门序号 */
        private Integer doorSerial;

        /** 接入协议 */
        private String treatyType;

        /** 所属区域 */
        private String regionIndexCode;

        /** 所属区域目录，以@符号分割，包含本节点 */
        private String regionPath;

        /** 创建时间 */
        private String createTime;

        /** 更新时间 */
        private String updateTime;

        /** 描述 */
        private String description;

        /** 通道类型，door：门禁点 */
        private String channelType;

        /** 区域名称 */
        private String regionName;

        /** 所属区域目录名，符号"@"进行分隔 */
        private String regionPathName;

        /** 安装位置 */
        private String installLocation;
    }
}
