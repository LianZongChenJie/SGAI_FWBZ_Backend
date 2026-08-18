package org.jeecg.modules.fwbz.coldSourceSystem.service;

import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.config.ColdSourceProperties;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.RealDataResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * pSpace WebApi(HTTP) 客户端 —— 依据 pSpaceWebApi 文档实现
 *
 * 对应文档 [RealData] 获取实时数据：
 *   GET/POST : http://{host}:{port}/RealData
 *
 * 请求参数：
 *   tagids   : 点ID集合（逗号分隔），与 tagnames 至少传一个
 *   tagnames : 点名称集合（逗号分隔）
 *   timetype : 0 字符串时间，1 时间戳（默认 1）
 *   charset  : 返回数据编码（默认 utf-8）
 *   archived : 返回数据是否压缩（默认 false）
 *
 * 响应：
 *   { "code": 0, "mesg": "succeed",
 *     "data": { "count": n, "values": [ {pid, name, pv, tm, qy} ] } }
 */
@Slf4j
@Service
public class RealDataApiService {

    @Autowired
    private ColdSourceProperties properties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * WebApi 基础地址，如 http://10.22.163.239:8080
     */
    public String getBaseUrl() {
        String webApiHost = properties.getWebApiHost();
        if (webApiHost == null || webApiHost.trim().isEmpty()) {
            webApiHost = properties.getHost();
        }
        return "http://" + webApiHost + ":" + properties.getWebApiPort();
    }

    /**
     * 按点ID集合获取实时数据
     *
     * @param tagIds 点ID集合
     * @return 实时数据响应
     */
    public RealDataResp getRealDataByTagIds(List<Long> tagIds) {
        return getRealData(joinComma(tagIds), null, 1);
    }

    /**
     * 按点长名集合获取实时数据
     *
     * @param tagNames 点长名集合
     * @return 实时数据响应
     */
    public RealDataResp getRealDataByTagNames(List<String> tagNames) {
        return getRealData(null, joinComma(tagNames), 1);
    }

    /**
     * 获取实时数据（通用入口，对应文档 /RealData）
     *
     * @param tagIds   点ID集合（逗号分隔），可为 null
     * @param tagNames 点名称集合（逗号分隔），可为 null
     * @param timetype 0 字符串时间，1 时间戳
     * @return 实时数据响应
     */
    public RealDataResp getRealData(String tagIds, String tagNames, Integer timetype) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (tagIds != null && !tagIds.trim().isEmpty()) {
                params.put("tagids", tagIds.trim());
            }
            if (tagNames != null && !tagNames.trim().isEmpty()) {
                params.put("tagnames", tagNames.trim());
            }
            if (params.isEmpty()) {
                throw new IllegalArgumentException("tagids 与 tagnames 至少传一个");
            }
            params.put("timetype", timetype == null ? 1 : timetype);
            params.put("charset", "utf-8");
            params.put("archived", 0);

            String url = getBaseUrl() + "/RealData";
            String respBody = HttpRequest.post(url)
                    .timeout(properties.getWebApiTimeoutMs())
                    .header("Content-Type", "application/json")
                    .body(objectMapper.writeValueAsString(params))
                    .execute()
                    .body();
            log.debug("RealData 响应: {}", respBody);
            return objectMapper.readValue(respBody, RealDataResp.class);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 pSpace /RealData 获取实时数据失败", e);
            throw new RuntimeException("调用 pSpace /RealData 获取实时数据失败: " + e.getMessage(), e);
        }
    }

    private String joinComma(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
