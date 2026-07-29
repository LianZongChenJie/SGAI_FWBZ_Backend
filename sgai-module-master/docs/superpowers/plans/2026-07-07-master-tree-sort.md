# 类别 / 空间 排序字段（sort）实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为类别（`device_category`）和空间（`space`）两棵树各增加 `sort` 字段，支持同级内按 sort 升序展示，新增节点 sort 为空时后端自动取「同 pid 下 max(sort)+1」。

**Architecture:** 后端实体加字段；`listAll` 改排序为 `ORDER BY sort ASC`（NULL 排最前，不加 ISNULL）；`create` 为空时自动赋值；新增私有 `nextSort(pid)`。前端 `*.data.ts` 加一列 + 一个可选表单项，复用既有保序的 `listToTree`，`index.vue`/`*Modal.vue` 不改。两树对称，类别先做、空间对称复刻。

**Tech Stack:** Java 17 · MyBatis-Plus（LambdaQueryWrapper，零 xml）· JUnit5 + Mockito · jeecg-boot 3.7.0 · Vue3 + TS（vben/antdv）

## Global Constraints

- **不操作 git**：不 `git add/commit/push`。每个任务以「验证通过」收尾，不写提交步骤。
- **测试用 IDE 运行**：父工程 pom 硬编码 `<skipTests>true</skipTests>`，`mvn test` 不会执行测试。所有「运行测试」步骤一律在 IDE（IDEA）中右键运行指定测试类/方法。
- **零 mapper xml**：所有查询用 `LambdaQueryWrapper`；`nextSort` 用 `baseMapper.selectOne(wrapper)` + `last("LIMIT 1")`，不写自定义 mapper 方法。
- **两树对称**：类别（`DeviceCategory`）与空间（`Space`）逻辑完全一致，仅类名/表名/文案不同；空间侧无 `SpaceServiceImplTest`，Task 2 需新建。
- **DDL 用户手动执行**：计划产出 `docs/sql/2026-07-07-master-tree-sort.sql`，不自动跑迁移；老数据 `sort=NULL`，`ORDER BY sort ASC` 下排最前，不做编号迁移。
- **MockitoExtension 默认 STRICT_STUBS**：每个测试只 stub 它实际会调用的方法，避免 `UnnecessaryStubbingException`。

---

## 文件结构

后端（`sgai-module-master`）：
- `entity/DeviceCategory.java` · `entity/Space.java` —— 各 +1 字段 `Integer sort`
- `service/impl/DeviceCategoryServiceImpl.java` · `service/impl/SpaceServiceImpl.java` —— `listAll` 排序、`create` 赋值、新增 `nextSort`
- `test/.../service/DeviceCategoryServiceImplTest.java`（已存在，加用例）· `test/.../service/SpaceServiceImplTest.java`（新建）
- `docs/sql/2026-07-07-master-tree-sort.sql` —— 新建 DDL

前端（`fwbz-web/src/views/master/`）：
- `category/category.data.ts` · `space/space.data.ts` —— `columns` +1 列、`formSchema` +1 项
- `index.vue` / `*Modal.vue` / `*.api.ts` —— 不改

---

## Task 1: 后端类别 sort 字段（实体 + listAll + create 自动赋值 + nextSort）

**Files:**
- Modify: `src/main/java/org/jeecg/module/master/entity/DeviceCategory.java:40`（`updateTime` 字段后插入）
- Modify: `src/main/java/org/jeecg/module/master/service/impl/DeviceCategoryServiceImpl.java:34`（listAll 排序）、`:46-47`（create 赋值）、`:103` 区块（新增 nextSort）
- Test: `src/test/java/org/jeecg/module/master/service/DeviceCategoryServiceImplTest.java`（加 4 个用例）

**Interfaces:**
- Consumes: 实体 `DeviceCategory.sort`（本任务新增）；`baseMapper.selectOne`（ServiceImpl 父类 `baseMapper` 字段）
- Produces: `nextSort(String pid) → Integer`（private）；`create` 对 `sort` 的自动赋值语义

- [ ] **Step 1: 实体加 sort 字段**

在 `DeviceCategory.java` 的 `updateTime` 字段（第 40 行）之后、类闭合 `}` 之前插入：

```java
    @ApiModelProperty("同级内排序，升序，小在前")
    private Integer sort;
```

- [ ] **Step 2: 写失败测试 —— sort 为空且同级无节点时自动赋 1**

在 `DeviceCategoryServiceImplTest.java` 末尾 `}` 之前追加：

