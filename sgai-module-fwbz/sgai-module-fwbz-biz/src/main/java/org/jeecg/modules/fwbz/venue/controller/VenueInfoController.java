package org.jeecg.modules.fwbz.venue.controller;

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
import org.jeecg.modules.fwbz.venue.VenueInfo;
import org.jeecg.modules.fwbz.venue.service.IVenueInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

 /**
 * @Description: 场馆基本信息
 * @Author: jeecg-boot
 * @Date:   2026-07-29
 * @Version: V1.0
 */
@Api(tags="场馆基本信息")
@RestController
@RequestMapping("/fwbz/venueInfo")
@Slf4j
public class VenueInfoController extends JeecgController<VenueInfo, IVenueInfoService> {
    @Autowired
    private IVenueInfoService venueInfoService;

    /**
     * 分页列表查询
     *
     * @param venueInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "场馆基本信息-分页列表查询")
    @ApiOperation(value="场馆基本信息-分页列表查询", notes="场馆基本信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<VenueInfo>> queryPageList(VenueInfo venueInfo,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<VenueInfo> queryWrapper = QueryGenerator.initQueryWrapper(venueInfo, req.getParameterMap());
        Page<VenueInfo> page = new Page<VenueInfo>(pageNo, pageSize);
        IPage<VenueInfo> pageList = venueInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     *   添加
     *
     * @param venueInfo
     * @return
     */
    @AutoLog(value = "场馆基本信息-添加")
    @ApiOperation(value="场馆基本信息-添加", notes="场馆基本信息-添加")
//    @RequiresPermissions("fwbz:venue_info:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody VenueInfo venueInfo) {
        venueInfoService.save(venueInfo);
        return Result.OK("添加成功！");
    }

    /**
     *  编辑
     *
     * @param venueInfo
     * @return
     */
    @AutoLog(value = "场馆基本信息-编辑")
    @ApiOperation(value="场馆基本信息-编辑", notes="场馆基本信息-编辑")
//    @RequiresPermissions("fwbz:venue_info:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody VenueInfo venueInfo) {
        venueInfoService.updateById(venueInfo);
        return Result.OK("编辑成功!");
    }

    /**
     *   通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "场馆基本信息-通过id删除")
    @ApiOperation(value="场馆基本信息-通过id删除", notes="场馆基本信息-通过id删除")
//    @RequiresPermissions("fwbz:venue_info:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id",required=true) String id) {
        venueInfoService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     *  批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "场馆基本信息-批量删除")
    @ApiOperation(value="场馆基本信息-批量删除", notes="场馆基本信息-批量删除")
//    @RequiresPermissions("fwbz:venue_info:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        this.venueInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "场馆基本信息-通过id查询")
    @ApiOperation(value="场馆基本信息-通过id查询", notes="场馆基本信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<VenueInfo> queryById(@RequestParam(name="id",required=true) String id) {
        VenueInfo venueInfo = venueInfoService.getById(id);
        if(venueInfo==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(venueInfo);
    }

    /**
     * 查询全部场馆（下拉列表）
     *
     * @return
     */
    //@AutoLog(value = "场馆基本信息-下拉列表查询")
    @ApiOperation(value="场馆基本信息-下拉列表查询", notes="场馆基本信息-下拉列表查询")
    @GetMapping(value = "/listAll")
    public Result<List<VenueInfo>> queryAllList() {
        List<VenueInfo> list = venueInfoService.getAllVenueList();
        return Result.OK(list);
    }

    /**
    * 导出excel
    *
    * @param request
    * @param venueInfo
    */
//    @RequiresPermissions("fwbz:venue_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, VenueInfo venueInfo) {
        return super.exportXls(request, venueInfo, VenueInfo.class, "场馆基本信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("fwbz:venue_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, VenueInfo.class);
    }

}
