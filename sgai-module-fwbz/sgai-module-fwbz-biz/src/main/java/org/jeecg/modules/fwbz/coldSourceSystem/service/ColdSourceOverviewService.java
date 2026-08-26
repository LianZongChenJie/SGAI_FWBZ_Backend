package org.jeecg.modules.fwbz.coldSourceSystem.service;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.fwbz.coldSourceSystem.entity.TablePageInfo;
import org.jeecg.modules.fwbz.coldSourceSystem.mapper.TablePageInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 冷源系统「前端字段 key -> 测点(tagId)」映射表服务
 *
 * 维护 fieldMap 供实时订阅推送服务（{@link ColdSourceRealPushService}）做 tagId -> key 反查：
 * 冷源 SDK 订阅回传 tagId，此处提供该 tagId 对应的前端字段 key（一个测点可映射多个 key）。
 *
 * 说明：映射数据从数据库表 FWBZ.table_page_info 读取（front_data=前端字段 key，tag_id=测点ID），
 * 由人工在数据库中维护；tag_id 为 NULL 表示该 key 在点表中无对应测点（接口返回 null，前端可兜底）。
 */
@Slf4j
@Service
public class ColdSourceOverviewService {

    @Autowired
    private TablePageInfoMapper tablePageInfoMapper;

    /** 前端字段 key -> 测点ID数组(tagId[])，null 表示该 key 在点表中无对应测点（返回 null） */
    private Map<String, List<Long>> fieldMap;

    @PostConstruct
    public void init() {
        this.fieldMap = buildFieldMap();
    }

    /**
     * 从数据库表 FWBZ.table_page_info 构建前端字段 key -> 测点ID(tagId) 映射表。
     * front_data 列为前端字段 key，tag_id 列为采集点ID；
     * tag_id 为 NULL 表示点表中无对应测点（接口返回 null，前端可兜底）。
     */
    private Map<String, List<Long>> buildFieldMap() {
        Map<String, List<Long>> m = new LinkedHashMap<>();
        List<TablePageInfo> list = tablePageInfoMapper.selectList(null);
        for (TablePageInfo info : list) {
            String key = info.getFrontData();
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            Long tagId = info.getTagId();
            if (tagId == null) {
                // 点表中无对应测点 -> null（前端兜底）
                m.putIfAbsent(key, null);
            } else {
                List<Long> ids = m.get(key);
                if (ids == null) {
                    // 首次出现（或此前为 null 占位），转为真实列表
                    ids = new ArrayList<>();
                    m.put(key, ids);
                }
                ids.add(tagId);
            }
        }
        log.info("冷源前端字段映射表已从 table_page_info 加载完成，共 {} 个字段 key", m.size());
        return m;
    }

    /**
     * 当前映射的字段数量（调试用）
     */
    public int getFieldCount() {
        return fieldMap.size();
    }

    /**
     * 全量返回 FIELD_MAP 映射：前端字段 key -> 测点ID数组(tagId[])，null 表示该 key 无对应测点
     *
     * @return 只读视图（防御性拷贝，外部修改不影响内部映射）
     */
    public Map<String, List<Long>> getFieldMap() {
        Map<String, List<Long>> copy = new LinkedHashMap<>();
        fieldMap.forEach((key, ids) -> copy.put(key, ids == null ? null : new ArrayList<>(ids)));
        return copy;
    }
}
