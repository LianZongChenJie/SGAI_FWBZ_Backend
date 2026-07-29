# 数据对接管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在主数据（类别/空间/设备）之上新增「数据对接管理」——把主数据推送给配置好的对接系统（实时增量 + 手动全量），并接收外部系统按 id 推来的设备（按类别范围过滤 upsert + hub 分发），含对接系统运行时配置与对接日志。

**Architecture:** 方案 A——Spring `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`（专用线程池）做实时增量推送，不引入 MQ；hutool `HttpUtil` 发 HTTP；对接系统/类别范围/日志三张新表；通用 `MasterDataChangeEvent` 在现有三个 Service 的增删改方法内发布；接收端令牌鉴权 + 逐设备尽力而为 upsert + hub 异步分发。**id 全局唯一**充当跨系统键，设备表不新增外部编码字段。

**Tech Stack:** Java 17、jeecg-boot 3.7.0（Spring Boot + MyBatis-Plus + hutool + Mockito/JUnit5）、前端 fwbz-web（Vue3 + TS）。

## Global Constraints

（提炼自 spec 第 0/7 节与项目 CLAUDE.md，所有 task 隐含遵守）

- **零 mapper xml**：全部 `LambdaQueryWrapper` + `IService` 内置方法（`save/updateById/removeByIds/saveBatch/updateBatchById/selectPage/selectList/selectCount/selectBatchIds/selectById`），列表显示名称用「两次查询 + 内存拼」。
- **uuid 主键**：insert 前用 `org.jeecg.module.master.common.TreeFullNameHelper.generateUuid()`（即 `IdUtil.simpleUUID()`，32 位无横线），前端不传 id。**接收 upsert 例外**——外部系统已分配 id，按传入 id 落库，不重新生成。
- **业务校验失败**统一抛 `org.jeecg.common.exception.JeecgBootException("文案")`，由全局异常处理器转失败 `Result`。
- **物理删除**，不设 `del_flag`；删除采用「保守拒绝」。
- **事务**：涉及多次写入的 service 方法加 `@Transactional(rollbackFor = Exception.class)`；**日志写入用 `@Transactional(propagation = Propagation.REQUIRES_NEW)` 独立小事务**，绝不并入主数据事务。
- **启动类 `SgaiModuleMasterApplication` 已有 `@EnableAsync`**（`src/main/java/org/jeecg/SgaiModuleMasterApplication.java:20`）——异步监听器直接可用，**不要**再给启动类加注解。
- **不操作 git**（项目约定）：每个 task 以「测试通过 / 编译通过」为完成标志，**不自动 `git add/commit`**；plan 中原 commit step 替换为「告知用户可手动提交」。
- **后端测试**：父 pom 硬编码 `skipTests=true`，**用 IDE 运行**单测（右键 → Run），不要 `mvn test`。纯算法测试（common 包）与 Service 级 Mockito 测试都遵循现有 `DeviceCategoryServiceImplTest` 风格（`@ExtendWith(MockitoExtension.class)` + `@Mock` mapper + `@InjectMocks`）。
- **JSON 序列化**统一用 hutool `cn.hutool.json.JSONUtil`（项目已依赖 hutool）。
- **HTTP** 统一用 hutool `cn.hutool.http.HttpRequest`（便于带 header + timeout）。
- **接收接口登录放行**：`POST /master/integration/receive` 对外部系统开放，需排除平台登录拦截。放行配置在 **nacos 的 `jeecg.yaml`**（不在本仓库 `application.yml`），实现时对照 `sgai-module-third` 的 `ThirdSystemLoginController`（用户最近正在改它）所用方式——优先 `@org.jeecg.common.aspect.annotation.IgnoreAuth` 注解，若平台无此注解则在 nacos `shiro.filterChainDefinitions` 加 `/master/integration/receive = anon`。具体见 Task 13。
- **前端**在独立仓库 `fwbz-web/src/views/master/`，骨架完全复刻 `views/master/device/`（列表 + 弹窗 + api.ts + data.ts 模式），仅给差异代码。

## 文件结构（新增 / 修改清单）

**新增后端**（`src/main/java/org/jeecg/module/master/`）：

```
common/
  MasterDataChangeEvent.java        通用变更事件（EntityType/Op 枚举 + 静态工厂 + excludeSystemCode）
  CategoryScopeResolver.java        纯算法：系统×类别集 → 精确匹配命中系统集
  PushPayloadBuilder.java           纯算法：组装 IntegrationPayload（设备/类别/空间 → PushItem）
  AsyncConfig.java                  @Configuration：integrationTaskExecutor 线程池 bean
  MasterDataChangeListener.java     @Component：@Async @TransactionalEventListener(AFTER_COMMIT) fan-out
entity/
  IntegrationSystem.java            对接系统（字段同表）
  IntegrationSystemCategory.java    对接系统-类别范围（system_id, category_id）
  IntegrationLog.java               对接日志（字段同表）
mapper/
  IntegrationSystemMapper.java      extends BaseMapper<IntegrationSystem>
  IntegrationSystemCategoryMapper.java
  IntegrationLogMapper.java
service/
  IIntegrationSystemService.java    对接系统 CRUD + 类别范围覆盖 + 删除停用校验
  IIntegrationPushService.java      报文发送 + HTTP + 日志 + 手动全量
  IIntegrationReceiveService.java   鉴权 + 过滤 + 逐条 upsert + hub
  IIntegrationLogService.java       日志写入(REQUIRES_NEW) + 查询
  impl/IntegrationSystemServiceImpl.java
  impl/IntegrationPushServiceImpl.java
  impl/IntegrationReceiveServiceImpl.java
  impl/IntegrationLogServiceImpl.java
controller/
  IntegrationSystemController.java  /master/integrationSystem（CRUD + /{id}/push 手动全量）
  IntegrationReceiveController.java /master/integration/receive
  IntegrationLogController.java     /master/integrationLog（只读 list/getById）
vo/
  IntegrationPayload.java           推送/接收统一报文（Type/Op 枚举 + data）
  DevicePushItem.java / CategoryPushItem.java / SpacePushItem.java
  ReceiveResult.java                { batchId, accepted, rejected[] }
  IntegrationSystemForm.java        新增/编辑/详情表单（含 categoryIds[]）
```

**修改后端**（仅必要点）：

- `service/IDeviceService.java`：新增 `upsertFromIntegration(Device, String excludeSystemCode)` 与 `deleteFromIntegration(String deviceId, String excludeSystemCode)`。
- `service/impl/DeviceServiceImpl.java`：实现上述两方法；并给 `create/updateNode/removeBatch/batchImport` 接入 `MasterDataChangeEvent` 发布。
- `service/impl/DeviceCategoryServiceImpl.java`：给 `create/updateNode/removeNode` 接入事件发布。
- `service/impl/SpaceServiceImpl.java`：同上。

**新增 SQL**（用户手动执行）：`docs/sql/2026-07-08-data-integration-ddl.sql`

**新增前端**（`fwbz-web/src/views/master/`）：`integrationSystem/`（index.vue + api + data + Modal）、`integrationLog/`（index.vue + api + data）。

---

## Task 1: 建表 DDL 文件

**Files:**
- Create: `docs/sql/2026-07-08-data-integration-ddl.sql`

- [ ] **Step 1: 创建 DDL 文件**

内容（来自 spec 第 4 节，逐字一致）：

```sql
-- 数据对接管理 建表 DDL（用户在目标库手动执行，不要自动跑迁移）
CREATE TABLE integration_system (
  id              varchar(32)  NOT NULL COMMENT '主键uuid',
  name            varchar(100) NOT NULL COMMENT '系统名称',
  code            varchar(50)  NOT NULL COMMENT '系统编码(唯一,日志冗余追溯)',
  push_enabled    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否启用推送 0否1是',
  push_url        varchar(500) NULL COMMENT '推送目标URL',
  push_token      varchar(100) NULL COMMENT '我们→下游鉴权令牌',
  receive_enabled tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否启用接收 0否1是',
  receive_token   varchar(100) NULL COMMENT '下游→我们令牌(唯一,反查来源)',
  remark          varchar(500) NULL COMMENT '备注',
  create_by       varchar(50)  NULL,
  create_time     datetime     NULL,
  update_by       varchar(50)  NULL,
  update_time     datetime     NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code),
  UNIQUE KEY uk_receive_token (receive_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对接系统';

CREATE TABLE integration_system_category (
  id          varchar(32) NOT NULL COMMENT '主键uuid',
  system_id   varchar(32) NOT NULL COMMENT '对接系统id',
  category_id varchar(32) NOT NULL COMMENT '类别id',
  PRIMARY KEY (id),
  UNIQUE KEY uk_system_category (system_id, category_id),
  KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对接系统-类别范围(推送/接收共用)';

CREATE TABLE integration_log (
  id            varchar(32)   NOT NULL COMMENT '主键uuid',
  direction     varchar(10)   NOT NULL COMMENT 'PUSH/RECEIVE',
  system_id     varchar(32)   NULL COMMENT '对接系统id',
  system_code   varchar(50)   NULL COMMENT '对接系统编码(冗余,系统删除后仍可追溯)',
  type          varchar(10)   NOT NULL COMMENT 'CATEGORY/SPACE/DEVICE',
  op            varchar(10)   NOT NULL COMMENT 'UPSERT/DELETE/SNAPSHOT',
  batch_id      varchar(32)   NOT NULL COMMENT '批次id',
  payload_count int           NOT NULL DEFAULT 0 COMMENT '数据条数',
  status        varchar(10)   NOT NULL COMMENT 'SUCCESS/PARTIAL/FAIL',
  payload       text          NULL COMMENT '原始报文JSON(仅审计)',
  error         varchar(2000) NULL COMMENT '失败原因/接收逐条拒绝明细',
  cost_ms       int           NULL COMMENT '耗时毫秒',
  create_by     varchar(50)   NULL,
  create_time   datetime      NULL,
  PRIMARY KEY (id),
  KEY idx_system (system_id),
  KEY idx_batch (batch_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对接日志';
```

- [ ] **Step 2: 交付用户执行**

告知用户：在目标库手动执行该 SQL；`receive_token` 唯一键 MySQL 允许多 NULL，未启用接收的系统互不冲突。

---

## Task 2: MasterDataChangeEvent（通用变更事件）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/common/MasterDataChangeEvent.java`
- Test: `src/test/java/org/jeecg/module/master/common/MasterDataChangeEventTest.java`

**Interfaces:**
- Produces: `MasterDataChangeEvent`（普通 POJO，Spring `@TransactionalEventListener` 可直接监听任意类型，**无需** extends `ApplicationEvent`）；嵌套枚举 `MasterDataChangeEvent.EntityType { CATEGORY, SPACE, DEVICE }`、`MasterDataChangeEvent.Op { CREATE, UPDATE, DELETE }`；静态工厂 `ofDevices(Op, List<Device>, String excludeSystemCode)` / `ofCategories(...)` / `ofSpaces(...)`；getter `getEntityType()/getOp()/getDevices()/getCategories()/getSpaces()/getExcludeSystemCode()`。后续 Task 11/12/13 据此发布与读取事件。

- [ ] **Step 1: 写失败测试**

```java
package org.jeecg.module.master.common;

import org.jeecg.module.master.entity.Device;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MasterDataChangeEventTest {

    @Test
    void ofDevices_setsFieldsAndExclude() {
        Device d = new Device();
        d.setId("D1");
        d.setCategoryId("C1");
        List<Device> devices = Arrays.asList(d);

        MasterDataChangeEvent event = MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.CREATE, devices, "SYS_A");

        assertEquals(MasterDataChangeEvent.EntityType.DEVICE, event.getEntityType());
        assertEquals(MasterDataChangeEvent.Op.CREATE, event.getOp());
        assertSame(devices, event.getDevices());
        assertEquals("SYS_A", event.getExcludeSystemCode());
        assertTrue(event.getCategories().isEmpty());
        assertTrue(event.getSpaces().isEmpty());
    }

    @Test
    void ofDevices_nullExclude_allowed() {
        MasterDataChangeEvent event = MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Arrays.asList(), null);
        assertNull(event.getExcludeSystemCode());
    }
}
```

- [ ] **Step 2: 运行测试，确认失败（类不存在）**

IDE 运行 `MasterDataChangeEventTest`。Expected: 编译失败 / 类找不到。

- [ ] **Step 3: 实现**

```java
package org.jeecg.module.master.common;

import lombok.Getter;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.Space;

import java.util.Collections;
import java.util.List;

/**
 * 主数据变更事件（通用载体）。在主数据增删改事务内发布，
 * 由 @TransactionalEventListener(AFTER_COMMIT) + @Async 监听器做实时增量推送。
 *
 * affected* 列表携带完整实体字段（设备 categoryId、类别/空间 id 等），
 * 监听器据此做命中判定，无需回查库。
 * excludeSystemCode：仅 hub 接收分发时填（=来源系统 code），本地写入场景为 null。
 */
@Getter
public class MasterDataChangeEvent {

    public enum EntityType { CATEGORY, SPACE, DEVICE }
    public enum Op { CREATE, UPDATE, DELETE }

    private final EntityType entityType;
    private final Op op;
    private final List<Device> devices;
    private final List<DeviceCategory> categories;
    private final List<Space> spaces;
    private final String excludeSystemCode;

    private MasterDataChangeEvent(EntityType entityType, Op op,
                                  List<Device> devices,
                                  List<DeviceCategory> categories,
                                  List<Space> spaces,
                                  String excludeSystemCode) {
        this.entityType = entityType;
        this.op = op;
        this.devices = devices == null ? Collections.emptyList() : devices;
        this.categories = categories == null ? Collections.emptyList() : categories;
        this.spaces = spaces == null ? Collections.emptyList() : spaces;
        this.excludeSystemCode = excludeSystemCode;
    }

    public static MasterDataChangeEvent ofDevices(Op op, List<Device> devices, String excludeSystemCode) {
        return new MasterDataChangeEvent(EntityType.DEVICE, op, devices, null, null, excludeSystemCode);
    }

    public static MasterDataChangeEvent ofCategories(Op op, List<DeviceCategory> categories, String excludeSystemCode) {
        return new MasterDataChangeEvent(EntityType.CATEGORY, op, null, categories, null, excludeSystemCode);
    }

    public static MasterDataChangeEvent ofSpaces(Op op, List<Space> spaces, String excludeSystemCode) {
        return new MasterDataChangeEvent(EntityType.SPACE, op, null, null, spaces, excludeSystemCode);
    }
}
```

- [ ] **Step 4: 运行测试，确认通过**

IDE 运行 `MasterDataChangeEventTest`。Expected: 2 个测试全 PASS。

- [ ] **Step 5: 完成确认**

告知用户：Task 2 完成（事件载体就绪），可手动提交。

## Task 3: CategoryScopeResolver（精确匹配命中判定）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/common/CategoryScopeResolver.java`
- Test: `src/test/java/org/jeecg/module/master/common/CategoryScopeResolverTest.java`

**Interfaces:**
- Consumes: 无（纯算法）
- Produces: `CategoryScopeResolver.resolveHitSystems(Map<String,Set<String>> systemToCategoryIds, Set<String> targetCategoryIds, String excludeSystemCode)` → `Set<String>` 命中的 systemCode 集合。规则：系统类别范围与 `targetCategoryIds` **有交集**（精确匹配，非子树）且 `code ≠ excludeSystemCode`。后续 Task 10（监听器 fan-out）与 Task 12（接收过滤）都调用它。

