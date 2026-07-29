# 主数据模块 设计文档

- 日期：2026-07-06
- 范围：类别主数据、空间主数据、设备主数据的后端接口与前端页面
- 后端模块：`sgai-module-master`（已存在，单模块，不拆 `-api`）
- 前端目录：`fwbz-web/src/views/master/`（已存在，为空）
- 约束：执行过程中不操作 git；数据库操作使用 MyBatis-Plus（优先 wrapper，不写 mapper xml）

---

## 1. 概述

三个主数据实体：

1. **类别主数据** `device_category`：管理设备类别，树形结构。字段：主键 uuid、类别名称、类别全称、上级 id。
2. **空间主数据** `space`：管理空间数据，树形结构。字段：主键 uuid、空间名称、空间全称、上级 id。
3. **设备主数据** `device`：管理设备数据，平铺列表。字段：设备名称（系统唯一）、类别 id、空间 id、备注。

类别与空间是两棵独立的树；设备通过 `category_id`、`space_id` 引用类别与空间。

---

## 2. 关键决策摘要（澄清结论）

| 议题 | 决策 |
|---|---|
| 类别/空间全称维护 | **存储冗余字段 `full_name`**；新增/改名/移动时递归（迭代）重算并更新自身及整棵子树 |
| 全称重算实现 | **迭代 BFS + 内存拼接 + `updateBatchById`**，不使用递归方法调用 |
| 前端交互形态 | 类别/空间 = **树表格**；设备 = **筛选列表 + 弹窗表单** |
| 删除策略 | **物理删除 + 保守拒绝**：有子节点拒绝；被设备引用拒绝。表不加 `del_flag` |
| 设备字段 | 不加设备编码；**设备名称系统唯一** |
| 附加能力 | 树节点移动（改上级）、设备 Excel 导入/导出；**不建 Feign api 子模块** |
| 后端代码组织 | **组合模式**：`TreeFullNameHelper`（纯静态算法）+ 三个独立 Service；Mapper 仅继承 `BaseMapper`，**零 mapper xml**，查询全部用 MyBatis-Plus wrapper |

---

## 3. 总体架构

### 后端包结构（基于已存在的 `sgai-module-master`）

```
org.jeecg.module.master                // 按层分包（controller/service/mapper/entity），不按功能拆子目录
├── common
│   └── TreeFullNameHelper             // 纯静态算法：全称拼接、移动防环、uuid 生成
├── controller
│   ├── DeviceCategoryController
│   ├── SpaceController
│   └── DeviceController
├── service
│   ├── IDeviceCategoryService
│   ├── ISpaceService
│   ├── IDeviceService
│   └── impl
│       ├── DeviceCategoryServiceImpl
│       ├── SpaceServiceImpl
│       └── DeviceServiceImpl
├── mapper                             // 仅 extends BaseMapper<T>，无自定义方法
│   ├── DeviceCategoryMapper
│   ├── SpaceMapper
│   └── DeviceMapper
├── entity
│   ├── DeviceCategory
│   ├── Space
│   └── Device
└── vo
    └── DeviceVO                      // 设备列表联表展示用
```

> 启动类 `SgaiModuleMasterApplication` 位于上层 `org.jeecg` 包（已存在），业务代码在其子包 `org.jeecg.module.master`，可被正常扫描。需确认启动类 `@MapperScan` 覆盖 `org.jeecg.module.master.mapper`（jeecg 模板默认 `org.jeecg.**.mapper` 即满足）。

- `Mapper` 仅 `extends BaseMapper<T>`，**无自定义方法、无 xml**。
- `Service` 继承 `ServiceImpl<Mapper, T>`，业务写在 `*ServiceImpl`。
- 设备列表显示类别/空间名称：**两次查询内存拼**（先 device 分页，再 `selectBatchIds` 取本页涉及的类别/空间，组装 VO），不做联表 xml。

---

## 4. 数据库表设计（DDL）

