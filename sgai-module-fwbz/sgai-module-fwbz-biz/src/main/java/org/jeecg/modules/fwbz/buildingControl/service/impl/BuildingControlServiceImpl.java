package org.jeecg.modules.fwbz.buildingControl.service.impl;

import com.sunwayland.pspace.entity.Base;
import com.sunwayland.pspace.entity.PsResult;
import com.sunwayland.pspace.enums.PsErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.buildingControl.dto.UpdRealDataItemDto;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlService;
import org.jeecg.modules.fwbz.buildingControl.service.BuildingControlServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 楼控系统实时数据写入服务实现
 *
 * 连接复用 BuildingControlServerService 管理的长连接（幂等），
 * 避免每次写点都 connect/disconnect 干扰实时订阅链路。
 */
@Slf4j
@Service
public class BuildingControlServiceImpl implements BuildingControlService {

    @Autowired
    private BuildingControlServerService buildingControlServerService;

    /**
     * 向楼控系统(pSpace)写入实时数据（写点/控制）
     *
     * @param items 前端传入的更新项（tagid + 设定值）
     * @return 楼控系统返回结果
     */
    @Override
    public String updRealData(UpdRealDataItemDto items) {
        if (items == null) {
            throw new IllegalArgumentException("更新数据不能为空");
        }
        PsResult<Base> result = buildingControlServerService.realWrite(items.getTagid(), items.getPv());
        PsErrorCodeEnum code = result.getCode();
        if (!Objects.equals(code, PsErrorCodeEnum.PSRET_OK)) {
            log.warn("楼控写点失败: tagid={}, value={}, code={}", items.getTagid(), items.getPv(), code);
        }
        if (result.getData() == null || result.getData().isEmpty()) {
            return "写点完成: " + code;
        }
        return result.getData().get(0).toString();
    }
}
