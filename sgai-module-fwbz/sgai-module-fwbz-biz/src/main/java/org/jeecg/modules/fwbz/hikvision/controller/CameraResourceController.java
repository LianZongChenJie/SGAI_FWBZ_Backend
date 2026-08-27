package org.jeecg.modules.fwbz.hikvision.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.config.HlsProperties;
import org.jeecg.modules.fwbz.hikvision.dto.CameraListVO;
import org.jeecg.modules.fwbz.hikvision.dto.CameraPlayUrlVO;
import org.jeecg.modules.fwbz.hikvision.dto.CameraResourcePageDto;
import org.jeecg.modules.fwbz.hikvision.dto.RegionCameraTreeVO;
import org.jeecg.modules.fwbz.hikvision.service.ICameraResourceService;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 摄像头资源管理控制器
 * <p>触发从海康平台全量拉取摄像头数据并同步到本地数据库。
 * 同步策略：先清空表，再全量导入。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/fwbz/hikvision/camera")
@Api(tags = "海康摄像头资源管理")
public class CameraResourceController {

    /** 摄像头分组过滤关键字：仅保留一级分组名称包含该关键字的数据 */
    private static final List<String> PACKAGE_KEYWORD = Arrays.asList("服贸会", "园区高点");

    private final ICameraResourceService cameraResourceService;

    /**
     * HLS转码相关配置（含 publicBaseUrl：前端可访问的后端基础地址，
     * 配置后HLS播放地址固定使用该地址拼接；不配置则自动取当前请求的Host）
     */
    private final HlsProperties hlsProperties;

    /**
     * 触发全量同步海康摄像头数据
     * <p>请求无需参数，内部使用固定参数逐页拉取海康全部摄像头。
     * 先清空 table_camera_resource 表，再批量插入新数据。</p>
     *
     * @return 同步结果（包含同步条数）
     */
    @PostMapping("/sync")
    @ApiOperation(value = "全量同步海康摄像头数据", notes = "先清空本地表，再从海康平台全量拉取摄像头数据导入")
    public Result<Integer> syncCameras() {
        try {
            int count = cameraResourceService.syncFromHikvision();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步摄像头数据失败", e);
            return Result.error("同步摄像头数据失败: " + e.getMessage());
        }
    }

    /**
     * 同步IOC平台摄像头分组
     * <p>先清空 table_camera_group 表，再调用IOC平台接口拉取分组树，
     * 仅同步分组信息到 table_camera_group 表（不处理摄像头列表）。</p>
     *
     * @return 同步结果（分组数）
     */
    @PostMapping("/syncIoc")
    @ApiOperation(value = "同步IOC平台摄像头分组", notes = "先清空摄像头分组表，再从IOC平台拉取分组数据导入")
    public Result<Integer> syncIocCameras() {
        try {
            int count = cameraResourceService.syncFromIoc();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步IOC平台摄像头分组失败", e);
            return Result.error("同步IOC平台摄像头分组失败: " + e.getMessage());
        }
    }

    /**
     * 获取摄像头本地HLS播放地址
     * <p>流程：前端传入1个或多个摄像头唯一编码 -> 海康SDK获取RTSP地址 -> JavaCV转码为本地HLS ->
     * 返回 /hls/{编码}/index.m3u8 完整访问地址。同一摄像头正在拉流时直接复用已生成的HLS流，不做重复转码。</p>
     *
     * @param body    请求体，其中 cameraIndexCode 为摄像头唯一编码列表
     * @param request 当前请求，用于拼接HLS访问地址
     * @return 播放地址列表（每项包含 cameraIndexCode 和 url）
     */
    @PostMapping("/playUrls")
    @ApiOperation(value = "获取摄像头本地HLS播放地址", notes = "传入 {\\\"cameraIndexCode\\\": [...]}，返回对应的本地HLS播放地址（海康RTSP经JavaCV转码）")
    public Result<List<CameraPlayUrlVO>> getPlayUrls(@RequestBody Map<String, List<String>> body,
                                                     HttpServletRequest request) {
        try {
            List<String> cameraIndexCodes = body.get("cameraIndexCode");
            List<CameraPlayUrlVO> playUrls = cameraResourceService.getPlayUrls(cameraIndexCodes);
            // 将服务端返回的相对地址拼接为前端可访问的完整地址
            String baseUrl = buildBaseUrl(request);
            for (CameraPlayUrlVO vo : playUrls) {
                if (vo.getUrl() != null && vo.getUrl().startsWith("/")) {
                    vo.setUrl(baseUrl + vo.getUrl());
                }
            }
            return Result.ok(playUrls);
        } catch (Exception e) {
            log.error("获取摄像头播放地址失败", e);
            return Result.error("获取摄像头播放地址失败: " + e.getMessage());
        }
    }

