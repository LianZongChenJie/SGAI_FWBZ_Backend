package org.jeecg.modules.fwbz.api;

import org.jeecg.modules.fwbz.api.fallback.FwbzDeviceFallback;
import org.jeecg.modules.fwbz.entity.SpaceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "sgai-Fwbz", fallbackFactory = FwbzDeviceFallback.class)
public interface FwbzSpaceApi {

    /**
     * 获取空间详细信息
     * @param spaceIds 空间id
     * @return 空间详细信息
     */
    @GetMapping(value = "/fwbz/space/api/spaceInfoList")
    List<SpaceInfo> spaceInfoList(@RequestParam String spaceIds);
}
