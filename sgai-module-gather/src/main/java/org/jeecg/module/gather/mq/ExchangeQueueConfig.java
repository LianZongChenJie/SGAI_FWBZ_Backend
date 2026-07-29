package org.jeecg.module.gather.mq;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
/**
 * mq交换机、队列配置
 */
@Configuration
public class ExchangeQueueConfig {

    /**
     * 设备能源数据采集
     */
    @Bean
    public Queue deviceEnergyDataGather() {
        return new Queue(MqConstant.QUEUE_DEVICE_ENERGY_DATA_GATHER, true);
    }

    /**
     * 设备运行状态变更
     */
    @Bean
    public Queue deviceRunStatusChange(){
        return new Queue(MqConstant.QUEUE_DEVICE_RUN_STATUS_CHANGE, true);
    }

}