- [ ] **Step 1: 写失败测试**

```java
package org.jeecg.module.master.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryScopeResolverTest {

    private Map<String, Set<String>> map(String code, String... cats) {
        Map<String, Set<String>> m = new HashMap<>();
        m.put(code, new HashSet<>(Arrays.asList(cats)));
        return m;
    }

    @Test
    void hit_whenCategoryInSystemScope() {
        Map<String, Set<String>> sys = new HashMap<>();
        sys.put("A", new HashSet<>(Arrays.asList("C1", "C2")));
        sys.put("B", new HashSet<>(Collections.singletonList("C9")));

        Set<String> hit = CategoryScopeResolver.resolveHitSystems(
                sys, new HashSet<>(Collections.singletonList("C1")), null);

        assertEquals(new HashSet<>(Collections.singletonList("A")), hit);
    }

    @Test
    void excludeSystemCode_removed() {
        Map<String, Set<String>> sys = new HashMap<>();
        sys.put("A", new HashSet<>(Arrays.asList("C1", "C2")));
        sys.put("B", new HashSet<>(Collections.singletonList("C1")));

        Set<String> hit = CategoryScopeResolver.resolveHitSystems(
                sys, new HashSet<>(Collections.singletonList("C1")), "A");

        assertEquals(new HashSet<>(Collections.singletonList("B")), hit);
    }

    @Test
    void emptyTarget_returnsEmpty() {
        Map<String, Set<String>> sys = map("A", "C1");
        assertTrue(CategoryScopeResolver.resolveHitSystems(sys, Collections.emptySet(), null).isEmpty());
    }

    @Test
    void nullArgs_returnEmpty() {
        assertTrue(CategoryScopeResolver.resolveHitSystems(null, new HashSet<>(Collections.singletonList("C1")), null).isEmpty());
        Map<String, Set<String>> sys = map("A", "C1");
        assertTrue(CategoryScopeResolver.resolveHitSystems(sys, null, null).isEmpty());
    }

    @Test
    void multipleTargets_multipleHits() {
        Map<String, Set<String>> sys = new HashMap<>();
        sys.put("A", new HashSet<>(Arrays.asList("C1")));
        sys.put("B", new HashSet<>(Arrays.asList("C2")));
        sys.put("C", new HashSet<>(Arrays.asList("C3")));

        Set<String> hit = CategoryScopeResolver.resolveHitSystems(
                sys, new HashSet<>(Arrays.asList("C1", "C2")), null);

        assertEquals(new HashSet<>(Arrays.asList("A", "B")), hit);
    }
}
```

- [ ] **Step 2: 运行测试，确认失败**

IDE 运行 `CategoryScopeResolverTest`。Expected: 编译失败 / 类找不到。

- [ ] **Step 3: 实现**

```java
package org.jeecg.module.master.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 精确匹配命中判定（纯静态，不依赖 Spring / Mapper）。
 *
 * 给定各系统（仅 push_enabled）的类别范围，与本次变更涉及的 categoryId 集合，
 * 返回「类别范围与变更集合有交集且未被排除」的系统 code 集合。
 * 精确匹配——不做子树展开（子树语义已 YAGNI）。
 */
public final class CategoryScopeResolver {

    private CategoryScopeResolver() {
    }

    public static Set<String> resolveHitSystems(Map<String, Set<String>> systemToCategoryIds,
                                                Set<String> targetCategoryIds,
                                                String excludeSystemCode) {
        Set<String> hit = new HashSet<>();
        if (systemToCategoryIds == null || systemToCategoryIds.isEmpty()
                || targetCategoryIds == null || targetCategoryIds.isEmpty()) {
            return hit;
        }
        for (Map.Entry<String, Set<String>> e : systemToCategoryIds.entrySet()) {
            String code = e.getKey();
            if (code != null && code.equals(excludeSystemCode)) {
                continue;
            }
            Set<String> scope = e.getValue();
            if (scope != null && intersects(scope, targetCategoryIds)) {
                hit.add(code);
            }
        }
        return hit;
    }

    private static boolean intersects(Set<String> a, Set<String> b) {
        for (String x : a) {
            if (b.contains(x)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 4: 运行测试，确认通过**

IDE 运行 `CategoryScopeResolverTest`。Expected: 5 个测试全 PASS。

- [ ] **Step 5: 完成确认**

告知用户：Task 3 完成，可手动提交。

---

## Task 4: 推送/接收报文 VO + PushPayloadBuilder

**Files:**
- Create: `src/main/java/org/jeecg/module/master/vo/IntegrationPayload.java`
- Create: `src/main/java/org/jeecg/module/master/vo/DevicePushItem.java`
- Create: `src/main/java/org/jeecg/module/master/vo/CategoryPushItem.java`
- Create: `src/main/java/org/jeecg/module/master/vo/SpacePushItem.java`
- Create: `src/main/java/org/jeecg/module/master/vo/ReceiveResult.java`
- Create: `src/main/java/org/jeecg/module/master/common/PushPayloadBuilder.java`
- Test: `src/test/java/org/jeecg/module/master/common/PushPayloadBuilderTest.java`

**Interfaces:**
- Consumes: `Device` / `DeviceCategory` / `Space` entity（Task 已存在）
- Produces:
  - `IntegrationPayload`：`source(默认"sgai-master") / systemCode / type(Type枚举 DEVICE|CATEGORY|SPACE) / op(Op枚举 UPSERT|DELETE|SNAPSHOT) / batchId / data(List<Object>)`。
  - `DevicePushItem{id,name,categoryId,spaceId,remark}`、`CategoryPushItem{id,name,fullName,pid}`、`SpacePushItem{id,name,fullName,pid}`、`ReceiveResult{batchId,accepted,rejected[List<Reject>]}`（`Reject{id,reason}`）。
  - `PushPayloadBuilder.devices(systemCode, Op, batchId, List<Device>)` / `.categories(...)` / `.spaces(...)` → `IntegrationPayload`（内部实体→PushItem 转换，DELETE 时 data 仍带完整项含 categoryId，便于下游/接收过滤）。Task 9/10/13 依赖。

- [ ] **Step 1: 创建报文 VO（纯 POJO，无逻辑）**

`IntegrationPayload.java`：
```java
package org.jeecg.module.master.vo;

import lombok.Data;
import java.util.List;

@Data
public class IntegrationPayload {

    public enum Type { DEVICE, CATEGORY, SPACE }
    public enum Op { UPSERT, DELETE, SNAPSHOT }

    /** 固定 "sgai-master"（接收时为外部系统标识，由对方填） */
    private String source = "sgai-master";
    private String systemCode;
    private Type type;
    private Op op;
    private String batchId;
    /** 条目为 DevicePushItem / CategoryPushItem / SpacePushItem（按 type） */
    private List<Object> data;

    public int dataCount() {
        return data == null ? 0 : data.size();
    }
}
```

`DevicePushItem.java`：
```java
package org.jeecg.module.master.vo;

import lombok.Data;

@Data
public class DevicePushItem {
    private String id;
    private String name;
    private String categoryId;
    private String spaceId;
    private String remark;
}
```

`CategoryPushItem.java`：
```java
package org.jeecg.module.master.vo;

import lombok.Data;

@Data
public class CategoryPushItem {
    private String id;
    private String name;
    private String fullName;
    private String pid;
}
```

`SpacePushItem.java`：
```java
package org.jeecg.module.master.vo;

import lombok.Data;

@Data
public class SpacePushItem {
    private String id;
    private String name;
    private String fullName;
    private String pid;
}
```

`ReceiveResult.java`：
```java
package org.jeecg.module.master.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ReceiveResult {
    private String batchId;
    private int accepted;
    private final List<Reject> rejected = new ArrayList<>();

    @Data
    public static class Reject {
        private final String id;
        private final String reason;
    }
}
```

> `@Data` + `final` 字段 → 生成 `@RequiredArgsConstructor`；故 `ReceiveResult` 显式加 `@NoArgsConstructor` 以支持 `new ReceiveResult()`，`rejected` 在无参构造时由字段初始化赋值。`Reject` 的 `(id, reason)` 构造由 `@Data` 生成，勿手写以免重复。

- [ ] **Step 2: 写 PushPayloadBuilder 失败测试**

```java
package org.jeecg.module.master.common;

import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.vo.IntegrationPayload;
import org.jeecg.module.master.vo.DevicePushItem;
import org.jeecg.module.master.vo.CategoryPushItem;
import org.jeecg.module.master.vo.SpacePushItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PushPayloadBuilderTest {

    @Test
    void devices_mapsFields() {
        Device d = new Device();
        d.setId("D1"); d.setName("设备A"); d.setCategoryId("C1"); d.setSpaceId("S1"); d.setRemark("r");

        IntegrationPayload p = PushPayloadBuilder.devices(
                "SYS_A", IntegrationPayload.Op.UPSERT, "BATCH1", Collections.singletonList(d));

        assertEquals("sgai-master", p.getSource());
        assertEquals("SYS_A", p.getSystemCode());
        assertEquals(IntegrationPayload.Type.DEVICE, p.getType());
        assertEquals(IntegrationPayload.Op.UPSERT, p.getOp());
        assertEquals("BATCH1", p.getBatchId());
        assertEquals(1, p.dataCount());
        DevicePushItem item = (DevicePushItem) p.getData().get(0);
        assertEquals("D1", item.getId());
        assertEquals("设备A", item.getName());
        assertEquals("C1", item.getCategoryId());
        assertEquals("S1", item.getSpaceId());
        assertEquals("r", item.getRemark());
    }

    @Test
    void categories_mapsFields() {
        DeviceCategory c = new DeviceCategory();
        c.setId("C1"); c.setName("电气"); c.setFullName("建筑-电气"); c.setPid("C0");

        IntegrationPayload p = PushPayloadBuilder.categories(
                "SYS_A", IntegrationPayload.Op.UPSERT, "B1", Collections.singletonList(c));

        assertEquals(IntegrationPayload.Type.CATEGORY, p.getType());
        CategoryPushItem item = (CategoryPushItem) p.getData().get(0);
        assertEquals("C1", item.getId());
        assertEquals("电气", item.getName());
        assertEquals("建筑-电气", item.getFullName());
        assertEquals("C0", item.getPid());
    }

    @Test
    void spaces_mapsFields() {
        Space s = new Space();
        s.setId("S1"); s.setName("一楼"); s.setFullName("园区-一楼"); s.setPid("S0");

        IntegrationPayload p = PushPayloadBuilder.spaces(
                "SYS_A", IntegrationPayload.Op.SNAPSHOT, "B1", Collections.singletonList(s));

        assertEquals(IntegrationPayload.Type.SPACE, p.getType());
        assertEquals(IntegrationPayload.Op.SNAPSHOT, p.getOp());
        SpacePushItem item = (SpacePushItem) p.getData().get(0);
        assertEquals("S1", item.getId());
        assertEquals("园区-一楼", item.getFullName());
    }

    @Test
    void delete_carriesFullItemWithCategoryId() {
        Device d = new Device();
        d.setId("D1"); d.setCategoryId("C1");
        IntegrationPayload p = PushPayloadBuilder.devices(
                "SYS_A", IntegrationPayload.Op.DELETE, "B1", Collections.singletonList(d));
        DevicePushItem item = (DevicePushItem) p.getData().get(0);
        assertEquals("C1", item.getCategoryId());
    }
}
```

- [ ] **Step 3: 运行测试，确认失败**

IDE 运行 `PushPayloadBuilderTest`。Expected: 编译失败 / 类找不到。

- [ ] **Step 4: 实现 PushPayloadBuilder**

```java
package org.jeecg.module.master.common;

import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.vo.CategoryPushItem;
import org.jeecg.module.master.vo.DevicePushItem;
import org.jeecg.module.master.vo.IntegrationPayload;
import org.jeecg.module.master.vo.SpacePushItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 组装统一推送/接收报文（纯静态，不依赖 Spring / Mapper）。
 * 实体 → PushItem 转换；DELETE 时仍带完整项（含 categoryId），便于下游过滤/分发判定。
 */
public final class PushPayloadBuilder {

    private PushPayloadBuilder() {
    }

    public static IntegrationPayload devices(String systemCode, IntegrationPayload.Op op,
                                             String batchId, List<Device> devices) {
        IntegrationPayload p = base(systemCode, op, batchId, IntegrationPayload.Type.DEVICE);
        List<Object> data = new ArrayList<>();
        if (devices != null) {
            for (Device d : devices) {
                DevicePushItem item = new DevicePushItem();
                item.setId(d.getId());
                item.setName(d.getName());
                item.setCategoryId(d.getCategoryId());
                item.setSpaceId(d.getSpaceId());
                item.setRemark(d.getRemark());
                data.add(item);
            }
        }
        p.setData(data);
        return p;
    }

    public static IntegrationPayload categories(String systemCode, IntegrationPayload.Op op,
                                                String batchId, List<DeviceCategory> categories) {
        IntegrationPayload p = base(systemCode, op, batchId, IntegrationPayload.Type.CATEGORY);
        List<Object> data = new ArrayList<>();
        if (categories != null) {
            for (DeviceCategory c : categories) {
                CategoryPushItem item = new CategoryPushItem();
                item.setId(c.getId());
                item.setName(c.getName());
                item.setFullName(c.getFullName());
                item.setPid(c.getPid());
                data.add(item);
            }
        }
        p.setData(data);
        return p;
    }

    public static IntegrationPayload spaces(String systemCode, IntegrationPayload.Op op,
                                           String batchId, List<Space> spaces) {
        IntegrationPayload p = base(systemCode, op, batchId, IntegrationPayload.Type.SPACE);
        List<Object> data = new ArrayList<>();
        if (spaces != null) {
            for (Space s : spaces) {
                SpacePushItem item = new SpacePushItem();
                item.setId(s.getId());
                item.setName(s.getName());
                item.setFullName(s.getFullName());
                item.setPid(s.getPid());
                data.add(item);
            }
        }
        p.setData(data);
        return p;
    }

    private static IntegrationPayload base(String systemCode, IntegrationPayload.Op op,
                                          String batchId, IntegrationPayload.Type type) {
        IntegrationPayload p = new IntegrationPayload();
        p.setSystemCode(systemCode);
        p.setOp(op);
        p.setBatchId(batchId);
        p.setType(type);
        p.setData(Collections.emptyList());
        return p;
    }
}
```

- [ ] **Step 5: 运行测试，确认通过**

IDE 运行 `PushPayloadBuilderTest`。Expected: 4 个测试全 PASS。

- [ ] **Step 6: 完成确认**

告知用户：Task 4 完成（报文 VO + 组装器就绪），可手动提交。

## Task 5: 数据层（entity + mapper + 表单 VO）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/entity/IntegrationSystem.java`
- Create: `src/main/java/org/jeecg/module/master/entity/IntegrationSystemCategory.java`
- Create: `src/main/java/org/jeecg/module/master/entity/IntegrationLog.java`
- Create: `src/main/java/org/jeecg/module/master/mapper/IntegrationSystemMapper.java`
- Create: `src/main/java/org/jeecg/module/master/mapper/IntegrationSystemCategoryMapper.java`
- Create: `src/main/java/org/jeecg/module/master/mapper/IntegrationLogMapper.java`
- Create: `src/main/java/org/jeecg/module/master/vo/IntegrationSystemForm.java`

