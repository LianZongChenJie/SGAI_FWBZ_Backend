package org.jeecg.modules.master.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.service.IDeviceService;
import org.jeecg.modules.master.vo.DeviceImportDTO;
import org.jeecg.modules.master.vo.DeviceVO;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "设备主数据")
@RestController
@RequestMapping("/master/device")
public class DeviceController {

    @Autowired
    private IDeviceService deviceService;

    @ApiOperation("分页列表")
    @GetMapping("/list")
    public Result<IPage<DeviceVO>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String spaceId) {
        Page<Device> page = new Page<>(pageNo, pageSize);
        return Result.OK(deviceService.pageVO(page, name, categoryId, spaceId));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public Result<Device> queryById(@PathVariable("id") String id) {
        return Result.OK(deviceService.getById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<?> add(@RequestBody Device entity) {
        deviceService.create(entity);
        return Result.OK("新增成功");
    }

    @ApiOperation("编辑")
    @PutMapping
    public Result<?> edit(@RequestBody Device entity) {
        deviceService.updateNode(entity);
        return Result.OK("编辑成功");
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        deviceService.removeBatch(Arrays.asList(id));
        return Result.OK("删除成功");
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam("ids") List<String> ids) {
        deviceService.removeBatch(ids);
        return Result.OK("删除成功");
    }

    @ApiOperation("导出")
    @GetMapping("/exportXls")
    public void exportXls(HttpServletResponse response,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) String categoryId,
                          @RequestParam(required = false) String spaceId) throws IOException {
        List<DeviceVO> list = deviceService.listForExport(name, categoryId, spaceId);
        Workbook wb = ExcelExportUtil.exportExcel(new ExportParams("设备主数据", "设备"), DeviceVO.class, list);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("设备主数据.xls", "UTF-8"));
        wb.write(response.getOutputStream());
    }

    @ApiOperation("下载导入模板")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws IOException {
        List<DeviceImportDTO> empty = new ArrayList<>();
        Workbook wb = ExcelExportUtil.exportExcel(new ExportParams(null, "设备"), DeviceImportDTO.class, empty);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("设备导入模板.xls", "UTF-8"));
        wb.write(response.getOutputStream());
    }

    @ApiOperation("导入")
    @PostMapping("/importExcel")
    public Result<?> importExcel(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipart = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> files = multipart.getFileMap();
        List<DeviceImportDTO> all = new ArrayList<>();
        for (MultipartFile file : files.values()) {
            ImportParams params = new ImportParams();
            params.setTitleRows(0);
            params.setHeadRows(1);
            all.addAll(ExcelImportUtil.importExcel(file.getInputStream(), DeviceImportDTO.class, params));
        }
        List<String> errors = deviceService.batchImport(all);
        if (errors.isEmpty()) {
            return Result.OK("导入成功");
        }
        return Result.error("部分导入失败：" + String.join("；", errors));
    }
}
