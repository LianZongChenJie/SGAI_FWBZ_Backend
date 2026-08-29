package org.jeecg.modules.fwbz.coldSourceSystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sunwayland.pspace.entity.Base;
import com.sunwayland.pspace.entity.PsConnectInfo;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.entity.PsServerProp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceHistoryPageDto;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceHistoryPageQueryDto;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceWriteDto;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceHistoryService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceOverviewService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.ColdSourceServerService;
import org.jeecg.modules.fwbz.coldSourceSystem.service.SaveHisttoryService;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 冷源系统(pSpace) —— 字段映射查询 + 服务器信息查询
 *
 * 实时数据统一由 WebSocket 订阅推送（前端连接 /fwbz/coldSource/ws 接收），
 * 不再提供 HTTP 轮询接口；本接口仅提供映射/服务器信息查询。
 */
@RestController
@RequestMapping("/fwbz/coldSource")
@AllArgsConstructor
@Slf4j
@Api(tags = "冷源系统")
public class ColdSourceController {

    private final ColdSourceServerService coldSourceServerService;

    private final ColdSourceOverviewService coldSourceOverviewService;

    private final SaveHisttoryService saveHisttoryService;

    private final ColdSourceHistoryService coldSourceHistoryService;

    /**
     * 手动触发一次冷源历史数据保存
     * （等价于整十分钟定时任务执行一次：取 is_save=1 的 tagid -> 读值 -> 写 table_cold_source_history）
     */
    @PostMapping("/saveHistory")
    @ApiOperation(value = "手动触发冷源历史数据保存", notes = "从 table_tagid_info 取 is_save=1 的采集点，读取最新值写入 table_cold_source_history")
    public Result<String> saveHistory() {
        try {
            saveHisttoryService.saveHistory();
            return Result.ok("冷源历史数据保存任务执行完成");
        } catch (Exception e) {
            log.error("冷源历史数据保存异常", e);
            return Result.error("冷源历史数据保存异常: " + e.getMessage());
        }
    }

    /**
     * 查询冷源历史记录（分页）
     * 条件: tagId(采集点id, 精确) / desc(描述, 模糊) / startTime(开始, 仅日期默认00:00:00) / endTime(结束, 仅日期默认23:59:59)
     * 只传开始时间查此时间之后，只传结束时间查此时间之前；返回: tagId, desc, dataTime(采集时间), value(值)
     */
    @GetMapping("/history/page")
    @ApiOperation(value = "查询冷源历史记录(分页)", notes = "条件: tagId(精确)/desc(描述模糊)/startTime(开始,仅日期默认00:00:00)/endTime(结束,仅日期默认23:59:59); 返回: tagId, desc, dataTime(采集时间), value(值)")
    public Result<IPage<ColdSourceHistoryPageDto>> queryHistoryPage(ColdSourceHistoryPageQueryDto params) {
        try {
            LocalDateTime startTime = resolveStartTime(params.getStartTime());
            LocalDateTime endTime = resolveEndTime(params.getEndTime());
            return Result.ok(coldSourceHistoryService.pageHistory(params, startTime, endTime));
        } catch (Exception e) {
            log.error("查询冷源历史记录异常", e);
            return Result.error("查询冷源历史记录异常: " + e.getMessage());
        }
    }