    /**
     * 释放摄像头观看（前端停止播放时调用）
     * <p>对应摄像头观看人数-1，无人观看时由HLS流管理器延迟自动停止RTSP拉流，释放摄像头通道。</p>
     *
     * @param body 请求体，其中 cameraIndexCode 为摄像头唯一编码列表
     * @return 操作结果
     */
    @PostMapping("/releasePlay")
    @ApiOperation(value = "释放摄像头观看", notes = "前端停止播放时调用，无人观看时自动停止RTSP拉流")
    public Result<Boolean> releasePlay(@RequestBody Map<String, List<String>> body) {
        try {
            List<String> cameraIndexCodes = body.get("cameraIndexCode");
            cameraResourceService.releasePlay(cameraIndexCodes);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("释放摄像头观看失败", e);
            return Result.error("释放摄像头观看失败: " + e.getMessage());
        }
    }

    /**
     * 摄像头播放心跳续期（播放过程中周期调用）
     * <p>前端正常播放时每30秒调用一次，防止页面异常关闭导致拉流泄漏无人回收。</p>
     *
     * @param body 请求体，其中 cameraIndexCode 为摄像头唯一编码列表
     * @return 操作结果
     */
    @PostMapping("/heartbeat")
    @ApiOperation(value = "摄像头播放心跳续期", notes = "播放过程中周期调用，防止页面异常关闭导致拉流泄漏")
    public Result<Boolean> heartbeat(@RequestBody Map<String, List<String>> body) {
        try {
            List<String> cameraIndexCodes = body.get("cameraIndexCode");
            cameraResourceService.heartbeat(cameraIndexCodes);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("摄像头播放心跳续期失败", e);
            return Result.error("摄像头播放心跳续期失败: " + e.getMessage());
        }
    }

    /**
     * 构建HLS播放地址的完整访问基础地址
     * <p>优先使用配置 fwbz.hls.public-base-url；未配置时取当前请求的Host
     * （兼容网关转发场景，优先取 X-Forwarded-Host）。</p>
     */
    private String buildBaseUrl(HttpServletRequest request) {
        if (StringUtils.isNotBlank(hlsProperties.getPublicBaseUrl())) {
            return StringUtils.removeEnd(hlsProperties.getPublicBaseUrl(), "/");
        }
        String scheme = request.getScheme();
        String host = request.getHeader("X-Forwarded-Host");
        if (StringUtils.isBlank(host)) {
            host = request.getHeader("Host");
        }
        if (StringUtils.isBlank(host)) {
            host = request.getServerName()
                    + (request.getServerPort() == 80 || request.getServerPort() == 443
                    ? "" : ":" + request.getServerPort());
        }
        return scheme + "://" + host;
    }

