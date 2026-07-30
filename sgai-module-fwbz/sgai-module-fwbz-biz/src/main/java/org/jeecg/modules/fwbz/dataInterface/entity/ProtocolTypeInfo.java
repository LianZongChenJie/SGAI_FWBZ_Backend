package org.jeecg.modules.fwbz.dataInterface.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.fwbz.entity.BaseEntity;

/**
 * 接口协议类型
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("table_protocol_type_info")
public class ProtocolTypeInfo extends BaseEntity {

    /**
     * 协议类型名称
     */
    private String typeName;
}
