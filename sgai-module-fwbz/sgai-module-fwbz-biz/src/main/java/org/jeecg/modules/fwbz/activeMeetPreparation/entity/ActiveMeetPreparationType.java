package org.jeecg.modules.fwbz.activeMeetPreparation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会前筹备类型
 */
@Data
@TableName("table_activeMeet_preparation_type")
public class ActiveMeetPreparationType {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 筹备名称
     */
    private String typeName;
}
