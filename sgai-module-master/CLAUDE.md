# sgai-module-master

主数据管理微服务模块（jeecg-boot 3.7.0 生态）。管理三类主数据：类别（树形）、空间（树形）、设备（平铺）。

> 本文件是 Claude 工作指南，提炼自 `docs/` 下的设计与计划文档。详细设计与完整实现样例见 `docs/`，不要在本文件复制大段代码。

---

## 1. 技术栈

- **框架**：jeecg-boot 3.7.0（Spring Boot + MyBatis-Plus + AutoPoi）
- **语言/编译**：Java 17
- **父工程**：`sgai-server-cloud`（`org.jeecgframework.boot:3.7.0`），本模块坐标 `com.mushen:sgai-module-master:1.0-SNAPSHOT`
- **核心依赖**：`jeecg-boot-starter-cloud`、`sgai-boot-base-core`
- **前端**：独立仓库 `fwbz-web`（Vue3 + TS + jeecg-boot 前端），**不在本仓库内**。主数据前端代码路径：
  `/Users/zhangchong/bjsg/workspace/fwbz-web/src/views/master`（相对本仓库根：`../../fwbz-web/src/views/master`）
- **测试**：JUnit5 + Mockito（后端）

---

## 2. 模块结构（api / biz / start 三子模块）

参照 `sgai-module-fwbz` 的多模块结构（2026-07-10 由单模块拆分）。父 pom `packaging=pom`，坐标 `com.mushen:sgai-module-master:1.0-SNAPSHOT`，parent `sgai-server-cloud`。依赖拓扑：`api ← biz ← start`。

```
sgai-module-master/
├── sgai-module-master-api/      空骨架，预留 Feign（当前仅 .gitkeep），依赖 sgai-boot-base-core
├── sgai-module-master-biz/      全部业务实现，依赖 api + sgai-boot-base-core
│   └── src/main/java/org/jeecg/modules/master/
│       ├── common/      算法/事件/HTTP 执行器（TreeFullNameHelper 等）；AsyncConfig 已上移到 start
│       ├── controller/  /master/deviceCategory | /master/space | /master/device | /master/integration*
│       ├── service/     I*Service + impl/*ServiceImpl
│       ├── mapper/      仅 extends BaseMapper<T>，无自定义方法、无 xml
│       ├── entity/      DeviceCategory / Space / Device / Integration*（@TableName）
│       └── vo/          DeviceVO（列表/导出）、DeviceImportDTO（导入解析）、集成推送/接收 DTO
│   └── src/test/java/  JUnit5 + Mockito 单测
└── sgai-module-master-start/    启动入口，依赖 jeecg-boot-starter-cloud + biz
    ├── src/main/java/org/jeecg/SgaiModuleMasterApplication.java
    ├── src/main/java/org/jeecg/config/AsyncConfig.java
    └── src/main/resources/{application.yml, logback-spring.xml}
```

启动类在 `org.jeecg` 包（start 模块），`@SpringBootApplication` 默认扫描 `org.jeecg.**`，覆盖 biz 的 controller/service/mapper/listener；mapper 扫描沿用拆分前既有机制。

前端目录（**已实现**，路径 `fwbz-web/src/views/master/`）：

```
master/
├── utils/tree.ts            listToTree / listToTreeSelect（扁平转树 / TreeSelect 数据）
├── category/                类别树表格页
│   ├── index.vue  category.api.ts  category.data.ts
│   └── components/CategoryModal.vue
├── space/                   空间树表格页（对称复刻类别）
│   ├── index.vue  space.api.ts  space.data.ts
│   └── components/SpaceModal.vue
└── device/                  设备筛选列表页（含导入/导出/批量删除）
    ├── index.vue  device.api.ts  device.data.ts
    └── components/DeviceModal.vue
```

---

## 3. 硬约束（改代码前必须遵守）

这些是 docs 明确决策过的约定，**不要擅自改变**：

1. **零 mapper xml**：所有数据访问用 MyBatis-Plus `LambdaQueryWrapper`（`selectCount/selectList/selectBatchIds/selectPage`）+ `IService` 内置方法（`save/updateById/removeById/updateBatchById`）。设备列表显示类别/空间名称用「两次查询 + 内存拼 VO」，**不写联表 xml**。
2. **全称（full_name）冗余存储，写时迭代重算，禁止递归**：子树收集与子树全称重算都用循环（按层 `in(pid, frontier)` BFS），不写 Java 递归方法，也不用 SQL 递归 CTE。
3. **物理删除，不设 `del_flag`**：删除采用「保守拒绝」——有子节点拒绝、被设备引用拒绝。表不加逻辑删除字段。
4. **主键 uuid**：后端 insert 前用 hutool `IdUtil.simpleUUID()`（32 位无横线）生成，前端不传 id。
5. **根节点 `pid = "0"`**；全称分隔符 `-`；根节点 full_name = name 自身。
6. **设备 name 系统唯一**（表 `uk_name` 约束 + 后端校验），不加设备编码字段；`category_id`/`space_id` 必填且校验存在。
7. **类别/空间是两棵独立的树**，对称结构，空间逻辑复刻类别（唯一差别：删除引用校验查 `device.space_id`）。
8. **api 子模块为空骨架**（预留 Feign，当前无跨服务调用需求、不放业务类）；类别/空间不提供 Excel，仅设备支持导入/导出。
9. **遵循 jeecg-boot 3.7.0 标准模式**：`@TableName/@TableId/@Excel`、`JeecgController`、`ServiceImpl<M,T>`、`Result<T>`、AutoPoi 导入导出工具。

