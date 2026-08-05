package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.entity.DoorResource;
import org.jeecg.modules.fwbz.hikvision.dto.DoorSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.DoorSearchResponse;
import org.jeecg.modules.fwbz.hikvision.service.IDoorResourceService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.mapper.DoorResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 门禁点资源同步服务实现
 * <p>每次同步先清空表，再全量拉取海康数据批量插入。</p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class DoorResourceServiceImpl extends ServiceImpl<DoorResourceMapper, DoorResource>
        implements IDoorResourceService {

    /** 海康门禁点查询API路径 */
    private static final String DOOR_SEARCH_API = "/api/resource/v2/door/search";

    /** 固定分页大小（最大1000） */
    private static final int PAGE_SIZE = 1000;

    private final HikvisionUtil hikvisionUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromHikvision() {
        log.info("开始从海康平台全量同步门禁点数据...");

        // 1. 先逐页从海康拉取全部数据
        List<DoorSearchResponse.DoorItem> allItems = fetchAllFromHikvision();

        // 2. 判断海康返回数据是否为空，为空则不处理
        if (allItems.isEmpty()) {
            log.warn("海康未返回任何门禁点数据，跳过同步，保留现有记录");
            return 0;
        }

        // 3. 清空表全部数据
        int deletedCount = baseMapper.delete(null);
        log.info("已清空门禁点资源表, 删除{}条记录", deletedCount);

        // 4. 批量转换并插入
        Date now = new Date();
        List<DoorResource> entityList = new ArrayList<>(allItems.size());
        for (DoorSearchResponse.DoorItem item : allItems) {
            DoorResource entity = convertToEntity(item);
            entity.setGmtCreate(now);
            entity.setGmtModified(now);
            entityList.add(entity);
        }

        saveBatch(entityList);
        log.info("海康门禁点数据全量同步完成, 共同步{}条", entityList.size());
        return entityList.size();
    }

    /**
     * 逐页从海康拉取全部门禁点数据
     *
     * @return 全部门禁点列表
     */
    private List<DoorSearchResponse.DoorItem> fetchAllFromHikvision() {
        List<DoorSearchResponse.DoorItem> allItems = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            DoorSearchRequest request = buildFixedRequest(pageNo);

            try {
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康门禁点列表, pageNo={}, pageSize={}", pageNo, PAGE_SIZE);

                String responseBody = hikvisionUtil.doPostJson(DOOR_SEARCH_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("海康门禁点查询失败: {}", responseBody);
                    throw new RuntimeException("海康门禁点查询失败: " + responseBody);
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null) {
                    log.warn("海康返回的data为空");
                    break;
                }

                DoorSearchResponse response = dataJson.toJavaObject(DoorSearchResponse.class);
                List<DoorSearchResponse.DoorItem> doorList = response.getList();

                if (doorList == null || doorList.isEmpty()) {
                    log.info("海康门禁点列表为空，拉取结束");
                    break;
                }

                allItems.addAll(doorList);
                log.info("第{}页拉取完成, 本页{}条, 累计{}条", pageNo, doorList.size(), allItems.size());

                // 判断是否还有下一页
                int total = response.getTotal() != null ? response.getTotal() : 0;
                if (pageNo * PAGE_SIZE >= total) {
                    hasMore = false;
                } else {
                    pageNo++;
                }

            } catch (Exception e) {
                log.error("拉取海康门禁点数据异常, pageNo={}", pageNo, e);
                throw new RuntimeException("拉取海康门禁点数据失败: " + e.getMessage(), e);
            }
        }

        log.info("海康数据拉取完成, 共获取{}条门禁点记录", allItems.size());
        return allItems;
    }

    /**
     * 构建固定的查询请求参数
     */
    private DoorSearchRequest buildFixedRequest(int pageNo) {
        DoorSearchRequest request = new DoorSearchRequest();
        request.setPageNo(pageNo);
        request.setPageSize(PAGE_SIZE);
        return request;
    }

    /**
     * 将海康返回的门禁点数据转换为数据库实体
     */
    private DoorResource convertToEntity(DoorSearchResponse.DoorItem item) {
        DoorResource entity = new DoorResource();
        entity.setIndexCode(item.getIndexCode());
        entity.setResourceType(item.getResourceType());
        entity.setName(item.getName());
        entity.setDoorNo(item.getDoorNo());
        entity.setChannelNo(item.getChannelNo());
        entity.setParentIndexCode(item.getParentIndexCode());
        entity.setControlOneId(item.getControlOneId());
        entity.setControlTwoId(item.getControlTwoId());
        entity.setReaderInId(item.getReaderInId());
        entity.setReaderOutId(item.getReaderOutId());
        entity.setDoorSerial(item.getDoorSerial());
        entity.setTreatyType(item.getTreatyType());
        entity.setRegionIndexCode(item.getRegionIndexCode());
        entity.setRegionPath(item.getRegionPath());
        entity.setCreateTime(item.getCreateTime());
        entity.setUpdateTime(item.getUpdateTime());
        entity.setDescription(item.getDescription());
        entity.setChannelType(item.getChannelType());
        entity.setRegionName(item.getRegionName());
        entity.setRegionPathName(item.getRegionPathName());
        entity.setInstallLocation(item.getInstallLocation());
        return entity;
    }
}
