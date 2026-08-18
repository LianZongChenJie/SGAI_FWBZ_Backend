package org.jeecg.modules.fwbz.hikvision.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.CameraListVO;
import org.jeecg.modules.fwbz.hikvision.dto.CameraPlayUrlVO;
import org.jeecg.modules.fwbz.hikvision.service.ICameraResourceService;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/camera")
@Api(tags = "海康摄像头资源管理")
public class CameraResourceController {

    /** 摄像头分组过滤关键字：仅保留一级分组名称包含该关键字的数据 */
    private static final List<String> PACKAGE_KEYWORD = Arrays.asList("服贸会", "园区高点");

    private final ICameraResourceService cameraResourceService;

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
     * 获取摄像头播放地址
     * <p>前端传入1个或多个摄像头唯一编码，逐个请求海康获取播放地址后统一返回。</p>
     *
     * @param body 请求体，其中 cameraIndexCode 为摄像头唯一编码列表
     * @return 播放地址列表（每项包含 cameraIndexCode 和 url）
     */
    @PostMapping("/playUrls")
    @ApiOperation(value = "获取摄像头播放地址", notes = "传入 {\\\"cameraIndexCode\\\": [...]}，返回对应的播放地址")
    public Result<List<CameraPlayUrlVO>> getPlayUrls(@RequestBody Map<String, List<String>> body) {
        try {
            List<String> cameraIndexCodes = body.get("cameraIndexCode");
            List<CameraPlayUrlVO> playUrls = cameraResourceService.getPlayUrls(cameraIndexCodes);
            return Result.ok(playUrls);
        } catch (Exception e) {
            log.error("获取摄像头播放地址失败", e);
            return Result.error("获取摄像头播放地址失败: " + e.getMessage());
        }
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
