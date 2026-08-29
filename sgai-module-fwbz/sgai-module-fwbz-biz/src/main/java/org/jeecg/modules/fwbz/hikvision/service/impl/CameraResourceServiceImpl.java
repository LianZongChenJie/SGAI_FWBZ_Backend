package org.jeecg.modules.fwbz.hikvision.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.fwbz.hikvision.config.HlsProperties;
import org.jeecg.modules.fwbz.hikvision.entity.CameraGroup;
import org.jeecg.modules.fwbz.hikvision.entity.CameraInfo;
import org.jeecg.modules.fwbz.hikvision.entity.CameraResource;
import org.jeecg.modules.fwbz.hikvision.dto.CameraOnlineRequest;
import org.jeecg.modules.fwbz.hikvision.dto.CameraOnlineResponse;
import org.jeecg.modules.fwbz.hikvision.dto.CameraSearchRequest;
import org.jeecg.modules.fwbz.hikvision.dto.CameraSearchResponse;
import org.jeecg.modules.fwbz.hikvision.service.ICameraInfoService;
import org.jeecg.modules.fwbz.hikvision.service.ICameraResourceService;
import org.jeecg.modules.fwbz.hikvision.util.CameraHlsStream;
import org.jeecg.modules.fwbz.hikvision.util.HikvisionUtil;
import org.jeecg.modules.fwbz.hikvision.util.HlsStreamManager;
import org.jeecg.modules.fwbz.hikvision.mapper.CameraGroupMapper;
import org.jeecg.modules.fwbz.hikvision.mapper.CameraResourceMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import org.jeecg.modules.fwbz.hikvision.dto.CameraListVO;
import org.jeecg.modules.fwbz.hikvision.dto.CameraPlayUrlVO;
import org.jeecg.modules.fwbz.hikvision.dto.CameraResourcePageDto;
import org.jeecg.modules.fwbz.hikvision.dto.PlayUrlRequest;
import org.jeecg.modules.fwbz.hikvision.dto.PlayUrlResponse;
import org.jeecg.modules.fwbz.hikvision.dto.RegionCameraTreeVO;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 摄像头资源同步服务实现
 * <p>每次同步先清空表，再全量拉取海康数据批量插入。</p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CameraResourceServiceImpl extends ServiceImpl<CameraResourceMapper, CameraResource>
        implements ICameraResourceService {

    /**
     * 海康摄像头查询API路径
     */
    private static final String CAMERA_SEARCH_API = "/api/resource/v1/cameras";

    /**
     * 海康获取摄像头播放地址API路径
     */
    private static final String CAMERA_PREVIEW_URL_API = "/api/video/v2/cameras/previewURLs";

    /**
     * 海康监控点在线状态查询API路径
     */
    private static final String CAMERA_ONLINE_API = "/api/nms/v1/online/camera/get";

    /**
     * IOC平台摄像头分组数据API（返回分组树，含分组信息、下级摄像头列表与子分组）
     */
    private static final String IOC_PACKAGE_GROUP_API = "http://10.168.47.26:9999/sgai-ioc-data/admin/video/packageGroup";

    /**
     * 摄像头分组过滤关键字：仅保留一级分组名称包含该关键字的子树（服贸会、园区高点）
     */
    private static final List<String> PACKAGE_KEYWORD = Arrays.asList("服贸会", "园区高点");

    /**
     * 固定分页大小（最大1000）
     */
    private static final int PAGE_SIZE = 1000;

    /**
     * 日期解析格式（兼容多种）
     */
    private static final String[] DATE_PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss"
    };

    private final HikvisionUtil hikvisionUtil;

    /**
     * 摄像头分组信息 Mapper（table_camera_group 表）
     */
    private final CameraGroupMapper cameraGroupMapper;

    /**
     * 摄像头信息 Service（camera_info 表）
     */
    private final ICameraInfoService cameraInfoService;

    /**
     * HLS流管理器：负责RTSP拉流转码、流复用与无人观看自动停止
     */
    private final HlsStreamManager hlsStreamManager;

    /**
     * HLS转码相关配置
     */
    private final HlsProperties hlsProperties;

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

        // 达梦驱动对JDBC批量(executeBatch)支持有缺陷，大数据量时会抛index out of range，改为循环单条插入绕开该问题
        for (CameraResource entity : entityList) {
            baseMapper.insert(entity);
        }
        log.info("海康摄像头数据全量同步完成, 共同步{}条", entityList.size());
        return entityList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromIoc() {
        log.info("开始从IOC平台同步摄像头分组...");

        // 1. 调用IOC平台接口拉取分组树
        JSONArray groupTree = fetchIocPackageGroup();
        if (groupTree == null || groupTree.isEmpty()) {
            log.warn("IOC平台未返回任何分组数据，跳过同步，保留现有记录");
            return 0;
        }

        // 2. 递归解析分组树，仅收集分组信息（不处理摄像头列表）
        List<CameraGroup> groupList = new ArrayList<>();
        for (int i = 0; i < groupTree.size(); i++) {
            parseIocGroup(groupTree.getJSONObject(i), 0L, groupList);
        }
        log.info("IOC平台数据拉取完成, 分组{}个", groupList.size());

        // 3. 清空分组表（全量同步策略）
        int deletedGroupCount = cameraGroupMapper.delete(null);
        log.info("已清空摄像头分组表{}条", deletedGroupCount);

        // 4. 插入分组（达梦驱动对JDBC批量插入支持有缺陷，循环单条插入绕开该问题）
        for (CameraGroup group : groupList) {
            cameraGroupMapper.insert(group);
        }

        log.info("IOC平台分组同步完成, 共同步分组{}个", groupList.size());
        return groupList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncIocCameraList() {
        log.info("开始从IOC平台同步摄像头列表...");

        // 1. 与同步分组使用同一个IOC分组树接口（返回数据每个分组含 videoList 数组，即该分组下摄像头列表）
        JSONArray groupTree = fetchIocPackageGroup();
        if (groupTree == null || groupTree.isEmpty()) {
            log.warn("IOC平台未返回任何分组数据，跳过同步，保留现有记录");
            return 0;
        }

        // 2. 递归遍历分组树，收集各分组下的摄像头列表
        List<CameraInfo> cameraList = new ArrayList<>();
        for (int i = 0; i < groupTree.size(); i++) {
            parseIocVideo(groupTree.getJSONObject(i), cameraList);
        }
        log.info("IOC平台摄像头数据拉取完成, 摄像头{}个", cameraList.size());

        // 3. 清空摄像头信息表（全量同步策略）
        boolean deletedCount = cameraInfoService.remove(null);
        log.info("已清空摄像头信息表{}条", deletedCount);

        // 4. 插入摄像头（达梦驱动对JDBC批量插入支持有缺陷，循环单条插入绕开该问题）
        for (CameraInfo camera : cameraList) {
            cameraInfoService.save(camera);
        }

        log.info("IOC平台摄像头列表同步完成, 共同步{}个", cameraList.size());
        return cameraList.size();
    }

    /**
     * 调用IOC平台接口获取摄像头分组树
     *
     * @return 一级分组节点列表
     */
    private JSONArray fetchIocPackageGroup() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(factory);

        String body = restTemplate.exchange(IOC_PACKAGE_GROUP_API, HttpMethod.GET, null, String.class).getBody();
        JSONObject json = JSONObject.parseObject(body);
        Boolean success = json.getBoolean("success");
        if (success == null || !success) {
            throw new RuntimeException("IOC平台接口返回失败: " + body);
        }
        return json.getJSONArray("result");
    }

    /**
     * 递归解析IOC分组节点，仅收集分组信息
     * <p>节点包含分组信息（id/name/description/sortNum/dimension）、下级摄像头列表（videoList）和子分组（children），
     * 此处只处理分组信息与递归子分组，忽略摄像头列表。</p>
     *
     * @param node      当前分组节点
     * @param parentId  父分组ID，根节点为0
     * @param groupList 分组收集列表
     */
    private void parseIocGroup(JSONObject node, Long parentId, List<CameraGroup> groupList) {
        if (node == null) {
            return;
        }
        Long groupId = node.getLong("id");
        if (groupId == null) {
            log.warn("IOC分组节点缺少id字段, 跳过该分组: {}", node.toJSONString());
            return;
        }

        // 收集分组信息（各字段按表列长度截断，避免达梦"字符串截断"报错）
        CameraGroup group = new CameraGroup();
        group.setId(groupId);
        group.setName(truncate(node.getString("name"), 255));
        group.setDescription(truncate(node.getString("description"), 255));
        group.setSortNum(node.getInteger("sortNum"));
        group.setDimension(truncate(node.getString("dimension"), 255));
        group.setParentId(parentId);
        groupList.add(group);

        // 递归子分组
        JSONArray children = node.getJSONArray("children");
        if (children != null && !children.isEmpty()) {
            for (int i = 0; i < children.size(); i++) {
                parseIocGroup(children.getJSONObject(i), groupId, groupList);
            }
        }
    }

    /**
     * 递归遍历IOC分组树，收集各分组下的摄像头列表（videoList）
     * <p>与同步分组使用同一个IOC分组树接口，返回数据每个分组含 videoList 数组，
     * 即该分组下的摄像头列表。分组ID与分组名一并写入摄像头记录。</p>
     *
     * @param node       当前分组节点
     * @param cameraList 摄像头收集列表
     */
    private void parseIocVideo(JSONObject node, List<CameraInfo> cameraList) {
        if (node == null) {
            return;
        }
        Long groupId = node.getLong("id");
        String groupName = node.getString("name");

        // 收集当前分组下的摄像头列表
        JSONArray videoList = node.getJSONArray("videoList");
        if (videoList != null && !videoList.isEmpty()) {
            for (int i = 0; i < videoList.size(); i++) {
                CameraInfo camera = convertIocVideoToCameraInfo(videoList.getJSONObject(i), groupId, groupName);
                if (camera != null) {
                    cameraList.add(camera);
                }
            }
        }

        // 递归子分组
        JSONArray children = node.getJSONArray("children");
        if (children != null && !children.isEmpty()) {
            for (int i = 0; i < children.size(); i++) {
                parseIocVideo(children.getJSONObject(i), cameraList);
            }
        }
    }

    /**
     * 将IOC平台摄像头节点转换为摄像头信息实体
     * <p>映射关系：systemId 对应 system_id（摄像头唯一标识），longitude/latitude 对应经纬度，
     * online 对应在线状态，所属分组ID/名称对应 group_id/group_name（各字段按表列长度截断，
     * 避免达梦"字符串截断"报错）。</p>
     *
     * @param video     IOC平台摄像头节点
     * @param groupId   所属分组ID
     * @param groupName 所属分组名称
     * @return 摄像头信息实体，systemId为空时返回null
     */
    private CameraInfo convertIocVideoToCameraInfo(JSONObject video, Long groupId, String groupName) {
        if (video == null) {
            return null;
        }
        String systemId = video.getString("systemId");
        if (StringUtils.isBlank(systemId)) {
            log.warn("IOC摄像头节点缺少systemId, 跳过该摄像头: {}", video.toJSONString());
            return null;
        }
        CameraInfo entity = new CameraInfo();
        entity.setSystemId(truncate(systemId, 64));
        entity.setName(truncate(video.getString("name"), 128));
        entity.setGroupId(groupId);
        entity.setGroupName(truncate(groupName, 128));
        entity.setLongitude(parseCoordinateString(video.getString("longitude")));
        entity.setLatitude(parseCoordinateString(video.getString("latitude")));
        Boolean online = video.getBoolean("online");
        entity.setOnline(Boolean.TRUE.equals(online) ? 1 : 0);
        return entity;
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
        // 各字段按表列长度截断，避免超出导致达梦"字符串截断"报错
        entity.setIndexCode(truncate(item.getCameraIndexCode(), 64));
        entity.setName(truncate(item.getCameraName(), 128));
        entity.setCameraType(item.getCameraType());
        entity.setCapability(truncate(item.getCapabilitySet(), 512));
        entity.setChannelType(truncate(item.getChannelType(), 16));
        entity.setInstallLocation(truncate(item.getInstallLocation(), 256));
        entity.setRecordLocation(truncate(item.getRecordLocation(), 32));
        entity.setRegionIndexCode(truncate(item.getRegionIndexCode(), 64));
        entity.setTransType(item.getTransType());
        entity.setTreatyType(truncate(item.getTreatyType(), 32));
        entity.setExternalIndexCode(truncate(item.getGbIndexCode(), 64));

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
     * 按数据库列长度截断字符串，超长时截断并记录警告（达梦VARCHAR超出列定义会报"字符串截断"）
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        log.warn("字段值超出列定义长度({}字符)，已截断处理, 原始长度={}", maxLength, value.length());
        return value.substring(0, maxLength);
    }

    /**
     * 安全解析坐标字符串（过滤"null"字符串）
     */
    private String parseCoordinateString(String value) {
        if (value == null || value.isEmpty() || "null".equals(value)) {
            return null;
        }
        return value;
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
     * BigDecimal转字符串（去除科学计数法，保留原样）
     */
    private String bigDecimalToStr(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.toPlainString();
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

        log.info("开始获取{}个摄像头的本地HLS播放地址", cameraIndexCodes.size());
        List<CameraPlayUrlVO> result = new ArrayList<>();

        for (String cameraIndexCode : cameraIndexCodes) {
            try {
                // 1. 请求海康SDK获取RTSP播放地址
                PlayUrlRequest request = buildPlayUrlRequest(cameraIndexCode);
                String requestBody = JSON.toJSONString(request);
                log.info("请求海康摄像头RTSP播放地址, cameraIndexCode={}", cameraIndexCode);

                String responseBody = hikvisionUtil.doPostJson(CAMERA_PREVIEW_URL_API, requestBody);

                if (!hikvisionUtil.isSuccess(responseBody)) {
                    log.error("获取摄像头[{}]RTSP地址失败, 海康响应: {}", cameraIndexCode, responseBody);
                    continue;
                }

                JSONObject dataJson = hikvisionUtil.getResponseData(responseBody);
                if (dataJson == null || StringUtils.isBlank(dataJson.getString("url"))) {
                    log.warn("摄像头[{}] 海康未返回RTSP地址", cameraIndexCode);
                    continue;
                }
                String rtspUrl = dataJson.getString("url");
                log.info("摄像头[{}] RTSP地址获取成功: {}", cameraIndexCode, rtspUrl);

                // 2. 通过HLS流管理器获取本地HLS流：同一摄像头正在拉流时直接复用，不重复转码
                CameraHlsStream stream = hlsStreamManager.getOrCreate(cameraIndexCode, rtspUrl);
                if (stream == null) {
                    log.error("摄像头[{}] HLS转码任务创建失败", cameraIndexCode);
                    continue;
                }

                // 3. 等待HLS流就绪（首个切片已生成），超时仍返回地址由前端自行重试
                boolean ready = stream.awaitReady(hlsProperties.getReadyWaitSeconds());
                if (!ready) {
                    if (!stream.isRunning()) {
                        // 拉流启动失败，清理无效任务
                        log.error("摄像头[{}] HLS转码启动失败: {}", cameraIndexCode, stream.getErrorMessage());
                        hlsStreamManager.removeStream(cameraIndexCode);
                        continue;
                    }
                    log.warn("摄像头[{}] HLS流未在{}s内就绪, 仍返回地址由前端重试",
                            cameraIndexCode, hlsProperties.getReadyWaitSeconds());
                }

                // 4. 返回本地HLS相对播放地址（由Controller拼装完整访问地址）
                result.add(new CameraPlayUrlVO(cameraIndexCode, stream.getHlsRelativeUrl()));
                log.info("摄像头[{}] 本地HLS播放地址: {}", cameraIndexCode, stream.getHlsRelativeUrl());
            } catch (Exception e) {
                log.error("获取摄像头[{}]本地HLS播放地址异常", cameraIndexCode, e);
            }
        }

        log.info("本地HLS播放地址获取完成, 成功{}个/共{}个", result.size(), cameraIndexCodes.size());
        return result;
    }

    @Override
    public void releasePlay(List<String> cameraIndexCodes) {
        if (cameraIndexCodes == null || cameraIndexCodes.isEmpty()) {
            return;
        }
        for (String cameraIndexCode : cameraIndexCodes) {
            hlsStreamManager.release(cameraIndexCode);
        }
        log.info("已释放{}个摄像头的观看, 无人观看时将自动停止拉流", cameraIndexCodes.size());
    }

    @Override
    public void heartbeat(List<String> cameraIndexCodes) {
        if (cameraIndexCodes == null || cameraIndexCodes.isEmpty()) {
            return;
        }
        for (String cameraIndexCode : cameraIndexCodes) {
            hlsStreamManager.heartbeat(cameraIndexCode);
        }
        log.debug("已续期{}个摄像头的心跳", cameraIndexCodes.size());
    }

    /**
     * 构建获取RTSP播放地址的固定请求参数（协议为rtsp，由JavaCV拉流转码为本地HLS）
     */
    private PlayUrlRequest buildPlayUrlRequest(String cameraIndexCode) {
        PlayUrlRequest request = new PlayUrlRequest();
        request.setCameraIndexCode(cameraIndexCode);
        request.setStreamType(0);
        request.setProtocol("rtsp");
        request.setTransmode(1);
        request.setExpand("transcode=0");
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

    @Override
    public List<CameraListVO> getCameraList() {
        log.info("查询camera_info表中全部摄像头列表");
        // 分组锁定为包含"服贸会"、"园区高点"关键字的（含其子分组）
        List<Long> packageGroupIds = collectPackageGroupIds();
        List<CameraInfo> cameraList;
        if (packageGroupIds.isEmpty()) {
            cameraList = Collections.emptyList();
        } else {
            cameraList = cameraInfoService.list(new LambdaQueryWrapper<CameraInfo>()
                    .in(CameraInfo::getGroupId, packageGroupIds));
        }
        List<CameraListVO> result = new ArrayList<>(cameraList.size());
        for (CameraInfo camera : cameraList) {
            result.add(cameraInfoToVO(camera));
        }
        log.info("查询摄像头列表完成, 共{}条", result.size());
        return result;
    }

    @Override
    public List<CameraListVO> getCameraListForExport() {
        log.info("查询导出用摄像头列表");
        // 分组锁定为包含"服贸会"、"园区高点"关键字的（含其子分组）
        List<Long> packageGroupIds = collectPackageGroupIds();
        if (packageGroupIds.isEmpty()) {
            log.info("查询导出用摄像头列表完成, 未匹配到[服贸会/园区高点]分组, 返回空");
            return Collections.emptyList();
        }
        List<CameraInfo> cameraList = cameraInfoService.list(new LambdaQueryWrapper<CameraInfo>()
                .in(CameraInfo::getGroupId, packageGroupIds));

        // 收集所有groupId，联动table_camera_group表获取分组名称
        Set<Long> groupIds = cameraList.stream()
                .map(CameraInfo::getGroupId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> groupNameMap = Collections.emptyMap();
        if (!groupIds.isEmpty()) {
            groupNameMap = cameraGroupMapper.selectList(
                            new LambdaQueryWrapper<CameraGroup>()
                                    .in(CameraGroup::getId, groupIds))
                    .stream()
                    .collect(Collectors.toMap(CameraGroup::getId, CameraGroup::getName, (v1, v2) -> v1));
        }

        List<CameraListVO> result = new ArrayList<>(cameraList.size());
        for (CameraInfo camera : cameraList) {
            CameraListVO vo = cameraInfoToVO(camera);
            // 优先使用分组表的名称，取不到时兜底camera_info冗余的groupName
            vo.setRegionName(groupNameMap.getOrDefault(camera.getGroupId(), camera.getGroupName()));
            result.add(vo);
        }
        log.info("查询导出用摄像头列表完成, 共{}条", result.size());
        return result;
    }

    @Override
    public IPage<CameraListVO> getCameraPage(CameraResourcePageDto dto) {
        log.info("分页查询摄像头列表, pageNo={}, pageSize={}, indexCode={}, name={}, regionName={}, treatyType={}, installLocation={}, online={}, cameraType={}",
                dto.getPageNo(), dto.getPageSize(), dto.getIndexCode(), dto.getName(), dto.getRegionName(),
                dto.getTreatyType(), dto.getInstallLocation(), dto.getOnline(), dto.getCameraType());

        // 分组锁定为包含"服贸会"、"园区高点"关键字的（含其子分组）
        List<Long> packageGroupIds = collectPackageGroupIds();
        if (packageGroupIds.isEmpty()) {
            IPage<CameraListVO> emptyPage = new Page<>(dto.getPageNo(), dto.getPageSize());
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);
            log.info("分页查询摄像头列表完成, 未匹配到[服贸会/园区高点]分组, 返回空");
            return emptyPage;
        }

        LambdaQueryWrapper<CameraInfo> wrapper = new LambdaQueryWrapper<CameraInfo>()
                .in(CameraInfo::getGroupId, packageGroupIds)
                .eq(StringUtils.isNotBlank(dto.getIndexCode()), CameraInfo::getSystemId, dto.getIndexCode())
                .like(StringUtils.isNotBlank(dto.getName()), CameraInfo::getName, dto.getName())
                .like(StringUtils.isNotBlank(dto.getInstallLocation()), CameraInfo::getPointPath, dto.getInstallLocation())
                .eq(dto.getOnline() != null, CameraInfo::getOnline, dto.getOnline())
                .eq(dto.getCameraType() != null, CameraInfo::getCameraType, dto.getCameraType())
                .orderByAsc(CameraInfo::getId);

        // 区域名称(分组名称)过滤 —— 联动table_camera_group表
        if (StringUtils.isNotBlank(dto.getRegionName())) {
            List<Long> matchedGroupIds = cameraGroupMapper.selectList(
                            new LambdaQueryWrapper<CameraGroup>()
                                    .like(CameraGroup::getName, dto.getRegionName())
                                    .select(CameraGroup::getId))
                    .stream()
                    .map(CameraGroup::getId)
                    .collect(Collectors.toList());
            if (matchedGroupIds.isEmpty()) {
                IPage<CameraListVO> emptyPage = new Page<>(dto.getPageNo(), dto.getPageSize());
                emptyPage.setRecords(Collections.emptyList());
                emptyPage.setTotal(0);
                log.info("分页查询摄像头列表完成, 未匹配到分组名称[{}], 返回空", dto.getRegionName());
                return emptyPage;
            }
            wrapper.in(CameraInfo::getGroupId, matchedGroupIds);
        }

        IPage<CameraInfo> cameraPage = cameraInfoService.page(new Page<>(dto.getPageNo(), dto.getPageSize()), wrapper);

        // 收集所有groupId，联动table_camera_group表获取分组名称
        Set<Long> groupIds = cameraPage.getRecords().stream()
                .map(CameraInfo::getGroupId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> groupNameMap = Collections.emptyMap();
        if (!groupIds.isEmpty()) {
            groupNameMap = cameraGroupMapper.selectList(
                            new LambdaQueryWrapper<CameraGroup>()
                                    .in(CameraGroup::getId, groupIds))
                    .stream()
                    .collect(Collectors.toMap(CameraGroup::getId, CameraGroup::getName, (v1, v2) -> v1));
        }

        List<CameraListVO> voList = new ArrayList<>(cameraPage.getRecords().size());
        for (CameraInfo camera : cameraPage.getRecords()) {
            CameraListVO vo = cameraInfoToVO(camera);
            // 优先使用分组表的名称，取不到时兜底camera_info冗余的groupName
            String resolvedGroupName = groupNameMap.getOrDefault(camera.getGroupId(), camera.getGroupName());
            vo.setRegionName(resolvedGroupName);
            voList.add(vo);
        }

        IPage<CameraListVO> resultPage = new Page<>(dto.getPageNo(), dto.getPageSize(), cameraPage.getTotal());
        resultPage.setRecords(voList);

        log.info("分页查询摄像头列表完成, 共{}条, 当前页{}条", cameraPage.getTotal(), voList.size());
        return resultPage;
    }

    /**
     * 将摄像头实体转换为列表VO
     *
     * @param camera 摄像头实体
     * @return 摄像头列表VO
     */
    private CameraListVO cameraToVO(CameraResource camera) {
        CameraListVO vo = new CameraListVO();
        vo.setSystemId(camera.getIndexCode());
        vo.setName(camera.getName());
        vo.setCameraType(camera.getCameraType());
        vo.setInstallLocation(camera.getInstallLocation());
        vo.setRegionIndexCode(camera.getRegionIndexCode());
        vo.setRegionName(camera.getRegionName());
        vo.setLongitude(bigDecimalToStr(camera.getLongitude()));
        vo.setLatitude(bigDecimalToStr(camera.getLatitude()));
        vo.setChannelType(camera.getChannelType());
        vo.setOnline(camera.getOnline());
        vo.setExternalIndexCode(camera.getExternalIndexCode());
        vo.setCreateTime(camera.getCreateTime());
        vo.setUpdateTime(camera.getUpdateTime());
        return vo;
    }

    /**
     * 将camera_info表实体转换为摄像头列表VO
     * <p>映射关系：indexCode对应systemId（摄像头唯一标识），regionName对应分组名称groupName，
     * installLocation对应点位路径pointPath，其余无对应字段的VO属性保持为空。</p>
     *
     * @param camera camera_info表实体
     * @return 摄像头列表VO
     */
    private CameraListVO cameraInfoToVO(CameraInfo camera) {
        CameraListVO vo = new CameraListVO();
        vo.setSystemId(camera.getSystemId());
        vo.setName(camera.getName());
        vo.setCameraType(camera.getCameraType());
        vo.setInstallLocation(camera.getPointPath());
        vo.setRegionName(camera.getGroupName());
        vo.setLongitude(camera.getLongitude());
        vo.setLatitude(camera.getLatitude());
        vo.setOnline(camera.getOnline());
        return vo;
    }

    @Override
    public List<CameraListVO> listByRegion(String regionIndexCode) {
        if (regionIndexCode == null || regionIndexCode.trim().isEmpty()) {
            log.warn("查询区域摄像头失败: regionIndexCode为空");
            return Collections.emptyList();
        }
        log.info("查询区域[{}]下直属摄像头列表", regionIndexCode);
        List<CameraResource> cameraList = list(new LambdaQueryWrapper<CameraResource>()
                .eq(CameraResource::getRegionIndexCode, regionIndexCode)
                .orderByAsc(CameraResource::getName));
        List<CameraListVO> result = new ArrayList<>(cameraList.size());
        for (CameraResource camera : cameraList) {
            result.add(cameraToVO(camera));
        }
        log.info("查询区域[{}]摄像头列表完成, 共{}条", regionIndexCode, result.size());
        return result;
    }

    @Override
    public List<RegionCameraTreeVO> getRegionCameraGroup() {
        log.info("开始构建区域摄像头分组信息");
        // 1. 查询摄像头分组表
        List<CameraGroup> allGroups = cameraGroupMapper.selectList(null);
        if (allGroups == null || allGroups.isEmpty()) {
            log.warn("摄像头分组表为空, 返回空分组");
            return Collections.emptyList();
        }

        // 2. 查询全部摄像头并按分组ID聚合（groupId -> 摄像头列表）
        Map<Long, List<CameraListVO>> groupCameraMap = cameraInfoService.list().stream()
                .filter(camera -> camera.getGroupId() != null)
                .collect(Collectors.groupingBy(CameraInfo::getGroupId,
                        Collectors.mapping(this::cameraInfoToVO, Collectors.toList())));

        // 3. 仅保留一级分组（根节点）中包含关键字的子树，递归构建分组树
        List<RegionCameraTreeVO> result = new ArrayList<>();
        for (CameraGroup group : allGroups) {
            if (group.getParentId() == null || group.getParentId() == 0L) {
                if (isPackageGroup(group.getName())) {
                    result.add(convertGroupTree(group, allGroups, groupCameraMap));
                }
            }
        }
        log.info("区域摄像头分组构建完成, 根节点{}个, 摄像头分组{}个", result.size(), groupCameraMap.size());
        return result;
    }

    /**
     * 判断分组名称是否属于需要展示的一级分组（服贸会、园区高点）
     *
     * @param name 分组名称
     * @return 是否保留该分组
     */
    private boolean isPackageGroup(String name) {
        return StringUtils.isNotBlank(name)
                && PACKAGE_KEYWORD.stream().anyMatch(kw -> StringUtils.contains(name, kw));
    }

    /**
     * 收集分组名称包含"服贸会"、"园区高点"关键字的全部分组 id（含其子孙分组），
     * 用于将摄像头列表/分页查询锁定在指定分组范围内。
     */
    private List<Long> collectPackageGroupIds() {
        List<CameraGroup> allGroups = cameraGroupMapper.selectList(null);
        if (allGroups == null || allGroups.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<CameraGroup>> childrenMap = allGroups.stream()
                .filter(g -> g.getParentId() != null)
                .collect(Collectors.groupingBy(CameraGroup::getParentId));
        Set<Long> matchedIds = new HashSet<>();
        for (CameraGroup group : allGroups) {
            if (isPackageGroup(group.getName())) {
                matchedIds.add(group.getId());
                collectPackageChildren(group.getId(), childrenMap, matchedIds);
            }
        }
        return new ArrayList<>(matchedIds);
    }

    /**
     * 递归收集指定分组的全部子孙分组 id
     */
    private void collectPackageChildren(Long parentId, Map<Long, List<CameraGroup>> childrenMap, Set<Long> matchedIds) {
        List<CameraGroup> children = childrenMap.get(parentId);
        if (children == null) {
            return;
        }
        for (CameraGroup child : children) {
            if (matchedIds.add(child.getId())) {
                collectPackageChildren(child.getId(), childrenMap, matchedIds);
            }
        }
    }

    /**
     * 将分组实体转换为区域摄像头分组节点，并递归填充子分组及videoList
     *
     * @param group          分组实体
     * @param allGroups      全部分组列表
     * @param groupCameraMap 分组ID -> 摄像头列表映射
     * @return 区域摄像头分组节点
     */
    private RegionCameraTreeVO convertGroupTree(CameraGroup group, List<CameraGroup> allGroups,
                                                Map<Long, List<CameraListVO>> groupCameraMap) {
        RegionCameraTreeVO vo = new RegionCameraTreeVO();
        vo.setIndexCode(String.valueOf(group.getId()));
        vo.setName(group.getName());
        vo.setParentIndexCode(group.getParentId() == null ? null : String.valueOf(group.getParentId()));
        vo.setSort(group.getSortNum());
        // 填充该分组下直属摄像头列表
        vo.setVideoList(groupCameraMap.getOrDefault(group.getId(), Collections.emptyList()));
        // 递归子分组
        for (CameraGroup child : allGroups) {
            if (group.getId().equals(child.getParentId())) {
                vo.getChildren().add(convertGroupTree(child, allGroups, groupCameraMap));
            }
        }
        return vo;
    }
}
