package org.jeecg.modules.fwbz.buildingControl.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sunwayland.pspace.PSpaceClient;
import com.sunwayland.pspace.entity.Base;
import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.enums.PsErrorCodeEnum;
import com.sunwayland.pspace.enums.PsQualityEnum;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataRequest;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataResponse;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlService;
import org.jeecg.modules.fwbz.coldSourceSystem.config.ColdSourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 楼控系统实时数据写入服务实现
 */
@Slf4j
@Service
public class BuildingControlServiceImpl implements BuildingControlService {

    /**
     * 外部系统 UpdRealData 接口地址
     */
    @Value("${fwbz.building-control.upd-real-data-url:http://10.61.13.131:8889/UpdRealData}")
    private String updRealDataUrl;
    @Autowired
    private ColdSourceProperties properties;

    private volatile PSpaceClient client;
    /**
     * 时间戳格式，与外部系统示例保持一致
     */
    private static final DateTimeFormatter TM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

    /**
     * 默认质量戳
     */
    private static final int DEFAULT_QY = 232;

    private final RestTemplate restTemplate;

    public BuildingControlServiceImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        restTemplate = new RestTemplate(factory);
    }

    public synchronized PSpaceClient connect() {
        if (client != null) {
            return client;
        }
        client = PSpaceClient.getInstance(
                properties.getHost(),
                properties.getPort(),
                properties.getUsername(),
                properties.getPassword());
        try {
            client.connect();
            log.info("冷源系统连接成功: {}:{}", properties.getHost(), properties.getPort());
        } catch (Exception e) {
            client = null;
            throw e;
        }
        return client;
    }

    /**
     * 向外部系统批量写入实时数据
     *
     * @param items 前端传入的更新项
     * @return 外部系统响应
     */
    @Override
    public String updRealData(UpdRealDataItemDto items) {
        if (items == null) {
            throw new IllegalArgumentException("更新数据不能为空");
        }
        connect();

        // 实时值
        PsData stringData = new PsData(items.getPv(), PsQualityEnum.WRITE_BY_CONTROL);
        PsResult<Base> result = client.realWrite(items.getTagid(), stringData);

        PsErrorCodeEnum code = result.getCode();
        // 成功则返回状态码 PSRET_OK
        if (Objects.equals(code, PsErrorCodeEnum.PSRET_OK)) {
            System.out.println("success");
        } else {
            // 根据状态码处理相应的错误
            System.out.println(code);
        }
        client.disconnect();
        return result.getData().get(0).toString();
    }
}
