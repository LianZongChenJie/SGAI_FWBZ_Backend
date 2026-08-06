package org.jeecg.modules.fwbz.venue;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 场馆基本信息
 * @Author: jeecg-boot
 * @Date:   2026-07-29
 * @Version: V1.0
 */
@Data
@TableName("table_venue_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="table_venue_info对象", description="场馆基本信息")
public class VenueInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**场馆名称*/
    @Excel(name = "场馆名称", width = 15)
    @ApiModelProperty(value = "场馆名称")
    private String venueName;

    /**位置*/
    @Excel(name = "位置", width = 15)
    @ApiModelProperty(value = "位置")
    private String location;

    /**朝向*/
    @Excel(name = "朝向", width = 15)
    @ApiModelProperty(value = "朝向")
    private String orientation;

    /**建筑面积*/
    @Excel(name = "建筑面积", width = 15)
    @ApiModelProperty(value = "建筑面积")
    private String area;

    /**层高*/
    @Excel(name = "层高", width = 15)
    @ApiModelProperty(value = "层高")
    private String ceilingH;

    /**采光条件*/
    @Excel(name = "采光条件", width = 15)
    @ApiModelProperty(value = "采光条件")
    private String lighting;

    /**基础情况*/
    @Excel(name = "基础情况", width = 15)
    @ApiModelProperty(value = "基础情况")
    private String basicFacility;

    /**可施工 1=是 0=否*/
    @Excel(name = "可施工", width = 15)
    @ApiModelProperty(value = "可施工 1=是 0=否")
    private Integer buildable;

    /**楼层*/
    @Excel(name = "楼层", width = 15)
    @ApiModelProperty(value = "楼层")
    private String floors;

    /**经度*/
    @ApiModelProperty(value = "经度")
    private java.math.BigDecimal longitude;

    /**纬度*/
    @ApiModelProperty(value = "纬度")
    private java.math.BigDecimal latitude;
}
