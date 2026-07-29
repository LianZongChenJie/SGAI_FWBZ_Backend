package org.jeecg.modules.fwbz.api.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.jeecg.modules.fwbz.api.FwbzHelloApi;
import lombok.Setter;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author JeecgBoot
 */
@Slf4j
@Component
public class FwbzHelloFallback implements FallbackFactory<FwbzHelloApi> {
    @Setter
    private Throwable cause;

    @Override
    public FwbzHelloApi create(Throwable throwable) {
        log.error("微服务接口调用失败： {}", cause);
        return null;
    }

}
