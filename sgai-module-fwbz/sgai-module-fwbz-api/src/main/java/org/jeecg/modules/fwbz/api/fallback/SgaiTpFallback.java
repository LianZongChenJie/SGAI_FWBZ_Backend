package org.jeecg.modules.fwbz.api.fallback;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.api.SgaiTpApi;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * sgai-tp 服务调用降级工厂
 *
 * @author fwbz
 */
@Slf4j
@Component
public class SgaiTpFallback implements FallbackFactory<SgaiTpApi> {

    @Override
    public SgaiTpApi create(Throwable cause) {
        log.error("sgai-tp微服务接口调用失败", cause);
        return new SgaiTpApi() {
            @Override
            public String findHourElectricityByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
                log.warn("sgai-tp降级处理, startTime={}, endTime={}", startTime, endTime);
                return null;
            }
        };
    }
}
