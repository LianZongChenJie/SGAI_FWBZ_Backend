package org.jeecg.modules.fwbz.hikvision.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.RegionTreeVO;
import org.jeecg.modules.fwbz.hikvision.service.IRegionResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 区域资源管理控制器
 * <p>触发从海康平台全量拉取区域数据并同步到本地数据库。
 * 同步策略：先清空表，再全量导入。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/region")
@Api(tags = "海康区域资源管理")
public class RegionResourceController {

    private final IRegionResourceService regionResourceService;

    /**
     * 触发全量同步海康区域数据
     * <p>请求无需参数，内部使用固定参数逐页拉取海康全部区域。
     * 先清空 table_region_resource 表，再批量插入新数据。</p>
     *
     * @return 同步结果（包含同步条数）
     */
    @PostMapping("/sync")
    @ApiOperation(value = "全量同步海康区域数据", notes = "先清空本地表，再从海康平台全量拉取区域数据导入")
    public Result<Integer> syncRegions() {
        try {
            int count = regionResourceService.syncFromHikvision();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步区域数据失败", e);
            return Result.error("同步区域数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取区域树形结构
     * <p>从数据库查询全部区域数据，按 parentIndexCode 构建父子关系，
     * 返回根节点列表，每个节点递归包含 children。</p>
     *
     * @return 区域树根节点列表
     */
    @GetMapping("/tree")
    @ApiOperation(value = "获取区域树形结构", notes = "返回从根节点开始的完整区域树，每个节点包含子节点列表")
    public Result<List<RegionTreeVO>> getRegionTree() {
        try {
            List<RegionTreeVO> tree = regionResourceService.buildRegionTree();
            return Result.ok(tree);
        } catch (Exception e) {
            log.error("获取区域树失败", e);
            return Result.error("获取区域树失败: " + e.getMessage());
        }
    }
}