**Interfaces:**
- Produces: 三个 entity（字段同 DDL，驼峰↔下划线由 MyBatis-Plus 自动映射；`push_enabled/receive_enabled` 用 `Integer` 0/1）；三个 mapper（`extends BaseMapper<T>`，无自定义方法、无 xml，被 `@MapperScan("org.jeecg.**.mapper")` 自动覆盖）；`IntegrationSystemForm`（新增/编辑/详情表单，含 `categoryIds`）。后续 Task 6/7/9/13 依赖。

- [ ] **Step 1: 创建三个 entity**

`IntegrationSystem.java`：
```java
package org.jeecg.module.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("integration_system")
@ApiModel("对接系统")
public class IntegrationSystem {

    @TableId(type = IdType.NONE)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("系统名称")
    private String name;

    @ApiModelProperty("系统编码(唯一)")
    private String code;

    @ApiModelProperty("是否启用推送 0否1是")
    private Integer pushEnabled;

    @ApiModelProperty("推送目标URL")
    private String pushUrl;

    @ApiModelProperty("我们→下游鉴权令牌")
    private String pushToken;

    @ApiModelProperty("是否启用接收 0否1是")
    private Integer receiveEnabled;

    @ApiModelProperty("下游→我们令牌(唯一)")
    private String receiveToken;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
```

`IntegrationSystemCategory.java`：
```java
package org.jeecg.module.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("integration_system_category")
@ApiModel("对接系统-类别范围")
public class IntegrationSystemCategory {

    @TableId(type = IdType.NONE)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("对接系统id")
    private String systemId;

    @ApiModelProperty("类别id")
    private String categoryId;
}
```

`IntegrationLog.java`：
```java
package org.jeecg.module.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("integration_log")
@ApiModel("对接日志")
public class IntegrationLog {

    @TableId(type = IdType.NONE)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("方向 PUSH/RECEIVE")
    private String direction;

    @ApiModelProperty("对接系统id")
    private String systemId;

    @ApiModelProperty("对接系统编码(冗余)")
    private String systemCode;

    @ApiModelProperty("类型 CATEGORY/SPACE/DEVICE")
    private String type;

    @ApiModelProperty("操作 UPSERT/DELETE/SNAPSHOT")
    private String op;

    @ApiModelProperty("批次id")
    private String batchId;

    @ApiModelProperty("数据条数")
    private Integer payloadCount;

    @ApiModelProperty("状态 SUCCESS/PARTIAL/FAIL")
    private String status;

    @ApiModelProperty("原始报文JSON(仅审计)")
    private String payload;

    @ApiModelProperty("失败原因/接收逐条拒绝明细")
    private String error;

    @ApiModelProperty("耗时毫秒")
    private Integer costMs;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
```

- [ ] **Step 2: 创建三个 mapper**

`IntegrationSystemMapper.java`：
```java
package org.jeecg.module.master.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.module.master.entity.IntegrationSystem;

@Mapper
public interface IntegrationSystemMapper extends BaseMapper<IntegrationSystem> {
}
```

`IntegrationSystemCategoryMapper.java`：
```java
package org.jeecg.module.master.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.module.master.entity.IntegrationSystemCategory;

@Mapper
public interface IntegrationSystemCategoryMapper extends BaseMapper<IntegrationSystemCategory> {
}
```

`IntegrationLogMapper.java`：
```java
package org.jeecg.module.master.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.module.master.entity.IntegrationLog;

@Mapper
public interface IntegrationLogMapper extends BaseMapper<IntegrationLog> {
}
```

- [ ] **Step 3: 创建表单 VO**

`IntegrationSystemForm.java`：
```java
package org.jeecg.module.master.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("对接系统表单(新增/编辑/详情)")
public class IntegrationSystemForm {

    @ApiModelProperty("id（编辑/详情时有）")
    private String id;

    @ApiModelProperty("系统名称(必填)")
    private String name;

    @ApiModelProperty("系统编码(必填,唯一)")
    private String code;

    @ApiModelProperty("是否启用推送 0否1是")
    private Integer pushEnabled;

    @ApiModelProperty("推送目标URL")
    private String pushUrl;

    @ApiModelProperty("我们→下游鉴权令牌")
    private String pushToken;

    @ApiModelProperty("是否启用接收 0否1是")
    private Integer receiveEnabled;

    @ApiModelProperty("下游→我们令牌(唯一)")
    private String receiveToken;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("类别范围 categoryIds(必填,推送/接收共用)")
    private List<String> categoryIds;
}
```

- [ ] **Step 4: 编译验证**

IDE 编译模块（Build → Build Module）。Expected: 无错误，新 entity/mapper 被识别。

- [ ] **Step 5: 完成确认**

告知用户：Task 5 完成（数据层就绪），可手动提交。

---

## Task 6: IntegrationLogService（写入 + 查询）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/service/IIntegrationLogService.java`
- Create: `src/main/java/org/jeecg/module/master/service/impl/IntegrationLogServiceImpl.java`
- Test: `src/test/java/org/jeecg/module/master/service/IntegrationLogServiceImplTest.java`

**Interfaces:**
- Consumes: `IntegrationLog` entity / `IntegrationLogMapper`（Task 5）
- Produces: `IIntegrationLogService extends IService<IntegrationLog>`，方法 `writeLog(IntegrationLog log)`（`@Transactional(REQUIRES_NEW)`，内部填 id + createTime 后 insert，**独立小事务**，与主数据写入隔离）；查询走 IService 内置 `page`/`getById`。Task 9/13 调 `writeLog`，Task 14 调查询。

- [ ] **Step 1: 写失败测试**

```java
package org.jeecg.module.master.service;

import org.jeecg.module.master.entity.IntegrationLog;
import org.jeecg.module.master.mapper.IntegrationLogMapper;
import org.jeecg.module.master.service.impl.IntegrationLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationLogServiceImplTest {

    @Mock
    IntegrationLogMapper baseMapper;

    @InjectMocks
    IntegrationLogServiceImpl service;

    @Test
    void writeLog_fillsIdAndCreateTime_thenInsert() {
        IntegrationLog log = new IntegrationLog();
        log.setDirection("PUSH");
        log.setStatus("SUCCESS");
        when(baseMapper.insert(any(IntegrationLog.class))).thenReturn(1);

        service.writeLog(log);

        assertNotNull(log.getId());
        assertNotNull(log.getCreateTime());
        verify(baseMapper).insert(log);
    }

    @Test
    void writeLog_nullError_keepsNull() {
        IntegrationLog log = new IntegrationLog();
        log.setDirection("RECEIVE");
        log.setStatus("PARTIAL");
        when(baseMapper.insert(any(IntegrationLog.class))).thenReturn(1);

        service.writeLog(log);

        ArgumentCaptor<IntegrationLog> cap = ArgumentCaptor.forClass(IntegrationLog.class);
        verify(baseMapper).insert(cap.capture());
        assertNull(cap.getValue().getError());
    }
}
```

- [ ] **Step 2: 运行测试，确认失败**

IDE 运行 `IntegrationLogServiceImplTest`。Expected: 编译失败 / 类找不到。

- [ ] **Step 3: 实现接口**

`IIntegrationLogService.java`：
```java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.master.entity.IntegrationLog;

public interface IIntegrationLogService extends IService<IntegrationLog> {

    /** 写一条对接日志（独立小事务 REQUIRES_NEW，与主数据写入隔离）。 */
    void writeLog(IntegrationLog log);
}
```

- [ ] **Step 4: 实现类**

```java
package org.jeecg.module.master.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.module.master.common.TreeFullNameHelper;
import org.jeecg.module.master.entity.IntegrationLog;
import org.jeecg.module.master.mapper.IntegrationLogMapper;
import org.jeecg.module.master.service.IIntegrationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class IntegrationLogServiceImpl
        extends ServiceImpl<IntegrationLogMapper, IntegrationLog>
        implements IIntegrationLogService {

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void writeLog(IntegrationLog log) {
        if (log.getId() == null) {
            log.setId(TreeFullNameHelper.generateUuid());
        }
        if (log.getCreateTime() == null) {
            log.setCreateTime(new Date());
        }
        baseMapper.insert(log);
    }
}
```

- [ ] **Step 5: 运行测试，确认通过**

IDE 运行 `IntegrationLogServiceImplTest`。Expected: 2 个测试全 PASS。

- [ ] **Step 6: 完成确认**

告知用户：Task 6 完成（日志写入就绪），可手动提交。

## Task 7: IntegrationSystemService（CRUD + 类别范围覆盖 + 唯一性 + 删除停用）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/service/IIntegrationSystemService.java`
- Create: `src/main/java/org/jeecg/module/master/service/impl/IntegrationSystemServiceImpl.java`
- Test: `src/test/java/org/jeecg/module/master/service/IntegrationSystemServiceImplTest.java`

**Interfaces:**
- Consumes: `IntegrationSystem` / `IntegrationSystemCategory` entity + mapper（Task 5）、`IntegrationSystemForm` VO（Task 5）
- Produces: `IIntegrationSystemService extends IService<IntegrationSystem>`：
  - `saveFromForm(IntegrationSystemForm)`：校验 code/receive_token 唯一 → 生成 id → save 主表 → 覆盖类别子表。
  - `updateFromForm(IntegrationSystemForm)`：校验唯一（排除自身）→ updateById 主表 → 覆盖类别子表。
  - `getFormById(String id)` → `IntegrationSystemForm`（主表 + categoryIds）。
  - `listPage(Page, name, code)` → `IPage<IntegrationSystem>`。
  - `removeByIdWithCheck(String id)`：`push_enabled=1 || receive_enabled=1` → 拒绝「请先停用该对接系统」；否则级联删子表 + 物理删主表。
  - Task 8（controller）/ Task 12（接收按 token 反查，用 `findByReceiveToken`，见下）依赖。

> **补充方法（接收链路需要，本 task 一并实现）**：`IntegrationSystem findByReceiveToken(String token)`——按 `receive_token` 唯一反查来源系统（`selectOne(eq(receiveToken).eq(receiveEnabled,1))`），Task 13 接收鉴权调用。

- [ ] **Step 1: 写失败测试**

```java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.entity.IntegrationSystemCategory;
import org.jeecg.module.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.module.master.mapper.IntegrationSystemMapper;
import org.jeecg.module.master.service.impl.IntegrationSystemServiceImpl;
import org.jeecg.module.master.vo.IntegrationSystemForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationSystemServiceImplTest {

    @Mock
    IntegrationSystemMapper baseMapper;
    @Mock
    IntegrationSystemCategoryMapper integrationSystemCategoryMapper;

    @InjectMocks
    IntegrationSystemServiceImpl service;

    private IntegrationSystemForm form(String name, String code, List<String> catIds) {
        IntegrationSystemForm f = new IntegrationSystemForm();
        f.setName(name);
        f.setCode(code);
        f.setPushEnabled(1);
        f.setReceiveEnabled(0);
        f.setCategoryIds(catIds);
        return f;
    }

    @Test
    void save_duplicateCode_throws() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThrows(JeecgBootException.class, () -> service.saveFromForm(form("A", "CODE1", Arrays.asList("C1"))));
    }

    @Test
    void save_ok_insertsSystemAndCategoryRows() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(IntegrationSystem.class))).thenReturn(1);
        when(integrationSystemCategoryMapper.insert(any(IntegrationSystemCategory.class))).thenReturn(1);

        service.saveFromForm(form("A", "CODE1", Arrays.asList("C1", "C2")));

        ArgumentCaptor<IntegrationSystem> sysCap = ArgumentCaptor.forClass(IntegrationSystem.class);
        verify(baseMapper).insert(sysCap.capture());
        assertNotNull(sysCap.getValue().getId());
        assertEquals("CODE1", sysCap.getValue().getCode());
        // 类别子表：先删（无）+ 插 2 行
        verify(integrationSystemCategoryMapper).delete(any(LambdaQueryWrapper.class));
        verify(integrationSystemCategoryMapper, times(2)).insert(any(IntegrationSystemCategory.class));
    }

    @Test
    void save_emptyCategoryIds_throws() {
        IntegrationSystemForm f = form("A", "CODE1", Collections.emptyList());
        // validateForm 在唯一性查询之前即校验 categoryIds 非空，此处不触发 selectCount
        assertThrows(JeecgBootException.class, () -> service.saveFromForm(f));
    }

    @Test
    void update_overwritesCategories_deleteThenInsert() {
        IntegrationSystemForm f = form("A", "CODE1", Arrays.asList("C3"));
        f.setId("S1");
        IntegrationSystem old = new IntegrationSystem();
        old.setId("S1"); old.setCode("CODE1");
        when(baseMapper.selectById("S1")).thenReturn(old);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.updateById(any(IntegrationSystem.class))).thenReturn(1);
        when(integrationSystemCategoryMapper.insert(any(IntegrationSystemCategory.class))).thenReturn(1);

        service.updateFromForm(f);

        verify(integrationSystemCategoryMapper).delete(any(LambdaQueryWrapper.class));
        verify(integrationSystemCategoryMapper, times(1)).insert(any(IntegrationSystemCategory.class));
        verify(baseMapper).updateById(any(IntegrationSystem.class));
    }

    @Test
    void remove_pushEnabled_throws() {
        IntegrationSystem sys = new IntegrationSystem();
        sys.setId("S1"); sys.setPushEnabled(1); sys.setReceiveEnabled(0);
        when(baseMapper.selectById("S1")).thenReturn(sys);
        assertThrows(JeecgBootException.class, () -> service.removeByIdWithCheck("S1"));
    }

    @Test
    void remove_disabled_deletesCategoriesAndSystem() {
        IntegrationSystem sys = new IntegrationSystem();
        sys.setId("S1"); sys.setPushEnabled(0); sys.setReceiveEnabled(0);
        when(baseMapper.selectById("S1")).thenReturn(sys);
        when(integrationSystemCategoryMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(baseMapper.deleteById("S1")).thenReturn(1);

        service.removeByIdWithCheck("S1");

        verify(integrationSystemCategoryMapper).delete(any(LambdaQueryWrapper.class));
        verify(baseMapper).deleteById("S1");
    }

    @Test
    void getFormById_returnsFormWithCategoryIds() {
        IntegrationSystem sys = new IntegrationSystem();
        sys.setId("S1"); sys.setName("A"); sys.setCode("CODE1");
        when(baseMapper.selectById("S1")).thenReturn(sys);
        IntegrationSystemCategory row = new IntegrationSystemCategory();
        row.setSystemId("S1"); row.setCategoryId("C1");
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(row));

        IntegrationSystemForm form = service.getFormById("S1");

        assertEquals("S1", form.getId());
        assertEquals(Arrays.asList("C1"), form.getCategoryIds());
    }

    @Test
    void findByReceiveToken_enabled_matches() {
        IntegrationSystem sys = new IntegrationSystem();
        sys.setCode("CODE1"); sys.setReceiveEnabled(1);
        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sys);

        IntegrationSystem found = service.findByReceiveToken("TOK");

        assertNotNull(found);
        assertEquals("CODE1", found.getCode());
    }
}
```

- [ ] **Step 2: 运行测试，确认失败**

IDE 运行 `IntegrationSystemServiceImplTest`。Expected: 编译失败 / 类找不到。

- [ ] **Step 3: 实现接口**

