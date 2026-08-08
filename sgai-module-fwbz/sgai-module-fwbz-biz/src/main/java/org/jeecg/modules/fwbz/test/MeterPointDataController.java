package org.jeecg.modules.fwbz.test;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.api.SgaiTpApi;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 仪表点位数据代理控制器
 * <p>通过 FeignClient 调用 sgai-tp 服务的 meterPointData 接口，将数据返回给前端。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@RequestMapping("/test/meterPointData")
public class MeterPointDataController {

    @Resource
    private SgaiTpApi sgaiTpApi;

    /**
     * 按日期范围查询每小时用电量数据
     *
     * @param startTime 开始日期（yyyy-MM-dd HH:mm:ss）
     * @param endTime   结束日期（yyyy-MM-dd HH:mm:ss）
     * @return 目标服务返回的JSON数据
     */
    @GetMapping("/findHourElectricityByDateRange")
    public Result<Object> findHourElectricityByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        log.info("Feign调用sgai-tp - 查询每小时用电量, startTime={}, endTime={}", startTime, endTime);

        try {
            String response = sgaiTpApi.findHourElectricityByDateRange(startTime, endTime);
            log.debug("sgai-tp响应: {}", response);

            if (response == null) {
                return Result.error("sgai-tp服务不可用，已触发降级");
            }

            Result<?> result = JSONObject.parseObject(response).toJavaObject(Result.class);
            return Result.ok(result.getResult());
        } catch (Exception e) {
            log.error("调用sgai-tp用电量接口异常, startTime={}, endTime={}", startTime, endTime, e);
            return Result.error("调用sgai-tp服务异常: " + e.getMessage());
        }
    }
}
