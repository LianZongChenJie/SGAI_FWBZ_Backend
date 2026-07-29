package org.jeecg;

import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.GlobalConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.jeecg")
@EnableScheduling
@EnableConfigurationProperties
@EnableAsync
public class SgaiModuleThirdApplication  implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SgaiModuleThirdApplication.class,args);
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        BaseMap params = new BaseMap();
        params.put(GlobalConstants.HANDLER_NAME, GlobalConstants.LODER_ROUDER_HANDLER);
        //刷新网关
        redisTemplate.convertAndSend(GlobalConstants.REDIS_TOPIC_NAME, params);
    }
}
