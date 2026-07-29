# 数据对接管理 设计文档

- 日期：2026-07-08
- 范围：将主数据（类别/空间/设备）推送到其他系统，以及接收其他系统推送的设备数据；对接系统运行时配置；对接日志。
- 后端模块：`sgai-module-master`（在现有 `org.jeecg.module.master` 各层包内新增，不拆子目录）
- 前端目录：`fwbz-web/src/views/master/`（新增 `integrationSystem/`、`integrationLog/`）
- 约束：沿用主数据模块硬约束——零 mapper xml（全 LambdaQueryWrapper + IService）、uuid 主键、`JeecgBootException` 兜底、物理删除、不建 Feign api 子模块。

---

## 1. 概述

在已有主数据（类别/空间两棵树 + 设备平铺）之上，新增「数据对接管理」：

- **推送（出）**：类别、空间、设备 → 推送给配置好的「对接系统」。
- **接收（入）**：只接收设备 → 按来源系统的类别范围过滤后 upsert 入库，并以 hub 身份分发给其他相关系统。
- **身份模型**：**id 是所有系统共用的全局身份**。谁创建实体谁分配 uuid，所有系统共用同一 id。推送带 id、接收按 id upsert，因此**设备表无需新增「外部编码」字段**（id 即充当跨系统键），不破坏现有「设备名系统唯一、不加设备编码」约定。

---

## 2. 关键决策摘要（澄清结论）

| 议题 | 决策 |
|---|---|
| 推送时机 | **实时增量 + 手动全量** 两种都要 |
| 实时增量执行模型 | **方案 A**：Spring `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`（专用线程池），不引入 MQ |
| 推送失败处理 | **异步 + 记日志，不自动重试**；失败恢复靠「手动全量推送」对齐下游 |
| 对接系统配置 | **运行时 CRUD**（新表 + 管理页） |
| 方向模型 | 一个系统可「只推 / 只收 / 双向」，由 `push_enabled`、`receive_enabled` 两个开关组合 |
| 类别范围 | **一张共享子表** `integration_system_category`，**推送与接收共用同一套类别范围** |
| 类别范围语义 | **精确匹配**（非子树）：命中 = `category_id ∈ 该系统类别集` |
| 空间推送范围 | **推给所有启用推送的系统**（全量，恒定） |
| 类别推送范围 | 按系统类别集过滤（精确，仅推集合内的类别本身，靠 `full_name` 保留路径） |
| 设备推送范围 | 按系统类别集过滤（`device.category_id ∈ 集合`） |
| 手动全量含义 | 全量=快照模式，**过滤规则不变**（推「该系统该拿的全量快照」，非全库设备） |
| 接收匹配键 | **按主键 id 匹配**（id 全局唯一）：存在→更新，不存在→用该 id 新增 |
| 接收批处理 | **尽力而为**：逐设备通过/丢弃+记原因，单条坏数据不连累整批 |
| Hub 分发 | **做 hub**：A 推来的设备按类别分发给 B 等；通过事件带 `excludeSystemCode=来源` 排除回推来源，环路靠幂等兜底 |
| 手动重推 | **不做**（YAGNI），失败恢复用全量推送兜底 |
| 对接日志 payload | **保留原始报文 JSON**，仅作审计/排错 |

---

## 3. 总体架构与拓扑

```
            ┌─────────────────── 本模块 (sgai-master) ───────────────────┐
            │  类别/空间树 (主源, 只出不进)    设备 (双向, id 全局唯一)      │
            └──┬─────────────────────────────────┬──────────────────────┘
   推:空间全量(给所有)  推:类别/设备(按系统类别集过滤)   收:设备(按系统类别集过滤+hub分发)
   ─────────────────▶ 系统A ─────────────────────▶ ────────────────────◀
   ─────────────────▶ 系统B ─────────────────────▶ ────────────────────◀
        (对接系统运行时CRUD: 名称/编码/推送URL/方向开关/类别范围/令牌)
```

