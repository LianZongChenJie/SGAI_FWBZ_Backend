package org.jeecg.modules.fwbz.api;
import org.jeecg.modules.fwbz.api.fallback.FwbzHelloFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "jeecg-Fwbz", fallbackFactory = FwbzHelloFallback.class)
public interface FwbzHelloApi {

    /**
     * Fwbz hello 微服务接口
     * @param
     * @return
     */
    @GetMapping(value = "/Fwbz/hello")
    String callHello();
}
