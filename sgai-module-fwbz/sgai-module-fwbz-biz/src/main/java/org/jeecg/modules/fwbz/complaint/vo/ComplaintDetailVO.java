package org.jeecg.modules.fwbz.complaint.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintInfo;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintRecord;

import java.io.Serializable;
import java.util.List;

/**
 * 投诉建议详情 VO（包含投诉信息与处理记录）
 *
 * @author fwbz
 */
@Data
@ApiModel(value = "投诉建议详情", description = "投诉建议信息及处理记录")
public class ComplaintDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("投诉建议信息")
    private ComplaintInfo complaintInfo;

    @ApiModelProperty("处理记录列表")
    private List<ComplaintRecord> records;
}
