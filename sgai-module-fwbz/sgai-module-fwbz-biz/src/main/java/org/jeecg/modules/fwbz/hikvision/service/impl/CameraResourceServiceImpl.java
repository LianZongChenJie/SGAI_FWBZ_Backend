package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.entity.CameraResource;
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
import java.util.List;

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
    private static final String CAMERA_SEARCH_API = "/api/resource/v2/camera/advance/cameraList";

    /** 海康获取摄像头播放地址API路径 */
    private static final String CAMERA_PREVIEW_URL_API = "/api/video/v1/cameras/previewURLs";

    /** 固定分页大小 */
    private static final int PAGE_SIZE = 500;

    /** 固定权限码：只读 */
    private static final List<String> AUTH_CODES = Collections.singletonList("view");

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
     * <p>只传必要分页参数和权限码，拉取全部摄像头。</p>
     */
    private CameraSearchRequest buildFixedRequest(int pageNo) {
        CameraSearchRequest request = new CameraSearchRequest();
        request.setPageNo(pageNo);
        request.setPageSize(PAGE_SIZE);
        request.setAuthCodes(AUTH_CODES);
        return request;
    }

    /**
     * 将海康返回的摄像头数据转换为数据库实体
     */
    private CameraResource convertToEntity(CameraSearchResponse.CameraItem item) {
        CameraResource entity = new CameraResource();
        entity.setIndexCode(item.getIndexCode());
        entity.setResourceType(item.getResourceType());
        entity.setExternalIndexCode(item.getExternalIndexCode());
        entity.setName(item.getName());
        entity.setChanNum(item.getChanNum());
        entity.setCascadeCode(item.getCascadeCode());
        entity.setParentIndexCode(item.getParentIndexCode());

        // 经纬度转换
        if (item.getLongitude() != null && !item.getLongitude().isEmpty()) {
            try {
                entity.setLongitude(new BigDecimal(item.getLongitude()));
            } catch (NumberFormatException e) {
                log.warn("经度转换失败: {}", item.getLongitude());
            }
        }
        if (item.getLatitude() != null && !item.getLatitude().isEmpty()) {
            try {
                entity.setLatitude(new BigDecimal(item.getLatitude()));
            } catch (NumberFormatException e) {
                log.warn("纬度转换失败: {}", item.getLatitude());
            }
        }

        entity.setElevation(item.getElevation());
        entity.setCameraType(item.getCameraType());
        entity.setCapability(item.getCapability());
        entity.setRecordLocation(item.getRecordLocation());
        entity.setChannelType(item.getChannelType());
        entity.setRegionIndexCode(item.getRegionIndexCode());
        entity.setRegionPath(item.getRegionPath());
        entity.setTransType(item.getTransType());
        entity.setTreatyType(item.getTreatyType());
        entity.setInstallLocation(item.getInstallLocation());

        // 日期解析
        entity.setCreateTime(parseDate(item.getCreateTime()));
        entity.setUpdateTime(parseDate(item.getUpdateTime()));

        entity.setDisOrder(item.getDisOrder());
        entity.setResourceIndexCode(item.getResourceIndexCode());
        entity.setDecodeTag(item.getDecodeTag());
        entity.setCameraRelateTalk(item.getCameraRelateTalk());
        entity.setRegionName(item.getRegionName());
        entity.setRegionPathName(item.getRegionPathName());

        return entity;
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
}
