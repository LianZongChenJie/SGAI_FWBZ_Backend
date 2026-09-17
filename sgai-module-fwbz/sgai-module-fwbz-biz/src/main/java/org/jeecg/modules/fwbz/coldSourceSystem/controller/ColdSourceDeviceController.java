package org.jeecg.modules.fwbz.coldSourceSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sunwayland.pspace.entity.PsData;
import com.sunwayland.pspace.entity.PsDataWithTagId;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceDeviceDetailDto;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceDevicePageDto;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceDeviceQueryDto;
import org.jeecg.modules.fwbz.coldSourceSystem.dto.ColdSourceDeviceStatsDto;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceDevice;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.ColdSourceDeviceAttribute;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TableColdSourceHistory;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.ColdSourceDeviceAttributeMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.ColdSourceDeviceMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.TableColdSourceHistoryMapper;
import org.jeecg.modules.fwbz.coldSourceSystem.service.SaveHisttoryService;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 冷源设备列表
 */
@RestController
@RequestMapping("/fwbz/coldSource/device")
@AllArgsConstructor
@Slf4j
@Api(tags = "冷源设备列表")
public class ColdSourceDeviceController {

    private final ColdSourceDeviceMapper coldSourceDeviceMapper;

    private final ColdSourceDeviceAttributeMapper coldSourceDeviceAttributeMapper;

    private final SaveHisttoryService saveHisttoryService;

    private final TableColdSourceHistoryMapper tableColdSourceHistoryMapper;

    /** 今日制冷量统计 tagid（制冷量累计） */
    private static final List<Long> COOLING_CAPACITY_TAG_IDS = Arrays.asList(542L, 549L, 556L, 563L, 577L);

    /** 今日功率统计 tagid */
    private static final List<Long> POWER_TAG_IDS = Arrays.asList(543L, 550L, 557L, 564L, 578L);

    /**
     * 查询冷源设备列表（分页）
     * 条件: deviceName(设备名称, 模糊) / deviceCode(设备编号, 模糊) / status(设备状态, 精确) / categoryId(设备类别id, 精确)
     * 返回: 设备信息 + 类别名称（关联 cold_source_equipment_category）
     */
    @GetMapping("/page")
    @ApiOperation(value = "查询冷源设备列表(分页)", notes = "条件: deviceName(设备名称模糊)/deviceCode(设备编号模糊)/status(设备状态精确)/categoryId(设备类别id精确); 返回设备信息含类别名称")
    public Result<IPage<ColdSourceDevicePageDto>> queryDevicePage(ColdSourceDeviceQueryDto params) {
        try {
            Page<ColdSourceDevicePageDto> page = new Page<>(
                    params.getPageNo() == null ? 1 : params.getPageNo(),
                    params.getPageSize() == null ? 10 : params.getPageSize());
            return Result.ok(coldSourceDeviceMapper.selectDevicePage(page,
                    params.getDeviceName(), params.getDeviceCode(), params.getStatus(), params.getCategoryId()));
        } catch (Exception e) {
            log.error("查询冷源设备列表异常", e);
            return Result.error("查询冷源设备列表异常: " + e.getMessage());
        }
    }