    /**
     * 导出冷源历史记录（Excel xlsx）
     * 条件与分页接口一致，导出全部匹配数据
     */
    @GetMapping("/history/export")
    @ApiOperation(value = "导出冷源历史记录(Excel)", notes = "条件: tagId(精确)/desc(描述模糊)/startTime(开始,仅日期默认00:00:00)/endTime(结束,仅日期默认23:59:59); 导出全部匹配数据")
    public void exportHistory(HttpServletResponse response, ColdSourceHistoryPageQueryDto params) throws Exception {
        LocalDateTime startTime = resolveStartTime(params.getStartTime());
        LocalDateTime endTime = resolveEndTime(params.getEndTime());
        List<ColdSourceHistoryPageDto> list = coldSourceHistoryService.exportHistory(params, startTime, endTime);
        log.info("导出冷源历史记录: {}", list.size());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename="
                + URLEncoder.encode("冷源历史记录.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("冷源历史记录", "冷源历史记录", ExcelType.XSSF),
                ColdSourceHistoryPageDto.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 开始时间解析：支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss，仅日期默认 00:00:00，空值返回 null
     */
    private LocalDateTime resolveStartTime(String time) {
        if (StringUtils.isBlank(time)) {
            return null;
        }
        String t = time.trim();
        if (t.length() <= 10) {
            return LocalDate.parse(t).atStartOfDay();
        }
        return LocalDateTime.parse(t, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 结束时间解析：支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss，仅日期默认 23:59:59，空值返回 null
     */
    private LocalDateTime resolveEndTime(String time) {
        if (StringUtils.isBlank(time)) {
            return null;
        }
        String t = time.trim();
        if (t.length() <= 10) {
            return LocalDate.parse(t).atTime(23, 59, 59);
        }
        return LocalDateTime.parse(t, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * FIELD_MAP 全量映射查询：前端字段 key -> 测点ID数组(tagId[])，null 表示无对应测点
     */
    @GetMapping("/fieldMap")
    @ApiOperation(value = "查询 FIELD_MAP 全量映射", notes = "返回前端字段 key 与测点ID(tagId) 的全部对应关系，null 表示该 key 在点表中无对应测点")
    public Result<Map<String, List<Long>>> getFieldMap() {
        try {
            return Result.ok(coldSourceOverviewService.getFieldMap());
        } catch (Exception e) {
            log.error("查询 FIELD_MAP 映射异常", e);
            return Result.error("查询 FIELD_MAP 映射异常: " + e.getMessage());
        }
    }

    /**
     * 查询服务器连接信息
     */
    @GetMapping("/server/getAllConnectInfo")
    @ApiOperation(value = "查询服务器连接信息", notes = "调用 pSpace serverGetAllConnectInfo")
    public Result<List<PsConnectInfo>> getAllConnectInfo() {
        try {
            PsResult<PsConnectInfo> result = coldSourceServerService.serverGetAllConnectInfo();
            if (result.isSuccess()) {
                return Result.ok(result.getData());
            }
            return Result.error("获取服务器连接信息失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("获取服务器连接信息异常", e);
            return Result.error("获取服务器连接信息异常: " + e.getMessage());
        }
    }

    /**
     * 查询服务器时间
     */
    @GetMapping("/server/getTime")
    @ApiOperation(value = "查询服务器时间", notes = "调用 pSpace serverGetTime")
    public Result<Long> getTime() {
        try {
            PsResult<Long> result = coldSourceServerService.serverGetTime();
            if (result.isSuccess()) {
                return Result.ok(result.getData().get(0));
            }
            return Result.error("获取服务器时间失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("获取服务器时间异常", e);
            return Result.error("获取服务器时间异常: " + e.getMessage());
        }
    }

    /**
     * 查询服务器属性（安全区、权限等）
     */
    @GetMapping("/server/getProp")
    @ApiOperation(value = "查询服务器属性", notes = "调用 pSpace serverGetProp")
    public Result<PsServerProp> getProp() {
        try {
            PsResult<PsServerProp> result = coldSourceServerService.serverGetProp();
            if (result.isSuccess()) {
                return Result.ok(result.getData().get(0));
            }
            return Result.error("获取服务器属性失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("获取服务器属性异常", e);
            return Result.error("获取服务器属性异常: " + e.getMessage());
        }
    }

    /**
     * 更新点位信息数据（单点写值/控制）
     * 请求体: {"tagId": 600, "value": "1"}  或 {"tagId": 600, "value": "25.5"}
     */
    @PostMapping("/realWrite")
    @ApiOperation(value = "更新点位信息数据(单点)", notes = "调用 pSpace realWrite 按测点ID写值")
    public Result<Base> realWrite(@RequestBody ColdSourceWriteDto dto) {
        try {
            if (dto.getTagId() == null || dto.getValue() == null) {
                return Result.error("tagId 和 value 不能为空");
            }
            PsResult<Base> result = coldSourceServerService.realWrite(dto.getTagId(), dto.getValue());
            if (result.isSuccess()) {
                return Result.ok("写点成功");
            }
            return Result.error("写点失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("冷源写点异常: tagId={}, value={}", dto.getTagId(), dto.getValue(), e);
            return Result.error("冷源写点异常: " + e.getMessage());
        }
    }

    /**
     * 更新点位信息数据（批量写值）
     * 请求体: {"tagIds": [600,601], "values": ["1","25.5"]}
     */
    @PostMapping("/realWriteList")
    @ApiOperation(value = "更新点位信息数据(批量)", notes = "调用 pSpace realWriteList 批量按测点ID写值")
    public Result<Base> realWriteList(@RequestBody ColdSourceWriteDto dto) {
        try {
            if (dto.getTagIds() == null || dto.getValues() == null || dto.getTagIds().isEmpty()
                    || dto.getTagIds().size() != dto.getValues().size()) {
                return Result.error("tagIds 与 values 不能为空且数量必须一致");
            }
            PsResult<Base> result = coldSourceServerService.realWriteList(dto.getTagIds(), dto.getValues());
            if (result.isSuccess()) {
                return Result.ok("批量写点成功");
            }
            return Result.error("批量写点失败, code=" + result.getCode());
        } catch (Exception e) {
            log.error("冷源批量写点异常", e);
            return Result.error("冷源批量写点异常: " + e.getMessage());
        }
    }

}