`IIntegrationSystemService.java`：
```java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.vo.IntegrationSystemForm;

public interface IIntegrationSystemService extends IService<IntegrationSystem> {

    void saveFromForm(IntegrationSystemForm form);

    void updateFromForm(IntegrationSystemForm form);

    IntegrationSystemForm getFormById(String id);

    IPage<IntegrationSystem> listPage(Page<IntegrationSystem> page, String name, String code);

    void removeByIdWithCheck(String id);

    /** 按 receive_token 反查「启用接收」的系统（接收鉴权用）。 */
    IntegrationSystem findByReceiveToken(String token);
}
```

- [ ] **Step 4: 实现类**

```java
package org.jeecg.module.master.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.common.TreeFullNameHelper;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.entity.IntegrationSystemCategory;
import org.jeecg.module.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.module.master.mapper.IntegrationSystemMapper;
import org.jeecg.module.master.service.IIntegrationSystemService;
import org.jeecg.module.master.vo.IntegrationSystemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IntegrationSystemServiceImpl
        extends ServiceImpl<IntegrationSystemMapper, IntegrationSystem>
        implements IIntegrationSystemService {

    @Autowired
    private IntegrationSystemCategoryMapper integrationSystemCategoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFromForm(IntegrationSystemForm form) {
        validateForm(form);
        assertCodeUnique(form.getCode(), null);
        assertReceiveTokenUnique(form.getReceiveToken(), null);

        IntegrationSystem entity = new IntegrationSystem();
        BeanUtil.copyProperties(form, entity);
        entity.setId(TreeFullNameHelper.generateUuid());
        baseMapper.insert(entity);
        overwriteCategories(entity.getId(), form.getCategoryIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFromForm(IntegrationSystemForm form) {
        if (StrUtil.isBlank(form.getId())) {
            throw new JeecgBootException("id不能为空");
        }
        validateForm(form);
        assertCodeUnique(form.getCode(), form.getId());
        assertReceiveTokenUnique(form.getReceiveToken(), form.getId());

        IntegrationSystem entity = new IntegrationSystem();
        BeanUtil.copyProperties(form, entity);
        baseMapper.updateById(entity);
        overwriteCategories(entity.getId(), form.getCategoryIds());
    }

    @Override
    public IntegrationSystemForm getFormById(String id) {
        IntegrationSystem sys = baseMapper.selectById(id);
        if (sys == null) {
            return null;
        }
        IntegrationSystemForm form = new IntegrationSystemForm();
        BeanUtil.copyProperties(sys, form);
        List<IntegrationSystemCategory> rows = integrationSystemCategoryMapper.selectList(
                new LambdaQueryWrapper<IntegrationSystemCategory>()
                        .eq(IntegrationSystemCategory::getSystemId, id));
        form.setCategoryIds(rows.stream()
                .map(IntegrationSystemCategory::getCategoryId)
                .collect(Collectors.toList()));
        return form;
    }

    @Override
    public IPage<IntegrationSystem> listPage(Page<IntegrationSystem> page, String name, String code) {
        LambdaQueryWrapper<IntegrationSystem> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            w.like(IntegrationSystem::getName, name);
        }
        if (StrUtil.isNotBlank(code)) {
            w.like(IntegrationSystem::getCode, code);
        }
        w.orderByDesc(IntegrationSystem::getCreateTime);
        return baseMapper.selectPage(page, w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIdWithCheck(String id) {
        IntegrationSystem sys = baseMapper.selectById(id);
        if (sys == null) {
            return;
        }
        if (Integer.valueOf(1).equals(sys.getPushEnabled())
                || Integer.valueOf(1).equals(sys.getReceiveEnabled())) {
            throw new JeecgBootException("请先停用该对接系统");
        }
        integrationSystemCategoryMapper.delete(new LambdaQueryWrapper<IntegrationSystemCategory>()
                .eq(IntegrationSystemCategory::getSystemId, id));
        baseMapper.deleteById(id);
    }

    @Override
    public IntegrationSystem findByReceiveToken(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return baseMapper.selectOne(new LambdaQueryWrapper<IntegrationSystem>()
                .eq(IntegrationSystem::getReceiveToken, token)
                .eq(IntegrationSystem::getReceiveEnabled, 1));
    }

    // ---------- 私有工具 ----------

    private void validateForm(IntegrationSystemForm form) {
        if (form == null || StrUtil.isBlank(form.getName())) {
            throw new JeecgBootException("系统名称不能为空");
        }
        if (StrUtil.isBlank(form.getCode())) {
            throw new JeecgBootException("系统编码不能为空");
        }
        if (form.getCategoryIds() == null || form.getCategoryIds().isEmpty()) {
            throw new JeecgBootException("请选择类别范围");
        }
    }

    private void assertCodeUnique(String code, String excludeId) {
        LambdaQueryWrapper<IntegrationSystem> w = new LambdaQueryWrapper<IntegrationSystem>()
                .eq(IntegrationSystem::getCode, code);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(IntegrationSystem::getId, excludeId);
        }
        if (baseMapper.selectCount(w) > 0) {
            throw new JeecgBootException("系统编码已存在");
        }
    }

    private void assertReceiveTokenUnique(String token, String excludeId) {
        if (StrUtil.isBlank(token)) {
            return; // 未启用接收，无 token
        }
        LambdaQueryWrapper<IntegrationSystem> w = new LambdaQueryWrapper<IntegrationSystem>()
                .eq(IntegrationSystem::getReceiveToken, token);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(IntegrationSystem::getId, excludeId);
        }
        if (baseMapper.selectCount(w) > 0) {
            throw new JeecgBootException("接收令牌已存在");
        }
    }

    /** 类别范围整体覆盖：先按 system_id 删，再批量 insert。 */
    private void overwriteCategories(String systemId, List<String> categoryIds) {
        integrationSystemCategoryMapper.delete(new LambdaQueryWrapper<IntegrationSystemCategory>()
                .eq(IntegrationSystemCategory::getSystemId, systemId));
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        for (String cid : categoryIds) {
            IntegrationSystemCategory row = new IntegrationSystemCategory();
            row.setId(TreeFullNameHelper.generateUuid());
            row.setSystemId(systemId);
            row.setCategoryId(cid);
            integrationSystemCategoryMapper.insert(row);
        }
    }
}
```

- [ ] **Step 5: 运行测试，确认通过**

IDE 运行 `IntegrationSystemServiceImplTest`。Expected: 8 个测试全 PASS。

- [ ] **Step 6: 完成确认**

告知用户：Task 7 完成，可手动提交。

## Task 8: IntegrationSystemController（CRUD 端点）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/controller/IntegrationSystemController.java`

**Interfaces:**
- Consumes: `IIntegrationSystemService`（Task 7）。手动全量推送端点 `POST /{id}/push` 依赖 PushService，**留到 Task 12-F 补**（本 task 在 controller 内留一行注释占位，避免编译依赖未实现的 PushService）。
- Produces: `/master/integrationSystem` 下 `GET /list`、`GET /{id}`、`POST /`、`PUT /`、`DELETE /{id}`。

- [ ] **Step 1: 实现 controller**

```java
package org.jeecg.module.master.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.service.IIntegrationSystemService;
import org.jeecg.module.master.vo.IntegrationSystemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "对接系统")
@RestController
@RequestMapping("/master/integrationSystem")
public class IntegrationSystemController {

    @Autowired
    private IIntegrationSystemService integrationSystemService;

    @ApiOperation("分页列表")
    @GetMapping("/list")
    public Result<IPage<IntegrationSystem>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        Page<IntegrationSystem> page = new Page<>(pageNo, pageSize);
        return Result.OK(integrationSystemService.listPage(page, name, code));
    }

    @ApiOperation("详情（含类别范围 categoryIds）")
    @GetMapping("/{id}")
    public Result<IntegrationSystemForm> queryById(@PathVariable("id") String id) {
        return Result.OK(integrationSystemService.getFormById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<?> add(@RequestBody IntegrationSystemForm form) {
        integrationSystemService.saveFromForm(form);
        return Result.OK("新增成功");
    }

    @ApiOperation("编辑")
    @PutMapping
    public Result<?> edit(@RequestBody IntegrationSystemForm form) {
        integrationSystemService.updateFromForm(form);
        return Result.OK("编辑成功");
    }

    @ApiOperation("删除（须先停用）")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        integrationSystemService.removeByIdWithCheck(id);
        return Result.OK("删除成功");
    }

    // POST /{id}/push 手动全量推送 → Task 12 补（依赖 IIntegrationPushService）
}
```

- [ ] **Step 2: 编译验证**

IDE 编译模块。Expected: 无错误。

- [ ] **Step 3: 完成确认**

告知用户：Task 8 完成（CRUD 端点就绪，手动推送端点 Task 12 补），可手动提交。

---

## Task 9: DeviceService 接收专用 upsert / delete（不发普通推送事件，自带 hub 事件）

**Files:**
- Modify: `src/main/java/org/jeecg/module/master/service/IDeviceService.java`（新增两方法签名）
- Modify: `src/main/java/org/jeecg/module/master/service/impl/DeviceServiceImpl.java`（实现两方法 + 注入 `ApplicationEventPublisher`）
- Test: `src/test/java/org/jeecg/module/master/service/DeviceServiceUpsertFromIntegrationTest.java`

**Interfaces:**
- Consumes: `Device` entity、`DeviceCategoryMapper`/`SpaceMapper`（已注入）、`MasterDataChangeEvent`（Task 2）
- Produces:
  - `upsertFromIntegration(Device incoming, String excludeSystemCode)`：校验 categoryId/spaceId 存在 + 名称冲突（撞别的 id）→ id 已存在 `updateById` / 不存在按传入 id `insert` → 发布 `ofDevices(exists?UPDATE:CREATE, [incoming], excludeSystemCode)`。`@Transactional`。**不**走 `create/updateNode`（避免重复发普通事件 + 重生成 id）。
  - `deleteFromIntegration(Device incoming, String excludeSystemCode)`：`deleteById(incoming.id)`（本地不存在也发，幂等）→ 发布 `ofDevices(DELETE, [incoming], excludeSystemCode)`（incoming.categoryId 供监听器过滤）。
  - Task 13（接收）调用，hub 事件由监听器 Task 10 在 `AFTER_COMMIT` 分发。

- [ ] **Step 1: 写失败测试**

```java
package org.jeecg.module.master.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.common.MasterDataChangeEvent;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.DeviceCategoryMapper;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.mapper.SpaceMapper;
import org.jeecg.module.master.service.impl.DeviceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceUpsertFromIntegrationTest {

    @Mock
    DeviceMapper baseMapper;
    @Mock
    DeviceCategoryMapper deviceCategoryMapper;
    @Mock
    SpaceMapper spaceMapper;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    DeviceServiceImpl service;

    private Device incoming() {
        Device d = new Device();
        d.setId("D1");
        d.setName("设备A");
        d.setCategoryId("C1");
        d.setSpaceId("S1");
        d.setRemark("r");
        return d;
    }

    private void stubRefsExist() {
        when(deviceCategoryMapper.selectById("C1")).thenReturn(new DeviceCategory());
        when(spaceMapper.selectById("S1")).thenReturn(new Space());
    }

    @Test
    void upsert_newId_insertsWithGivenIdAndPublishesCreate() {
        Device d = incoming();
        stubRefsExist();
        when(baseMapper.selectCount(any())).thenReturn(0L); // countName 无冲突
        when(baseMapper.selectById("D1")).thenReturn(null); // 新增
        when(baseMapper.insert(any(Device.class))).thenReturn(1);

        service.upsertFromIntegration(d, "SYS_A");

        verify(baseMapper).insert(d); // 用传入 id，不重新生成
        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.Op.CREATE, cap.getValue().getOp());
        assertEquals("SYS_A", cap.getValue().getExcludeSystemCode());
    }

    @Test
    void upsert_existingId_updatesAndPublishesUpdate() {
        Device d = incoming();
        stubRefsExist();
        when(baseMapper.selectCount(any())).thenReturn(0L);
        when(baseMapper.selectById("D1")).thenReturn(new Device()); // 已存在

        service.upsertFromIntegration(d, null);

        verify(baseMapper).updateById(d);
        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.Op.UPDATE, cap.getValue().getOp());
        verify(baseMapper, never()).insert(any(Device.class));
    }

    @Test
    void upsert_categoryNotExist_throws() {
        Device d = incoming();
        when(deviceCategoryMapper.selectById("C1")).thenReturn(null);
        assertThrows(JeecgBootException.class, () -> service.upsertFromIntegration(d, null));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void upsert_nameConflict_throws() {
        Device d = incoming();
        stubRefsExist();
        when(baseMapper.selectCount(any())).thenReturn(1L); // 名称撞别的 id
        assertThrows(JeecgBootException.class, () -> service.upsertFromIntegration(d, null));
        verify(baseMapper, never()).insert(any(Device.class));
    }

    @Test
    void delete_deletesAndPublishesDeleteWithCategoryId() {
        Device d = incoming();
        when(baseMapper.deleteById("D1")).thenReturn(1);

        service.deleteFromIntegration(d, "SYS_A");

        verify(baseMapper).deleteById("D1");
        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.Op.DELETE, cap.getValue().getOp());
        assertEquals("C1", cap.getValue().getDevices().get(0).getCategoryId());
    }
}
```

- [ ] **Step 2: 运行测试，确认失败**

IDE 运行 `DeviceServiceUpsertFromIntegrationTest`。Expected: 编译失败（方法不存在 / publisher 字段不存在）。

- [ ] **Step 3: 在 IDeviceService 加方法签名**

在 `IDeviceService.java` 接口末尾追加：

```java
    /** 接收：按传入 id upsert（不复用 create/update，避免重复发普通事件+重生成 id）。校验引用存在 + 名称冲突，成功后发 hub 事件。 */
    void upsertFromIntegration(Device incoming, String excludeSystemCode);

    /** 接收：按 id 物理删（本地不存在也发，幂等），发 hub DELETE 事件（incoming.categoryId 供过滤）。 */
    void deleteFromIntegration(Device incoming, String excludeSystemCode);
```

并在该文件 import 区补：`import org.jeecg.module.master.entity.Device;`（若已存在则跳过）。

- [ ] **Step 4: 在 DeviceServiceImpl 加字段 + 两方法**

在 `DeviceServiceImpl.java` import 区追加：
```java
import org.jeecg.module.master.common.MasterDataChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Collections;
```

在类字段区（`private SpaceMapper spaceMapper;` 之后）追加：
```java
    @Autowired
    private ApplicationEventPublisher eventPublisher;
```

在类内（`removeBatch` 方法之后、`// ---------- 私有工具 ----------` 之前）追加两方法：

