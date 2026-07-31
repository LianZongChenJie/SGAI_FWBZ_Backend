package org.jeecg.modules.fwbz.energyAnalysis.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CostVo {

    /**
     * 总量
     */
    private BigDecimal total;

    /**
     * 费用
     */
    private BigDecimal cost;

}