- 类别/空间是**主源**，只推送不接收。
- 设备**双向**：实时增量（主数据增删改触发）+ 手动全量（按钮触发）出；接收端按 id upsert 入库并 hub 分发。
- 推送、接收共用：对接系统配置、HTTP 客户端（hutool `HttpUtil`）、对接日志表。

---

## 4. 数据库表设计（DDL）

> 由用户在目标库手动执行，**不自动跑迁移**。

```sql
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
  UNIQUE KEY uk_receive_token (receive_token)   -- MySQL 允许多个 NULL,未启用接收的系统互不冲突
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

### 关键约定
- 主键：insert 前用 hutool `IdUtil.simpleUUID()` 生成 32 位无横线 uuid，前端不传 id。
- `receive_token` 唯一：靠它从入站请求反查来源系统；MySQL 唯一键允许多 NULL，未启用接收的系统（无 token）互不冲突。
- `integration_log.system_code` 冗余：对接系统删除后日志仍可追溯（删除规则见 5.1）。

---

## 5. 后端设计

### 5.1 对接系统配置

**实体 `IntegrationSystem`**（字段同表）。**`IntegrationSystemCategory`**（id / system_id / category_id）。

**接口端点** `/master/integrationSystem`（继承 `JeecgController`，返回 `Result<T>`）：

| 方法 | 端点 | 说明 |
|---|---|---|
| GET | `/list?name=&code=` | 扁平分页/全量列表，支持名称、编码筛选 |
| GET | `/{id}` | 详情（含类别范围 categoryIds） |
| POST | `/` | 新增（body: name, code, pushEnabled, pushUrl, pushToken, receiveEnabled, receiveToken, remark, categoryIds[]） |
| PUT | `/` | 编辑（同上；类别范围整体覆盖子表） |
| DELETE | `/{id}` | 删除（须先停用，见下） |
| POST | `/{id}/push` | 手动全量推送（触发该系统 3 次快照推送） |

**Service 流程**：
- 新增/编辑：`@Transactional(rollbackFor=Exception.class)`。主表 upsert + 类别范围子表「先按 system_id 删，再批量 insert」（整体覆盖）。`code` 唯一性、`receive_token` 唯一性由表约束兜底 + Service 预查给出友好提示。
- 删除：**保守拒绝**——`push_enabled=1` 或 `receive_enabled=1` → 拒绝「请先停用该对接系统」；否则物理删主表 + 级联删 `integration_system_category`。日志不拦截删除。

### 5.2 推送链路

#### 5.2.1 统一推送报文

POST 到 `integration_system.push_url`，请求头 `X-Integration-Token: {push_token}`、`X-Source: sgai-master`。Body：

```jsonc
{
  "source": "sgai-master",
  "systemCode": "A",
  "type": "DEVICE | CATEGORY | SPACE",
  "op":    "UPSERT | DELETE | SNAPSHOT",   // 实时增量=UPSERT/DELETE；手动全量=SNAPSHOT
  "batchId": "uuid",                       // 一次推送一个，日志关联/下游去重
  "data": [
    // 设备: { id, name, categoryId, spaceId, remark }
    // 类别: { id, name, fullName, pid }
    // 空间: { id, name, fullName, pid }
  ]
}
```

#### 5.2.2 实时增量（事务提交后异步）

**事件**：通用 `MasterDataChangeEvent`，携带 `{entityType(CATEGORY/SPACE/DEVICE), op(CREATE/UPDATE/DELETE), affectedEntities, excludeSystemCode?}`。在三个现有 Service 的增删改方法内（`@Transactional` 上下文中）发布。

> `affectedEntities` 携带**完整实体字段**（设备的 `categoryId`、类别/空间的 `id` 等），监听器据此做命中判定，无需回查库；设备 DELETE 时实体仍带删除前的 `categoryId`，便于按原类别匹配目标系统。`excludeSystemCode` 仅 hub 接收分发时填（=来源系统 code），本地写入场景为 null（fan-out 到全部命中系统）。

**监听**：`@TransactionalEventListener(phase=AFTER_COMMIT)` + `@Async`（线程池 `integrationTaskExecutor`），按类型分发：

| 触发 | 分发目标 | op |
|---|---|---|
| 设备 C/U | `push_enabled=1` 且类别集含 `device.category_id` 的系统（排除 `excludeSystemCode`） | UPSERT 该设备 |
| 设备 DELETE | 类别集含「该设备原 category_id」的系统（排除 `excludeSystemCode`） | DELETE 该设备 id |
| 类别 C/U | 类别集含 `category.id` 的系统（排除 `excludeSystemCode`，精确只它自己） | UPSERT 该类别 |
| 类别 DELETE | 类别集含 `category.id` 的系统（排除 `excludeSystemCode`） | DELETE 该类别 id |
| 空间 C/U/D | **所有** `push_enabled=1` 系统（排除 `excludeSystemCode`） | UPSERT/DELETE 受影响空间节点（改名/移动致子树全称变更时，把每个受影响节点都推 UPSERT） |

> 批量操作（设备批量删、`importExcel`）发布**一个聚合事件**（affectedEntities 含全部受影响实体），避免事件风暴。

**HTTP 执行**：hutool `HttpUtil`；响应 2xx 记 SUCCESS 日志，否则/超时/异常记 FAIL 日志（含错误信息），**不自动重试**。日志写入独立小事务，与主数据写入彻底隔离。

#### 5.2.3 手动全量（按钮触发）

`POST /master/integrationSystem/{id}/push` → 对该系统发 **3 次请求**（每种数据类型一次，op=SNAPSHOT，各自独立记日志）：
- 空间：全量空间列表（空间恒全量）。
- 类别：该系统类别集内的全部类别。
- 设备：`category_id ∈ 该系统类别集` 的全部设备。

> 手动推送为**同步**调用（用户点按钮需即时反馈），三次 HTTP 顺序执行，每次带超时（hutool `HttpUtil` 设置 timeout），返回 3 条结果（每条 SUCCESS/FAIL）；失败同样记日志。下游离/超时不阻塞过久（受 timeout 约束）。

**失败恢复**：实时增量失败后，对该系统点「推送」全量快照即可把下游对齐到当前状态，等价补推。

### 5.3 接收链路（只收设备）

#### 5.3.1 入口与鉴权

`POST /master/integration/receive`（对外部系统开放，**排除平台登录拦截**，改用令牌鉴权；匿名放行方式实现时对照平台已有「第三方登录」开放接口 `sgai-module-third` 的 `ThirdSystemLoginController` 模式对齐）。

请求头 `X-Integration-Token: {token}` → 按 `receive_token`（唯一）反查 `integration_system`：
- 查无 / `receive_enabled=0` → 记 FAIL 日志（error=鉴权失败）+ 返回 401。

报文与推送同构（`type` 只认 `DEVICE`）：
```jsonc
{ "source": "<外部系统标识>", "type": "DEVICE",
  "op": "UPSERT | DELETE | SNAPSHOT", "batchId": "uuid",
  "data": [ { id, name, categoryId, spaceId, remark }, ... ] }