```java
    @Test
    void create_sortNull_noSibling_assigns1() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        // 实现前 create 不调 selectOne；实现后 selectOne 默认返回 null → nextSort 返回 1
        assertEquals(Integer.valueOf(1), c.getSort());
    }
```

- [ ] **Step 3: 运行测试，确认失败**

在 IDEA 中打开 `DeviceCategoryServiceImplTest`，运行 `create_sortNull_noSibling_assigns1`。
Expected: FAIL —— `c.getSort()` 为 `null`，断言 `expected: 1 but was: null`（用 `Integer.valueOf` 走 Object 重载，避免 null 拆箱 NPE）。

- [ ] **Step 4: 实现 nextSort + create 自动赋值**

在 `DeviceCategoryServiceImpl.create` 中，第 46 行 `entity.setFullName(...)` 之后、第 47 行 `this.save(entity);` 之前插入：

```java
        if (entity.getSort() == null) {
            entity.setSort(nextSort(pid));
        }
```

在私有工具区（`countSameLevel` 方法之前，约第 104 行 `// ---------- 私有工具 ----------` 之下）新增：

```java
    /** 同级(pid)下 sort 非 null 的最大值 +1；同 pid 无有效节点则返回 1。 */
    private Integer nextSort(String pid) {
        DeviceCategory max = baseMapper.selectOne(new LambdaQueryWrapper<DeviceCategory>()
                .eq(DeviceCategory::getPid, pid)
                .isNotNull(DeviceCategory::getSort)
                .orderByDesc(DeviceCategory::getSort)
                .last("LIMIT 1"));
        return max == null ? 1 : max.getSort() + 1;
    }
```

- [ ] **Step 5: 运行测试，确认通过**

IDEA 运行 `create_sortNull_noSibling_assigns1`。
Expected: PASS。

- [ ] **Step 6: 写测试 —— sort 为空且同级最大 sort=5 时自动赋 6**

在测试类末尾追加：

```java
    @Test
    void create_sortNull_siblingMax5_assigns6() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        DeviceCategory max = new DeviceCategory();
        max.setSort(5);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(max);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        assertEquals(Integer.valueOf(6), c.getSort());
    }
```

- [ ] **Step 7: 运行测试，确认通过**

IDEA 运行 `create_sortNull_siblingMax5_assigns6`。
Expected: PASS。

- [ ] **Step 8: 写测试 —— sort 非空时保留传入值且不查 max**

在测试类末尾追加：

```java
    @Test
    void create_sortProvided_keepsProvided() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        c.setSort(3);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        assertEquals(Integer.valueOf(3), c.getSort());
        verify(baseMapper, never()).selectOne(any());
    }
```

- [ ] **Step 9: 运行测试，确认通过**

IDEA 运行 `create_sortProvided_keepsProvided`。
Expected: PASS（sort 非空，`if (entity.getSort() == null)` 为 false，不调 `nextSort` → 不调 `selectOne`；STRICT_STUBS 不会因未 stub `selectOne` 报错，因为它根本没被调用）。

- [ ] **Step 10: 写失败测试 —— listAll 按 sort 升序**

在测试类顶部 import 区追加（若已有 `ArgumentCaptor`/`verify`/`never` 则跳过）：

```java
import org.mockito.ArgumentCaptor;
```
（`verify`、`never` 已在第 17 行 `import static org.mockito.Mockito.*;` 覆盖。）

在测试类末尾追加：

```java
    @Test
    void listAll_ordersBySort() {
        when(baseMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(java.util.Collections.emptyList());

        service.listAll(null);

        ArgumentCaptor<LambdaQueryWrapper> cap = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(baseMapper).selectList(cap.capture());
        String sql = cap.getValue().getSqlSegment();
        assertTrue(sql.contains("sort"));
    }
```

- [ ] **Step 11: 运行测试，确认失败**

IDEA 运行 `listAll_ordersBySort`。
Expected: FAIL —— 当前 listAll 用 `orderByDesc(getCreateTime)`，sqlSegment 不含 `sort`。

- [ ] **Step 12: 改 listAll 排序**

将 `DeviceCategoryServiceImpl.listAll` 第 34 行：

```java
        w.orderByDesc(DeviceCategory::getCreateTime);
```

替换为：

```java
        w.orderByAsc(DeviceCategory::getSort);
```

- [ ] **Step 13: 运行测试，确认通过**

IDEA 运行整个 `DeviceCategoryServiceImplTest`。
Expected: PASS（全部既有用例 + 4 个新用例）。

> **updateNode 无需改动**：编辑/移动时 sort 随实体走 `updateById`；MyBatis-Plus 默认 `FieldStrategy.NOT_NULL`，sort 为空不拼入 UPDATE，自动满足设计 4.3「为空保持原值」，无需在 `updateNode` 写任何 sort 相关代码。