```java
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertFromIntegration(Device incoming, String excludeSystemCode) {
        if (StrUtil.isBlank(incoming.getId())) {
            throw new JeecgBootException("设备id不能为空");
        }
        if (StrUtil.isBlank(incoming.getCategoryId())) {
            throw new JeecgBootException("类别不能为空");
        }
        if (StrUtil.isBlank(incoming.getSpaceId())) {
            throw new JeecgBootException("空间不能为空");
        }
        if (deviceCategoryMapper.selectById(incoming.getCategoryId()) == null) {
            throw new JeecgBootException("类别不存在");
        }
        if (spaceMapper.selectById(incoming.getSpaceId()) == null) {
            throw new JeecgBootException("空间不存在");
        }
        if (countName(incoming.getName(), incoming.getId()) > 0) {
            throw new JeecgBootException("设备名称冲突");
        }
        boolean exists = baseMapper.selectById(incoming.getId()) != null;
        if (exists) {
            baseMapper.updateById(incoming);
        } else {
            baseMapper.insert(incoming); // 用传入 id，不重新生成
        }
        MasterDataChangeEvent.Op op = exists
                ? MasterDataChangeEvent.Op.UPDATE
                : MasterDataChangeEvent.Op.CREATE;
        eventPublisher.publishEvent(
                MasterDataChangeEvent.ofDevices(op, Collections.singletonList(incoming), excludeSystemCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFromIntegration(Device incoming, String excludeSystemCode) {
        baseMapper.deleteById(incoming.getId());
        eventPublisher.publishEvent(
                MasterDataChangeEvent.ofDevices(MasterDataChangeEvent.Op.DELETE,
                        Collections.singletonList(incoming), excludeSystemCode));
    }
```

- [ ] **Step 5: 运行测试，确认通过**

IDE 运行 `DeviceServiceUpsertFromIntegrationTest`。Expected: 5 个测试全 PASS。

- [ ] **Step 6: 完成确认**

告知用户：Task 9 完成（接收专用 upsert/delete 就绪），可手动提交。

## Task 10: IntegrationPushService（HTTP 端口 + 实时发送 + 手动全量）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/vo/PushSnapshotResult.java`
- Create: `src/main/java/org/jeecg/module/master/common/IntegrationHttpExecutor.java`
- Create: `src/main/java/org/jeecg/module/master/common/HutoolIntegrationHttpExecutor.java`
- Create: `src/main/java/org/jeecg/module/master/service/IIntegrationPushService.java`
- Create: `src/main/java/org/jeecg/module/master/service/impl/IntegrationPushServiceImpl.java`
- Test: `src/test/java/org/jeecg/module/master/service/IntegrationPushServiceImplTest.java`

**Interfaces:**
- Consumes: `IntegrationPayload`/`PushPayloadBuilder`（Task 4）、`IntegrationSystem`/`IntegrationSystemCategory` mapper（Task 5）、`Device`/`DeviceCategory`/`Space` mapper（已有）、`IIntegrationLogService.writeLog`（Task 6）
- Produces:
  - `IntegrationHttpExecutor.post(url, token, body)` → `int` HTTP 状态码（异常/超时返回 -1）。端口接口，便于 mock（替代直接 mock hutool 静态）。
  - `IIntegrationPushService.pushOne(IntegrationSystem, IntegrationPayload)`：实时增量单系统推送，写 PUSH 日志。
  - `IIntegrationPushService.pushSnapshotForSystem(systemId)` → `List<PushSnapshotResult>`：手动全量，3 次快照（空间/类别/设备），各写日志，返回 3 条结果。
  - Task 11（监听器）调 `pushOne`；Task 12（controller /push）调 `pushSnapshotForSystem`。

- [ ] **Step 1: 创建返回结果 VO + HTTP 端口**

`PushSnapshotResult.java`：
```java
package org.jeecg.module.master.vo;

import lombok.Data;

@Data
public class PushSnapshotResult {
    private String type;        // SPACE/CATEGORY/DEVICE
    private int payloadCount;
    private String status;      // SUCCESS/FAIL
    private String error;
}
```

`IntegrationHttpExecutor.java`：
```java
package org.jeecg.module.master.common;

/** 推送 HTTP 端口（抽象以便测试 mock，生产用 hutool）。返回 HTTP 状态码；异常/超时返回 -1。 */
public interface IntegrationHttpExecutor {
    int post(String url, String token, String body);
}
```

`HutoolIntegrationHttpExecutor.java`：
```java
package org.jeecg.module.master.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.stereotype.Component;

@Component
public class HutoolIntegrationHttpExecutor implements IntegrationHttpExecutor {

    private static final int TIMEOUT_MS = 5000;

    @Override
    public int post(String url, String token, String body) {
        if (url == null || url.isEmpty()) {
            return -1;
        }
        try (HttpResponse resp = HttpRequest.post(url)
                .header("X-Integration-Token", token == null ? "" : token)
                .header("X-Source", "sgai-master")
                .body(body == null ? "" : body)
                .timeout(TIMEOUT_MS)
                .execute()) {
            return resp.getStatus();
        } catch (Exception e) {
            return -1;
        }
    }
}
```

- [ ] **Step 2: 写失败测试**

```java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.module.master.common.IntegrationHttpExecutor;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.IntegrationLog;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.entity.IntegrationSystemCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.DeviceCategoryMapper;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.module.master.mapper.IntegrationSystemMapper;
import org.jeecg.module.master.mapper.SpaceMapper;
import org.jeecg.module.master.service.impl.IntegrationPushServiceImpl;
import org.jeecg.module.master.vo.IntegrationPayload;
import org.jeecg.module.master.vo.PushSnapshotResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationPushServiceImplTest {

    @Mock IntegrationSystemMapper integrationSystemMapper;
    @Mock IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Mock DeviceCategoryMapper deviceCategoryMapper;
    @Mock SpaceMapper spaceMapper;
    @Mock DeviceMapper deviceMapper;
    @Mock IIntegrationLogService logService;
    @Mock IntegrationHttpExecutor httpExecutor;

    @InjectMocks
    IntegrationPushServiceImpl service;

    private IntegrationSystem pushSystem() {
        IntegrationSystem s = new IntegrationSystem();
        s.setId("S1"); s.setCode("CODE1"); s.setPushEnabled(1);
        s.setPushUrl("http://x"); s.setPushToken("TOK");
        return s;
    }

    private IntegrationPayload devicePayload() {
        Device d = new Device();
        d.setId("D1"); d.setCategoryId("C1");
        return org.jeecg.module.master.common.PushPayloadBuilder.devices(
                "CODE1", IntegrationPayload.Op.UPSERT, "B1", Collections.singletonList(d));
    }

    @Test
    void pushOne_2xx_writesSuccessLog() {
        when(httpExecutor.post(anyString(), anyString(), anyString())).thenReturn(200);
        service.pushOne(pushSystem(), devicePayload());
        verify(logService).writeLog(argThat((IntegrationLog l) -> "SUCCESS".equals(l.getStatus())
                && "PUSH".equals(l.getDirection()) && "DEVICE".equals(l.getType())));
    }

    @Test
    void pushOne_non2xx_writesFailLogWithError() {
        when(httpExecutor.post(anyString(), anyString(), anyString())).thenReturn(500);
        service.pushOne(pushSystem(), devicePayload());
        verify(logService).writeLog(argThat((IntegrationLog l) -> "FAIL".equals(l.getStatus())
                && l.getError() != null));
    }

    @Test
    void pushOne_exception_writesFailLog() {
        when(httpExecutor.post(anyString(), anyString(), anyString())).thenReturn(-1);
        service.pushOne(pushSystem(), devicePayload());
        verify(logService).writeLog(argThat((IntegrationLog l) -> "FAIL".equals(l.getStatus())));
    }

    @Test
    void pushSnapshot_disabled_throws() {
        IntegrationSystem s = pushSystem();
        s.setPushEnabled(0);
        when(integrationSystemMapper.selectById("S1")).thenReturn(s);
        assertThrows(org.jeecg.common.exception.JeecgBootException.class,
                () -> service.pushSnapshotForSystem("S1"));
    }

    @Test
    void pushSnapshot_returns3ResultsAndWrites3Logs() {
        when(integrationSystemMapper.selectById("S1")).thenReturn(pushSystem());
        // 类别范围
        IntegrationSystemCategory row = new IntegrationSystemCategory();
        row.setSystemId("S1"); row.setCategoryId("C1");
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(row));
        // 空间全量
        Space sp = new Space(); sp.setId("SP1");
        when(spaceMapper.selectList(any())).thenReturn(Collections.singletonList(sp));
        // 类别（类别集内）
        org.jeecg.module.master.entity.DeviceCategory cat = new org.jeecg.module.master.entity.DeviceCategory();
        cat.setId("C1");
        when(deviceCategoryMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(cat));
        // 设备（category_id ∈ 集合）
        Device d = new Device(); d.setId("D1"); d.setCategoryId("C1");
        when(deviceMapper.selectList(any())).thenReturn(Collections.singletonList(d));
        when(httpExecutor.post(anyString(), anyString(), anyString())).thenReturn(200);

        List<PushSnapshotResult> results = service.pushSnapshotForSystem("S1");

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(r -> "SUCCESS".equals(r.getStatus())));
        verify(logService, times(3)).writeLog(any(IntegrationLog.class));
    }

    private static <T> T argThat(org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
```

> 注：测试顶部已 `import static org.mockito.ArgumentMatchers.any/anyString`；为避免与 `argThat` 冲突，文件内自定义的 `private static argThat` 是本地小桥接（Mockito 的 `ArgumentMatchers.argThat` 返回泛型值，桥接便于 lambda 写法）。亦可直接用 `ArgumentMatchers.<IntegrationLog>argThat(...)`，二选一。

- [ ] **Step 3: 运行测试，确认失败**

IDE 运行 `IntegrationPushServiceImplTest`。Expected: 编译失败 / 类找不到。

- [ ] **Step 4: 实现接口**

`IIntegrationPushService.java`：
```java
package org.jeecg.module.master.service;

import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.vo.IntegrationPayload;
import org.jeecg.module.master.vo.PushSnapshotResult;

import java.util.List;

public interface IIntegrationPushService {

    /** 实时增量：向单系统推送一条已组装报文，写 PUSH 日志。 */
    void pushOne(IntegrationSystem system, IntegrationPayload payload);

    /** 手动全量：对该系统发 3 次快照推送（空间/类别/设备），各写日志，返回 3 条结果。 */
    List<PushSnapshotResult> pushSnapshotForSystem(String systemId);
}
```

- [ ] **Step 5: 实现类**

```java
package org.jeecg.module.master.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.common.IntegrationHttpExecutor;
import org.jeecg.module.master.common.PushPayloadBuilder;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.IntegrationLog;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.entity.IntegrationSystemCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.DeviceCategoryMapper;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.module.master.mapper.IntegrationSystemMapper;
import org.jeecg.module.master.mapper.SpaceMapper;
import org.jeecg.module.master.service.IIntegrationLogService;
import org.jeecg.module.master.service.IIntegrationPushService;
import org.jeecg.module.master.vo.IntegrationPayload;
import org.jeecg.module.master.vo.PushSnapshotResult;
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
        int status = httpExecutor.post(sys.getPushUrl(), sys.getPushToken(), json);
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
```

- [ ] **Step 6: 运行测试，确认通过**

IDE 运行 `IntegrationPushServiceImplTest`。Expected: 5 个测试全 PASS。

- [ ] **Step 7: 完成确认**

告知用户：Task 10 完成（推送服务就绪），可手动提交。

## Task 11: 线程池配置 + AFTER_COMMIT 监听器 fan-out

**Files:**
- Create: `src/main/java/org/jeecg/module/master/common/AsyncConfig.java`
- Create: `src/main/java/org/jeecg/module/master/common/MasterDataChangeListener.java`
- Test: `src/test/java/org/jeecg/module/master/common/MasterDataChangeListenerTest.java`

**Interfaces:**
- Consumes: `MasterDataChangeEvent`（Task 2）、`CategoryScopeResolver`（Task 3）、`PushPayloadBuilder`（Task 4）、`IntegrationSystem`/`IntegrationSystemCategory` mapper（Task 5）、`IIntegrationPushService.pushOne`（Task 10）
- Produces: `integrationTaskExecutor` bean（专用线程池）；`MasterDataChangeListener.onChange(MasterDataChangeEvent)`——`@Async("integrationTaskExecutor")` + `@TransactionalEventListener(AFTER_COMMIT)`，按事件类型 fan-out 到命中系统（设备/类别按系统类别集精确过滤，空间推给全部 push_enabled 系统排除来源）。Task 12 发布事件后由此触发实时增量推送。

- [ ] **Step 1: 写失败测试**

```java
package org.jeecg.module.master.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.entity.IntegrationSystemCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.module.master.mapper.IntegrationSystemMapper;
import org.jeecg.module.master.service.IIntegrationPushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterDataChangeListenerTest {

    @Mock IntegrationSystemMapper integrationSystemMapper;
    @Mock IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Mock IIntegrationPushService pushService;

    @InjectMocks
    MasterDataChangeListener listener;

    private IntegrationSystem sys(String id, String code) {
        IntegrationSystem s = new IntegrationSystem();
        s.setId(id); s.setCode(code); s.setPushEnabled(1);
        s.setPushUrl("http://x"); s.setPushToken("T");
        return s;
    }

    private void stubSystemsAndScope() {
        // load() 第 1 次查询：integrationSystemMapper.selectList → 两系统
        when(integrationSystemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(sys("S1", "CODE1"), sys("S2", "CODE2")));
        // load() 第 2 次查询：integrationSystemCategoryMapper.selectList → 类别范围（注意是另一个 mapper）
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(scope("S1", "C1"), scope("S2", "C2")));
    }

    private IntegrationSystemCategory scope(String systemId, String categoryId) {
        IntegrationSystemCategory r = new IntegrationSystemCategory();
        r.setSystemId(systemId); r.setCategoryId(categoryId);
        return r;
    }

    @Test
    void deviceChange_pushesOnlyToScopedSystem() {
        stubSystemsAndScope();
        Device d = new Device();
        d.setId("D1"); d.setCategoryId("C1"); // 命中 CODE1（S1→C1）

        listener.onChange(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Collections.singletonList(d), null));

        verify(pushService, times(1)).pushOne(any(IntegrationSystem.class), any());
        verify(pushService).pushOne(eq(sys("S1", "CODE1")), any());
        verify(pushService, never()).pushOne(eq(sys("S2", "CODE2")), any());
    }

    @Test
    void deviceChange_excludesSourceSystem() {
        // 两系统都含 C1，exclude=CODE1 → 只推 CODE2
        when(integrationSystemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(sys("S1", "CODE1"), sys("S2", "CODE2")));
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(scope("S1", "C1"), scope("S2", "C1")));
        Device d = new Device();
        d.setId("D1"); d.setCategoryId("C1");

        listener.onChange(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Collections.singletonList(d), "CODE1"));

        verify(pushService, times(1)).pushOne(any(IntegrationSystem.class), any());
        verify(pushService).pushOne(eq(sys("S2", "CODE2")), any());
        verify(pushService, never()).pushOne(eq(sys("S1", "CODE1")), any());
    }

    @Test
    void categoryChange_exactMatchOnly() {
        stubSystemsAndScope();
        DeviceCategory c = new DeviceCategory();
        c.setId("C1"); c.setName("电气");

        listener.onChange(MasterDataChangeEvent.ofCategories(
                MasterDataChangeEvent.Op.CREATE, Collections.singletonList(c), null));

        verify(pushService).pushOne(eq(sys("S1", "CODE1")), any());
        verify(pushService, never()).pushOne(eq(sys("S2", "CODE2")), any());
    }

    @Test
    void spaceChange_pushesToAllPushEnabledExceptExclude() {
        stubSystemsAndScope();
        Space sp = new Space();
        sp.setId("SP1");

        listener.onChange(MasterDataChangeEvent.ofSpaces(
                MasterDataChangeEvent.Op.UPDATE, Collections.singletonList(sp), "CODE2"));

        verify(pushService).pushOne(eq(sys("S1", "CODE1")), any());
        verify(pushService, never()).pushOne(eq(sys("S2", "CODE2")), any());
    }

    @Test
    void deviceChange_emptyDevices_doesNothing() {
        listener.onChange(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Collections.emptyList(), null));
        verifyNoInteractions(pushService);
    }
}
```

