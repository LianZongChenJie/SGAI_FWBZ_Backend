package org.jeecg.modules.fwbz.hikvision.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDeviceListVO;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDevicePageDto;
import org.jeecg.modules.fwbz.hikvision.service.IAcsDeviceService;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 门禁设备资源管理控制器
 * <p>触发从海康平台全量拉取门禁设备数据并同步到本地数据库。
 * 同步策略：先清空表，再全量导入。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/acsDevice")
@Api(tags = "海康门禁设备资源管理")
public class AcsDeviceController {

    private final IAcsDeviceService acsDeviceService;

    @PostMapping("/sync")
    @ApiOperation(value = "全量同步海康门禁设备数据", notes = "先清空本地表，再从海康平台全量拉取门禁设备数据导入")
    public Result<Integer> syncDevices() {
        try {
            int count = acsDeviceService.syncFromHikvision();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步门禁设备数据失败", e);
            return Result.error("同步门禁设备数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/syncOnlineStatus")
    @ApiOperation(value = "同步海康门禁设备在线状态", notes = "从海康平台拉取门禁设备在线状态，更新本地online字段")
    public Result<Integer> syncOnlineStatus() {
        try {
            int count = acsDeviceService.syncOnlineStatus();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步门禁设备在线状态失败", e);
            return Result.error("同步门禁设备在线状态失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取门禁设备列表
     * <p>分页查询门禁设备数据，支持按名称、设备类型编码、区域名称、在线状态、IP检索，为空查全部。</p>
     *
     * @param dto 分页及查询条件
     * @return 门禁设备分页列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "分页获取门禁设备列表", notes = "分页查询门禁设备数据，支持按名称、设备类型、区域名称、在线状态、IP检索，为空查全部")
    public Result<IPage<AcsDeviceListVO>> getDeviceList(AcsDevicePageDto dto) {
        try {
            IPage<AcsDeviceListVO> page = acsDeviceService.getDeviceList(dto);
            return Result.ok(page);
        } catch (Exception e) {
            log.error("获取门禁设备列表失败", e);
            return Result.error("获取门禁设备列表失败: " + e.getMessage());
        }
    }

    /**
     * 导出门禁设备数据
     * <p>前端可传名称、设备类型、区域名称、在线状态、IP等条件，不传则导出全部；导出不分页。</p>
     *
     * @param dto      查询条件（可为空）
     * @param response HTTP 响应
     */
    @GetMapping("/export")
    @ApiOperation(value = "导出门禁设备数据", notes = "导出门禁设备数据，支持按名称、设备类型、区域名称、在线状态、IP过滤，不传条件导出全部，不分页")
    public void exportDevices(AcsDevicePageDto dto, HttpServletResponse response) throws Exception {
        List<AcsDeviceListVO> list = acsDeviceService.getDeviceListForExport(dto);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("门禁设备数据.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("门禁设备数据", "门禁设备数据", ExcelType.XSSF),
                AcsDeviceListVO.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }
}
