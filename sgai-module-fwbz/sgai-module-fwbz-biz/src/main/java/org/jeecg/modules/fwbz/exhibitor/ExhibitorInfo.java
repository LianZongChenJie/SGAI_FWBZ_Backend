package org.jeecg.modules.fwbz.exhibitor;

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
 * @Description: 参展厂商信息
 * @Author: jeecg-boot
 * @Date:   2026-09-02
 * @Version: V1.0
 */
@Data
@TableName("table_exhibitor_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="table_exhibitor_info对象", description="参展厂商信息")
public class ExhibitorInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    /**展商名称中文*/
    @Excel(name = "展商名称中文", width = 15)
    @ApiModelProperty(value = "展商名称中文")
    private String exhibitorNameCn;

    /**展商名称英文*/
    @Excel(name = "展商名称英文", width = 15)
    @ApiModelProperty(value = "展商名称英文")
    private String exhibitorNameEn;

    /**展位号*/
    @Excel(name = "展位号", width = 15)
    @ApiModelProperty(value = "展位号")
    private String boothNumber;

    /**专题展名称*/
    @Excel(name = "专题展名称", width = 15)
    @ApiModelProperty(value = "专题展名称")
    private String thematicTxhibitionTitle;

    /**场馆id*/
    @ApiModelProperty(value = "场馆id")
    private Long venueId;
}
