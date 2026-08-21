package org.jeecg.modules.fwbz.mqtt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.mqtt.entity.MqttHistory;
import org.jeecg.modules.fwbz.mqtt.mapper.MqttHistoryMapper;
import org.jeecg.modules.fwbz.mqtt.service.IMqttHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MQTT低压配电数据 Service 实现
 *
 * @author fwbz
 */
@Slf4j
@Service
public class MqttHistoryServiceImpl extends ServiceImpl<MqttHistoryMapper, MqttHistory>
        implements IMqttHistoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveHistoryList(List<MqttHistory> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        int rows = baseMapper.insertBatch(list);
        log.info("MQTT低压配电数据落库完成, 共{}条", rows);
        return rows > 0;
    }
}
