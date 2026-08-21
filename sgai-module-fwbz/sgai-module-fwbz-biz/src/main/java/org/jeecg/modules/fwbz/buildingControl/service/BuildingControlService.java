package org.jeecg.modules.fwbz.buildingControl.service;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataRequest;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 楼控系统实时数据写入服务
 */
@Slf4j
@Service
public class BuildingControlService {

    /** 外部系统 UpdRealData 接口地址 */
    @Value("${fwbz.building-control.upd-real-data-url:http://127.0.0.1:8080/UpdRealData}")
    private String updRealDataUrl;

    /**
     * 向外部系统批量写入实时数据
     *
     * @param items 前端传入的更新项
     * @return 外部系统响应
     */
    public UpdRealDataResponse updRealData(List<UpdRealDataItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("更新数据不能为空");
        }

        List<UpdRealDataRequest.UpdRealDataValue> values = items.stream()
                .map(item -> new UpdRealDataRequest.UpdRealDataValue(
                        item.getTagid(),
                        item.getPv(),
                        item.getTm(),
                        item.getQy()))
                .collect(Collectors.toList());

        UpdRealDataRequest request = new UpdRealDataRequest(values.size(), values);
        String body = JSONObject.toJSONString(request);
        log.info("向外部系统写入实时数据，url={}，body={}", updRealDataUrl, body);

        String responseBody = HttpUtil.createPut(updRealDataUrl)
                .body(body, ContentType.JSON.toString())
                .execute()
                .body();

        log.info("外部系统响应：{}", responseBody);
        return JSONObject.parseObject(responseBody, UpdRealDataResponse.class);
    }
}
