package org.jeecg.modules.fwbz.hikvision.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 海康摄像头查询请求参数
 *
 * @author fwbz
 */
@Data
@Accessors(chain = true)
public class CameraSearchRequest {

    /** 资源名称（模糊查询） */
    private String name;

    /** 区域编码列表 */
    private List<String> regionIndexCodes;

    /** 是否包含下级区域 */
    private Boolean isSubRegion;

    /** 页码 */
    private Integer pageNo;

    /** 每页条数 */
    private Integer pageSize;

    /** 权限码 */
    private List<String> authCodes;

    /** 查询表达式 */
    private List<Expression> expressions;

    /** 排序字段 */
    private String orderBy;

    /** 排序方式：asc/desc */
    private String orderType;

    /**
     * 查询表达式
     */
    @Data
    @Accessors(chain = true)
    public static class Expression {

        /** 字段名 */
        private String key;

        /** 操作符：0-等于，1-不等于，2-大于，3-小于，4-大于等于，5-小于等于，6-包含，7-不包含 */
        private Integer operator;

        /** 值列表 */
        private List<String> values;
    }
}