业务校验失败统一抛 `org.jeecg.common.exception.JeecgBootException("文案")`，由全局异常处理器转失败 `Result`。涉及子树重算的 `update`（改名/移动）、多次写入的 `importExcel` 加 `@Transactional(rollbackFor = Exception.class)`。

---

## 4. 关键业务规则速查

| 场景 | 行为 / 提示文案 |
|---|---|
| 同层重名（类别/空间） | 拒绝：`同级下已存在同名「类别/空间」` |
| 树节点移动到自身/子级下 | 拒绝（防环）：`不能移动到自身或其子级下` |
| 编辑改 pid | 即「移动」，触发子树全称重算（pid 或 name 变化都触发） |
| 删除有子节点 | 拒绝：`存在子级，请先删除子级` |
| 删除被设备引用 | 拒绝：`该「类别/空间」被设备引用，无法删除` |
| 设备名重复 | 拒绝：`设备名称已存在` |
| 设备类别/空间为空 / 不存在 | 拒绝：`请选择类别/空间` / `所选「类别/空间」不存在` |
| 类别/空间同级排序 | `ORDER BY sort ASC`（**不**加 ISNULL；MySQL 升序下 NULL 排最前） |
| 新增 sort 为空 | 后端自动取「同 pid 下 max(sort)+1」，同 pid 首条为 1 |
| 新增 sort 非空 / 编辑 sort | 透传传入值；编辑留空则保持原值（`updateById` 默认 NOT_NULL 不覆盖）；移动 pid **不**重算 sort |

设备导入按「类别全称 / 空间全称」定位（预载全称→id 映射），失败行回传前端逐行提示。

类别/空间 **sort（同级排序字段，2026-07-07）**：仅表单填值、列表按 `sort ASC` 展示；**不做**拖拽/上移下移按钮、**不**为 NULL 做特殊处理（故老数据 NULL 排最前）、**不**在移动/改名时重算 sort —— 均为有意决策（YAGNI），改动前先看 `docs/superpowers/specs/2026-07-07-master-tree-sort-design.md`。

---

## 5. 数据表（DDL 见 `docs/sql/`）

- `device_category`：id / name / full_name / pid / sort / 审计字段；索引 `idx_pid_name (pid, name)`
- `space`：同 device_category 结构（含 sort）
- `device`：id / name(唯一) / category_id / space_id / remark / 审计字段；`uk_name` + `idx_category_id` + `idx_space_id`

> DDL 由用户在目标库手动执行，**不要自动跑迁移**。

---

## 6. 工作流程约定（来自全局指南，本项目同样适用）

- **代码修改前先提交方案并等待确认**；方案更新需重新确认。文档修改可直接执行。
- 小步、分段修改，**不要**整文件重写或大范围重构；不顺手改无关内容。
- **不主动运行**编译/打包/测试/部署命令；不主动安装/升级/删除依赖或改锁文件。
- **不操作 git**（不 add/commit/push）；严格尊重 `.gitignore`，不用 `git add -f`。
- **优先复用项目现有代码模式**，而非读框架源码或跨模块搜索。
- 后端测试：docs 计划中使用 `mvn -pl sgai-module-master -am test -Dtest=...`；若父 pom 的 `skipTests` 导致 mvn 不执行测试，**改用 IDE 运行**。

---

## 7. 文档索引

| 文件 | 内容 |
|---|---|
| `docs/superpowers/specs/2026-07-06-master-data-design.md` | 主数据模块设计（架构、DDL、接口、流程、决策摘要） |
| `docs/superpowers/plans/2026-07-06-master-data.md` | 按 Task 拆解的实施计划（含完整代码样例，TDD 步骤） |
| `docs/sql/2026-07-06-master-data-ddl.sql` | 三张表建表 DDL（用户手动执行） |
| `docs/superpowers/specs/2026-07-07-master-tree-sort-design.md` | 类别/空间 sort 排序字段设计（决策、DDL、接口、流程） |
| `docs/superpowers/plans/2026-07-07-master-tree-sort.md` | sort 字段实施计划（5 Task，TDD 步骤） |
| `docs/sql/2026-07-07-master-tree-sort.sql` | device_category / space 加 sort 列 DDL（用户手动执行） |

实现时优先按 plans 文档的 Task 顺序（核心链路最小可用优先）：helper → 数据层 → 类别后端 → 空间后端 → 设备后端 → 前端三页 → 菜单 + 端到端验证。
