package org.jeecg.modules.master.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.master.common.IntegrationHttpExecutor;
import org.jeecg.modules.master.common.PushPayloadBuilder;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.entity.IntegrationLog;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.entity.IntegrationSystemCategory;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.mapper.DeviceCategoryMapper;
import org.jeecg.modules.master.mapper.DeviceMapper;
import org.jeecg.modules.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.modules.master.mapper.IntegrationSystemMapper;
import org.jeecg.modules.master.mapper.SpaceMapper;
import org.jeecg.modules.master.service.IIntegrationLogService;
import org.jeecg.modules.master.service.IIntegrationPushService;
import org.jeecg.modules.master.vo.IntegrationPayload;
import org.jeecg.modules.master.vo.PushSnapshotResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IntegrationPushServiceImpl implements IIntegrationPushService {

    @Autowired private IntegrationSystemMapper integrationSystemMapper;
    @Autowired private IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Autowired private DeviceCategoryMapper deviceCategoryMapper;
    @Autowired private SpaceMapper spaceMapper;
    @Autowired private DeviceMapper deviceMapper;
    @Autowired private IIntegrationLogService logService;
    @Autowired private IntegrationHttpExecutor httpExecutor;

    @Override
    public void pushOne(IntegrationSystem system, IntegrationPayload payload) {
        if (system == null || payload == null) {
            return;
        }
        PushOutcome o = doPush(system, payload);
        writePushLog(system, payload, o);
    }

    @Override
    public List<PushSnapshotResult> pushSnapshotForSystem(String systemId) {
        IntegrationSystem sys = integrationSystemMapper.selectById(systemId);
        if (sys == null) {
            throw new JeecgBootException("对接系统不存在");
        }
        if (!Integer.valueOf(1).equals(sys.getPushEnabled())) {
            throw new JeecgBootException("该系统未启用推送");
        }
        Set<String> categoryIds = loadCategoryScope(systemId);

        List<PushSnapshotResult> results = new ArrayList<>();
        // 1. 空间：恒全量
        List<Space> spaces = spaceMapper.selectList(null);
        results.add(snapshot(sys, IntegrationPayload.Type.SPACE,
                buildPayload(sys, IntegrationPayload.Type.SPACE, spaces)));
        // 2. 类别：类别集内
        List<DeviceCategory> cats = categoryIds.isEmpty() ? Collections.emptyList()
                : deviceCategoryMapper.selectBatchIds(categoryIds);
        results.add(snapshot(sys, IntegrationPayload.Type.CATEGORY,
                buildPayload(sys, IntegrationPayload.Type.CATEGORY, cats)));
        // 3. 设备：category_id ∈ 集合
        List<Device> devices = categoryIds.isEmpty() ? Collections.emptyList()
                : deviceMapper.selectList(new LambdaQueryWrapper<Device>()
                        .in(Device::getCategoryId, categoryIds));
        results.add(snapshot(sys, IntegrationPayload.Type.DEVICE,
                buildPayload(sys, IntegrationPayload.Type.DEVICE, devices)));
        return results;
    }

    // ---------- 私有工具 ----------

    @SuppressWarnings("unchecked")
    private <T> IntegrationPayload buildPayload(IntegrationSystem sys,
                                                IntegrationPayload.Type type, List<T> data) {
        String batchId = IdUtil.simpleUUID();
        if (type == IntegrationPayload.Type.DEVICE) {
            return PushPayloadBuilder.devices(sys.getCode(), IntegrationPayload.Op.SNAPSHOT, batchId, (List<Device>) data);
        } else if (type == IntegrationPayload.Type.CATEGORY) {
            return PushPayloadBuilder.categories(sys.getCode(), IntegrationPayload.Op.SNAPSHOT, batchId, (List<DeviceCategory>) data);
        } else {
            return PushPayloadBuilder.spaces(sys.getCode(), IntegrationPayload.Op.SNAPSHOT, batchId, (List<Space>) data);
        }
    }

    private PushSnapshotResult snapshot(IntegrationSystem sys, IntegrationPayload.Type type, IntegrationPayload payload) {
        PushOutcome o = doPush(sys, payload);
        writePushLog(sys, payload, o);
        PushSnapshotResult r = new PushSnapshotResult();
        r.setType(type.name());
        r.setPayloadCount(payload.dataCount());
        r.setStatus(o.status);
        r.setError(o.error);
        return r;
    }

    private PushOutcome doPush(IntegrationSystem sys, IntegrationPayload payload) {
        String json = JSONUtil.toJsonStr(payload);
        long start = System.currentTimeMillis();
        int status = httpExecutor.post(sys.getPushUrl(), sys.getToken(), json);
        int cost = (int) (System.currentTimeMillis() - start);
        PushOutcome o = new PushOutcome();
        o.json = json;
        o.costMs = cost;
        if (status >= 200 && status < 300) {
            o.status = "SUCCESS";
            o.error = null;
        } else {
            o.status = "FAIL";
            o.error = status < 0 ? "请求异常/超时" : ("HTTP " + status);
        }
        return o;
    }

    private void writePushLog(IntegrationSystem sys, IntegrationPayload payload, PushOutcome o) {
        IntegrationLog log = new IntegrationLog();
        log.setDirection("PUSH");
        log.setSystemId(sys.getId());
        log.setSystemCode(sys.getCode());
        log.setType(payload.getType().name());
        log.setOp(payload.getOp().name());
        log.setBatchId(payload.getBatchId());
        log.setPayloadCount(payload.dataCount());
        log.setStatus(o.status);
        log.setPayload(o.json);
        log.setError(o.error);
        log.setCostMs(o.costMs);
        log.setCreateBy("system"); // 实时/自动场景
        logService.writeLog(log);
    }

    private Set<String> loadCategoryScope(String systemId) {
        List<IntegrationSystemCategory> rows = integrationSystemCategoryMapper.selectList(
                new LambdaQueryWrapper<IntegrationSystemCategory>()
                        .eq(IntegrationSystemCategory::getSystemId, systemId));
        return rows.stream()
                .map(IntegrationSystemCategory::getCategoryId)
                .collect(Collectors.toSet());
    }

    /** 推送执行结果（内部）。 */
    private static class PushOutcome {
        String status;
        String error;
        String json;
        int costMs;
    }
}