```

#### 5.3.2 逐设备处理（尽力而为）

每条设备独立判定，通过的入库，不通过的跳过+记原因，最后回汇总。**逐条独立写入，无包裹大事务**，保证「单条失败不影响其他条」。具体：接收 Service 遍历 data，对每台设备调用 `deviceService.upsertFromIntegration(oneDevice)`（**该方法本身 `@Transactional`，单台一个事务**）；某台抛异常则 catch → 记拒绝原因 → 继续下一台（该台事务回滚、不发 hub 事件，行为正确）。这样既保持逐台独立，又让 hub 的 `AFTER_COMMIT` 事件在每台提交后可靠触发。判定顺序：

1. **类别过滤（精确匹配）**：`categoryId` 不在该系统类别集 → 丢弃「类别不在允许范围」。
2. **引用校验**：`categoryId`/`spaceId` 本地必须存在（沿用主数据 NOT NULL + 必须存在规则）→ 不存在丢弃「类别/空间不存在」。
3. **按 id upsert**（走专用方法 `deviceService.upsertFromIntegration(...)`，**不**复用会发推送事件的普通 save/update）：
   - id 已存在 → 更新(name/categoryId/spaceId/remark)。
   - id 不存在 → 用该 id 新增。
4. **名称冲突**：incoming `name` 撞到**别的 id** 的已有设备（违反 uk_name）→ 丢弃「设备名称冲突」。

> **DELETE op**：data 中每条仍是完整设备项（含 `categoryId`），故过滤与分发判定与 UPSERT 完全一致——先用 payload 的 `categoryId` 做类别过滤（精确）→ 命中则按 id 物理删本地设备 → 并 hub 分发 DELETE 给「类别集含该 categoryId 且 ≠ 来源」的系统（排除来源，幂等）。这样即便设备本地已不存在，仍可凭 payload 的 `categoryId` 正确判定目标系统。

#### 5.3.3 Hub 分发

本地入库成功后，对该设备发布 `MasterDataChangeEvent`（entityType=DEVICE，op 对应），**带 `excludeSystemCode = 来源系统 code`**。异步监听器分发到「`push_enabled=1` 且类别集含该设备 category_id 且 ≠ 来源系统」的系统。

- **环路安全靠幂等**：upsert 按 id（内容相同即无副作用）、DELETE 按 id 幂等。我们只在自己这一跳排除直接来源；更深层环路即使发生也不产生重复数据，仅多余流量（可接受）。
- 分发走异步，接收接口本身只等本地入库即快速返回。

#### 5.3.4 响应

`Result` 回 `{ batchId, accepted: n, rejected: [{id, reason}] }`，让调用方知道哪些落库。接收结果记一条 RECEIVE 日志（status：全成功 SUCCESS / 有丢弃 PARTIAL / 鉴权或解析失败 FAIL，`error` 列拒绝明细）。

### 5.4 对接日志

**实体 `IntegrationLog`**（字段同表）。

**接口端点** `/master/integrationLog`：

| 方法 | 端点 | 说明 |
|---|---|---|
| GET | `/list?direction=&systemId=&type=&status=` | 分页列表，支持方向/系统/类型/状态筛选 |
| GET | `/{id}` | 详情（含 payload 原文，仅审计查看） |

日志为只读审计，不提供编辑/删除/重推接口（YAGNI）。`create_by`：实时/自动场景写 `system` 或来源 systemCode，手动推送写操作人。

### 5.5 代码组织（遵循「按层分包、不拆子目录」）

集成相关类直接加进现有各层包：

```
org.jeecg.module.master
├── controller
│   ├── IntegrationSystemController      对接系统 CRUD + 手动推送
│   ├── IntegrationReceiveController     接收入口
│   └── IntegrationLogController         日志查询
├── service
│   ├── IIntegrationSystemService        对接系统 CRUD
│   ├── IIntegrationPushService          推送（报文组装 + HTTP + 日志）
│   ├── IIntegrationReceiveService       接收（鉴权 + 过滤 + upsert + hub）
│   └── impl/...
├── mapper                               仅 extends BaseMapper<T>，无自定义方法、无 xml
│   ├── IntegrationSystemMapper
│   ├── IntegrationSystemCategoryMapper
│   └── IntegrationLogMapper
├── entity
│   ├── IntegrationSystem
│   ├── IntegrationSystemCategory
│   └── IntegrationLog
├── vo
│   ├── IntegrationPayload               推送/接收统一报文
│   ├── DevicePushItem / CategoryPushItem / SpacePushItem
│   └── ReceiveResult                    { batchId, accepted, rejected[] }
└── common
    ├── MasterDataChangeEvent            通用变更事件（含 excludeSystemCode）
    ├── CategoryScopeResolver            纯算法：精确匹配命中判定（系统×类别集 → 命中系统集）
    └── PushPayloadBuilder               组装统一报文
