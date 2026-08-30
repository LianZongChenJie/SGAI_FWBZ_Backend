package org.jeecg.modules.fwbz.hikvision.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.DoorControlRequest;
import org.jeecg.modules.fwbz.hikvision.dto.DoorControlResultVO;
import org.jeecg.modules.fwbz.hikvision.dto.DoorListVO;
import org.jeecg.modules.fwbz.hikvision.dto.DoorResourcePageDto;
import org.jeecg.modules.fwbz.hikvision.service.IDoorResourceService;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 门禁点资源管理控制器
 * <p>触发从海康平台全量拉取门禁点数据并同步到本地数据库。
 * 同步策略：先清空表，再全量导入。</p>
 *
 * @author fwbz
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/fwbz/hikvision/door")
@Api(tags = "海康门禁点资源管理")
public class DoorResourceController {

    private final IDoorResourceService doorResourceService;

    /**
     * 触发全量同步海康门禁点数据
     * <p>请求无需参数，内部使用固定参数逐页拉取海康全部门禁点。
     * 先清空 table_door_resource 表，再批量插入新数据。</p>
     *
     * @return 同步结果（包含同步条数）
     */
    @PostMapping("/sync")
    @ApiOperation(value = "全量同步海康门禁点数据", notes = "先清空本地表，再从海康平台全量拉取门禁点数据导入")
    public Result<Integer> syncDoors() {
        try {
            int count = doorResourceService.syncFromHikvision();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步门禁点数据失败", e);
            return Result.error("同步门禁点数据失败: " + e.getMessage());
        }
    }

    /**
     * 同步海康门禁状态数据
     * <p>从海康平台逐页拉取门禁状态，根据 indexCode 匹配更新本地 door_state 字段。
     * 仅更新状态发生变化的记录。</p>
     *
     * @return 同步结果（包含更新条数）
     */
    @PostMapping("/syncDoorStatus")
    @ApiOperation(value = "同步海康门禁状态", notes = "从海康平台拉取门禁状态，更新本地door_state字段")
    public Result<Integer> syncDoorStatus() {
        try {
            int count = doorResourceService.syncDoorStatus();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("同步门禁状态失败", e);
            return Result.error("同步门禁状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取全部门禁点列表
     * <p>从本地数据库查询全部门禁点数据并返回列表。</p>
     *
     * @return 门禁点列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "分页获取门禁点列表", notes = "分页查询门禁点数据，支持按名称、门禁点编号、区域名称、门状态、接入协议、安装位置检索，为空查全部")
    public Result<IPage<DoorListVO>> getDoorList(DoorResourcePageDto dto) {
        try {
            IPage<DoorListVO> page = doorResourceService.getDoorList(dto);
            return Result.ok(page);
        } catch (Exception e) {
            log.error("获取门禁点列表失败", e);
            return Result.error("获取门禁点列表失败: " + e.getMessage());
        }
    }

    /**
     * 导出门禁点信息
     * <p>前端可传名称、安装位置等条件，不传则导出全部；导出不分页，返回全部符合条件的门禁点。</p>
     *
     * @param dto  查询条件（name、installLocation 等，可为空）
     * @param response HTTP 响应
     */
    @GetMapping("/export")
    @ApiOperation(value = "导出门禁点信息", notes = "导出门禁点数据，支持按名称、门禁点编号、区域名称、门状态、接入协议、安装位置过滤，不传条件导出全部，不分页")
    public void exportDoors(DoorResourcePageDto dto, HttpServletResponse response) throws Exception {
        List<DoorListVO> list = doorResourceService.getDoorListForExport(dto);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("门禁点信息.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("门禁点信息", "门禁点信息", ExcelType.XSSF),
                DoorListVO.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 反向控制海康门禁点
     * <p>请求体示例：{@code {"doorIndexCodes":["1f276203e5234bdca08f7d99e1097bba"],"controlType":3}}
     * controlType：0-常开、1-门闭、2-门开、3-常闭，最大支持10个门禁点。
     * 返回逐项结果，success=false 时附带海康返回的错误说明。</p>
     *
     * @param request 控制请求参数
     * @return 逐项控制结果
     */
    @PostMapping("/control")
    @ApiOperation(value = "反向控制海康门禁点", notes = "controlType: 0-常开、1-门闭、2-门开、3-常闭；doorIndexCodes最大10个")
    public Result<List<DoorControlResultVO>> controlDoor(@RequestBody DoorControlRequest request) {
        try {
            List<DoorControlResultVO> result = doorResourceService.controlDoor(request);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("反向控制海康门禁点失败", e);
            return Result.error("反向控制海康门禁点失败: " + e.getMessage());
        }
    }
}
