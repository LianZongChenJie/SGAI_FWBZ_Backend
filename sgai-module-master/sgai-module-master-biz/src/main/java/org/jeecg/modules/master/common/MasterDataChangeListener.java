package org.jeecg.modules.master.common;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.master.entity.Device;
import org.jeecg.modules.master.entity.DeviceCategory;
import org.jeecg.modules.master.entity.IntegrationSystem;
import org.jeecg.modules.master.entity.IntegrationSystemCategory;
import org.jeecg.modules.master.entity.Space;
import org.jeecg.modules.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.modules.master.mapper.IntegrationSystemMapper;
import org.jeecg.modules.master.service.IIntegrationPushService;
import org.jeecg.modules.master.vo.IntegrationPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实时增量推送监听器：主数据事务提交后异步 fan-out 到命中系统。
 * 一次性预载所有 push_enabled 系统及其类别范围（对接系统数量小），按变更实体 categoryId 精确匹配。
 */
@Component
public class MasterDataChangeListener {

    @Autowired private IntegrationSystemMapper integrationSystemMapper;
    @Autowired private IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Autowired private IIntegrationPushService pushService;

    @Async("integrationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChange(MasterDataChangeEvent event) {
        if (event == null) {
            return;
        }
        switch (event.getEntityType()) {
            case DEVICE:
                fanoutDevices(event);
                break;
            case CATEGORY:
                fanoutCategories(event);
                break;
            case SPACE:
                fanoutSpaces(event);
                break;
            default:
        }
    }

    private void fanoutDevices(MasterDataChangeEvent event) {
        List<Device> devices = event.getDevices();
        if (devices == null || devices.isEmpty()) {
            return;
        }
        Loaded systems = load();
        Set<String> targetCatIds = devices.stream()
                .map(Device::getCategoryId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> hitCodes = CategoryScopeResolver.resolveHitSystems(
                systems.scopeByCode, targetCatIds, event.getExcludeSystemCode());
        IntegrationPayload.Op op = event.getOp() == MasterDataChangeEvent.Op.DELETE
                ? IntegrationPayload.Op.DELETE : IntegrationPayload.Op.UPSERT;
        for (String code : hitCodes) {
            IntegrationSystem sys = systems.systemByCode.get(code);
            Set<String> scope = systems.scopeByCode.get(code);
            List<Device> subset = devices.stream()
                    .filter(d -> scope != null && scope.contains(d.getCategoryId()))
                    .collect(Collectors.toList());
            if (subset.isEmpty()) {
                continue;
            }
            pushService.pushOne(sys, PushPayloadBuilder.devices(
                    code, op, IdUtil.simpleUUID(), subset));
        }
    }

    private void fanoutCategories(MasterDataChangeEvent event) {
        List<DeviceCategory> cats = event.getCategories();
        if (cats == null || cats.isEmpty()) {
            return;
        }
        Loaded systems = load();
        Set<String> targetIds = cats.stream()
                .map(DeviceCategory::getId)
                .collect(Collectors.toSet());
        Set<String> hitCodes = CategoryScopeResolver.resolveHitSystems(
                systems.scopeByCode, targetIds, event.getExcludeSystemCode());
        IntegrationPayload.Op op = event.getOp() == MasterDataChangeEvent.Op.DELETE
                ? IntegrationPayload.Op.DELETE : IntegrationPayload.Op.UPSERT;
        for (String code : hitCodes) {
            IntegrationSystem sys = systems.systemByCode.get(code);
            pushService.pushOne(sys, PushPayloadBuilder.categories(
                    code, op, IdUtil.simpleUUID(), cats));
        }
    }

    private void fanoutSpaces(MasterDataChangeEvent event) {
        List<Space> spaces = event.getSpaces();
        if (spaces == null || spaces.isEmpty()) {
            return;
        }
        Loaded systems = load();
        IntegrationPayload.Op op = event.getOp() == MasterDataChangeEvent.Op.DELETE
                ? IntegrationPayload.Op.DELETE : IntegrationPayload.Op.UPSERT;
        for (IntegrationSystem sys : systems.systemByCode.values()) {
            if (sys.getCode() != null && sys.getCode().equals(event.getExcludeSystemCode())) {
                continue;
            }
            pushService.pushOne(sys, PushPayloadBuilder.spaces(
                    sys.getCode(), op, IdUtil.simpleUUID(), spaces));
        }
    }

    /** 一次性预载所有 push_enabled 系统 + 各类别范围（code → categoryId 集合）。 */
    private Loaded load() {
        Loaded loaded = new Loaded();
        List<IntegrationSystem> systems = integrationSystemMapper.selectList(
                new LambdaQueryWrapper<IntegrationSystem>()
                        .eq(IntegrationSystem::getPushEnabled, 1));
        if (systems == null || systems.isEmpty()) {
            return loaded;
        }
        for (IntegrationSystem s : systems) {
            if (StrUtil.isNotBlank(s.getCode())) {
                loaded.systemByCode.put(s.getCode(), s);
            }
        }
        Set<String> sysIds = systems.stream().map(IntegrationSystem::getId).collect(Collectors.toSet());
        List<IntegrationSystemCategory> rows = integrationSystemCategoryMapper.selectList(
                new LambdaQueryWrapper<IntegrationSystemCategory>()
                        .in(IntegrationSystemCategory::getSystemId, sysIds));
        Map<String, String> idToCode = systems.stream()
                .collect(Collectors.toMap(IntegrationSystem::getId, IntegrationSystem::getCode));
        for (IntegrationSystemCategory r : rows) {
            String code = idToCode.get(r.getSystemId());
            if (code == null) {
                continue;
            }
            loaded.scopeByCode.computeIfAbsent(code, k -> new HashSet<>()).add(r.getCategoryId());
        }
        return loaded;
    }

    private static class Loaded {
        final Map<String, IntegrationSystem> systemByCode = new HashMap<>();
        final Map<String, Set<String>> scopeByCode = new HashMap<>();
    }
}
