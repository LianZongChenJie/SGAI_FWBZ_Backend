package org.jeecg.modules.fwbz.history.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.history.service.IDeviceAttributeHistoryCleanService;
import org.jeecg.modules.fwbz.mdm.entity.DeviceAttributeHistory;
import org.jeecg.modules.fwbz.mdm.service.IDeviceAttributeHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备属性历史数据清理服务实现
 * <p>
 * 分批删除（每批 500 条）：先查出过期记录 id，再按 id 集合删除，
 * 避免一次 DELETE 扫描全表产生大事务/锁表（达梦对大事务支持有限）。
 * </p>
 *
 * @author fwbz
 */
@Slf4j
@Service
@AllArgsConstructor
public class DeviceAttributeHistoryCleanServiceImpl implements IDeviceAttributeHistoryCleanService {

    /** 单批删除条数 */
    private static final int BATCH_SIZE = 500;

    private final IDeviceAttributeHistoryService deviceAttributeHistoryService;

    @Override
    public int cleanHistoryBefore(LocalDateTime cutoff) {
        if (cutoff == null) {
            return 0;
        }
        int total = 0;
        while (true) {
            // 先查出一批过期记录的 id（LIMIT 分页取），再按 id 批量删除
            List<DeviceAttributeHistory> batch = deviceAttributeHistoryService.list(
                    new LambdaQueryWrapper<DeviceAttributeHistory>()
                            .select(DeviceAttributeHistory::getId)
                            .lt(DeviceAttributeHistory::getCollectionTime, cutoff)
                            .last("LIMIT " + BATCH_SIZE));
            if (batch.isEmpty()) {
                break;
            }
            List<Long> ids = batch.stream()
                    .map(DeviceAttributeHistory::getId)
                    .collect(Collectors.toList());
            deviceAttributeHistoryService.removeByIds(ids);
            total += ids.size();
            if (ids.size() < BATCH_SIZE) {
                break;
            }
        }
        return total;
    }
}
