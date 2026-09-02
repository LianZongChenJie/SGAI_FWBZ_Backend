package org.jeecg.modules.fwbz.buildingControl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 楼控发送控制历史（building_control_point_send_history）
 * <p>
 * 记录 pSpace 写点（控制）操作：每次 updRealData 写点成功后，
 * 按 tagId（device_attribute.acquisition_coding）关联的设备属性落一条历史。
 */
@TableName("building_control_point_send_history")
@Data
public class BuildingControlSendHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属性id（device_attribute.id） */
    private Long attributeId;

    /** 控制值（写点下发的值，VARCHAR 存原始下发内容） */
    private String value;

    /** 控制时间 */
    private LocalDateTime collectionTime;

    /** 设备id（device_attribute.device_id） */
    private Long deviceId;

    /** 属性名称（device_attribute.attribute_name） */
    private String attributeName;

    /** 控制人 */
    private String controlBy;
}
