package org.sgai.dto;

import lombok.Data;

import java.util.List;

@Data
public class YbttDto {
    /**
     * 仪表编号 （唯⼀）
     */
    private String deviceNum;
    /**
     * 数据列表
     */
    private List<YbttAttribute> dataList;

}