```

**对现有代码的改动**（仅必要点，不顺手改无关）：
- `DeviceCategoryServiceImpl` / `SpaceServiceImpl` / `DeviceServiceImpl` 的增删改方法：在事务内发布 `MasterDataChangeEvent`（设备 `importExcel`/批量删发聚合事件）。
- 这些方法需处于 `@Transactional` 上下文以保证 `AFTER_COMMIT` 可靠触发（多数已是；单条 add/delete 补 `@Transactional`，开销可忽略）。

### 5.6 校验类查询（全 wrapper）

| 校验 | wrapper |
|---|---|
| 系统编码唯一 | `selectCount(eq(code).ne(id!=null, id))` |
| 接收令牌唯一 | `selectCount(eq(receiveToken).ne(id!=null, id))` |
| 删除前停用 | `push_enabled=0 AND receive_enabled=0` 否则拒绝 |
| 设备类别命中系统集 | 内存：预载 `integration_system_category` 按 category_id 分组，O(1) 查命中系统 |
| 接收类别过滤 | 同上：来源系统类别集（精确）含 categoryId |
| 接收引用存在 | `categoryMapper.selectById` / `spaceMapper.selectById`（或 selectBatchIds 批量）|
| 接收按 id upsert | `deviceMapper.selectById` 判存在 → updateById / insert |

> 实时推送 fan-out 时，监听器**一次性预载**所有 `push_enabled=1` 系统及其类别范围到内存（对接系统数量小），再按变更实体的 category_id 精确匹配命中系统集，避免逐系统查库。

---

## 6. 前端设计（`fwbz-web/src/views/master/`，复用现有模式）

### 6.1 目录结构
```
master/
├── integrationSystem/                 对接系统管理
│   ├── index.vue                      列表页（含「推送」行按钮）
│   ├── integrationSystem.api.ts
│   ├── integrationSystem.data.ts
│   └── components/IntegrationSystemModal.vue   新增/编辑（含类别范围多选）
└── integrationLog/                    对接日志
    ├── index.vue                      列表页（筛选 + 查看原文抽屉）
    ├── integrationLog.api.ts
    └── integrationLog.data.ts
