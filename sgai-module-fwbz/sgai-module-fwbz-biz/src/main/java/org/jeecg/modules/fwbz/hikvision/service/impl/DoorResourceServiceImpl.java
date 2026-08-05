package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.hikvision.entity.DoorResource;
import org.jeecg.modules.fwbz.hikvision.dto.DoorListVO;
import org.jeecg.modules.fwbz.hikvision.dto.DoorSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.DoorSearchResponse;
import org.jeecg.modules.fwbz.hikvision.dto.DoorStatusRequest;
import org.jeecg.modules.fwbz.hikvision.dto.DoorStatusResponse;
import org.jeecg.modules.fwbz.hikvision.service.IDoorResourceService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.hikvision.mapper.DoorResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** 海康门禁状态查询API路径 */
    private static final String DOOR_STATUS_API = "/api/acs/v1/door/states";

    /** 门禁点列表查询分页大小（最大1000） */
    private static final int PAGE_SIZE = 1000;

    /** 门禁状态批量查询每批最大数量 */
    private static final int DOOR_STATUS_BATCH_SIZE = 200;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncDoorStatus() {
        log.info("开始从海康平台同步门禁状态...");

        // 1. 从数据库获取所有门禁点的 indexCode
        List<DoorResource> dbList = baseMapper.selectList(new LambdaQueryWrapper<>());
        if (dbList.isEmpty()) {
            log.warn("数据库中没有门禁点记录，请先执行全量同步");
            return 0;
        }

        List<String> allIndexCodes = new ArrayList<>();
        for (DoorResource db : dbList) {
            if (db.getIndexCode() != null) {
                allIndexCodes.add(db.getIndexCode());
            }
        }
        log.info("数据库中共有{}条门禁点记录", allIndexCodes.size());

        // 2. 分批（每批最多200个）请求海康门禁状态接口
        Map<String, String> statusMap = fetchDoorStatusInBatches(allIndexCodes);
        if (statusMap.isEmpty()) {
            log.warn("海康门禁状态数据为空，跳过同步");
            return 0;
        }
        log.info("从海康获取到{}条门禁状态记录", statusMap.size());

        // 3. 逐一比对，只更新状态有变化的记录
        int updatedCount = 0;
        Date now = new Date();
        for (DoorResource db : dbList) {
            String indexCode = db.getIndexCode();
            if (indexCode == null) {
                continue;
            }
            String newState = statusMap.get(indexCode);
            if (newState == null) {
                continue;
            }
            // 仅当状态有变化时才更新
            String oldState = db.getDoorState();
            if (!newState.equals(oldState)) {
                db.setDoorState(newState);
                db.setGmtModified(now);
                baseMapper.updateById(db);
                updatedCount++;
                log.debug("门禁点[{}]状态变更: {} -> {}", indexCode, oldState, newState);
            }
        }
        log.info("门禁状态同步完成, 共更新{}条记录", updatedCount);
        return updatedCount;
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
     * 分批从海康拉取门禁状态数据
     * <p>每次最多查询200个门禁点，从数据库获取所有 indexCode 后分批请求。</p>
     *
     * @param allIndexCodes 数据库中所有门禁点 indexCode
     * @return indexCode -> doorState 映射表
     */
    private Map<String, String> fetchDoorStatusInBatches(List<String> allIndexCodes) {
        Map<String, String> statusMap = new HashMap<>();
        int totalBatches = (allIndexCodes.size() + DOOR_STATUS_BATCH_SIZE - 1) / DOOR_STATUS_BATCH_SIZE;

        for (int batchNo = 0; batchNo < totalBatches; batchNo++) {
            int fromIndex = batchNo * DOOR_STATUS_BATCH_SIZE;
            int toIndex = Math.min(fromIndex + DOOR_STATUS_BATCH_SIZE, allIndexCodes.size());
            List<String> batch = allIndexCodes.subList(fromIndex, toIndex);

            try {
                DoorStatusRequest request = new DoorStatusRequest()
                        .setDoorIndexCodes(batch);
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康门禁状态, 第{}/{}批, 本批{}条", batchNo + 1, totalBatches, batch.size());

                String responseBody = hikvisionUtil.doPostJson(DOOR_STATUS_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("海康门禁状态查询失败, 第{}/{}批: {}", batchNo + 1, totalBatches, responseBody);
                    throw new RuntimeException("海康门禁状态查询失败: " + responseBody);
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null) {
                    log.warn("海康返回的门禁状态data为空, 第{}/{}批", batchNo + 1, totalBatches);
                    continue;
                }

                DoorStatusResponse response = dataJson.toJavaObject(DoorStatusResponse.class);

                // 记录无权限的门禁点
                List<String> noAuthList = response.getNoAuthDoorIndexCodeList();
                if (noAuthList != null && !noAuthList.isEmpty()) {
                    log.warn("第{}/{}批有{}个门禁点无权限: {}", batchNo + 1, totalBatches, noAuthList.size(), noAuthList);
                }

                // 解析有权限的门禁点状态
                List<DoorStatusResponse.DoorStatusItem> authList = response.getAuthDoorList();
                if (authList != null) {
                    for (DoorStatusResponse.DoorStatusItem item : authList) {
                        if (item.getDoorIndexCode() != null && item.getDoorState() != null) {
                            statusMap.put(item.getDoorIndexCode(), String.valueOf(item.getDoorState()));
                        }
                    }
                }
                log.info("第{}/{}批门禁状态拉取完成, 本批获取{}条, 累计{}条",
                        batchNo + 1, totalBatches,
                        authList != null ? authList.size() : 0,
                        statusMap.size());

            } catch (Exception e) {
                log.error("拉取海康门禁状态数据异常, 第{}/{}批", batchNo + 1, totalBatches, e);
                throw new RuntimeException("拉取海康门禁状态数据失败: " + e.getMessage(), e);
            }
        }

        log.info("海康门禁状态数据全部拉取完成, 共获取{}条", statusMap.size());
        return statusMap;
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

    @Override
    public List<DoorListVO> getDoorList() {
        log.info("查询本地数据库中全部门禁点列表");
        List<DoorResource> doorList = list();
        List<DoorListVO> result = new ArrayList<>(doorList.size());
        for (DoorResource door : doorList) {
            DoorListVO vo = new DoorListVO();
            vo.setIndexCode(door.getIndexCode());
            vo.setName(door.getName());
            vo.setDoorNo(door.getDoorNo());
            vo.setChannelNo(door.getChannelNo());
            vo.setRegionIndexCode(door.getRegionIndexCode());
            vo.setRegionName(door.getRegionName());
            vo.setInstallLocation(door.getInstallLocation());
            vo.setDoorState(door.getDoorState());
            vo.setTreatyType(door.getTreatyType());
            vo.setCreateTime(door.getCreateTime());
            vo.setUpdateTime(door.getUpdateTime());
            result.add(vo);
        }
        log.info("查询门禁点列表完成, 共{}条", result.size());
        return result;
    }
}
