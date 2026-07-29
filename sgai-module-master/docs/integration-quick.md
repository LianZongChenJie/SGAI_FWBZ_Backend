# 主数据对接 · 速查（精简版）

> 完整说明见 `integration-guide.md`。本文只列对接必需的最少信息。
> 对接前需在本模块管理后台配置对接系统，获取：**系统编码 code、共享令牌 token**（推送+接收共用一个）、**类别范围**（精确选定的类别 id 集合）；接收方还需提供 **pushUrl**。

## 一句话

- **id（32 位 uuid）是所有系统共用的全局身份**，推送/接收都带 id，按 id 落库（存在更新、不存在新增）。设备无需外部编码字段。
- 设备**双向**；类别/空间**只由本模块推给第三方，不接受反向**。
- **类别范围是精确匹配**：只处理 `categoryId ∈ 范围集合` 的设备/类别，不级联子类别。

---

## 接口 1：第三方 → 本模块（接收设备）

```
POST {BASE_URL}/master/integration/receive
Content-Type: application/json
X-Integration-Token: {token}
```
```jsonc
{ "source": "fwbz", "type": "DEVICE", "op": "UPSERT",
  "batchId": "批次uuid",
  "data": [ { "id": "uuid", "name": "1号冷水机组", "categoryId": "uuid", "spaceId": "uuid", "remark": "" } ] }
```
**响应 200**：`{ result: { batchId, accepted: n, rejected: [{id, reason}] } }`（逐条独立，单条失败不连累整批）
**鉴权失败**：HTTP 401（令牌无效/未启用接收）
**只认 type=DEVICE**；每台过：类别范围→引用存在→按 id upsert→名称冲突检查。

## 接口 2：本模块 → 第三方（推送）

本模块 POST 到第三方配置的 `pushUrl`：
```
POST {pushUrl}
X-Integration-Token: {token}
X-Source: sgai-master
```
```jsonc
{ "source": "sgai-master", "systemCode": "fwbz", "type": "DEVICE", "op": "UPSERT",
  "batchId": "uuid", "data": [ /* 设备/类别/空间项 */ ] }
```
**第三方返回 2xx = 成功**；否则/超时(5s)记失败，**不自动重推**（用管理后台「推送」按钮做全量补齐）。

---

## 报文字段

**设备项**（type=DEVICE）：`{ id, name, categoryId, spaceId, remark }`
**类别项**（type=CATEGORY）：`{ id, name, fullName, pid }`　fullName 路径用 `-` 连接，根 pid=`"0"`
**空间项**（type=SPACE）：`{ id, name, fullName, pid }`

| op | 含义 |
|---|---|
| UPSERT | 按 id：存在更新、不存在新增 |
| DELETE | 按 id 删（幂等，删不存在也算成功） |
| SNAPSHOT | 全量快照，按 UPSERT 处理 |

| type | data 元素 | 第三方可推给本模块？ |
|---|---|---|
| DEVICE | 设备项 | ✅ |
| CATEGORY | 类别项 | ❌（仅本模块→第三方） |
| SPACE | 空间项 | ❌（仅本模块→第三方） |

---

## 关键约定

1. **token 共用一个**：推送请求头和接收鉴权是同一个令牌。
2. **id 全局唯一**：双方共用 uuid，谁创建谁分配；不要自造编码字段。
3. **类别范围精确匹配**：超范围的设备会被拒（`类别不在允许范围`）。
4. **幂等**：UPSERT/DELETE 按 id 幂等，重发/分发环路不产生重复数据。
5. **失败不重推**：第三方接收异常时，本模块只记日志；补齐靠全量 SNAPSHOT。
6. **空间恒全量**：空间推给所有启用推送的系统，不按类别范围过滤。

## 常见拒绝原因（接收方）

`对接令牌无效或接收未启用`(401) · `仅支持设备接收` · `类别不在允许范围` · `类别不存在` · `空间不存在` · `设备名称冲突`
