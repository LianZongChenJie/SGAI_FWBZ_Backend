package org.jeecg.modules.fwbz.mqtt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.fwbz.mqtt.entity.MqttHistory;

import java.util.List;

/**
 * MQTT低压配电数据 Service 接口
 *
 * @author fwbz
 */
public interface IMqttHistoryService extends IService<MqttHistory> {

    /**
     * 批量保存MQTT低压配电数据
     *
     * @param list 数据列表
     * @return 是否成功
     */
    boolean saveHistoryList(List<MqttHistory> list);

    /**
     * 根据采集编码（uniqueKey）批量更新设备属性表中的采集值和采集时间
     *
     * @param list MQTT数据列表
     * @return 影响行数
     */
    int updateDeviceAttributeByUniqueKey(List<MqttHistory> list);
}