- [ ] **Step 2: 运行测试，确认失败**

IDE 运行 `MasterDataChangeListenerTest`。Expected: 编译失败 / 类找不到。

- [ ] **Step 3: 实现线程池配置**

`AsyncConfig.java`：
```java
package org.jeecg.module.master.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 集成推送专用线程池（实时增量 @Async 监听器用）。
 * 启动类已有 @EnableAsync，此处仅提供 bean。
 */
@Configuration
public class AsyncConfig {

    @Bean("integrationTaskExecutor")
    public ThreadPoolTaskExecutor integrationTaskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(8);
        exec.setQueueCapacity(500);
        exec.setKeepAliveSeconds(60);
        exec.setThreadNamePrefix("integration-push-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.initialize();
        return exec;
    }
}
```

- [ ] **Step 4: 实现监听器**

`MasterDataChangeListener.java`：
```java
package org.jeecg.module.master.common;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.entity.IntegrationSystemCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.module.master.mapper.IntegrationSystemMapper;
import org.jeecg.module.master.service.IIntegrationPushService;
import org.jeecg.module.master.vo.IntegrationPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.Collections;
import java.util.HashMap;
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
            loaded.scopeByCode.computeIfAbsent(code, k -> new java.util.HashSet<>()).add(r.getCategoryId());
        }
        return loaded;
    }

    private static class Loaded {
        final Map<String, IntegrationSystem> systemByCode = new HashMap<>();
        final Map<String, Set<String>> scopeByCode = new HashMap<>();
    }
}
```

- [ ] **Step 5: 运行测试，确认通过**

IDE 运行 `MasterDataChangeListenerTest`。Expected: 5 个测试全 PASS（写入文件时已按 Step 1 修正说明删除占位行）。

- [ ] **Step 6: 完成确认**

告知用户：Task 11 完成（实时增量 fan-out 就绪，但尚无事件源），可手动提交。

## Task 12: 现有三 Service 接入 MasterDataChangeEvent + 手动推送端点

**总览**：给 `DeviceCategoryServiceImpl` / `SpaceServiceImpl` / `DeviceServiceImpl` 的增删改方法在事务内发布 `MasterDataChangeEvent`（设备 `removeBatch`/`batchImport` 发聚合事件），并在 `IntegrationSystemController` 补 `POST /{id}/push`。**前置必要改动**：三个现有 ServiceImplTest 必须各加 `@Mock ApplicationEventPublisher eventPublisher;`，否则 `@InjectMocks` 注入 null、发事件时 NPE。

**Files:**
- Modify: `src/main/java/org/jeecg/module/master/service/impl/DeviceCategoryServiceImpl.java`
- Modify: `src/main/java/org/jeecg/module/master/service/impl/SpaceServiceImpl.java`
- Modify: `src/main/java/org/jeecg/module/master/service/impl/DeviceServiceImpl.java`
- Modify: `src/main/java/org/jeecg/module/master/controller/IntegrationSystemController.java`
- Modify: `src/test/java/org/jeecg/module/master/service/DeviceCategoryServiceImplTest.java`、`SpaceServiceImplTest.java`、`DeviceServiceImplTest.java`（各加 `@Mock ApplicationEventPublisher` + 追加一个发事件测试方法）

**Interfaces:**
- Consumes: `MasterDataChangeEvent`（Task 2）、`ApplicationEventPublisher`（Spring）、`IIntegrationPushService`（Task 10，controller 用）
- Produces: 三个 Service 增删改 → 事务内发事件 → Task 11 监听器 AFTER_COMMIT fan-out；`POST /master/integrationSystem/{id}/push` → 3 条快照结果。

### 12-A：DeviceCategoryServiceImpl 接入

- [ ] **Step 1: import 增量**

在 `DeviceCategoryServiceImpl.java` import 区追加：
```java
import org.jeecg.module.master.common.MasterDataChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
```

- [ ] **Step 2: 注入 publisher**

在 `@Autowired private DeviceMapper deviceMapper;` 之后追加：
```java
    @Autowired
    private ApplicationEventPublisher eventPublisher;
```

- [ ] **Step 3: 替换 create 方法（末尾发事件）**

```java
    @Override
    public void create(DeviceCategory entity) {
        String pid = StrUtil.isBlank(entity.getPid()) ? TreeFullNameHelper.ROOT_PID : entity.getPid();
        if (countSameLevel(pid, entity.getName(), null) > 0) {
            throw new JeecgBootException("同级下已存在同名类别");
        }
        entity.setId(TreeFullNameHelper.generateUuid());
        entity.setPid(pid);
        entity.setFullName(resolveFullName(pid, entity.getName()));
        if (entity.getSort() == null) {
            entity.setSort(nextSort(pid));
        }
        this.save(entity);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofCategories(
                MasterDataChangeEvent.Op.CREATE, Collections.singletonList(entity), null));
    }
```

- [ ] **Step 4: recalcSubtreeFullName 改返回 `List<DeviceCategory>`（先改，供下一步收集受影响子树）**

将方法签名 `private void recalcSubtreeFullName(String rootId) {` 改为 `private List<DeviceCategory> recalcSubtreeFullName(String rootId) {`，并在方法末尾 `if (!toUpdate.isEmpty()) { this.updateBatchById(toUpdate); }` 之后新增一行 `return toUpdate;`（原方法无 return 语句）。

- [ ] **Step 5: 替换 updateNode 末尾（收集受影响子树并发事件）**

将 updateNode 末尾：
```java
        if (pidChanged || nameChanged) {
            recalcSubtreeFullName(entity.getId());
        }
    }
```
替换为：
```java
        List<DeviceCategory> affected = new ArrayList<>();
        affected.add(entity);
        if (pidChanged || nameChanged) {
            affected.addAll(recalcSubtreeFullName(entity.getId()));
        }
        eventPublisher.publishEvent(MasterDataChangeEvent.ofCategories(
                MasterDataChangeEvent.Op.UPDATE, affected, null));
    }
```

- [ ] **Step 6: 替换 removeNode（删除后发 DELETE 事件，仅带 id 即可）**

```java
    @Override
    public void removeNode(String id) {
        long childCnt = this.count(new LambdaQueryWrapper<DeviceCategory>()
                .eq(DeviceCategory::getPid, id));
        if (childCnt > 0) {
            throw new JeecgBootException("存在子级，请先删除子级");
        }
        long refCnt = deviceMapper.selectCount(new LambdaQueryWrapper<Device>()
                .eq(Device::getCategoryId, id));
        if (refCnt > 0) {
            throw new JeecgBootException("该类别被设备引用，无法删除");
        }
        this.removeById(id);
        DeviceCategory deleted = new DeviceCategory();
        deleted.setId(id);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofCategories(
                MasterDataChangeEvent.Op.DELETE, Collections.singletonList(deleted), null));
    }
```

### 12-B：SpaceServiceImpl 接入（与类别对称）

- [ ] **Step 7: import 增量**（同 12-A，类名 `Space`）

```java
import org.jeecg.module.master.common.MasterDataChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
```

- [ ] **Step 8: 注入 publisher**（在 `@Autowired private DeviceMapper deviceMapper;` 之后）

```java
    @Autowired
    private ApplicationEventPublisher eventPublisher;
```

- [ ] **Step 9: 替换 create 方法**

```java
    @Override
    public void create(Space entity) {
        String pid = StrUtil.isBlank(entity.getPid()) ? TreeFullNameHelper.ROOT_PID : entity.getPid();
        if (countSameLevel(pid, entity.getName(), null) > 0) {
            throw new JeecgBootException("同级下已存在同名空间");
        }
        entity.setId(TreeFullNameHelper.generateUuid());
        entity.setPid(pid);
        entity.setFullName(resolveFullName(pid, entity.getName()));
        if (entity.getSort() == null) {
            entity.setSort(nextSort(pid));
        }
        this.save(entity);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofSpaces(
                MasterDataChangeEvent.Op.CREATE, Collections.singletonList(entity), null));
    }
```

- [ ] **Step 10: recalcSubtreeFullName 改返回 `List<Space>`（先改，供下一步收集受影响子树）**

签名 `private void recalcSubtreeFullName(String rootId)` → `private List<Space> recalcSubtreeFullName(String rootId)`，末尾新增 `return toUpdate;`。

- [ ] **Step 11: 替换 updateNode 末尾（收集受影响子树并发事件）**

将末尾：
```java
        if (pidChanged || nameChanged) {
            recalcSubtreeFullName(entity.getId());
        }
    }
```
替换为：
```java
        List<Space> affected = new ArrayList<>();
        affected.add(entity);
        if (pidChanged || nameChanged) {
            affected.addAll(recalcSubtreeFullName(entity.getId()));
        }
        eventPublisher.publishEvent(MasterDataChangeEvent.ofSpaces(
                MasterDataChangeEvent.Op.UPDATE, affected, null));
    }
```

- [ ] **Step 12: 替换 removeNode**

```java
    @Override
    public void removeNode(String id) {
        long childCnt = this.count(new LambdaQueryWrapper<Space>()
                .eq(Space::getPid, id));
        if (childCnt > 0) {
            throw new JeecgBootException("存在子级，请先删除子级");
        }
        long refCnt = deviceMapper.selectCount(new LambdaQueryWrapper<Device>()
                .eq(Device::getSpaceId, id));
        if (refCnt > 0) {
            throw new JeecgBootException("该空间被设备引用，无法删除");
        }
        this.removeById(id);
        Space deleted = new Space();
        deleted.setId(id);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofSpaces(
                MasterDataChangeEvent.Op.DELETE, Collections.singletonList(deleted), null));
    }
```

### 12-C：DeviceServiceImpl 接入

> DeviceServiceImpl 的 `eventPublisher` 字段已在 Task 9 Step 4 注入；本节只加发事件代码。

- [ ] **Step 13: import 增量**

在 `DeviceServiceImpl.java` import 区补（`MasterDataChangeEvent`/`Collections` 若 Task 9 已加则跳过）：
```java
import org.jeecg.module.master.common.MasterDataChangeEvent;
import java.util.Collections;
```

- [ ] **Step 14: create 末尾发 CREATE 事件**

将：
```java
    public void create(Device entity) {
        validate(entity, null);
        entity.setId(TreeFullNameHelper.generateUuid());
        this.save(entity);
    }
```
改为：
```java
    public void create(Device entity) {
        validate(entity, null);
        entity.setId(TreeFullNameHelper.generateUuid());
        this.save(entity);
        eventPublisher.publishEvent(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.CREATE, Collections.singletonList(entity), null));
    }
```

- [ ] **Step 15: updateNode 末尾发 UPDATE 事件**

在 updateNode 的 `this.updateById(entity);` 之后追加：
```java
        eventPublisher.publishEvent(MasterDataChangeEvent.ofDevices(
                MasterDataChangeEvent.Op.UPDATE, Collections.singletonList(entity), null));
```

- [ ] **Step 16: removeBatch 先查受影响设备再删 + 发 DELETE 聚合事件**

将：
```java
    public void removeBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new JeecgBootException("未选择删除数据");
        }
        this.removeByIds(ids);
    }
```
改为：
```java
    public void removeBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new JeecgBootException("未选择删除数据");
        }
        List<Device> affected = this.listByIds(ids);
        this.removeByIds(ids);
        if (affected != null && !affected.isEmpty()) {
            eventPublisher.publishEvent(MasterDataChangeEvent.ofDevices(
                    MasterDataChangeEvent.Op.DELETE, affected, null));
        }
    }
```

- [ ] **Step 17: batchImport 收集成功设备 + 发 CREATE 聚合事件**

在 batchImport 内 `List<String> errors = new ArrayList<>();` 之后新增：
```java
        List<Device> imported = new ArrayList<>();
```
将成功分支的 `baseMapper.insert(d);` 之后追加 `imported.add(d);`（仍在 try 块内）：
```java
                baseMapper.insert(d);
                imported.add(d);
```
在方法 `return errors;` 之前追加：
```java
        if (!imported.isEmpty()) {
            eventPublisher.publishEvent(MasterDataChangeEvent.ofDevices(
                    MasterDataChangeEvent.Op.CREATE, imported, null));
        }
```

### 12-D：现有三个 ServiceImplTest 加 `@Mock ApplicationEventPublisher`

- [ ] **Step 18: 在三个现有测试类的 `@Mock` 区各加一行**

`DeviceCategoryServiceImplTest.java`、`SpaceServiceImplTest.java`、`DeviceServiceImplTest.java` 各加：
```java
    @Mock
    org.springframework.context.ApplicationEventPublisher eventPublisher;
```
（无需改现有断言——publishEvent 为 mock 空操作，现有测试继续通过。）

### 12-E：发事件验证（在现有三个 ServiceImplTest 各追加一个方法）

> 12-D 已为每个现有测试类加了 `@Mock ApplicationEventPublisher eventPublisher`，且每个测试类只 `@InjectMocks` 一个 service，故无注入歧义。直接在各测试类追加一个发事件测试方法。

- [ ] **Step 19: DeviceCategoryServiceImplTest 追加**

在 `DeviceCategoryServiceImplTest.java` import 区补：
```java
import org.jeecg.module.master.common.MasterDataChangeEvent;
import org.mockito.ArgumentCaptor;
```
在类内追加方法（`baseMapper`/`eventPublisher` 为现有 `@Mock` 字段名）：
```java
    @Test
    void create_publishesCreateEvent() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.EntityType.CATEGORY, cap.getValue().getEntityType());
        assertEquals(MasterDataChangeEvent.Op.CREATE, cap.getValue().getOp());
    }
```

- [ ] **Step 20: SpaceServiceImplTest 追加（对称）**

import 同上（`MasterDataChangeEvent`、`ArgumentCaptor`，`Space` 现有已有）。追加（`SpaceServiceImpl` 的 `@Mock` 字段名为 `baseMapper`/`eventPublisher`，与类别测试一致）：
```java
    @Test
    void create_publishesCreateEvent() {
        Space s = new Space();
        s.setName("一楼");
        s.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(Space.class))).thenReturn(1);

        service.create(s);

        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.EntityType.SPACE, cap.getValue().getEntityType());
        assertEquals(MasterDataChangeEvent.Op.CREATE, cap.getValue().getOp());
    }
```

- [ ] **Step 21: DeviceServiceImplTest 追加**

import 补 `MasterDataChangeEvent`、`ArgumentCaptor`。追加（字段名与现有 `DeviceServiceImplTest` 一致：`baseMapper`/`deviceCategoryMapper`/`spaceMapper` + 12-D 新增的 `eventPublisher`）：
```java
    @Test
    void create_publishesCreateEvent() {
        Device d = new Device();
        d.setName("设备A");
        d.setCategoryId("C1");
        d.setSpaceId("S1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L); // countName 无冲突
        when(deviceCategoryMapper.selectById("C1")).thenReturn(new DeviceCategory());
        when(spaceMapper.selectById("S1")).thenReturn(new Space());
        when(baseMapper.insert(any(Device.class))).thenReturn(1);

        service.create(d);

        ArgumentCaptor<MasterDataChangeEvent> cap = ArgumentCaptor.forClass(MasterDataChangeEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(MasterDataChangeEvent.EntityType.DEVICE, cap.getValue().getEntityType());
        assertEquals(MasterDataChangeEvent.Op.CREATE, cap.getValue().getOp());
    }
```