---

## Task 2: 后端空间 sort 字段（对称复刻 + 新建测试类）

**Files:**
- Modify: `src/main/java/org/jeecg/module/master/entity/Space.java:40`
- Modify: `src/main/java/org/jeecg/module/master/service/impl/SpaceServiceImpl.java:34`、`:46-47`、`:103` 区块
- Create: `src/test/java/org/jeecg/module/master/service/SpaceServiceImplTest.java`

**Interfaces:**
- Consumes: 实体 `Space.sort`（本任务新增）
- Produces: 与类别完全对称的 sort 行为；`SpaceServiceImplTest`（新建）

- [ ] **Step 1: 实体加 sort 字段**

在 `Space.java` 的 `updateTime` 字段（第 40 行）之后插入：

```java
    @ApiModelProperty("同级内排序，升序，小在前")
    private Integer sort;
```

- [ ] **Step 2: 改 SpaceServiceImpl.listAll 排序**

将第 34 行：

```java
        w.orderByDesc(Space::getCreateTime);
```

替换为：

```java
        w.orderByAsc(Space::getSort);
```

- [ ] **Step 3: 实现 nextSort + create 自动赋值**

在 `SpaceServiceImpl.create` 第 46 行 `entity.setFullName(...)` 之后、第 47 行 `this.save(entity);` 之前插入：

```java
        if (entity.getSort() == null) {
            entity.setSort(nextSort(pid));
        }
```

在私有工具区（`countSameLevel` 之前，`// ---------- 私有工具 ----------` 之下）新增：

```java
    /** 同级(pid)下 sort 非 null 的最大值 +1；同 pid 无有效节点则返回 1。 */
    private Integer nextSort(String pid) {
        Space max = baseMapper.selectOne(new LambdaQueryWrapper<Space>()
                .eq(Space::getPid, pid)
                .isNotNull(Space::getSort)
                .orderByDesc(Space::getSort)
                .last("LIMIT 1"));
        return max == null ? 1 : max.getSort() + 1;
    }
```

- [ ] **Step 4: 新建 SpaceServiceImplTest，写 4 个用例**

新建文件 `src/test/java/org/jeecg/module/master/service/SpaceServiceImplTest.java`，完整内容：

```java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.mapper.SpaceMapper;
import org.jeecg.module.master.service.impl.SpaceServiceImpl;
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
class SpaceServiceImplTest {

    @Mock
    SpaceMapper baseMapper;
    @Mock
    DeviceMapper deviceMapper;

    @InjectMocks
    SpaceServiceImpl service;

    @Test
    void create_sortNull_noSibling_assigns1() {
        Space s = new Space();
        s.setName("一层");
        s.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(Space.class))).thenReturn(1);

        service.create(s);

        assertEquals(Integer.valueOf(1), s.getSort());
    }

    @Test
    void create_sortNull_siblingMax5_assigns6() {
        Space s = new Space();
        s.setName("一层");
        s.setPid("0");
        Space max = new Space();
        max.setSort(5);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(max);
        when(baseMapper.insert(any(Space.class))).thenReturn(1);

        service.create(s);

        assertEquals(Integer.valueOf(6), s.getSort());
    }

    @Test
    void create_sortProvided_keepsProvided() {
        Space s = new Space();
        s.setName("一层");
        s.setPid("0");
        s.setSort(3);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(Space.class))).thenReturn(1);

        service.create(s);

        assertEquals(Integer.valueOf(3), s.getSort());
        verify(baseMapper, never()).selectOne(any());
    }

    @Test
    void listAll_ordersBySort() {
        when(baseMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(java.util.Collections.emptyList());

        service.listAll(null);

        ArgumentCaptor<LambdaQueryWrapper> cap = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(baseMapper).selectList(cap.capture());
        String sql = cap.getValue().getSqlSegment();
        assertTrue(sql.contains("sort"));
    }
}
```

> 注：本 4 个用例未直接断言异常，故不 import `JeecgBootException`；后续如需加异常用例再补 import。

- [ ] **Step 5: 运行测试，确认通过**

IDEA 运行整个 `SpaceServiceImplTest`。
Expected: PASS（4 个用例全绿）。

---

## Task 3: DDL 文件

**Files:**
- Create: `docs/sql/2026-07-07-master-tree-sort.sql`

**Interfaces:** 无（产出供用户手动执行的 SQL）

- [ ] **Step 1: 新建 DDL 文件**

新建 `docs/sql/2026-07-07-master-tree-sort.sql`，完整内容：

