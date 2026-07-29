package org.jeecg.modules.fwbz.controller;

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
import org.jeecg.modules.fwbz.entity.FwbzDevice;
import org.jeecg.modules.fwbz.service.IFwbzDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 设备管理Demo
 * @Author: jeecg-boot
 * @Date:   2026-07-21
 * @Version: V1.0
 */
@Api(tags="设备管理Demo")
@RestController
@RequestMapping("/fwbz/demo/device")
@Slf4j
public class FwbzDeviceController extends JeecgController<FwbzDevice, IFwbzDeviceService> {
	@Autowired
	private IFwbzDeviceService fwbzDeviceService;
	
	/**
	 * 分页列表查询
	 *
	 * @param fwbzDevice
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@ApiOperation(value="设备管理-分页列表查询", notes="设备管理-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<FwbzDevice>> queryPageList(FwbzDevice fwbzDevice,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<FwbzDevice> queryWrapper = QueryGenerator.initQueryWrapper(fwbzDevice, req.getParameterMap());
		Page<FwbzDevice> page = new Page<FwbzDevice>(pageNo, pageSize);
		IPage<FwbzDevice> pageList = fwbzDeviceService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param fwbzDevice
	 * @return
	 */
	@AutoLog(value = "设备管理-添加")
	@ApiOperation(value="设备管理-添加", notes="设备管理-添加")
//	@RequiresPermissions("fwbz:demo:device:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody FwbzDevice fwbzDevice) {
		fwbzDeviceService.save(fwbzDevice);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param fwbzDevice
	 * @return
	 */
	@AutoLog(value = "设备管理-编辑")
	@ApiOperation(value="设备管理-编辑", notes="设备管理-编辑")
//	@RequiresPermissions("fwbz:demo:device:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody FwbzDevice fwbzDevice) {
		fwbzDeviceService.updateById(fwbzDevice);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "设备管理-通过id删除")
	@ApiOperation(value="设备管理-通过id删除", notes="设备管理-通过id删除")
//	@RequiresPermissions("fwbz:demo:device:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		fwbzDeviceService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "设备管理-批量删除")
	@ApiOperation(value="设备管理-批量删除", notes="设备管理-批量删除")
//	@RequiresPermissions("fwbz:demo:device:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.fwbzDeviceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(value="设备管理-通过id查询", notes="设备管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<FwbzDevice> queryById(@RequestParam(name="id",required=true) String id) {
		FwbzDevice fwbzDevice = fwbzDeviceService.getById(id);
		if(fwbzDevice==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(fwbzDevice);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param fwbzDevice
    */
//    @RequiresPermissions("fwbz:demo:device:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FwbzDevice fwbzDevice) {
        return super.exportXls(request, fwbzDevice, FwbzDevice.class, "设备管理");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("fwbz:demo:device:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FwbzDevice.class);
    }

}
