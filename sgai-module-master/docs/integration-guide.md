# 主数据对接 API 文档（供第三方系统对接）

> 面向与「主数据管理（sgai-master）」模块对接的第三方系统开发人员。本文档描述双方如何互推主数据（类别 / 空间 / 设备），包含接口、报文、鉴权、字段、示例与错误处理。

---

## 1. 概述

本模块管理三类主数据：**类别（树形）、空间（树形）、设备（平铺）**。对接指本模块与第三方系统之间按约定互相同步主数据：

| 方向 | 说明 | 谁实现接口 |
|---|---|---|
| **本模块 → 第三方（推送）** | 本模块的主数据增删改实时（及手动全量）推送给第三方 | 第三方提供一个 HTTP 接收端点 |
| **第三方 → 本模块（接收）** | 第三方把设备推给本模块入库 | 本模块提供接收接口 |

一个对接系统可配置为「只推 / 只收 / 双向」。

> 类别、空间是**主源**，只由本模块产生并**只出不进**（本模块推给第三方，不接受第三方推类别/空间）。设备**双向**。

---

## 2. 对接前约定

对接前，双方需在本模块管理后台配置一个「对接系统」，并约定以下信息：

| 约定项 | 说明 |
|---|---|
| **系统编码（code）** | 第三方系统的唯一编码（如 `fwbz`），用于日志追溯 |
| **共享令牌（token）** | 双方约定的密钥，**推送与接收共用同一个**：本模块推给第三方时放在请求头；第三方推给本模块时也放在请求头 |
| **推送 URL（pushUrl）** | 第三方接收本模块推送的 HTTP 地址（仅「启用推送」时需要） |
| **类别范围（categoryIds）** | 精确指定该对接关系涉及的**类别集合**（一组类别 id）。推送与接收都只处理「类别落在这个集合内」的设备/类别 |

> 类别范围是**精确匹配**（不是子树）。例如选中「电气」类别，只匹配 `categoryId = 电气id` 的设备，不自动包含其子类别下的设备。需要哪些类别，就在范围里精确勾选哪些。

---

## 3. 身份模型（重要）

**id 是所有系统共用的全局身份，由 32 位无横线 uuid 表示。**

- 谁创建实体谁分配 uuid，所有系统共用同一个 id。
- 因此推送/接收都**带 id**，接收端按 id 匹配：存在则更新、不存在则用该 id 新增。
- **设备无需额外的「外部编码」字段**——id 即跨系统键。
- 设备名称在本模块内全局唯一；若接收到的设备名与本模块中**别的 id** 的设备重名，会被拒绝（见 §7）。

---

## 4. 第三方 → 本模块（接收接口）

第三方把设备推送给本模块入库（并可经由本模块 hub 分发给其他对接系统）。

### 4.1 请求

```
POST {BASE_URL}/master/integration/receive
Content-Type: application/json
X-Integration-Token: {共享令牌}
```

> `{BASE_URL}` 为本模块对外地址（经网关/模块前缀），由运维提供。该接口**排除平台登录拦截**，仅用 `X-Integration-Token` 鉴权。

### 4.2 请求体（ReceivePayload）

```jsonc
{
  "source": "fwbz",              // 来源系统标识（第三方自填，便于追溯）
  "systemCode": "fwbz",          // 可选；本模块按 token 反查系统，可不填
  "type": "DEVICE",              // 接收只认 DEVICE（类别/空间不接受）
  "op": "UPSERT",                // UPSERT | DELETE | SNAPSHOT
  "batchId": "9f3c...uuid",      // 本批次唯一 id，便于双方对账/去重
  "data": [
    { "id": "...uuid", "name": "1号冷水机组", "categoryId": "...uuid", "spaceId": "...uuid", "remark": "" }
  ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| source | string | 来源系统标识 |
| systemCode | string | 可选，来源系统编码 |
| type | string | 固定 `DEVICE`（接收端不处理 CATEGORY/SPACE） |
| op | string | `UPSERT`（新增或更新）/ `DELETE`（按 id 删）/ `SNAPSHOT`（等同 UPSERT 处理） |
| batchId | string | 批次 id，必填 |
| data | DevicePushItem[] | 设备条目数组，见 §6.1 |

### 4.3 鉴权

请求头 `X-Integration-Token` 必须等于本模块为该对接系统配置的**共享令牌**，且该系统「启用接收」。

- 令牌无效 / 未启用接收 → 返回 **HTTP 401**：
```json
{ "success": false, "message": "对接令牌无效或接收未启用", "code": 401, "result": null }
```

### 4.4 处理逻辑（逐设备，尽力而为）

本模块对 `data` 中**每台设备独立处理**，单条失败不影响其他条目。每台按顺序：

1. **类别过滤（精确匹配）**：`categoryId` 必须在该对接系统的「类别范围」内，否则丢弃（原因：`类别不在允许范围`）。
2. **引用校验**：`categoryId`、`spaceId` 必须在本模块存在，否则丢弃（原因：`类别不存在` / `空间不存在`）。
3. **按 id 落库**：
   - `op = UPSERT/SNAPSHOT`：该 id 已存在 → 更新 name/categoryId/spaceId/remark；不存在 → 用该 id 新增。
   - `op = DELETE`：按 id 物理删除本地设备（本地不存在也视为成功）。
4. **名称冲突**：`UPSERT` 时若 name 撞到**别的 id** 的已有设备 → 丢弃（原因：`设备名称冲突`）。

> 入库成功后，本模块会以 hub 身份把该设备**异步分发**给其他启用了推送、且类别范围命中的对接系统（排除来源系统本身，避免回推）。

### 4.5 响应（HTTP 200）

```jsonc
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "batchId": "9f3c...uuid",
    "accepted": 2,                // 成功落库条数
    "rejected": [                 // 被丢弃的条目及原因
      { "id": "...uuid", "reason": "类别不在允许范围" }
    ]
  }
}
```

状态判定（本模块记日志）：全部成功 `SUCCESS`，有丢弃 `PARTIAL`，鉴权/解析失败 `FAIL`。

### 4.6 接收示例

新增/更新两台、丢弃一台：
```jsonc
// 请求
POST /master/integration/receive
X-Integration-Token: fwbz010101