```sql
-- 类别 / 空间 增加排序字段 sort
-- 由用户在目标库手动执行；老数据 sort 保持 NULL，ORDER BY sort ASC 下排最前，不做编号迁移。

-- 类别
ALTER TABLE device_category ADD COLUMN sort INT NULL COMMENT '同级内排序，升序，小在前';

-- 空间
ALTER TABLE space ADD COLUMN sort INT NULL COMMENT '同级内排序，升序，小在前';
```

- [ ] **Step 2: 交付确认**

向用户提示：本 SQL 需在目标库手动执行，执行前两棵树的 `sort` 列不存在，后端启动后会因实体字段映射报错——请先执行 DDL 再启动后端验证。

---

## Task 4: 前端类别（columns + formSchema）

**Files:**
- Modify: `/Users/zhangchong/bjsg/workspace/fwbz-web/src/views/master/category/category.data.ts:3-7`（columns）、`:9-23`（formSchema）

**Interfaces:**
- Consumes: 后端返回的 `sort` 字段
- Produces: 类别表格显示「排序」列；新增/编辑弹窗含「排序」可选项

- [ ] **Step 1: columns 增加「排序」列**

将 `category.data.ts` 第 3-7 行：

```ts
export const columns: BasicColumn[] = [
  { title: '类别名称', dataIndex: 'name', width: 320, align: 'left' },
  { title: '类别全称', dataIndex: 'fullName', align: 'left' },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
];
```

替换为：

```ts
export const columns: BasicColumn[] = [
  { title: '类别名称', dataIndex: 'name', width: 320, align: 'left' },
  { title: '类别全称', dataIndex: 'fullName', align: 'left' },
  { title: '排序', dataIndex: 'sort', width: 80, align: 'center' },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
];
```

- [ ] **Step 2: formSchema 增加「排序」项**

将 `category.data.ts` 第 22 行：

```ts
  { label: '类别名称', field: 'name', required: true, component: 'Input' },
```

替换为：

```ts
  { label: '类别名称', field: 'name', required: true, component: 'Input' },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
    componentProps: { placeholder: '留空则自动排到同级末尾', min: 0 },
  },
```

- [ ] **Step 3: 手动验证（前端无自动化测试）**

启动 `fwbz-web`，打开「类别」页：
1. 列表出现「排序」列；
2. 按 DDL 里有 sort 的数据，子节点按 sort 升序、NULL 的排最前（依赖后端 Task 1 + 已执行 DDL）；
3. 点「新增」：弹窗含「排序」输入框；留空保存 → 刷新后该节点排同级末尾、sort=同级最大+1；
4. 点「编辑」：回填当前 sort；改成 1 保存 → 该节点排到同级最前。

Expected: 全部符合。

---

## Task 5: 前端空间（对称复刻）

**Files:**
- Modify: `/Users/zhangchong/bjsg/workspace/fwbz-web/src/views/master/space/space.data.ts:3-7`、`:9-23`

**Interfaces:** 与类别对称

- [ ] **Step 1: columns 增加「排序」列**

将 `space.data.ts` 第 3-7 行：

```ts
export const columns: BasicColumn[] = [
  { title: '空间名称', dataIndex: 'name', width: 320, align: 'left' },
  { title: '空间全称', dataIndex: 'fullName', align: 'left' },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
];
```

替换为：

```ts
export const columns: BasicColumn[] = [
  { title: '空间名称', dataIndex: 'name', width: 320, align: 'left' },
  { title: '空间全称', dataIndex: 'fullName', align: 'left' },
  { title: '排序', dataIndex: 'sort', width: 80, align: 'center' },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
];
```

- [ ] **Step 2: formSchema 增加「排序」项**

将 `space.data.ts` 第 22 行：

```ts
  { label: '空间名称', field: 'name', required: true, component: 'Input' },
```

替换为：

```ts
  { label: '空间名称', field: 'name', required: true, component: 'Input' },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
    componentProps: { placeholder: '留空则自动排到同级末尾', min: 0 },
  },
```

- [ ] **Step 3: 手动验证（前端无自动化测试）**

启动 `fwbz-web`，打开「空间」页，按 Task 4 Step 3 的 4 条同样验证空间页。
Expected: 全部符合。

---

## 收尾检查（全任务完成后）

- [ ] 后端：IDEA 运行 `DeviceCategoryServiceImplTest` + `SpaceServiceImplTest` 全绿。
- [ ] DDL 已交付用户手动执行。
- [ ] 前端：类别页 + 空间页排序展示、表单 sort 可选填、自动同级+1 均符合预期。
- [ ] 未执行任何 git 操作（符合项目约定）。
