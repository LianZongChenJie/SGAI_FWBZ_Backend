package org.jeecg.modules.fwbz.exhibitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.fwbz.exhibitor.ExhibitorInfo;
import org.jeecg.modules.fwbz.exhibitor.service.IExhibitorInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

 /**
 * @Description: 参展厂商信息
 * @Author: jeecg-boot
 * @Date:   2026-09-02
 * @Version: V1.0
 */
@Api(tags="参展厂商信息")
@RestController
@RequestMapping("/fwbz/exhibitorInfo")
@Slf4j
public class ExhibitorInfoController extends JeecgController<ExhibitorInfo, IExhibitorInfoService> {
    @Autowired
    private IExhibitorInfoService exhibitorInfoService;

    /**
     * 分页列表查询
     *
     * @param exhibitorInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "参展厂商信息-分页列表查询")
    @ApiOperation(value="参展厂商信息-分页列表查询", notes="参展厂商信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExhibitorInfo>> queryPageList(ExhibitorInfo exhibitorInfo,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<ExhibitorInfo> queryWrapper = QueryGenerator.initQueryWrapper(exhibitorInfo, req.getParameterMap());
        Page<ExhibitorInfo> page = new Page<ExhibitorInfo>(pageNo, pageSize);
        IPage<ExhibitorInfo> pageList = exhibitorInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     *   添加
     *
     * @param exhibitorInfo
     * @return
     */
    @AutoLog(value = "参展厂商信息-添加")
    @ApiOperation(value="参展厂商信息-添加", notes="参展厂商信息-添加")
//    @RequiresPermissions("fwbz:exhibitor_info:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ExhibitorInfo exhibitorInfo) {
        exhibitorInfoService.save(exhibitorInfo);
        return Result.OK("添加成功！");
    }

    /**
     *  编辑
     *
     * @param exhibitorInfo
     * @return
     */
    @AutoLog(value = "参展厂商信息-编辑")
    @ApiOperation(value="参展厂商信息-编辑", notes="参展厂商信息-编辑")
//    @RequiresPermissions("fwbz:exhibitor_info:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody ExhibitorInfo exhibitorInfo) {
        exhibitorInfoService.updateById(exhibitorInfo);
        return Result.OK("编辑成功!");
    }

    /**
     *   通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "参展厂商信息-通过id删除")
    @ApiOperation(value="参展厂商信息-通过id删除", notes="参展厂商信息-通过id删除")
//    @RequiresPermissions("fwbz:exhibitor_info:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id",required=true) String id) {
        exhibitorInfoService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     *  批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "参展厂商信息-批量删除")
    @ApiOperation(value="参展厂商信息-批量删除", notes="参展厂商信息-批量删除")
//    @RequiresPermissions("fwbz:exhibitor_info:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        this.exhibitorInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "参展厂商信息-通过id查询")
    @ApiOperation(value="参展厂商信息-通过id查询", notes="参展厂商信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExhibitorInfo> queryById(@RequestParam(name="id",required=true) String id) {
        ExhibitorInfo exhibitorInfo = exhibitorInfoService.getById(id);
        if(exhibitorInfo==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(exhibitorInfo);
    }

    /**
     * 根据场馆id查询参展厂商列表
     *
     * @param venueId 场馆id
     * @return
     */
    //@AutoLog(value = "参展厂商信息-根据场馆id查询列表")
    @ApiOperation(value="参展厂商信息-根据场馆id查询列表", notes="参展厂商信息-根据场馆id查询列表")
    @GetMapping(value = "/listByVenueId")
    public Result<List<ExhibitorInfo>> queryListByVenueId(@RequestParam(name="venueId", required=true) Long venueId) {
        List<ExhibitorInfo> list = exhibitorInfoService.getListByVenueId(venueId);
        return Result.OK(list);
    }

    /**
     * 根据场馆id统计参展厂商数量
     *
     * @param venueId 场馆id
     * @return
     */
    //@AutoLog(value = "参展厂商信息-根据场馆id统计厂商数量")
    @ApiOperation(value="参展厂商信息-根据场馆id统计厂商数量", notes="参展厂商信息-根据场馆id统计厂商数量")
    @GetMapping(value = "/countByVenueId")
    public Result<Long> countByVenueId(@RequestParam(name="venueId", required=true) Long venueId) {
        Long count = exhibitorInfoService.countByVenueId(venueId);
        return Result.OK(count);
    }

    /**
     * 根据场馆id列表批量统计参展厂商数量
     *
     * @param venueIds 场馆id列表，逗号分隔
     * @return
     */
    //@AutoLog(value = "参展厂商信息-根据场馆id列表批量统计厂商数量")
    @ApiOperation(value="参展厂商信息-根据场馆id列表批量统计厂商数量", notes="参展厂商信息-根据场馆id列表批量统计厂商数量")
    @GetMapping(value = "/countByVenueIds")
    public Result<Map<Long, Long>> countByVenueIds(@RequestParam(name="venueIds", required=true) String venueIds) {
        List<Long> venueIdList = Arrays.stream(venueIds.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        Map<Long, Long> countMap = exhibitorInfoService.countGroupByVenueId(venueIdList);
        return Result.OK(countMap);
    }

    /**
    * 导出excel
    *
    * @param request
    * @param exhibitorInfo
    */
//    @RequiresPermissions("fwbz:exhibitor_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ExhibitorInfo exhibitorInfo) {
        return super.exportXls(request, exhibitorInfo, ExhibitorInfo.class, "参展厂商信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("fwbz:exhibitor_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ExhibitorInfo.class);
    }

}