{
  "source": "fwbz",
  "type": "DEVICE",
  "op": "UPSERT",
  "batchId": "batch-20260709-001",
  "data": [
    { "id": "d1...uuid", "name": "1号冷水机组", "categoryId": "c1...uuid", "spaceId": "s1...uuid", "remark": "" },
    { "id": "d2...uuid", "name": "2号冷水机组", "categoryId": "c1...uuid", "spaceId": "s1...uuid", "remark": "" },
    { "id": "d3...uuid", "name": "某仪表",     "categoryId": "cX...uuid", "spaceId": "s1...uuid", "remark": "" }
  ]
}

// 响应（d3 的类别 cX 不在范围 → 被拒）
{
  "success": true, "code": 200,
  "result": { "batchId": "batch-20260709-001", "accepted": 2,
    "rejected": [ { "id": "d3...uuid", "reason": "类别不在允许范围" } ] }
}
```

删除设备（按 id，data 仍带完整条目以便过滤判定）：
```jsonc
{ "source": "fwbz", "type": "DEVICE", "op": "DELETE", "batchId": "batch-del-001",
  "data": [ { "id": "d1...uuid", "name": "1号冷水机组", "categoryId": "c1...uuid", "spaceId": "s1...uuid" } ] }
```

---

## 5. 本模块 → 第三方（推送）

本模块把主数据推送到第三方在管理后台配置的 `pushUrl`。第三方需实现一个 HTTP 接收端点接收下列请求。

### 5.1 请求（由本模块发出）

```
POST {第三方配置的 pushUrl}
Content-Type: application/json
X-Integration-Token: {共享令牌}
X-Source: sgai-master
```

### 5.2 请求体（IntegrationPayload）

```jsonc
{
  "source": "sgai-master",
  "systemCode": "fwbz",          // 接收方（第三方）的系统编码
  "type": "DEVICE",              // DEVICE | CATEGORY | SPACE
  "op": "UPSERT",                // UPSERT | DELETE | SNAPSHOT
  "batchId": "...uuid",          // 每次推送一个新 batchId
  "data": [ /* DevicePushItem | CategoryPushItem | SpacePushItem，按 type */ ]
}
```

### 5.3 第三方如何判定成功

- 返回 **HTTP 2xx** → 本模块记为成功。
- 返回非 2xx、连接异常、超时 → 本模块记为失败。
- **失败不会自动重推**。修复后可由本模块运维对该系统点「手动全量推送」补齐（见 §5.5）。
- 本模块请求**超时 5 秒**。

### 5.4 触发时机

| 场景 | type | op | 推送范围 |
|---|---|---|---|
| 设备 新增/编辑 | DEVICE | UPSERT | 类别范围命中该设备 category_id 的系统 |
| 设备 删除 | DEVICE | DELETE | 同上（带删除前 categoryId 供过滤） |
| 类别 新增/编辑/改名/移动 | CATEGORY | UPSERT | 类别范围含该类别 id 的系统（精确，仅该类别本身；全称随路径携带在 fullName） |
| 类别 删除 | CATEGORY | DELETE | 同上 |
| 空间 新增/编辑/改名/移动/删除 | SPACE | UPSERT / DELETE | **所有启用推送的系统**（空间恒全量） |
| 手动全量推送 | DEVICE/CATEGORY/SPACE | SNAPSHOT | 该系统该拿的全量快照（空间全量、类别按范围、设备按范围） |

> 批量操作（如批量删除设备、Excel 批量导入）合并为**一个聚合批次**推送，避免报文风暴。

### 5.5 手动全量（SNAPSHOT）

本模块管理后台「对接系统」列表行有「推送」按钮，点击后向第三方发 **3 次快照请求**（空间全量、类别按范围、设备按范围），每次独立记日志。用于初始同步或失败后补齐。第三方对 SNAPSHOT 的处理与 UPSERT 一致（按 id 落库）。

---

## 6. 数据结构

### 6.1 设备 DevicePushItem（type = DEVICE）
```jsonc
{ "id": "...uuid", "name": "1号冷水机组", "categoryId": "...uuid", "spaceId": "...uuid", "remark": "" }
```

### 6.2 类别 CategoryPushItem（type = CATEGORY）
```jsonc
{ "id": "...uuid", "name": "电气", "fullName": "建筑-电气", "pid": "...uuid 或 0" }
```
> `fullName` 为从根到本节点的全称，分隔符 `-`；根节点 `pid = "0"`、`fullName = name` 自身。

### 6.3 空间 SpacePushItem（type = SPACE）
```jsonc
{ "id": "...uuid", "name": "一楼", "fullName": "园区-一楼", "pid": "...uuid 或 0" }
```

---

## 7. 错误与边界

| 场景 | 行为 |
|---|---|
| 令牌无效 / 接收未启用 | 接收接口返回 401 |
| 接收报文 type 非 DEVICE | 该批所有条目被拒（原因 `仅支持设备接收`） |
| 接收条目 categoryId 不在类别范围 | 拒，原因 `类别不在允许范围` |
| 接收条目 categoryId/spaceId 本模块不存在 | 拒，原因 `类别不存在` / `空间不存在` |
| 接收条目名称撞别的 id 的设备 | 拒，原因 `设备名称冲突` |
| 第三方推送接收返回非 2xx / 超时 | 本模块记失败日志，不自动重推 |
| 同一批次部分好部分坏 | 好的入库，坏的进 `rejected`，整体 `PARTIAL` |

**幂等性**：
- `UPSERT` 按 id 幂等（相同内容重复推送无副作用）。
- `DELETE` 按 id 幂等（删不存在的 id 也算成功）。
- 因此网络重发、hub 分发环路都不会产生重复数据，只会产生多余流量。

---

## 8. 对接清单（第三方自查）

- [ ] 向本模块运维获取：系统编码、共享令牌、（若我方接收）类别范围、（若我方推送）我方 pushUrl。
- [ ] 若**接收本模块推送**：实现一个 POST 接收端点，校验 `X-Integration-Token`、解析 IntegrationPayload、按 id 落库（2xx 视为成功），处理 UPSERT/DELETE/SNAPSHOT。
- [ ] 若**推送给本模块**：调用 `POST {BASE_URL}/master/integration/receive`，请求头带 `X-Integration-Token`，body 为 ReceivePayload（type=DEVICE），按 `result.accepted/rejected` 处理丢弃。
- [ ] 约定双方共用的设备/类别/空间 id（uuid），确保 id 一致。
- [ ] 注意：类别范围是精确匹配，超范围的设备会被拒（`类别不在允许范围`）。

---

## 附录 A：op 取值

| op | 含义 |
|---|---|
| UPSERT | 存在则更新、不存在则新增（按 id） |
| DELETE | 按 id 删除（幂等） |
| SNAPSHOT | 全量快照，处理方式等同 UPSERT |

## 附录 B：type 取值

| type | data 元素 | 接收是否支持 |
|---|---|---|
| DEVICE | DevicePushItem | ✅ |
| CATEGORY | CategoryPushItem | ❌（仅本模块→第三方） |
| SPACE | SpacePushItem | ❌（仅本模块→第三方） |

## 附录 C：典型报文

本模块推设备给第三方（实时增量）：
```jsonc
POST {pushUrl}
X-Integration-Token: fwbz010101
X-Source: sgai-master

{ "source": "sgai-master", "systemCode": "fwbz", "type": "DEVICE", "op": "UPSERT",
  "batchId": "b...uuid",
  "data": [ { "id": "d1...uuid", "name": "1号冷水机组", "categoryId": "c1...uuid", "spaceId": "s1...uuid", "remark": "" } ] }
```

第三方推设备给本模块：
```jsonc
POST {BASE_URL}/master/integration/receive
X-Integration-Token: fwbz010101

{ "source": "fwbz", "type": "DEVICE", "op": "UPSERT", "batchId": "b...uuid",
  "data": [ { "id": "d1...uuid", "name": "1号冷水机组", "categoryId": "c1...uuid", "spaceId": "s1...uuid", "remark": "" } ] }
```
