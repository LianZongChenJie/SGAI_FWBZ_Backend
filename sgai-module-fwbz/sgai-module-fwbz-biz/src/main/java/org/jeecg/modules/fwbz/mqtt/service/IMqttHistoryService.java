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
     * 根据采集编码（uniqueKey）批量更新设备属性表中的采集值和采集时间
     *
     * @param list MQTT数据列表
     * @return 影响行数
     */
    int updateDeviceAttributeByUniqueKey(List<MqttHistory> list);

    /**
     * 电度数据触发设备能耗计算（分钟、小时、日、月、年）
     * <p>仅对uniqueKey包含"01Wp"的数据（正向有功电能表底值）进行能耗计算，替代原先通过MQ触发的计算逻辑。</p>
     *
     * @param list MQTT数据列表
     */
    void calculateEnergyData(List<MqttHistory> list);

    /**
     * 更新设备信息表（device）的在线状态与最后采集时间
     * <p>以 devKeys（设备编号）去重后对应 device.device_code 更新：
     * 在线状态置为"在线"，最后采集时间取数据时间戳对齐到15分钟槽位后的时间。</p>
     *
     * @param list MQTT数据列表
     */
    void updateDeviceOnlineStatus(List<MqttHistory> list);
}