- [ ] **Step 22: 运行测试，确认通过**

IDE 运行三个 ServiceImplTest。Expected: 现有测试 + 新增 3 个发事件测试全 PASS。

### 12-F：IntegrationSystemController 补手动推送端点

- [ ] **Step 23: 注入 PushService 并加端点**

在 `IntegrationSystemController.java` import 区补：
```java
import org.jeecg.module.master.service.IIntegrationPushService;
import org.jeecg.module.master.vo.PushSnapshotResult;
import java.util.List;
```
字段区追加：
```java
    @Autowired
    private IIntegrationPushService integrationPushService;
```
将类内注释 `// POST /{id}/push ...` 替换为：
```java
    @ApiOperation("手动全量推送（3 次快照：空间/类别/设备）")
    @PostMapping("/{id}/push")
    public Result<List<PushSnapshotResult>> push(@PathVariable("id") String id) {
        return Result.OK(integrationPushService.pushSnapshotForSystem(id));
    }
```

- [ ] **Step 24: 编译验证**

IDE 编译模块。Expected: 无错误（controller /push 接通 PushService）。

- [ ] **Step 25: 完成确认**

告知用户：Task 12 完成（事件源 + 手动推送端点就绪，推送链路全通），可手动提交。

## Task 13: 接收链路（Controller + Service：鉴权 + 过滤 + 逐条 upsert + hub）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/vo/ReceivePayload.java`
- Create: `src/main/java/org/jeecg/module/master/service/IIntegrationReceiveService.java`
- Create: `src/main/java/org/jeecg/module/master/service/impl/IntegrationReceiveServiceImpl.java`
- Create: `src/main/java/org/jeecg/module/master/controller/IntegrationReceiveController.java`
- Test: `src/test/java/org/jeecg/module/master/service/IntegrationReceiveServiceImplTest.java`

**Interfaces:**
- Consumes: `IIntegrationSystemService.findByReceiveToken`（Task 7）、`IDeviceService.upsertFromIntegration/deleteFromIntegration`（Task 9，内部发 hub 事件带 excludeSystemCode=来源 code）、`IntegrationSystemCategoryMapper`（Task 5）、`IIntegrationLogService.writeLog`（Task 6）、`ReceiveResult`/`DevicePushItem`（Task 4）
- Produces: `POST /master/integration/receive`（X-Integration-Token 头 + ReceivePayload body）→ `ReceiveResult{batchId, accepted, rejected[]}`；逐设备尽力而为，单条坏数据不连累整批；hub 分发由 upsert/delete 内部 `AFTER_COMMIT` 事件触发。

- [ ] **Step 1: 创建接收报文 VO（data 强类型 List<DevicePushItem>）**

`ReceivePayload.java`：
```java
package org.jeecg.module.master.vo;

import lombok.Data;
import java.util.List;

@Data
public class ReceivePayload {
    private String source;
    private String systemCode;
    private IntegrationPayload.Type type;
    private IntegrationPayload.Op op;
    private String batchId;
    private List<DevicePushItem> data;
}
```

- [ ] **Step 2: 写失败测试**

```java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.entity.IntegrationSystemCategory;
import org.jeecg.module.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.module.master.service.impl.IntegrationReceiveServiceImpl;
import org.jeecg.module.master.vo.DevicePushItem;
import org.jeecg.module.master.vo.IntegrationPayload;
import org.jeecg.module.master.vo.ReceivePayload;
import org.jeecg.module.master.vo.ReceiveResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationReceiveServiceImplTest {

    @Mock IIntegrationSystemService integrationSystemService;
    @Mock IDeviceService deviceService;
    @Mock IntegrationSystemCategoryMapper integrationSystemCategoryMapper;
    @Mock IIntegrationLogService logService;

    @InjectMocks
    IntegrationReceiveServiceImpl service;

    private IntegrationSystem sys() {
        IntegrationSystem s = new IntegrationSystem();
        s.setId("S1"); s.setCode("CODE1"); s.setReceiveEnabled(1);
        return s;
    }

    private DevicePushItem item(String id, String catId) {
        DevicePushItem d = new DevicePushItem();
        d.setId(id); d.setCategoryId(catId); d.setName(id); d.setSpaceId("SP1");
        return d;
    }

    private ReceivePayload payload(String op, DevicePushItem... items) {
        ReceivePayload p = new ReceivePayload();
        p.setType(IntegrationPayload.Type.DEVICE);
        p.setOp("DELETE".equals(op) ? IntegrationPayload.Op.DELETE : IntegrationPayload.Op.UPSERT);
        p.setBatchId("B1");
        p.setData(Arrays.asList(items));
        return p;
    }

    private void stubScope(String catId) {
        IntegrationSystemCategory row = new IntegrationSystemCategory();
        row.setSystemId("S1"); row.setCategoryId(catId);
        when(integrationSystemCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(row));
    }

    @Test
    void receive_authFail_throwsAndLogsFail() {
        when(integrationSystemService.findByReceiveToken("BAD")).thenReturn(null);
        assertThrows(JeecgBootException.class, () -> service.receive(payload("UPSERT", item("D1", "C1")), "BAD"));
        verify(logService).writeLog(argThat((org.jeecg.module.master.entity.IntegrationLog l) ->
                "FAIL".equals(l.getStatus()) && "RECEIVE".equals(l.getDirection())));
    }

    @Test
    void receive_mixedAcceptedRejected() {
        when(integrationSystemService.findByReceiveToken("TOK")).thenReturn(sys());
        stubScope("C1"); // 仅 C1 在范围
        ReceivePayload p = payload("UPSERT", item("D1", "C1"), item("D2", "C9"));

        ReceiveResult r = service.receive(p, "TOK");

        assertEquals(1, r.getAccepted());
        assertEquals(1, r.getRejected().size());
        assertEquals("D2", r.getRejected().get(0).getId());
        verify(logService).writeLog(argThat((org.jeecg.module.master.entity.IntegrationLog l) ->
                "PARTIAL".equals(l.getStatus())));
    }

    @Test
    void receive_upsertPassesExcludeSystemCode() {
        when(integrationSystemService.findByReceiveToken("TOK")).thenReturn(sys());
        stubScope("C1");
        service.receive(payload("UPSERT", item("D1", "C1")), "TOK");
        verify(deviceService).upsertFromIntegration(any(), eq("CODE1"));
    }

    @Test
    void receive_delete_callsDeleteWithExclude() {
        when(integrationSystemService.findByReceiveToken("TOK")).thenReturn(sys());
        stubScope("C1");
        service.receive(payload("DELETE", item("D1", "C1")), "TOK");
        verify(deviceService).deleteFromIntegration(any(), eq("CODE1"));
        verify(deviceService, never()).upsertFromIntegration(any(), anyString());
    }

    @Test
    void receive_allOk_logsSuccess() {
        when(integrationSystemService.findByReceiveToken("TOK")).thenReturn(sys());
        stubScope("C1");
        ReceiveResult r = service.receive(payload("UPSERT", item("D1", "C1")), "TOK");
        assertEquals(1, r.getAccepted());
        assertTrue(r.getRejected().isEmpty());
        verify(logService).writeLog(argThat((org.jeecg.module.master.entity.IntegrationLog l) ->
                "SUCCESS".equals(l.getStatus())));
    }

    private static <T> T argThat(java.util.function.Predicate<T> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
```

- [ ] **Step 3: 运行测试，确认失败**

IDE 运行 `IntegrationReceiveServiceImplTest`。Expected: 编译失败 / 类找不到。

- [ ] **Step 4: 实现接口**

`IIntegrationReceiveService.java`：
```java
package org.jeecg.module.master.service;

import org.jeecg.module.master.vo.ReceivePayload;
import org.jeecg.module.master.vo.ReceiveResult;

public interface IIntegrationReceiveService {

    /** 接收外部设备推送：鉴权 + 类别过滤 + 逐条 upsert/delete + hub。鉴权失败抛 JeecgBootException。 */
    ReceiveResult receive(ReceivePayload payload, String token);
}
```

- [ ] **Step 5: 实现类**

```java
package org.jeecg.module.master.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.IntegrationLog;
import org.jeecg.module.master.entity.IntegrationSystem;
import org.jeecg.module.master.entity.IntegrationSystemCategory;
import org.jeecg.module.master.mapper.IntegrationSystemCategoryMapper;
import org.jeecg.module.master.service.IDeviceService;
import org.jeecg.module.master.service.IIntegrationLogService;
import org.jeecg.module.master.service.IIntegrationReceiveService;
import org.jeecg.module.master.service.IIntegrationSystemService;
import org.jeecg.module.master.vo.DevicePushItem;
import org.jeecg.module.master.vo.IntegrationPayload;
import org.jeecg.module.master.vo.ReceivePayload;
import org.jeecg.module.master.vo.ReceiveResult;
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
        IntegrationSystem src = integrationSystemService.findByReceiveToken(token);
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
```

- [ ] **Step 6: 实现 controller**

`IntegrationReceiveController.java`：
```java
package org.jeecg.module.master.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.service.IIntegrationReceiveService;
import org.jeecg.module.master.vo.ReceivePayload;
import org.jeecg.module.master.vo.ReceiveResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "数据对接-接收")
@RestController
@RequestMapping("/master/integration")
public class IntegrationReceiveController {

    @Autowired
    private IIntegrationReceiveService receiveService;

    @ApiOperation("接收外部设备推送（令牌鉴权）")
    @PostMapping("/receive")
    public ResponseEntity<Result<ReceiveResult>> receive(
            @RequestHeader(value = "X-Integration-Token", required = false) String token,
            @RequestBody ReceivePayload payload) {
        try {
            return ResponseEntity.ok(Result.OK(receiveService.receive(payload, token)));
        } catch (JeecgBootException e) {
            // 仅鉴权失败会抛到此处（逐条业务异常已在 service 内 catch）
            return ResponseEntity.status(401).body(Result.error(e.getMessage()));
        }
    }
}
```

- [ ] **Step 7: 接收接口登录放行（用户配置 nacos）**

`/master/integration/receive` 对外部系统开放，需排除平台登录拦截。配置在 **nacos 的 `jeecg.yaml`**（不在本仓库）。请对照 `sgai-module-third` 的 `ThirdSystemLoginController` 实际放行方式二选一：

- **方式一（优先）**：若平台支持，在 `IntegrationReceiveController` 类或 `receive` 方法上加 jeecg 免登注解 `@org.jeecg.common.aspect.annotation.IgnoreAuth`（确认该注解存在于 `sgai-boot-base-core`）。
- **方式二**：在 nacos `jeecg.yaml` 的 `shiro.filterChainDefinitions` 追加 `/master/integration/receive = anon`。

> 令牌鉴权由 `receiveService`（按 `X-Integration-Token` 反查 `receive_token`）承担，与登录放行是两层；放行仅去掉平台登录拦截，令牌仍校验。

- [ ] **Step 8: 运行测试，确认通过**

IDE 运行 `IntegrationReceiveServiceImplTest`。Expected: 5 个测试全 PASS。

- [ ] **Step 9: 完成确认**

告知用户：Task 13 完成（接收链路就绪，含登录放行配置指引），可手动提交。

---

## Task 14: 对接日志查询 Controller

**Files:**
- Create: `src/main/java/org/jeecg/module/master/controller/IntegrationLogController.java`

**Interfaces:**
- Consumes: `IIntegrationLogService`（Task 6，`IService` 内置 `page`/`getById`）
- Produces: `/master/integrationLog` 下 `GET /list?direction=&systemId=&type=&status=`（分页）、`GET /{id}`（含 payload 原文）。只读审计，无编辑/删除/重推。

- [ ] **Step 1: 实现 controller**

```java
package org.jeecg.module.master.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;
import org.jeecg.common.api.vo.Result;
import org.jeecg.module.master.entity.IntegrationLog;
import org.jeecg.module.master.service.IIntegrationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "对接日志")
@RestController
@RequestMapping("/master/integrationLog")
public class IntegrationLogController {

    @Autowired
    private IIntegrationLogService integrationLogService;

    @ApiOperation("分页列表")
    @GetMapping("/list")
    public Result<Page<IntegrationLog>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String systemId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        Page<IntegrationLog> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<IntegrationLog> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(direction)) {
            w.eq(IntegrationLog::getDirection, direction);
        }
        if (StrUtil.isNotBlank(systemId)) {
            w.eq(IntegrationLog::getSystemId, systemId);
        }
        if (StrUtil.isNotBlank(type)) {
            w.eq(IntegrationLog::getType, type);
        }
        if (StrUtil.isNotBlank(status)) {
            w.eq(IntegrationLog::getStatus, status);
        }
        w.orderByDesc(IntegrationLog::getCreateTime);
        return Result.OK(integrationLogService.page(page, w));
    }

    @ApiOperation("详情（含 payload 原文）")
    @GetMapping("/{id}")
    public Result<IntegrationLog> queryById(@PathVariable("id") String id) {
        return Result.OK(integrationLogService.getById(id));
    }
}
```

- [ ] **Step 2: 编译验证**

IDE 编译模块。Expected: 无错误。

- [ ] **Step 3: 完成确认**

告知用户：Task 14 完成（日志查询就绪），后端全部就绪，可手动提交。

## Task 15: 前端 - 对接系统管理页

> 前端在独立仓库 `fwbz-web/src/views/master/`。**骨架完全复刻 `views/master/device/`**（`index.vue` 用 BasicTable + 弹窗、`api.ts` 用 defHttp、`data.ts` 定义 columns/searchFormSchema）。defHttp / BasicTable / useDialog 等组件的 import 路径与 `device.api.ts`/`device/index.vue` 保持一致。

**Files:**
- Create: `fwbz-web/src/views/master/integrationSystem/integrationSystem.api.ts`
- Create: `fwbz-web/src/views/master/integrationSystem/integrationSystem.data.ts`
- Create: `fwbz-web/src/views/master/integrationSystem/components/IntegrationSystemModal.vue`
- Create: `fwbz-web/src/views/master/integrationSystem/index.vue`

- [ ] **Step 1: api.ts**

```ts
// 与 device.api.ts 同样的 defHttp 导入路径
import { defHttp } from '/@/utils/http/axios';
import {getListUrl} from '/@/utils/http/axios';

enum Api {
  list = '/master/integrationSystem/list',
  detail = '/master/integrationSystem/',      // +id
  save = '/master/integrationSystem',
  edit = '/master/integrationSystem',
  remove = '/master/integrationSystem/',      // +id
  push = '/master/integrationSystem/',        // +id + '/push'
  categoryTree = '/master/deviceCategory/list',
}

export interface IntegrationSystemForm {
  id?: string;
  name: string;
  code: string;
  pushEnabled?: number;
  pushUrl?: string;
  pushToken?: string;
  receiveEnabled?: number;
  receiveToken?: string;
  remark?: string;
  categoryIds: string[];
}

export interface PushSnapshotResult { type: string; payloadCount: number; status: string; error?: string; }

export const list = (params) => defHttp.get({ url: Api.list, params });
export const detail = (id) => defHttp.get({ url: Api.detail + id });
export const save = (data) => defHttp.post({ url: Api.save, data });
export const edit = (data) => defHttp.put({ url: Api.edit, data });
export const remove = (id) => defHttp.delete({ url: Api.remove + id });
export const push = (id) => defHttp.post({ url: Api.push + id + '/push' });
export const categoryTree = () => defHttp.get({ url: Api.categoryTree });
```

