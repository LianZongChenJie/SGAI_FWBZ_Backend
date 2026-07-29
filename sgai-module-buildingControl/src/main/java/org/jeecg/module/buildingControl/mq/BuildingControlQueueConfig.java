package org.jeecg.module.buildingControl.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mq交换机、队列配置
 */
@Configuration
public class BuildingControlQueueConfig {

    public static final String BC_DATA = "bc_data";

    public static final String BC_CONTROL = "bc_control";

    /**
     * 楼控点位数据采集
     */
    @Bean
    public Queue bcData(){
        return new Queue(BC_DATA,true);
    }

    /**
     * 楼控点位数据控制
     */
    @Bean
    public Queue bcControl(){
    	return new Queue(BC_CONTROL,true);
    }

}
