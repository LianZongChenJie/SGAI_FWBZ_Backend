package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDeviceListVO;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDevicePageDto;
import org.jeecg.modules.fwbz.hikvision.entity.AcsDevice;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDeviceOnlineRequest;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDeviceOnlineResponse;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDeviceSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDeviceSearchResponse;
import org.jeecg.modules.fwbz.hikvision.entity.RegionResource;
import org.jeecg.modules.fwbz.hikvision.service.IAcsDeviceService;
import org.jeecg.modules.fwbz.hikvision.service.IRegionResourceService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.hikvision.mapper.AcsDeviceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 门禁设备同步服务实现
 * <p>每次同步先清空表，再全量拉取海康数据批量插入。</p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class AcsDeviceServiceImpl extends ServiceImpl<AcsDeviceMapper, AcsDevice>
        implements IAcsDeviceService {

    private static final String ACS_DEVICE_SEARCH_API = "/api/resource/v2/acsDevice/search";

    private static final String ACS_DEVICE_ONLINE_API = "/api/nms/v1/online/acs_device/get";

    private static final int PAGE_SIZE = 1000;

    /** 在线状态批量查询每批最大数量 */
    private static final int ONLINE_BATCH_SIZE = 500;

    private final HikvisionUtil hikvisionUtil;

    private final IRegionResourceService regionResourceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromHikvision() {
        log.info("开始从海康平台全量同步门禁设备数据...");

        List<AcsDeviceSearchResponse.AcsDeviceItem> allItems = fetchAllFromHikvision();

        if (allItems.isEmpty()) {
            log.warn("海康未返回任何门禁设备数据，跳过同步，保留现有记录");
            return 0;
        }

        int deletedCount = baseMapper.delete(null);
        log.info("已清空门禁设备资源表, 删除{}条记录", deletedCount);

        Date now = new Date();
        List<AcsDevice> entityList = new ArrayList<>(allItems.size());
        for (AcsDeviceSearchResponse.AcsDeviceItem item : allItems) {
            AcsDevice entity = convertToEntity(item);
            entity.setGmtCreate(now);
            entity.setGmtModified(now);
            entityList.add(entity);
        }

        // 达梦驱动对JDBC批量(executeBatch)支持有缺陷，大数据量时会抛index out of range，改为循环单条插入绕开该问题
        for (AcsDevice entity : entityList) {
            baseMapper.insert(entity);
        }
        log.info("海康门禁设备数据全量同步完成, 共同步{}条", entityList.size());
        return entityList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncOnlineStatus() {
        log.info("开始从海康平台同步门禁设备在线状态...");

        // 1. 从数据库获取所有门禁设备的 indexCode
        List<AcsDevice> dbList = baseMapper.selectList(new LambdaQueryWrapper<>());
        if (dbList.isEmpty()) {
            log.warn("数据库中没有门禁设备记录，请先执行全量同步");
            return 0;
        }

        List<String> allIndexCodes = new ArrayList<>();
        for (AcsDevice db : dbList) {
            if (db.getIndexCode() != null) {
                allIndexCodes.add(db.getIndexCode());
            }
        }
        log.info("数据库中共有{}条门禁设备记录", allIndexCodes.size());

        // 2. 分批（每批最多500个）请求海康在线状态接口
        Map<String, String> onlineMap = fetchOnlineStatusInBatches(allIndexCodes);
        if (onlineMap.isEmpty()) {
            log.warn("海康门禁设备在线状态数据为空，跳过同步");
            return 0;
        }
        log.info("从海康获取到{}条设备在线状态", onlineMap.size());

        // 3. 逐一比对，只更新状态有变化的记录
        int updatedCount = 0;
        Date now = new Date();
        for (AcsDevice db : dbList) {
            String indexCode = db.getIndexCode();
            if (indexCode == null) {
                continue;
            }
            String newOnline = onlineMap.get(indexCode);
            if (newOnline == null) {
                continue;
            }
            String oldOnline = db.getOnline();
            if (!newOnline.equals(oldOnline)) {
                db.setOnline(newOnline);
                db.setGmtModified(now);
                baseMapper.updateById(db);
                updatedCount++;
                log.debug("门禁设备[{}]在线状态变更: {} -> {}", indexCode, oldOnline, newOnline);
            }
        }
        log.info("门禁设备在线状态同步完成, 共更新{}条记录", updatedCount);
        return updatedCount;
    }

    private List<AcsDeviceSearchResponse.AcsDeviceItem> fetchAllFromHikvision() {
        List<AcsDeviceSearchResponse.AcsDeviceItem> allItems = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            AcsDeviceSearchRequest request = new AcsDeviceSearchRequest()
                    .setPageNo(pageNo)
                    .setPageSize(PAGE_SIZE);

            try {
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康门禁设备列表, pageNo={}, pageSize={}", pageNo, PAGE_SIZE);

                String responseBody = hikvisionUtil.doPostJson(ACS_DEVICE_SEARCH_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("海康门禁设备查询失败: {}", responseBody);
                    throw new RuntimeException("海康门禁设备查询失败: " + responseBody);
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null) {
                    log.warn("海康返回的data为空");
                    break;
                }

                AcsDeviceSearchResponse response = dataJson.toJavaObject(AcsDeviceSearchResponse.class);
                List<AcsDeviceSearchResponse.AcsDeviceItem> deviceList = response.getList();

                if (deviceList == null || deviceList.isEmpty()) {
                    log.info("海康门禁设备列表为空，拉取结束");
                    break;
                }

                allItems.addAll(deviceList);
                log.info("第{}页拉取完成, 本页{}条, 累计{}条", pageNo, deviceList.size(), allItems.size());

                int total = response.getTotal() != null ? response.getTotal() : 0;
                if (pageNo * PAGE_SIZE >= total) {
                    hasMore = false;
                } else {
                    pageNo++;
                }

            } catch (Exception e) {
                log.error("拉取海康门禁设备数据异常, pageNo={}", pageNo, e);
                throw new RuntimeException("拉取海康门禁设备数据失败: " + e.getMessage(), e);
            }
        }

        log.info("海康数据拉取完成, 共获取{}条门禁设备记录", allItems.size());
        return allItems;
    }

    /**
     * 分批从海康拉取门禁设备在线状态
     * <p>每次最多查询500个设备，从数据库获取所有 indexCode 后分批请求。</p>
     *
     * @param allIndexCodes 数据库中所有门禁设备 indexCode
     * @return indexCode -> online(0/1) 映射表
     */
    private Map<String, String> fetchOnlineStatusInBatches(List<String> allIndexCodes) {
        Map<String, String> onlineMap = new HashMap<>();
        int totalBatches = (allIndexCodes.size() + ONLINE_BATCH_SIZE - 1) / ONLINE_BATCH_SIZE;

        for (int batchNo = 0; batchNo < totalBatches; batchNo++) {
            int fromIndex = batchNo * ONLINE_BATCH_SIZE;
            int toIndex = Math.min(fromIndex + ONLINE_BATCH_SIZE, allIndexCodes.size());
            List<String> batch = allIndexCodes.subList(fromIndex, toIndex);

            try {
                AcsDeviceOnlineRequest request = new AcsDeviceOnlineRequest()
                        .setIndexCodes(batch);
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康门禁设备在线状态, 第{}/{}批, 本批{}条", batchNo + 1, totalBatches, batch.size());

                String responseBody = hikvisionUtil.doPostJson(ACS_DEVICE_ONLINE_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("海康门禁设备在线状态查询失败, 第{}/{}批: {}", batchNo + 1, totalBatches, responseBody);
                    throw new RuntimeException("海康门禁设备在线状态查询失败: " + responseBody);
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null) {
                    log.warn("海康返回的在线状态data为空, 第{}/{}批", batchNo + 1, totalBatches);
                    continue;
                }

                AcsDeviceOnlineResponse response = dataJson.toJavaObject(AcsDeviceOnlineResponse.class);
                List<AcsDeviceOnlineResponse.OnlineItem> onlineList = response.getList();

                if (onlineList != null) {
                    for (AcsDeviceOnlineResponse.OnlineItem item : onlineList) {
                        if (item.getIndexCode() != null && item.getOnline() != null) {
                            onlineMap.put(item.getIndexCode(), String.valueOf(item.getOnline()));
                        }
                    }
                }
                log.info("第{}/{}批在线状态拉取完成, 本批获取{}条, 累计{}条",
                        batchNo + 1, totalBatches,
                        onlineList != null ? onlineList.size() : 0,
                        onlineMap.size());

            } catch (Exception e) {
                log.error("拉取海康门禁设备在线状态异常, 第{}/{}批", batchNo + 1, totalBatches, e);
                throw new RuntimeException("拉取海康门禁设备在线状态失败: " + e.getMessage(), e);
            }
        }

        log.info("海康门禁设备在线状态全部拉取完成, 共获取{}条", onlineMap.size());
        return onlineMap;
    }

    @Override
    public IPage<AcsDeviceListVO> getDeviceList(AcsDevicePageDto dto) {
        log.info("分页查询门禁设备列表, pageNo={}, pageSize={}, name={}, devTypeCode={}, regionName={}, online={}, ip={}",
                dto.getPageNo(), dto.getPageSize(), dto.getName(), dto.getDevTypeCode(), dto.getRegionName(), dto.getOnline(), dto.getIp());

        LambdaQueryWrapper<AcsDevice> wrapper = new LambdaQueryWrapper<AcsDevice>()
                .like(StringUtils.isNotBlank(dto.getName()), AcsDevice::getName, dto.getName())
                .eq(StringUtils.isNotBlank(dto.getDevTypeCode()), AcsDevice::getDevTypeCode, dto.getDevTypeCode())
                .eq(StringUtils.isNotBlank(dto.getOnline()), AcsDevice::getOnline, dto.getOnline())
                .like(StringUtils.isNotBlank(dto.getIp()), AcsDevice::getIp, dto.getIp());

        // 区域名称过滤 —— 联动table_region_resource表
        if (StringUtils.isNotBlank(dto.getRegionName())) {
            List<String> matchedRegionCodes = regionResourceService.lambdaQuery()
                    .like(RegionResource::getName, dto.getRegionName())
                    .select(RegionResource::getIndexCode)
                    .list()
                    .stream()
                    .map(RegionResource::getIndexCode)
                    .collect(Collectors.toList());
            if (matchedRegionCodes.isEmpty()) {
                IPage<AcsDeviceListVO> emptyPage = new Page<>(dto.getPageNo(), dto.getPageSize());
                emptyPage.setRecords(Collections.emptyList());
                emptyPage.setTotal(0);
                log.info("分页查询门禁设备列表完成, 未匹配到区域名称[{}], 返回空", dto.getRegionName());
                return emptyPage;
            }
            wrapper.in(AcsDevice::getRegionIndexCode, matchedRegionCodes);
        }

        IPage<AcsDevice> devicePage = page(new Page<>(dto.getPageNo(), dto.getPageSize()), wrapper);

        // 收集所有regionIndexCode，联动table_region_resource表获取区域名称
        Set<String> regionIndexCodes = devicePage.getRecords().stream()
                .map(AcsDevice::getRegionIndexCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, String> regionNameMap = Collections.emptyMap();
        if (!regionIndexCodes.isEmpty()) {
            regionNameMap = regionResourceService.lambdaQuery()
                    .in(RegionResource::getIndexCode, regionIndexCodes)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(RegionResource::getIndexCode, RegionResource::getName, (v1, v2) -> v1));
        }

        List<AcsDeviceListVO> voList = new ArrayList<>((int) devicePage.getSize());
        for (AcsDevice device : devicePage.getRecords()) {
            AcsDeviceListVO vo = new AcsDeviceListVO();
            vo.setIndexCode(device.getIndexCode());
            vo.setName(device.getName());
            vo.setDevTypeCode(device.getDevTypeCode());
            vo.setDevTypeDesc(device.getDevTypeDesc());
            vo.setDeviceCode(device.getDeviceCode());
            vo.setManufacturer(device.getManufacturer());
            vo.setRegionIndexCode(device.getRegionIndexCode());
            // 优先从地域资源表获取区域名称，兜底使用设备表中冗余存储的名称
            String resolvedRegionName = regionNameMap.getOrDefault(device.getRegionIndexCode(), device.getRegionName());
            vo.setRegionName(resolvedRegionName);
            vo.setTreatyType(device.getTreatyType());
            vo.setIp(device.getIp());
            vo.setPort(device.getPort());
            vo.setOnline(device.getOnline());
            vo.setCreateTime(device.getCreateTime());
            vo.setUpdateTime(device.getDevUpdateTime());
            voList.add(vo);
        }

        IPage<AcsDeviceListVO> resultPage = new Page<>(dto.getPageNo(), dto.getPageSize(), devicePage.getTotal());
        resultPage.setRecords(voList);

        log.info("分页查询门禁设备列表完成, 共{}条, 当前页{}条", devicePage.getTotal(), voList.size());
        return resultPage;
    }

    private AcsDevice convertToEntity(AcsDeviceSearchResponse.AcsDeviceItem item) {
        AcsDevice entity = new AcsDevice();
        entity.setIndexCode(item.getIndexCode());
        entity.setResourceType(item.getResourceType());
        entity.setName(item.getName());
        entity.setParentIndexCode(item.getParentIndexCode());
        entity.setDevTypeCode(item.getDevTypeCode());
        entity.setDevTypeDesc(item.getDevTypeDesc());
        entity.setDeviceCode(item.getDeviceCode());
        entity.setManufacturer(item.getManufacturer());
        entity.setRegionIndexCode(item.getRegionIndexCode());
        entity.setRegionPath(item.getRegionPath());
        entity.setTreatyType(item.getTreatyType());
        entity.setCardCapacity(item.getCardCapacity());
        entity.setFingerCapacity(item.getFingerCapacity());
        entity.setVeinCapacity(item.getVeinCapacity());
        entity.setFaceCapacity(item.getFaceCapacity());
        entity.setDoorCapacity(item.getDoorCapacity());
        entity.setDeployId(item.getDeployId());
        entity.setNetZoneId(item.getNetZoneId());
        entity.setCreateTime(item.getCreateTime());
        entity.setDevUpdateTime(item.getUpdateTime());
        entity.setDescription(item.getDescription());
        entity.setAcsReaderVerifyModeAbility(item.getAcsReaderVerifyModeAbility());
        entity.setRegionName(item.getRegionName());
        entity.setRegionPathName(item.getRegionPathName());
        entity.setIp(item.getIp());
        entity.setPort(item.getPort());
        entity.setCapability(item.getCapability());
        entity.setDevSerialNum(item.getDevSerialNum());
        entity.setDataVersion(item.getDataVersion());
        return entity;
    }
}
