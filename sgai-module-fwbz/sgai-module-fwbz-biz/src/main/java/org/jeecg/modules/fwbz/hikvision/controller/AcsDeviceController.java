package org.jeecg.modules.fwbz.hikvision.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDeviceListVO;
import org.jeecg.modules.fwbz.hikvision.dto.AcsDevicePageDto;
import org.jeecg.modules.fwbz.hikvision.service.IAcsDeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
