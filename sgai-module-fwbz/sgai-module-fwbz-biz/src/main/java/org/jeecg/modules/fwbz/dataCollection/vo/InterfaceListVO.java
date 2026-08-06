package org.jeecg.modules.fwbz.dataCollection.vo;

import lombok.Data;

import java.util.Date;

/**
 * 接口列表响应VO（含采集统计）
 */
@Data
public class InterfaceListVO {

    private Long id;
    private String sysName;
    private String interfacePath;
    private Long protocolTypeId;
    private Integer state;
    private Date requestTime;
    private Long responseTime;
    private String testPath;
    private String cycle;
    private Long collectionPointLocation;
    private String sysOrgCode;

    /**
     * 今日采集量（KB）
     */
    private Double todayCollection;

    /**
     * 数据完整率（0或100）
     */
    private Integer dataCompleteRate;

    /**
     * 最后采集时间
     */
    private String lastCollectionTime;
}
