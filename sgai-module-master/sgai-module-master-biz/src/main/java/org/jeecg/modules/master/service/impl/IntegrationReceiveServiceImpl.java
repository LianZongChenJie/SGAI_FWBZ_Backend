package org.jeecg.modules.master.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.IntegrationLog;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.entity.IntegrationSystemCategory;
import org.jeecg.modules.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.modules.master.service.IDeviceService;
import org.jeecg.modules.master.service.IIntegrationLogService;
import org.jeecg.modules.master.service.IIntegrationReceiveService;
import org.jeecg.modules.master.service.IIntegrationSystemService;
import org.jeecg.modules.master.vo.DevicePushItem;
import org.jeecg.modules.master.vo.IntegrationPayload;
import org.jeecg.modules.master.vo.ReceivePayload;
import org.jeecg.modules.master.vo.ReceiveResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IntegrationReceiveServiceImpl implements IIntegrationReceiveService {

    @Autowired private IIntegrationSystemService integrationSystemService;
    @Autowired private IDeviceService deviceService;
    @Autowired private IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Autowired private IIntegrationLogService logService;

    @Override
    public ReceiveResult receive(ReceivePayload payload, String token) {
        IntegrationSystem src = integrationSystemService.findByToken(token);
        if (src == null) {
            writeReceiveLog(null, payload, 0, "FAIL", "对接令牌无效或接收未启用");
            throw new JeecgBootException("对接令牌无效或接收未启用");
        }

        ReceiveResult result = new ReceiveResult();
        result.setBatchId(payload == null ? null : payload.getBatchId());
        int accepted = 0;
        List<ReceiveResult.Reject> rejects = result.getRejected();

        if (payload != null && payload.getData() != null && !payload.getData().isEmpty()) {
            if (payload.getType() != IntegrationPayload.Type.DEVICE) {
                for (DevicePushItem dp : payload.getData()) {
                    rejects.add(new ReceiveResult.Reject(dp.getId(), "仅支持设备接收"));
                }
            } else {
                Set<String> scope = loadCategoryScope(src.getId());
                boolean isDelete = payload.getOp() == IntegrationPayload.Op.DELETE;
                for (DevicePushItem dp : payload.getData()) {
                    try {
                        if (dp.getCategoryId() == null || !scope.contains(dp.getCategoryId())) {
                            throw new JeecgBootException("类别不在允许范围");
                        }
                        Device d = toDevice(dp);
                        if (isDelete) {
                            deviceService.deleteFromIntegration(d, src.getCode());
                        } else {
                            deviceService.upsertFromIntegration(d, src.getCode());
                        }
                        accepted++;
                    } catch (Exception e) {
                        rejects.add(new ReceiveResult.Reject(dp.getId(), msg(e)));
                    }
                }
            }
        }

        String status = rejects.isEmpty() ? "SUCCESS" : "PARTIAL";
        String error = null;
        if (!rejects.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ReceiveResult.Reject r : rejects) {
                sb.append(r.getId()).append(":").append(r.getReason()).append(";");
            }
            error = sb.toString();
        }
        result.setAccepted(accepted);
        writeReceiveLog(src, payload, accepted, status, error);
        return result;
    }

    // ---------- 私有工具 ----------

    private Set<String> loadCategoryScope(String systemId) {
        List<IntegrationSystemCategory> rows = integrationSystemCategoryMapper.selectList(
                new LambdaQueryWrapper<IntegrationSystemCategory>()
                        .eq(IntegrationSystemCategory::getSystemId, systemId));
        return rows.stream()
                .map(IntegrationSystemCategory::getCategoryId)
                .collect(Collectors.toSet());
    }

    private Device toDevice(DevicePushItem dp) {
        Device d = new Device();
        d.setId(dp.getId());
        d.setName(dp.getName());
        d.setCategoryId(dp.getCategoryId());
        d.setSpaceId(dp.getSpaceId());
        d.setRemark(dp.getRemark());
        return d;
    }

    private String msg(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    private void writeReceiveLog(IntegrationSystem src, ReceivePayload payload,
                                 int accepted, String status, String error) {
        IntegrationLog log = new IntegrationLog();
        log.setDirection("RECEIVE");
        if (src != null) {
            log.setSystemId(src.getId());
            log.setSystemCode(src.getCode());
            log.setCreateBy(src.getCode()); // 来源系统标识
        }
        log.setType(payload != null && payload.getType() != null ? payload.getType().name() : "DEVICE");
        log.setOp(payload != null && payload.getOp() != null ? payload.getOp().name() : "UPSERT");
        log.setBatchId(payload != null ? payload.getBatchId() : null);
        log.setPayloadCount(payload != null && payload.getData() != null ? payload.getData().size() : 0);
        log.setStatus(status);
        log.setPayload(payload != null ? JSONUtil.toJsonStr(payload) : null);
        log.setError(error);
        logService.writeLog(log);
    }
}
