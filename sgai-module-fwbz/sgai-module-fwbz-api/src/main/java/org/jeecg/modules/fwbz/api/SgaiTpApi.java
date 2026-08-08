package org.jeecg.modules.fwbz.api;

import org.jeecg.modules.fwbz.api.fallback.SgaiTpFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * sgai-tp 服务 Feign 接口
 *
 * @author fwbz
 */
@FeignClient(value = "sgai-tp", fallbackFactory = SgaiTpFallback.class)
public interface SgaiTpApi {

    /**
     * 按日期范围查询每小时用电量数据
     *
     * @param startTime 开始日期
     * @param endTime   结束日期
     * @return 用电量数据JSON字符串
     */
    @GetMapping(value = "/fwbz/meterPointData/findHourElectricityByDateRange")
    String findHourElectricityByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime);
}
