package org.sgai.config;

import org.sgai.constant.MqConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ExchangeQueueConfig {

    @Bean
    public Queue ptadQueue(){
        return new Queue(MqConstant.QUEUE_NAME,true);
    }

}