    /**
     * 查询冷源设备列表（不分页，按条件查全部，用于下拉等场景）
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询冷源设备列表(不分页)", notes = "条件与分页接口一致; 返回全部匹配设备信息含类别名称")
    public Result<List<ColdSourceDevicePageDto>> queryDeviceList(ColdSourceDeviceQueryDto params) {
        try {
            return Result.ok(coldSourceDeviceMapper.selectDeviceList(
                    params.getDeviceName(), params.getDeviceCode(), params.getStatus(), params.getCategoryId()));
        } catch (Exception e) {
            log.error("查询冷源设备列表异常", e);
            return Result.error("查询冷源设备列表异常: " + e.getMessage());
        }
    }

    /**
     * 导出冷源设备列表
     * 入参与分页查询一致；分页信息(pageNo/pageSize)可不传，不传则导出全部，传则按分页信息导出对应页数据。
     * 导出内容关联 cold_source_equipment_category 设备类别表，包含类别名称列。
     */
    @GetMapping("/export")
    @AutoLog(value = "冷源设备列表-导出")
    @ApiOperation(value = "导出冷源设备列表", notes = "入参与分页查询一致; pageNo/pageSize可不传，不传导出全部，传则按分页导出")
    public void exportDevice(ColdSourceDeviceQueryDto params, HttpServletResponse response) throws Exception {
        List<ColdSourceDevicePageDto> list;
        if (params.getPageNo() != null && params.getPageSize() != null) {
            Page<ColdSourceDevicePageDto> page = new Page<>(params.getPageNo(), params.getPageSize());
            list = coldSourceDeviceMapper.selectDevicePage(page,
                    params.getDeviceName(), params.getDeviceCode(), params.getStatus(), params.getCategoryId()).getRecords();
        } else {
            list = coldSourceDeviceMapper.selectDeviceList(
                    params.getDeviceName(), params.getDeviceCode(), params.getStatus(), params.getCategoryId());
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode("冷源设备列表.xlsx", "UTF-8"));
        try (Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams("冷源设备列表", "冷源设备列表", ExcelType.XSSF),
                ColdSourceDevicePageDto.class, list)) {
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 读取冷源设备属性实时值
     * 获取 cold_source_device_attribute 表中所有 tagid 非空的属性（按 sort_order, id 排序），
     * 批量调用 readLatestValue 返回最新值；
     * 返回: 属性信息 + 实时值(value/实时dataType)，value 为 null 表示该测点读取失败或无数据
     */
    @GetMapping("/attribute/readValue")
    @ApiOperation(value = "读取冷源设备属性实时值", notes = "获取 cold_source_device_attribute 表中所有 tagid, 调用 readLatestValue 批量读取最新值")
    public Result<List<PsDataWithTagId>> readDeviceAttributeValue() {
        try {
            List<ColdSourceDeviceAttribute> attributes = coldSourceDeviceAttributeMapper.selectList(
                    new LambdaQueryWrapper<ColdSourceDeviceAttribute>()
                            .isNotNull(ColdSourceDeviceAttribute::getTagid)
                            .orderByAsc(ColdSourceDeviceAttribute::getSortOrder)
                            .orderByAsc(ColdSourceDeviceAttribute::getId));
            if (attributes == null || attributes.isEmpty()) {
                return Result.ok(Collections.emptyList());
            }
            // 收集全部 tagid，批量读取真实值（与属性列表一一对应）
            List<Long> tagIds = new ArrayList<>(attributes.size());
            for (ColdSourceDeviceAttribute attr : attributes) {
                tagIds.add(attr.getTagid().longValue());
            }
            List<PsDataWithTagId> latestList = saveHisttoryService.readLatestValue(tagIds);
            return Result.ok(latestList);
        } catch (Exception e) {
            log.error("读取冷源设备属性实时值异常", e);
            return Result.error("读取冷源设备属性实时值异常: " + e.getMessage());
        }
    }

    /**
     * 查询冷源设备详情
     * 返回: 设备信息（含类别名称）+ 关联属性列表（cold_source_device_attribute，按 sort_order 排序）
     *
     * @param deviceId 设备id
     */
    @GetMapping("/detail")
    @ApiOperation(value = "查询冷源设备详情", notes = "按设备id查询; 返回设备信息(含类别名称)及关联属性列表")
    public Result<ColdSourceDeviceDetailDto> queryDeviceDetail(@RequestParam Long deviceId) {
        try {
            if (deviceId == null) {
                return Result.error("设备id不能为空");
            }
            ColdSourceDevicePageDto device = coldSourceDeviceMapper.selectDeviceDetail(deviceId);
            if (device == null) {
                return Result.error("设备不存在: " + deviceId);
            }
            List<ColdSourceDeviceAttribute> attributes = coldSourceDeviceAttributeMapper.selectByDeviceId(deviceId);
            ColdSourceDeviceDetailDto detail = new ColdSourceDeviceDetailDto();
            detail.setId(device.getId());
            detail.setDeviceCode(device.getDeviceCode());
            detail.setDeviceName(device.getDeviceName());
            detail.setCategoryId(device.getCategoryId());
            detail.setCategoryName(device.getCategoryName());
            detail.setSystemCode(device.getSystemCode());
            detail.setNiagaraPath(device.getNiagaraPath());
            detail.setStatus(device.getStatus());
            detail.setSort(device.getSort());
            detail.setRemark(device.getRemark());
            detail.setAttributes(attributes);
            return Result.ok(detail);
        } catch (Exception e) {
            log.error("查询冷源设备详情异常", e);
            return Result.error("查询冷源设备详情异常: " + e.getMessage());
        }
    }

    /**
     * 冷源设备统计（首页统计卡片）
     * 返回: 冷源机组总数 / 在线 / 离线（cold_source_device 表 status 统计）；
     * 今日制冷量（table_cold_source_history 中 tagid 542/549/556/563/577 今日最新值 - 今日0点值 之和）；
     * 平均COP（今日制冷量 / 总功率，功率为 tagid 543/550/557/564/578 今日最新值之和）
     */
    @GetMapping("/stats")
    @ApiOperation(value = "冷源设备统计", notes = "返回机组总数/在线/离线/今日制冷量/平均COP")
    public Result<ColdSourceDeviceStatsDto> stats() {
        try {
            ColdSourceDeviceStatsDto stats = new ColdSourceDeviceStatsDto();
            // 1. 设备数量统计（cold_source_device: 1在线 / 0离线）
            Long total = coldSourceDeviceMapper.selectCount(null);
            Long online = coldSourceDeviceMapper.selectCount(
                    new LambdaQueryWrapper<ColdSourceDevice>().eq(ColdSourceDevice::getStatus, 1));
            stats.setDeviceTotal(total == null ? 0L : total);
            stats.setOnlineCount(online == null ? 0L : online);
            stats.setOfflineCount(Math.max(0L, stats.getDeviceTotal() - stats.getOnlineCount()));

            // 2. 今日制冷量 / 功率（table_cold_source_history）
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            Map<Long, BigDecimal> firstMap = toValueMap(
                    tableColdSourceHistoryMapper.selectDayFirstValues(COOLING_CAPACITY_TAG_IDS, startOfDay));
            Map<Long, BigDecimal> latestCoolingMap = toValueMap(
                    tableColdSourceHistoryMapper.selectDayLatestValues(COOLING_CAPACITY_TAG_IDS, startOfDay));
            Map<Long, BigDecimal> latestPowerMap = toValueMap(
                    tableColdSourceHistoryMapper.selectDayLatestValues(POWER_TAG_IDS, startOfDay));

            BigDecimal coolingCapacity = BigDecimal.ZERO;
            for (Long tagId : COOLING_CAPACITY_TAG_IDS) {
                BigDecimal latest = latestCoolingMap.get(tagId);
                BigDecimal first = firstMap.get(tagId);
                if (latest == null) {
                    continue;
                }
                // 无0点数据时按0点值为0处理（即今日增量）
                coolingCapacity = coolingCapacity.add(latest.subtract(first == null ? BigDecimal.ZERO : first));
            }
            stats.setTodayCoolingCapacity(coolingCapacity);

            BigDecimal power = BigDecimal.ZERO;
            boolean hasPower = false;
            for (Long tagId : POWER_TAG_IDS) {
                BigDecimal v = latestPowerMap.get(tagId);
                if (v != null) {
                    hasPower = true;
                    power = power.add(v);
                }
            }
            stats.setTodayPower(hasPower ? power : null);

            // 3. 平均COP = 今日制冷量 / 总功率
            if (hasPower && power.compareTo(BigDecimal.ZERO) > 0) {
                stats.setAvgCop(coolingCapacity.divide(power, 2, RoundingMode.HALF_UP));
            }
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("冷源设备统计异常", e);
            return Result.error("冷源设备统计异常: " + e.getMessage());
        }
    }

    /**
     * 历史记录列表转 tagid -> 值(BigDecimal) 映射，value 无法解析的忽略
     */
    private Map<Long, BigDecimal> toValueMap(List<TableColdSourceHistory> list) {
        Map<Long, BigDecimal> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (TableColdSourceHistory history : list) {
            if (history == null || history.getTagId() == null || history.getValue() == null) {
                continue;
            }
            try {
                map.put(history.getTagId(), new BigDecimal(history.getValue().trim()));
            } catch (NumberFormatException ignore) {
                log.warn("冷源设备统计: tagid={} 值 {} 无法转换为数字", history.getTagId(), history.getValue());
            }
        }
        return map;
    }
}
