package org.jeecg.modules.fwbz.activeMeetPreparation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 照明回路
 */
@Data
@TableName("lighting_circuit")
public class LightingCircuit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String circuitName;
    private String circuitCode;
    private String status;
    private Long areaId;
    private String comstat;
}