```sql
CREATE TABLE device_category (
  id          varchar(32)  NOT NULL COMMENT '主键uuid',
  name        varchar(100) NOT NULL COMMENT '类别名称',
  full_name   varchar(500) NOT NULL COMMENT '类别全称',
  pid         varchar(32)  NOT NULL DEFAULT '0' COMMENT '上级id，根为0',
  create_by   varchar(50)  NULL COMMENT '创建人',
  create_time datetime     NULL COMMENT '创建时间',
  update_by   varchar(50)  NULL COMMENT '更新人',
  update_time datetime     NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_pid_name (pid, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='类别主数据';

CREATE TABLE space (
  id          varchar(32)  NOT NULL,
  name        varchar(100) NOT NULL COMMENT '空间名称',
  full_name   varchar(500) NOT NULL COMMENT '空间全称',
  pid         varchar(32)  NOT NULL DEFAULT '0',
  create_by   varchar(50)  NULL,
  create_time datetime     NULL,
  update_by   varchar(50)  NULL,
  update_time datetime     NULL,
  PRIMARY KEY (id),
  KEY idx_pid_name (pid, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='空间主数据';

CREATE TABLE device (
  id          varchar(32)  NOT NULL,
  name        varchar(100) NOT NULL COMMENT '设备名称',
  category_id varchar(32)  NOT NULL COMMENT '类别id（必填）',
  space_id    varchar(32)  NOT NULL COMMENT '空间id（必填）',
  remark      varchar(500) NULL COMMENT '备注',
  create_by   varchar(50)  NULL,
  create_time datetime     NULL,
  update_by   varchar(50)  NULL,
  update_time datetime     NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name),
  KEY idx_category_id (category_id),
  KEY idx_space_id (space_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备主数据';
```

### 关键约定
- **主键**：后端 insert 前用 hutool `IdUtil.simpleUUID()` 生成 32 位无横线 uuid，前端不传 id。
- **根节点 pid**：统一 `"0"`。
- **全称分隔符**：`-`；根节点全称 = 自身 name；子节点全称 = `父全称 + "-" + name`。

---

## 5. 后端设计

### 5.1 接口端点（均继承 `JeecgController`，返回 `Result<T>`）

**`DeviceCategoryController` → `/master/deviceCategory`**（SpaceController 同构 → `/master/space`）

| 方法 | 端点 | 说明 |
|---|---|---|
| GET | `/list?name=` | 返回扁平全量列表（id/name/fullName/pid），前端 buildTree。name 可选模糊 |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增（body: name, pid） |
| PUT | `/` | 编辑（body: id, name, pid）—— pid 变化即触发"移动"，无需单独接口 |
| DELETE | `/{id}` | 删除 |

**`DeviceController` → `/master/device`**

| 方法 | 端点 | 说明 |
|---|---|---|
| GET | `/list` | 分页，支持 `name` 模糊 + `categoryId` + `spaceId` 筛选；返回 `DeviceVO`（含类别/空间名称） |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/` | 编辑 |
| DELETE | `/{id}` | 单个删除 |
| DELETE | `/batch?ids=` | 批量删除 |
| GET | `/exportXls` | 导出 |
| POST | `/importExcel` | 导入 |

类别/空间不提供 Excel。

### 5.2 `TreeFullNameHelper`（纯静态算法，无 Spring）

```
buildFullName(parentFullName, name)
  // parent 为空 或 "0" → 返回 name（根）
  // 否则 → parentFullName + "-" + name

assertMovable(Set<String> subtreeIds, String newPid)
  // newPid ∈ subtreeIds → 抛 JeecgBootException（防环：不能移到自身/子级下）

generateUuid()
  // IdUtil.simpleUUID()，32 位无横线