```
参考既有页：列表+弹窗参考 `views/master/device/`；类别范围多选用类别树 TreeSelect（多选/勾选，精确选取）。

### 6.2 对接系统页
- 列：名称、编码、推送(开关状态)、接收(开关状态)、推送URL、创建时间、操作（编辑/推送/删除）。
- 搜索区：名称、编码。
- `IntegrationSystemModal.vue`：字段 `id(隐藏) / name(必填) / code(必填) / pushEnabled(switch) / pushUrl / pushToken / receiveEnabled(switch) / receiveToken / categoryIds(类别树 TreeSelect 多选,必填) / remark`。
- 行按钮「推送」→ `POST /integrationSystem/{id}/push`，二次确认；后端返回 3 条推送结果，前端 message 汇总。
- 删除：popConfirm；后端拒绝（未停用）时由后端 message 提示。

### 6.3 对接日志页
- 列：方向、系统、类型、操作、批次号、条数、状态、耗时、创建时间。
- 搜索区：方向、系统（下拉）、类型、状态、时间范围。
- 行点击 → 抽屉展示 `payload` 原文（JSON 美化）+ error 明细。只读，无操作按钮。

### 6.4 菜单与路由
jeecg 动态菜单新增两条：「对接系统」组件 `master/integrationSystem/index`、「对接日志」组件 `master/integrationLog/index`。

---

## 7. 错误处理与事务

- 业务校验失败统一抛 `JeecgBootException("文案")`，由全局异常处理器转失败 `Result`。
- 推送失败/超时 → 记 FAIL 日志，**不自动重试**，不冒泡打断主数据写入（异步、`AFTER_COMMIT` 已隔离）。
- 接收逐条丢弃 → 整批记 PARTIAL（`error` 列逐条拒绝明细）。
- 接收鉴权失败 → 记 FAIL（error=鉴权失败）+ 返回 401。
- 事务边界：
  - 主数据写入：沿用现有 `@Transactional`；事件 `AFTER_COMMIT` 触发，推的是已落库数据。
  - 对接系统新增/编辑、类别范围覆盖：`@Transactional`。
  - 推送日志写入：独立小事务/自动提交，**绝不**并入主数据事务。
  - 接收：逐设备独立写入，无包裹大事务（尽力而为）。
  - Hub 分发：异步，在接收逐条提交之后。

| 场景 | 提示文案 |
|---|---|
| 系统编码重复 | 系统编码已存在 |
| 接收令牌重复 | 接收令牌已存在 |
| 删除未停用的系统 | 请先停用该对接系统 |
| 接收类别不在范围 | 类别不在允许范围 |
| 接收引用不存在 | 类别/空间不存在 |
| 接收名称冲突 | 设备名称冲突 |
| 接收鉴权失败 | 对接令牌无效或接收未启用 |

---

## 8. 测试策略

- **纯算法单测（无 Spring）**：
  - `CategoryScopeResolver`：精确匹配命中判定（给定变更实体 category_id + 各系统类别集 → 命中系统集）。
  - `PushPayloadBuilder`：三类报文字段组装正确（设备/类别/空间字段、op、batchId）。
- **Service 级（Mockito）**：
  - 现有 Service 增删改后 `MasterDataChangeEvent` 确在 `AFTER_COMMIT` 发布。
  - 接收尽力而为：混合合法/非法设备 → 正确子集入库 + 正确拒绝原因。
  - Hub 分发：接收自 A（`excludeSystemCode=A`）→ 分发事件排除 A、命中其他系统。
  - mock hutool `HttpUtil` 响应 → SUCCESS/FAIL 日志正确。
- **前端**：jeecg 惯例手测（对接系统 CRUD + 类别范围 + 推送按钮、日志筛选与原文查看、接收端到端联调）。

> 后端测试因父 pom `skipTests=true`，用 IDE 运行（见 MEMORY）。

---

## 9. 实施顺序（核心链路最小可用优先）

1. 建表 DDL（用户执行 `docs/sql/2026-07-08-data-integration-ddl.sql`）。
2. 后端 common：`MasterDataChangeEvent`、`CategoryScopeResolver`（+ 单测）、`PushPayloadBuilder`。
3. 后端对接系统：entity → mapper → service → controller（CRUD + 类别范围覆盖 + 删除停用校验）。
4. 后端推送：`IIntegrationPushService`（报文组装 + HTTP + 日志）；现有三个 Service 接入事件发布；`@Async` 监听器 + 线程池。
5. 后端接收：`IntegrationReceiveController` + `IIntegrationReceiveService`（鉴权 + 过滤 + upsert + hub）+ `deviceService.upsertFromIntegration`。
6. 后端日志：entity/mapper/controller 查询。
7. 前端对接系统页 + 日志页并联调。
8. 菜单配置 + 端到端验证（推送成功/失败日志、接收过滤/upsert、hub 分发）。

---

## 10. 未纳入范围（YAGNI）

- 手动重推（失败恢复用全量推送兜底）。
- 自动重试 / 可靠 MQ / 死信队列（容错策略为「记日志不重试」）。
- 类别范围的子树语义（仅精确匹配）。
- 推送/接收类别范围分离（共用一张子表）。
- 接收类别/空间（仅接收设备；类别/空间是主源只出不进）。
- 日志自动清理/归档。
- 设备外部编码字段（id 即跨系统键）。
- Feign api 子模块。
- mapper xml（全 wrapper + 内存组装）。
