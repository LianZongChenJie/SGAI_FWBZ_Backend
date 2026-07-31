package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.entity.CameraResource;
import org.jeecg.modules.fwbz.hikvision.dto.CameraOnlineRequest;
import org.jeecg.modules.fwbz.hikvision.dto.CameraOnlineResponse;
import org.jeecg.modules.fwbz.hikvision.dto.CameraSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.CameraSearchResponse;
import org.jeecg.modules.fwbz.hikvision.service.ICameraResourceService;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.mapper.CameraResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.jeecg.modules.fwbz.hikvision.dto.CameraPlayUrlVO;
import org.jeecg.modules.fwbz.hikvision.dto.PlayUrlRequest;
import org.jeecg.modules.fwbz.hikvision.dto.PlayUrlResponse;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 摄像头资源同步服务实现
 * <p>每次同步先清空表，再全量拉取海康数据批量插入。</p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class CameraResourceServiceImpl extends ServiceImpl<CameraResourceMapper, CameraResource>
        implements ICameraResourceService {

    /** 海康摄像头查询API路径 */
    private static final String CAMERA_SEARCH_API = "/api/resource/v1/cameras";

    /** 海康获取摄像头播放地址API路径 */
    private static final String CAMERA_PREVIEW_URL_API = "/api/video/v1/cameras/previewURLs";

    /** 海康监控点在线状态查询API路径 */
    private static final String CAMERA_ONLINE_API = "/api/nms/v1/online/camera/get";

    /** 固定分页大小（最大1000） */
    private static final int PAGE_SIZE = 1000;

    /** 日期解析格式（兼容多种） */
    private static final String[] DATE_PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss"
    };

    private final HikvisionUtil hikvisionUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromHikvision() {
        log.info("开始从海康平台全量同步摄像头数据...");

        // 1. 先逐页从海康拉取全部数据
        List<CameraSearchResponse.CameraItem> allItems = fetchAllFromHikvision();

        // 2. 判断海康返回数据是否为空，为空则不处理
        if (allItems.isEmpty()) {
            log.warn("海康未返回任何摄像头数据，跳过同步，保留现有记录");
            return 0;
        }

        // 3. 清空表全部数据
        int deletedCount = baseMapper.delete(null);
        log.info("已清空摄像头资源表, 删除{}条记录", deletedCount);

        // 4. 批量转换并插入
        Date now = new Date();
        List<CameraResource> entityList = new ArrayList<>(allItems.size());
        for (CameraSearchResponse.CameraItem item : allItems) {
            CameraResource entity = convertToEntity(item);
            entity.setGmtCreate(now);
            entity.setGmtModified(now);
            entityList.add(entity);
        }

        saveBatch(entityList);
        log.info("海康摄像头数据全量同步完成, 共同步{}条", entityList.size());
        return entityList.size();
    }

    /**
     * 逐页从海康拉取全部摄像头数据
     *
     * @return 全部摄像头列表
     */
    private List<CameraSearchResponse.CameraItem> fetchAllFromHikvision() {
        List<CameraSearchResponse.CameraItem> allItems = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            CameraSearchRequest request = buildFixedRequest(pageNo);

            try {
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康摄像头列表, pageNo={}, pageSize={}", pageNo, PAGE_SIZE);

                String responseBody = hikvisionUtil.doPostJson(CAMERA_SEARCH_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("海康摄像头查询失败: {}", responseBody);
                    throw new RuntimeException("海康摄像头查询失败: " + responseBody);
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null) {
                    log.warn("海康返回的data为空");
                    break;
                }

                CameraSearchResponse response = dataJson.toJavaObject(CameraSearchResponse.class);
                List<CameraSearchResponse.CameraItem> cameraList = response.getList();

                if (cameraList == null || cameraList.isEmpty()) {
                    log.info("海康摄像头列表为空，拉取结束");
                    break;
                }

                allItems.addAll(cameraList);
                log.info("第{}页拉取完成, 本页{}条, 累计{}条", pageNo, cameraList.size(), allItems.size());

                // 判断是否还有下一页
                int total = response.getTotal() != null ? response.getTotal() : 0;
                if (pageNo * PAGE_SIZE >= total) {
                    hasMore = false;
                } else {
                    pageNo++;
                }

            } catch (Exception e) {
                log.error("拉取海康摄像头数据异常, pageNo={}", pageNo, e);
                throw new RuntimeException("拉取海康摄像头数据失败: " + e.getMessage(), e);
            }
        }

        log.info("海康数据拉取完成, 共获取{}条摄像头记录", allItems.size());
        return allItems;
    }

    /**
     * 构建固定的查询请求参数
     * <p>只传 pageNo 和 pageSize，拉取全部摄像头。</p>
     */
    private CameraSearchRequest buildFixedRequest(int pageNo) {
        CameraSearchRequest request = new CameraSearchRequest();
        request.setPageNo(pageNo);
        request.setPageSize(PAGE_SIZE);
        return request;
    }

    /**
     * 将海康返回的摄像头数据（v1接口）转换为数据库实体
     */
    private CameraResource convertToEntity(CameraSearchResponse.CameraItem item) {
        CameraResource entity = new CameraResource();
        entity.setIndexCode(item.getCameraIndexCode());
        entity.setName(item.getCameraName());
        entity.setCameraType(item.getCameraType());
        entity.setCapability(item.getCapabilitySet());
        entity.setChannelType(item.getChannelType());
        entity.setInstallLocation(item.getInstallLocation());
        entity.setRecordLocation(item.getRecordLocation());
        entity.setRegionIndexCode(item.getRegionIndexCode());
        entity.setTransType(item.getTransType());
        entity.setTreatyType(item.getTreatyType());
        entity.setExternalIndexCode(item.getGbIndexCode());

        // 通道号转换（String -> Integer）
        String channelNoStr = item.getChannelNo();
        if (channelNoStr != null && !channelNoStr.isEmpty() && !"null".equals(channelNoStr)) {
            try {
                entity.setChanNum(Integer.parseInt(channelNoStr));
            } catch (NumberFormatException e) {
                log.warn("通道号转换失败: {}", channelNoStr);
            }
        }

        // 经纬度转换（过滤"null"字符串）
        entity.setLongitude(parseBigDecimal(item.getLongitude()));
        entity.setLatitude(parseBigDecimal(item.getLatitude()));

        // 海拔（过滤"null"字符串）
        String altitude = item.getAltitude();
        entity.setElevation(altitude != null && !"null".equals(altitude) ? altitude : null);

        // 日期解析
        entity.setCreateTime(parseDate(item.getCreateTime()));
        entity.setUpdateTime(parseDate(item.getUpdateTime()));

        return entity;
    }

    /**
     * 安全解析BigDecimal（过滤"null"字符串）
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty() || "null".equals(value)) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("BigDecimal转换失败: {}", value);
            return null;
        }
    }

    /**
     * 解析日期字符串（兼容多种格式）
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        for (String pattern : DATE_PATTERNS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                return sdf.parse(dateStr);
            } catch (ParseException ignored) {
                // 尝试下一种格式
            }
        }
        log.warn("日期解析失败: {}", dateStr);
        return null;
    }

    @Override
    public List<CameraPlayUrlVO> getPlayUrls(List<String> cameraIndexCodes) {
        if (cameraIndexCodes == null || cameraIndexCodes.isEmpty()) {
            log.warn("获取播放地址失败: cameraIndexCodes为空");
            return Collections.emptyList();
        }

        log.info("开始从海康获取{}个摄像头的播放地址", cameraIndexCodes.size());
        List<CameraPlayUrlVO> result = new ArrayList<>();

        for (String cameraIndexCode : cameraIndexCodes) {
            try {
                PlayUrlRequest request = buildPlayUrlRequest(cameraIndexCode);
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康摄像头播放地址, cameraIndexCode={}", cameraIndexCode);

                String responseBody = hikvisionUtil.doPostJson(CAMERA_PREVIEW_URL_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("获取摄像头[{}]播放地址失败, 海康响应: {}", cameraIndexCode, responseBody);
                    continue;
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null) {
                    log.warn("摄像头[{}]海康返回的data为空", cameraIndexCode);
                    continue;
                }

                PlayUrlResponse playUrlResp = dataJson.toJavaObject(PlayUrlResponse.class);
                result.add(new CameraPlayUrlVO(cameraIndexCode, playUrlResp.getUrl()));
                log.info("摄像头[{}]播放地址获取成功: {}", cameraIndexCode, playUrlResp.getUrl());

            } catch (Exception e) {
                log.error("获取摄像头[{}]播放地址异常", cameraIndexCode, e);
            }
        }

        log.info("海康播放地址获取完成, 成功{}个/共{}个", result.size(), cameraIndexCodes.size());
        return result;
    }

    /**
     * 构建获取播放地址的固定请求参数
     */
    private PlayUrlRequest buildPlayUrlRequest(String cameraIndexCode) {
        PlayUrlRequest request = new PlayUrlRequest();
        request.setCameraIndexCode(cameraIndexCode);
        request.setStreamType(0);
        request.setProtocol("hls");
        request.setTransmode(1);
        request.setExpand("transcode=0");
        request.setStreamform("ps");
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncOnlineStatus() {
        log.info("开始从海康平台同步监控点在线状态...");

        // 1. 逐页从海康拉取全部在线状态数据
        Map<String, Integer> onlineStatusMap = fetchAllOnlineStatus();

        if (onlineStatusMap.isEmpty()) {
            log.warn("海康未返回任何在线状态数据，跳过同步");
            return 0;
        }

        // 2. 查询数据库中全部摄像头
        List<CameraResource> allCameras = list(new LambdaQueryWrapper<CameraResource>()
                .select(CameraResource::getId, CameraResource::getIndexCode, CameraResource::getOnline));

        // 3. 根据海康返回的在线状态更新
        int updatedCount = 0;
        List<CameraResource> toUpdate = new ArrayList<>();
        for (CameraResource camera : allCameras) {
            Integer onlineStatus = onlineStatusMap.get(camera.getIndexCode());
            if (onlineStatus != null) {
                // 只有状态变化时才更新
                if (!onlineStatus.equals(camera.getOnline())) {
                    camera.setOnline(onlineStatus);
                    camera.setGmtModified(new Date());
                    toUpdate.add(camera);
                }
            }
        }

        // 4. 批量更新
        if (!toUpdate.isEmpty()) {
            updateBatchById(toUpdate);
            updatedCount = toUpdate.size();
        }

        log.info("监控点在线状态同步完成, 海康返回{}条, 更新{}条, 库中共{}条",
                onlineStatusMap.size(), updatedCount, allCameras.size());
        return updatedCount;
    }

    /**
     * 逐页从海康拉取全部监控点在线状态
     *
     * @return indexCode -> online 的映射
     */
    private Map<String, Integer> fetchAllOnlineStatus() {
        Map<String, Integer> statusMap = new HashMap<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            CameraOnlineRequest request = new CameraOnlineRequest();
            request.setPageNo(pageNo);
            request.setPageSize(PAGE_SIZE);

            try {
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康监控点在线状态, pageNo={}, pageSize={}", pageNo, PAGE_SIZE);

                String responseBody = hikvisionUtil.doPostJson(CAMERA_ONLINE_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("海康在线状态查询失败: {}", responseBody);
                    throw new RuntimeException("海康在线状态查询失败: " + responseBody);
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null) {
                    log.warn("海康返回的data为空");
                    break;
                }

                CameraOnlineResponse response = dataJson.toJavaObject(CameraOnlineResponse.class);
                List<CameraOnlineResponse.OnlineItem> onlineList = response.getList();

                if (onlineList == null || onlineList.isEmpty()) {
                    log.info("海康在线状态列表为空，拉取结束");
                    break;
                }

                for (CameraOnlineResponse.OnlineItem item : onlineList) {
                    if (item.getIndexCode() != null && item.getOnline() != null) {
                        statusMap.put(item.getIndexCode(), item.getOnline());
                    }
                }

                log.info("第{}页在线状态拉取完成, 本页{}条, 累计{}条",
                        pageNo, onlineList.size(), statusMap.size());

                // 判断是否还有下一页
                int total = response.getTotal() != null ? response.getTotal() : 0;
                if (pageNo * PAGE_SIZE >= total) {
                    hasMore = false;
                } else {
                    pageNo++;
                }

            } catch (Exception e) {
                log.error("拉取海康在线状态异常, pageNo={}", pageNo, e);
                throw new RuntimeException("拉取海康在线状态失败: " + e.getMessage(), e);
            }
        }

        log.info("海康在线状态拉取完成, 共获取{}条", statusMap.size());
        return statusMap;
    }
}