```

### 5.3 类别 / 空间 Service 核心流程（两者对称）

**新增**：
1. `generateUuid()`。
2. 同层重名校验：`selectCount(eq(pid).eq(name))` > 0 → 抛"同级下已存在同名类别"。
3. 计算 full_name：pid="0" → name；否则取 parent.full_name，`buildFullName`。
4. insert。

**编辑 / 移动**（同一 `update`，加 `@Transactional`）：
1. 取旧记录 old。
2. 同层重名校验：`selectCount(eq(pid).eq(name).ne(id))`。
3. 若 pid 变化：
   - 迭代查出 X 的所有子孙 id（按层 `selectList(in(pid, frontier))`，循环至空）。
   - `assertMovable(subtreeIds, newPid)` 防环。
   - 校验 newPid 存在。
4. 计算 X 新 full_name。
5. **pid 或 name 变化 → 迭代重算整棵子树全称**：
   - 内存按 pid 分组，BFS 逐层 `buildFullName(父新全称, node.name)`。
   - 收集 X + 所有子孙（均为从 DB 查出的完整实体，仅 fullName 已重算），`updateBatchById` 一次性批量更新。
6. 若 pid 与 name 均未变 → 仅更新 name 等普通字段。

**删除**：
1. `selectCount(eq(pid, id))` > 0 → 拒绝"存在子级，请先删除子级"。
2. 设备引用校验：`deviceMapper.selectCount(eq(categoryId, id))` > 0（空间为 `eq(spaceId, id)`）→ 拒绝"该类别被设备引用，无法删除"。
3. 物理删除。

> 子树查询为按层迭代（循环次数 = 树深度，通常 3~6），不使用 Java 递归方法，也不依赖 SQL 递归 CTE。

### 5.4 设备 Service
- 新增/编辑：`selectCount(eq(name).ne(id!=null, id))` 校验系统唯一；categoryId/spaceId **必填且校验存在**（为空 → "请选择类别/空间"；不存在 → "所选类别/空间不存在"）。
- 删除：无下游，直接物理删（支持批量）。
- 列表：分页查 device（wrapper 筛选）→ 收集本页 categoryId/spaceId → `selectBatchIds` 取类别/空间 → 内存组装 `DeviceVO`（含 categoryName/categoryFullName/spaceName/spaceFullName）。
- 导入：校验 name 唯一 + 引用存在；失败行回传前端。

### 5.5 校验类查询（全 wrapper）

| 校验 | wrapper |
|---|---|
| 同层重名 | `selectCount(eq(pid).eq(name).ne(id!=null, id))` |
| 有子节点 | `selectCount(eq(pid, id))` |
| 设备引用类别 | `deviceMapper.selectCount(eq(categoryId, id))` |
| 设备引用空间 | `deviceMapper.selectCount(eq(spaceId, id))` |
| 设备名唯一 | `selectCount(eq(name).ne(id!=null, id))` |

---

## 6. 前端设计

### 6.1 目录结构（`fwbz-web/src/views/master/`）

```
master/
├── category/                      类别主数据
│   ├── index.vue                  树表格页
│   ├── category.api.ts            enum Api + list/save/edit/delete/loadTreeData
│   ├── category.data.ts           columns + searchFormSchema + formSchema
│   └── components/CategoryModal.vue   新增/编辑弹窗
├── space/                         空间主数据（结构与 category 对称）
│   ├── index.vue / space.api.ts / space.data.ts / components/SpaceModal.vue
└── device/                        设备主数据
    ├── index.vue                  分页筛选列表
    ├── device.api.ts              list/save/edit/delete/deleteBatch/exportXls/importExcel
    ├── device.data.ts
    └── components/DeviceModal.vue
