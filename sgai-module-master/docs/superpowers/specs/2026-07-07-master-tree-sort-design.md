# 类别 / 空间 增加排序字段（sort）设计

> 日期：2026-07-07
> 范围：`sgai-module-master`（后端）+ `fwbz-web/src/views/master`（前端）
> 关联：`2026-07-06-master-data-design.md`（主数据模块基础设计）

---

## 1. 背景与目标

类别（`device_category`）与空间（`space`）两棵树当前仅按 `create_time DESC` 排序，业务无法控制节点展示顺序。本次为两棵树各增加一个 `sort` 排序字段，支持**同级内按 sort 升序**展示，并允许通过表单手动指定排序值。

两棵树结构对称，本改动对两者做**完全对称**的修改，唯一差别仅是表名/实体名/文案。

## 2. 决策摘要

| 决策点 | 结论 |
|---|---|
| 前端调序交互 | **仅表单填 sort**，列表按 sort 升序展示；不做上移/下移按钮，不做拖拽 |
| 新增节点 sort 赋值 | sort 为空 → 后端取「同 pid 下 max(sort)+1」；非空 → 用传入值 |
| 排序范围 | **同级内**（按 pid 聚合，sort 仅在同 pid 下比较） |
| 老数据 / null 处理 | **暂不处理**：DDL 加列为 NULL，不做特殊处理；MySQL `ORDER BY sort ASC` 下 NULL 视为最小值排最前，不写迁移 SQL |
| tie-breaker | 无（仅按 `sort ASC`；同 sort 值顺序不保证稳定，YAGNI） |

## 3. 数据层

### 3.1 实体

`DeviceCategory.java` / `Space.java` 各新增字段（对称）：

```java
/** 同级内排序，升序，小在前 */
private Integer sort;
```

字段名 `sort`，列名 `sort`（MyBatis-Plus 默认驼峰转下划线一致，无需额外注解）。

### 3.2 DDL（用户在目标库手动执行，不自动迁移）

新增 `docs/sql/2026-07-07-master-tree-sort.sql`：

```sql
-- 类别增加排序字段
ALTER TABLE device_category ADD COLUMN sort INT NULL COMMENT '同级内排序，升序，小在前';

-- 空间增加排序字段
ALTER TABLE space ADD COLUMN sort INT NULL COMMENT '同级内排序，升序，小在前';
```

老数据 `sort = NULL`，不做编号迁移；`ORDER BY sort ASC` 下 MySQL 将 NULL 视为最小值排最前（见 4.1），不做特殊处理。

## 4. 后端逻辑

`DeviceCategoryServiceImpl` / `SpaceServiceImpl`（对称）。

### 4.1 列表排序

`listAll` 将

```java
w.orderByDesc(DeviceCategory::getCreateTime);
```

改为（直接按 sort 升序，**不**加 ISNULL、**不**加 create_time 兜底）：

```java
w.orderByAsc(DeviceCategory::getSort);
```

> MySQL 升序下 NULL 视为最小值排最前，故老数据（NULL）会出现在有值节点之前 —— 有意接受，不为 NULL 做特殊处理。

### 4.2 新增节点赋值

`create` 中，当 `entity.getSort() == null` 时，调用 `nextSort(pid)` 自动赋值；非空则保留传入值：

```java
if (entity.getSort() == null) {
    entity.setSort(nextSort(entity.getPid()));
}
```

`nextSort`：取同 pid 下、sort 非 null 的最大值 +1（同 pid 无有效节点则为 1）：

```java
private Integer nextSort(String pid) {
    DeviceCategory max = baseMapper.selectOne(new LambdaQueryWrapper<DeviceCategory>()
            .eq(DeviceCategory::getPid, pid)
            .isNotNull(DeviceCategory::getSort)
            .orderByDesc(DeviceCategory::getSort)
            .last("LIMIT 1"));
    return max == null ? 1 : max.getSort() + 1;
}
```

> Space 版把实体与泛型换成 `Space` 即可。符合「零 mapper xml、用 LambdaQueryWrapper」硬约束。

### 4.3 更新节点（updateNode）

`sort` 跟随表单透传，由 `updateById` 整体更新。规则：

- 提交的 sort 非空 → 更新为新值；
- 提交的 sort 为空 → 保持原值（不主动重算）；
- 移动 pid（改上级）时 **不主动重算** sort，保持原值；同 pid 下若出现 sort 重复，顺序不保证（无 tie-breaker）。

> YAGNI：移动是低频操作，不为此引入同级重排逻辑。
>
> 实现细节：MyBatis-Plus 默认 `FieldStrategy.NOT_NULL`，`updateById` 时 sort 为 null 的字段不会拼入 UPDATE SQL，因此「为空保持原值」天然成立，无需额外判空处理。

### 4.4 Controller

无改动。sort 随实体自动透传，`POST`/`PUT` 接口签名不变。

## 5. 前端（`fwbz-web/src/views/master/`）

### 5.1 `category.data.ts` / `space.data.ts`

- `columns` 增加一列（建议置于「名称」之后）：

```ts
{ title: '排序', dataIndex: 'sort', width: 80, align: 'center' },
```

- `formSchema` 增加一项（非必填）：

```ts
{ label: '排序', field: 'sort', component: 'InputNumber', componentProps: { placeholder: '留空则自动排到同级末尾' } },
```

### 5.2 无需改动的文件

- `index.vue`：`utils/tree.ts` 的 `listToTree`（tree.ts:15-28）按扁平数组顺序 push 到 `parent.children`，**完全保序**。后端按 sort 升序返回扁平列表 → 前端树按 sort 升序展示，无需改前端排序逻辑。
- `CategoryModal.vue` / `SpaceModal.vue`：复用 `formSchema`，sort 自动随表单回填/提交。
- `*.api.ts`：接口不变。

## 6. 边界与不涉及

- 设备（`device`）**不**增加排序字段。
- 设备导入按「类别/空间全称」定位，**不涉及** sort。
- **不做** 拖拽排序、上移/下移按钮（YAGNI，后续如需要再扩展）。
- **不** 改菜单、**不** 改其他模块。
- 老数据不编号迁移，不处理 NULL（`ORDER BY sort ASC` 下 NULL 排最前，有意接受）。

## 7. 验证点

- 后端
  - `listAll` 生成的 SQL 为 `ORDER BY sort ASC`，NULL 行排最前；
  - 新增 sort 为空时自动 = 同 pid max(sort)+1，同 pid 首个节点为 1；
  - 新增 sort 非空时保留传入值；
  - 编辑改名/移动时 sort 透传，为空保持原值。
- 前端
  - 树按 sort 升序展示，同级内空 sort（NULL）节点排最前；
  - 表单「排序」可选填，编辑时正确回填当前 sort。

## 8. 文件改动清单（实现时按此最小集）

后端：
- `entity/DeviceCategory.java`、`entity/Space.java`（+字段）
- `service/impl/DeviceCategoryServiceImpl.java`、`service/impl/SpaceServiceImpl.java`（listAll 排序、create 赋值、nextSort）
- `docs/sql/2026-07-07-master-tree-sort.sql`（新增）

前端：
- `category/category.data.ts`、`space/space.data.ts`（columns + formSchema）