    /**
     * 同步监控点在线状态
     * <p>从海康逐页拉取全部监控点在线状态，根据唯一编码更新表中 online 字段。</p>
     *
     * @return 同步结果（包含更新条数）
     */
    @PostMapping("/syncOnlineStatus")
    @ApiOperation(value = "同步监控点在线状态", notes = "从海康平台拉取在线状态并更新到本地数据库")
    public Result<Integer> syncOnlineStatus() {
        try {
            int count = cameraResourceService.syncOnlineStatus();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步监控点在线状态失败", e);
            return Result.error("同步监控点在线状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取全部摄像头列表
     * <p>从本地数据库查询全部摄像头数据并返回列表。</p>
     *
     * @return 摄像头列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取全部摄像头列表", notes = "从本地数据库查询全部摄像头数据")
    public Result<List<CameraListVO>> getCameraList() {
        try {
            List<CameraListVO> list = cameraResourceService.getCameraList();
            return Result.ok(list);
        } catch (Exception e) {
            log.error("获取摄像头列表失败", e);
            return Result.error("获取摄像头列表失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取摄像头列表
     * <p>从本地数据库分页查询摄像头数据，支持按名称、唯一编码、区域名称、接入协议、
     * 安装位置、在线状态、监控点类型检索，条件为空查全部。</p>
     *
     * @param dto 分页查询参数
     * @return 分页摄像头列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页获取摄像头列表", notes = "分页查询摄像头数据，支持按名称、唯一编码、区域名称、接入协议、安装位置、在线状态、监控点类型检索，条件为空查全部")
    public Result<IPage<CameraListVO>> getCameraPage(CameraResourcePageDto dto) {
        try {
            IPage<CameraListVO> page = cameraResourceService.getCameraPage(dto);
            return Result.ok(page);
        } catch (Exception e) {
            log.error("分页获取摄像头列表失败", e);
            return Result.error("分页获取摄像头列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据摄像头所属区域编码查询摄像头列表
     *
     * @param regionIndexCode 区域编码
     * @return 该区域下直属摄像头列表
     */
    @GetMapping("/listByRegion")
    @ApiOperation(value = "按区域编码查询摄像头列表", notes = "传入区域编码 regionIndexCode，返回该区域下直属摄像头列表")
    public Result<List<CameraListVO>> getCameraListByRegion(String regionIndexCode) {
        try {
            List<CameraListVO> list = cameraResourceService.listByRegion(regionIndexCode);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("按区域编码查询摄像头列表失败, regionIndexCode={}", regionIndexCode, e);
            return Result.error("按区域编码查询摄像头列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取区域摄像头分组信息
     * <p>先获取区域树，再在每个区域节点下挂载该区域直属的摄像头列表（videoList），
     * 返回结构与海康区域树一致，节点中额外包含 videoList 项。</p>
     *packageGroup
     * @return 区域摄像头分组树根节点列表
     */
    @GetMapping("/regionCameraGroup")
    @ApiOperation(value = "获取区域摄像头分组信息", notes = "先获取区域树，每个区域节点下挂载该区域直属的摄像头列表（videoList）")
    public Result<List<RegionCameraTreeVO>> getRegionCameraGroup() {
        try {
            List<RegionCameraTreeVO> list = cameraResourceService.getRegionCameraGroup();
            return Result.ok(list);
        } catch (Exception e) {
            log.error("获取区域摄像头分组信息失败", e);
            return Result.error("获取区域摄像头分组信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取摄像头坐标分组分布
     * <p>代理转发到外部IOC数据平台，获取按坐标聚合的摄像头分布数据。</p>
     *
     * @return 摄像头坐标分组分布数据
     */
    @GetMapping("/coordinateGroup")
    @ApiOperation(value = "获取摄像头坐标分组分布", notes = "代理转发到外部IOC数据平台，获取按坐标聚合的摄像头分布")
    public Result<JSONObject> getCoordinateGroup() {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(10000);
            RestTemplate restTemplate = new RestTemplate(factory);

            String url = "http://10.168.47.26:9999/sgai-ioc-data/admin/video/coordinateGroup";
            String body = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();

            JSONObject json = JSONObject.parseObject(body);
            log.info("获取摄像头坐标分组分布成功");
            return Result.ok(json);
        } catch (Exception e) {
            log.error("获取摄像头坐标分组分布失败", e);
            return Result.error("获取摄像头坐标分组分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取摄像头分组数据
     * <p>代理转发到外部IOC数据平台，获取摄像头分组树形数据。</p>
     *
     * @return 摄像头分组数据
     */
    @GetMapping("/packageGroup")
    @ApiOperation(value = "获取摄像头分组数据", notes = "代理转发到外部IOC数据平台，获取摄像头分组数据")
    public Result<JSONArray> getPackageGroup() {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(10000);
            RestTemplate restTemplate = new RestTemplate(factory);

            String url = "http://10.168.47.26:9999/sgai-ioc-data/admin/video/packageGroup";
            String body = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();

            JSONObject json = JSONObject.parseObject(body);
            JSONArray result = json.getJSONArray("result");
            // 只保留一级分组中 name 包含任一关键字的分组，其下子树（children/videoList）原样保留
            JSONArray filtered = result == null ? new JSONArray()
                    : result.stream()
                            .map(JSONObject.class::cast)
                            .filter(item -> {
                                String name = item.getString("name");
                                return StringUtils.isNotBlank(name)
                                        && PACKAGE_KEYWORD.stream().anyMatch(kw -> StringUtils.contains(name, kw));
                            })
                            .collect(JSONArray::new, JSONArray::add, JSONArray::addAll);
            log.info("获取摄像头分组数据成功");
            return Result.ok(filtered);
        } catch (Exception e) {
            log.error("获取摄像头分组数据失败", e);
            return Result.error("获取摄像头分组数据失败: " + e.getMessage());
        }
    }
}