```

参考既有页：树表格参考 `views/system/category/`；列表+弹窗参考 `views/equipmentInspection/inspectionContent/`。统一用 `defHttp` + `useListPage` + `BasicTable` + `BasicModal/useModal` + `BasicForm/useForm` + `enum Api` + `columns/searchFormSchema`。

### 6.2 类别 / 空间页（树表格）
- `index.vue`：`useListPage` + `BasicTable`，`tableProps.isTreeTable = true`。
  - `/list` 返回扁平 list，在 api 层用 jeecg 通用 `listToTree`（无则新增约 30 行 util）转成带 `children` 的树再渲染。
  - `tableTitle`：「新增」（新增根节点）。
  - 操作列：「编辑」「添加下级」「删除」（popConfirm；删除被后端拒绝时由后端 message 提示）。
  - 列：名称（树形列）、全称、创建时间。
  - 搜索区：名称（模糊）。
- `CategoryModal.vue`：`BasicForm` 字段 `id(隐藏) / pid(TreeSelect, 根="0", 编辑时可改=移动) / name(必填)`。**fullName 由后端算，前端不传**。
- TreeSelect 数据源：`loadTreeData`（复用 `/list` 全量 + buildTree），供弹窗"父级节点"和设备页类别/空间下拉使用。

### 6.3 设备页（筛选列表 + 弹窗）
- `index.vue`：`useListPage` + `BasicTable`（分页）。
  - 搜索区：名称、类别（TreeSelect 类别树）、空间（TreeSelect 空间树）。
  - `tableTitle`：新增、导出、导入、批量删除。
  - 列：名称、类别（categoryFullName）、空间（spaceFullName）、备注、创建时间、操作。
- `DeviceModal.vue`：字段 `id(隐藏) / name(必填) / categoryId(TreeSelect,必填) / spaceId(TreeSelect,必填) / remark(textarea)`。name 唯一性由后端兜底。
- 导出/导入：复用 jeecg `useMethods` 的 `handleExportXls / handleImportXls`。

### 6.4 菜单与路由
jeecg 动态路由：在「系统管理 → 菜单管理」配置三条菜单，组件路径分别填 `master/category/index`、`master/space/index`、`master/device/index`，路由由菜单驱动生成，无需改前端静态路由文件。

---

## 7. 错误处理与事务

- 业务校验失败统一抛 `JeecgBootException("文案")`，由全局异常处理器转失败 `Result`，前端 `defHttp` 自动 message 提示。
- 涉及子树重算的 `update`（改名/移动）、涉及多次写入的 `importExcel` 加 `@Transactional`。
- 低并发主数据场景不引入乐观锁（YAGNI）。

| 场景 | 提示文案 |
|---|---|
| 同层重名 | 同级下已存在同名「类别/空间」 |
| 移动到自身/子级下 | 不能移动到自身或其子级下 |
| 删除有子节点 | 存在子级，请先删除子级 |
| 删除被设备引用 | 该「类别/空间」被设备引用，无法删除 |
| 设备名重复 | 设备名称已存在 |
| 设备类别/空间不存在 | 所选「类别/空间」不存在 |

---

## 8. 测试策略

- **`TreeFullNameHelper` 用 TDD**（纯算法、无 Spring）：根/子全称拼接、防环、uuid 格式。
- **Service 关键流程**（Mockito，可选）：同层重名拒绝、移动防环、子树全称重算正确性、删除引用拒绝、设备名唯一。
- **前端**：jeecg 惯例不写单测，手测关键路径（树展开/移动后全称刷新、设备筛选、导入失败行提示）。

---

## 9. 实施顺序（核心链路最小可用优先）

1. 建表 DDL（用户执行）。
2. 后端 `common/TreeFullNameHelper` + 单测。
3. 后端 **类别**：entity → mapper → service → controller，跑通树 CRUD + 全称 + 移动 + 删除校验。
4. 后端 **空间**：复刻类别。
5. 后端 **设备**：唯一性 + 引用校验 + 列表联表 VO + Excel 导入导出。
6. 前端 **类别页**（树表格 + 弹窗）并联调。
7. 前端 **空间页**（复刻类别）。
8. 前端 **设备页**（列表 + 弹窗 + 导入导出）。
9. 菜单配置 + 端到端验证。

---

## 10. 未纳入范围（YAGNI）

- Feign api 子模块（无跨服务调用需求）。
- 类别/空间的 Excel 导入导出。
- 乐观锁 / 并发版本控制。
- 设备编码字段。
- 逻辑删除（del_flag）。
- SQL 递归 CTE、mapper xml（全部用 wrapper + 内存组装替代）。