> 注：`getListUrl` 那行若 `device.api.ts` 无此导入则删除（仅保留与现有页一致的 defHttp 导入）。以现有 `device.api.ts` 实际导入为准。

- [ ] **Step 2: data.ts**

```ts
import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validate'; // 按 device.data.ts 实际路径对齐

export const columns: BasicColumn[] = [
  { title: '系统名称', dataIndex: 'name', width: 160 },
  { title: '系统编码', dataIndex: 'code', width: 140 },
  { title: '推送', dataIndex: 'pushEnabled', width: 80,
    customRender: ({ text }) => (text === 1 ? '启用' : '停用') },
  { title: '接收', dataIndex: 'receiveEnabled', width: 80,
    customRender: ({ text }) => (text === 1 ? '启用' : '停用') },
  { title: '推送URL', dataIndex: 'pushUrl', width: 220, ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
];

export const searchFormSchema: FormSchema[] = [
  { field: 'name', label: '系统名称', component: 'Input', colProps: { span: 8 } },
  { field: 'code', label: '系统编码', component: 'Input', colProps: { span: 8 } },
];
```

- [ ] **Step 3: IntegrationSystemModal.vue（关键字段：类别范围 TreeSelect 多选）**

```vue
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="700px">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm, FormSchema } from '/@/components/Form';
import { listToTreeSelect } from '../utils/tree'; // 复用主数据 utils（按实际路径对齐）
import { categoryTree, save, edit, detail } from './integrationSystem.api';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(false);
const title = computed(() => (isUpdate.value ? '编辑对接系统' : '新增对接系统'));
const categoryOptions = ref<any[]>([]);

const schemas: FormSchema[] = [
  { field: 'id', show: false },
  { field: 'name', label: '系统名称', component: 'Input', required: true, colProps: { span: 24 } },
  { field: 'code', label: '系统编码', component: 'Input', required: true, colProps: { span: 24 } },
  { field: 'pushEnabled', label: '启用推送', component: 'Switch',
    componentProps: { checkedValue: 1, unCheckedValue: 0 }, defaultValue: 0, colProps: { span: 12 } },
  { field: 'pushUrl', label: '推送URL', component: 'Input', colProps: { span: 24 },
    ifShow: ({ values }) => values.pushEnabled === 1 },
  { field: 'pushToken', label: '推送令牌', component: 'Input', colProps: { span: 24 },
    ifShow: ({ values }) => values.pushEnabled === 1 },
  { field: 'receiveEnabled', label: '启用接收', component: 'Switch',
    componentProps: { checkedValue: 1, unCheckedValue: 0 }, defaultValue: 0, colProps: { span: 12 } },
  { field: 'receiveToken', label: '接收令牌', component: 'Input', colProps: { span: 24 },
    ifShow: ({ values }) => values.receiveEnabled === 1 },
  { field: 'categoryIds', label: '类别范围', component: 'TreeSelect',
    componentProps: { treeData: categoryOptions, multiple: true, treeCheckable: true,
      showSearch: true, allowClear: true, placeholder: '请选择类别（精确匹配）' },
    required: true, colProps: { span: 24 } },
  { field: 'remark', label: '备注', component: 'InputTextArea', colProps: { span: 24 } },
];

const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
  labelWidth: 100, schemas, showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  resetFields();
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;
  if (isUpdate.value && data?.record?.id) {
    const form = await detail(data.record.id);
    setFieldsValue(form);
  }
});

onMounted(async () => {
  const flat = await categoryTree();
  categoryOptions.value = listToTreeSelect(flat); // 扁平 → 树
});

async function handleSubmit() {
  const values = await validate();
  setModalProps({ confirmLoading: true });
  if (isUpdate.value) await edit(values); else await save(values);
  setModalProps({ confirmLoading: false });
  closeModal();
  emit('success');
}
</script>
```

> 类别范围用 `TreeSelect` 多选（精确选取类别，与后端精确匹配语义一致）。`listToTreeSelect` 复用 `views/master/utils/tree.ts`（CLAUDE.md 已记录）。

- [ ] **Step 4: index.vue（含推送按钮）**

```vue
<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowKey="(r) => r.id">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate">新增</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-button type="link" @click="handleEdit(record)">编辑</a-button>
          <a-popconfirm title="确认全量推送？将向该系统发送 空间/类别/设备 三次快照" @confirm="handlePush(record)">
            <a-button type="link">推送</a-button>
          </a-popconfirm>
          <a-popconfirm title="确认删除？须先停用" @confirm="handleDelete(record)">
            <a-button type="link" danger>删除</a-button>
          </a-popconfirm>
        </template>
      </template>
    </BasicTable>
    <IntegrationSystemModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" setup>
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import IntegrationSystemModal from './components/IntegrationSystemModal.vue';
import { columns, searchFormSchema } from './integrationSystem.data';
import { list, remove, push } from './integrationSystem.api';

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();
const [registerTable, { reload }] = useTable({
  api: list,
  columns,
  formConfig: { schemas: searchFormSchema },
  actionColumn: { width: 220, title: '操作', dataIndex: 'action', key: 'action' },
  showIndexColumn: false,
});

function handleCreate() { openModal(true, { isUpdate: false }); }
function handleEdit(record) { openModal(true, { isUpdate: true, record }); }

async function handlePush(record) {
  const results = await push(record.id);
  const ok = results.filter((r) => r.status === 'SUCCESS').length;
  createMessage.success(`推送完成：${ok}/${results.length} 成功`);
  reload();
}

async function handleDelete(record) {
  await remove(record.id);
  createMessage.success('删除成功');
  reload();
}
</script>
```

> 上述为基于 jeecg-boot Vue3 通用模式的骨架；`BasicTable`/`useTable`/`useModal`/`useMessage` 的 import 与现有 `views/master/device/index.vue` 保持一致，若 fwbz-web 封装有差异，按 device 页对齐。

- [ ] **Step 5: 完成确认**

告知用户：Task 15 完成（对接系统页），可手动提交。

---

## Task 16: 前端 - 对接日志页

**Files:**
- Create: `fwbz-web/src/views/master/integrationLog/integrationLog.api.ts`
- Create: `fwbz-web/src/views/master/integrationLog/integrationLog.data.ts`
- Create: `fwbz-web/src/views/master/integrationLog/index.vue`

- [ ] **Step 1: api.ts**

```ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/master/integrationLog/list',
  detail = '/master/integrationLog/',  // +id
}

export const list = (params) => defHttp.get({ url: Api.list, params });
export const detail = (id) => defHttp.get({ url: Api.detail + id });
```

- [ ] **Step 2: data.ts**

```ts
import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  { title: '方向', dataIndex: 'direction', width: 80 },
  { title: '系统', dataIndex: 'systemCode', width: 120 },
  { title: '类型', dataIndex: 'type', width: 90 },
  { title: '操作', dataIndex: 'op', width: 90 },
  { title: '批次号', dataIndex: 'batchId', width: 220, ellipsis: true },
  { title: '条数', dataIndex: 'payloadCount', width: 70 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '耗时(ms)', dataIndex: 'costMs', width: 90 },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
];

export const searchFormSchema: FormSchema[] = [
  { field: 'direction', label: '方向', component: 'Select',
    componentProps: { options: [{ label: '推送', value: 'PUSH' }, { label: '接收', value: 'RECEIVE' }] },
    colProps: { span: 6 } },
  { field: 'systemId', label: '系统', component: 'Input', colProps: { span: 6 } },
  { field: 'type', label: '类型', component: 'Select',
    componentProps: { options: [
      { label: '设备', value: 'DEVICE' }, { label: '类别', value: 'CATEGORY' }, { label: '空间', value: 'SPACE' }] },
    colProps: { span: 6 } },
  { field: 'status', label: '状态', component: 'Select',
    componentProps: { options: [
      { label: '成功', value: 'SUCCESS' }, { label: '部分', value: 'PARTIAL' }, { label: '失败', value: 'FAIL' }] },
    colProps: { span: 6 } },
];
```

- [ ] **Step 3: index.vue（行点击 → 抽屉展示 payload 原文 + error）**

```vue
<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowKey="(r) => r.id" @row-click="handleRowClick">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 'SUCCESS' ? 'green' : (record.status === 'PARTIAL' ? 'orange' : 'red')">
            {{ record.status }}
          </a-tag>
        </template>
      </template>
    </BasicTable>
    <a-drawer :visible="visible" title="日志详情" width="700" @close="visible = false">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="方向">{{ current.direction }}</a-descriptions-item>
        <a-descriptions-item label="系统">{{ current.systemCode }}</a-descriptions-item>
        <a-descriptions-item label="类型/操作">{{ current.type }} / {{ current.op }}</a-descriptions-item>
        <a-descriptions-item label="批次号">{{ current.batchId }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ current.status }} ({{ current.payloadCount }} 条)</a-descriptions-item>
        <a-descriptions-item label="失败明细"><pre style="white-space:pre-wrap">{{ current.error || '—' }}</pre></a-descriptions-item>
        <a-descriptions-item label="原始报文">
          <pre style="white-space:pre-wrap; max-height:360px; overflow:auto">{{ pretty(current.payload) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicTable, useTable } from '/@/components/Table';
import { columns, searchFormSchema } from './integrationLog.data';
import { list, detail } from './integrationLog.api';

const visible = ref(false);
const current = ref<any>({});

const [registerTable] = useTable({
  api: list,
  columns,
  formConfig: { schemas: searchFormSchema },
  showIndexColumn: false,
});

async function handleRowClick(record) {
  current.value = await detail(record.id);
  visible.value = true;
}

function pretty(payload: string) {
  try { return JSON.stringify(JSON.parse(payload), null, 2); } catch { return payload || '—'; }
}
</script>
```

- [ ] **Step 4: 完成确认**

告知用户：Task 16 完成（对接日志页），可手动提交。

---

## Task 17: 菜单配置 + 端到端验证

**Files:**
- 无新增代码（菜单在 jeecg 系统管理配置；验证为手测）

- [ ] **Step 1: 配置菜单（用户在 jeecg 后台「系统管理 → 菜单管理」新增）**

| 菜单名称 | 前端组件 | 路由地址 |
|---|---|---|
| 对接系统 | `master/integrationSystem/index` | `/master/integrationSystem` |
| 对接日志 | `master/integrationLog/index` | `/master/integrationLog` |

给「主数据管理」一级菜单下新增两个子菜单（或按现有主数据菜单层级放置）。菜单授权给相关角色。

- [ ] **Step 2: 端到端验证清单（手测）**

前置：Task 1 DDL 已在目标库执行；接收接口登录放行已配（Task 13 Step 7）。

**对接系统 CRUD**：
- [ ] 新增系统（名称/编码/类别范围必填；编码重复→提示「系统编码已存在」；接收令牌重复→提示）。
- [ ] 编辑（类别范围整体覆盖；详情含 categoryIds）。
- [ ] 删除启用中的系统→提示「请先停用该对接系统」；停用后删除成功。

**实时增量推送**（系统 A 启用推送、类别范围含类别 C1）：
- [ ] 新增/编辑/删除一台 C1 下设备 → 查看对接日志，应有 PUSH/DEVICE/UPSERT|DELETE 一条；下游收到。
- [ ] 新增/编辑/删除类别 C1 → PUSH/CATEGORY 日志。
- [ ] 空间改名/移动 → PUSH/SPACE（推给所有启用推送系统）。
- [ ] 非范围类别下的设备变更 → 不产生该系统的 PUSH 日志。

**手动全量推送**：
- [ ] 点「推送」按钮 → 返回 3 条结果（空间/类别/设备），日志 3 条 SNAPSHOT。

**接收（系统 B 启用接收、receive_token=TOK、类别范围含 C1）**：
- [ ] POST `/master/integration/receive`（头 X-Integration-Token=TOK，body 设备 categoryId=C1）→ 入库 + 返回 accepted；无 TOK→401。
- [ ] categoryId 不在范围 → rejected「类别不在允许范围」、status=PARTIAL。
- [ ] 名称冲突 → rejected「设备名称冲突」。
- [ ] DELETE op → 本地按 id 删 + hub 分发。

**Hub 分发**：
- [ ] 系统 B 推来的设备 → 系统 A（A 类别范围含 C1 且 A≠B）收到 PUSH/DEVICE；B 不被回推（excludeSystemCode=B）。

**对接日志**：
- [ ] 列表筛选方向/类型/状态；行点击展示 payload 原文与 error。

- [ ] **Step 3: 完成确认**

告知用户：全部 Task 完成，端到端验证通过。

---

## Self-Review 记录

- **spec 覆盖**：DDL→T1；通用事件→T2；精确匹配→T3；报文→T4；三表数据层→T5；日志写入→T6；对接系统 CRUD/类别覆盖/删除停用/token 反查→T7；系统 CRUD 端点→T8；接收专用 upsert/delete+hub 事件→T9；推送 HTTP+全量→T10；监听器 fan-out→T11；事件源接入+手动推送端点→T12；接收链路→T13；日志查询→T14；前端两页→T15/T16；菜单+验证→T17。spec 第 2 节决策逐项落地。
- **占位符**：已清除 Task 3 / Task 11 中的临时占位行与"修正说明"（plan 内代码即为最终代码）。
- **类型一致性**：`MasterDataChangeEvent`（T2）的 `ofDevices/ofCategories/ofSpaces` + `Op/EntityType` 在 T9/T11/T12 一致；`IntegrationPayload.Type/Op`（T4）在 T10/T13 一致；`upsertFromIntegration(Device,String)`/`deleteFromIntegration(Device,String)`（T9）在 T13 调用一致；`pushOne(IntegrationSystem, IntegrationPayload)`（T10）在 T11 调用一致；`writeLog(IntegrationLog)`（T6）在 T10/T13 调用一致。
- **事务**：日志写入均 `REQUIRES_NEW`（T6）；upsert/delete/系统 CRUD 均 `@Transactional(rollbackFor=Exception.class)`。
- **风险提示**：接收接口登录放行（T13 Step 7）依赖平台机制，给了 `@IgnoreAuth` / nacos shiro anon 两方案，对照 `ThirdSystemLoginController` 确认；前端组件 import 路径以 `views/master/device` 现有页为准（未跨项目读取 fwbz-web）。
- **编译/测试正确性（review 中已修正）**：(1) T11 监听器测试改为分别 stub `integrationSystemMapper` 与 `integrationSystemCategoryMapper`（监听器 `load()` 是两次不同 mapper 的查询）；(2) T7 `save_emptyCategoryIds` 去掉未使用的 `selectCount` stub（避免 Mockito strict stubbing 的 `UnnecessaryStubbingException`）；(3) T4 `ReceiveResult` 加 `@NoArgsConstructor`、`Reject` 去手写构造（避免与 `@Data` 生成的 `@RequiredArgsConstructor` 冲突 / 无参构造缺失导致 `new ReceiveResult()` 编译失败）；(4) T12 补回遗漏的 `DeviceServiceImpl` 接入小节（12-C），并把 `recalcSubtreeFullName` 改返回 `List` 的步骤前置于 updateNode 替换之前（保证 `affected.addAll(recalc(...))` 可编译）。

