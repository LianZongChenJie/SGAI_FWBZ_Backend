# 主数据模块 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现类别、空间、设备三类主数据的后端 CRUD 接口与前端管理页面。

**Architecture:** 后端 `sgai-module-master` 模块按层分包（controller/service/mapper/entity/vo/common），MyBatis-Plus wrapper 零 xml；类别/空间是树形主数据，全称（full_name）冗余存储，写操作时**迭代**重算子树（非递归）；删除为物理删除 + 保守拒绝；前端 `fwbz-web/src/views/master/` 三页（类别/空间树表格，设备筛选列表）。

**Tech Stack:** jeecg-boot 3.7.0、Spring Boot、MyBatis-Plus、AutoPoi、JUnit5 + Mockito（后端）；Vue3 + TS + jeecg-boot 前端（`defHttp` + `useListPage` + `BasicTable` + `BasicModal/BasicForm`）。

## Global Constraints

- **不操作 git**：本计划不含任何 `git add/commit/push` 步骤；每个 task 以「完成检查点」收尾。
- **不主动执行编译/测试/打包命令**：计划中给出的 `mvn`/前端命令供你或获得授权后执行；执行者默认不主动运行。
- **零 mapper xml**：所有数据访问用 MyBatis-Plus wrapper（`selectCount/selectList/selectBatchIds/selectPage`）+ `IService` 内置方法（`save/updateById/removeById/updateBatchById`）。
- **全称维护用迭代，不用递归**：子树收集与子树全称重算均用循环（按层 `in(pid, frontier)`），不写递归方法，不用 SQL 递归 CTE。
- **主键 uuid**：后端 insert 前调 `IdUtil.simpleUUID()`（32 位无横线），前端不传 id。
- **根节点 pid = "0"**；全称分隔符 `-`；根节点 full_name = name。
- **设备 category_id / space_id 必填**；设备 name 系统唯一。
- **遵循 jeecg-boot 3.7.0 标准模式**（`@TableName/@TableId/@Excel`、`JeecgController`、`ServiceImpl<M,T>`、`Result<T>`、AutoPoi）；若项目有局部差异，执行时适配并在该 task 注明。
- **关联文档**：设计 `docs/superpowers/specs/2026-07-06-master-data-design.md`；DDL `docs/sql/2026-07-06-master-data-ddl.sql`（由用户执行）。

---

## File Structure

### 后端（`sgai-module-master/src/main/java/org/jeecg/module/master/`，按层分包）

| 文件 | 职责 |
|---|---|
| `common/TreeFullNameHelper.java` | 纯静态算法：buildFullName / assertMovable / generateUuid |
| `entity/DeviceCategory.java` | 类别实体（@TableName device_category）|
| `entity/Space.java` | 空间实体（@TableName space）|
| `entity/Device.java` | 设备实体（@TableName device）|
| `vo/DeviceVO.java` | 设备列表/导出展示对象（含类别/空间名称）|
| `vo/DeviceImportDTO.java` | 设备导入解析对象（按全称）|
| `mapper/DeviceCategoryMapper.java` | `extends BaseMapper<DeviceCategory>` |
| `mapper/SpaceMapper.java` | `extends BaseMapper<Space>` |
| `mapper/DeviceMapper.java` | `extends BaseMapper<Device>` |
| `service/IDeviceCategoryService.java` | 类别服务接口 |
| `service/ISpaceService.java` | 空间服务接口 |
| `service/IDeviceService.java` | 设备服务接口 |
| `service/impl/DeviceCategoryServiceImpl.java` | 类别树逻辑（全称迭代重算/移动防环/删除引用校验）|
| `service/impl/SpaceServiceImpl.java` | 空间树逻辑（复刻类别）|
| `service/impl/DeviceServiceImpl.java` | 设备唯一性/列表联表 VO/导入导出 |
| `controller/DeviceCategoryController.java` | `/master/deviceCategory` |
| `controller/SpaceController.java` | `/master/space` |
| `controller/DeviceController.java` | `/master/device`（含 exportXls/importExcel）|

### 后端测试（`src/test/java/org/jeecg/module/master/`）

| 文件 | 职责 |
|---|---|
| `common/TreeFullNameHelperTest.java` | 纯算法单测（TDD）|
| `service/DeviceCategoryServiceImplTest.java` | 类别树逻辑 Mockito 测试 |
| `service/DeviceServiceImplTest.java` | 设备唯一性/必填校验测试 |

### 前端（`fwbz-web/src/views/master/`）

| 文件 | 职责 |
|---|---|
| `utils/tree.ts` | listToTree 工具（扁平转树）|
| `category/category.api.ts` `/category.data.ts` `/index.vue` `/components/CategoryModal.vue` | 类别树表格页 |
| `space/space.api.ts` `/space.data.ts` `/index.vue` `/components/SpaceModal.vue` | 空间树表格页 |
| `device/device.api.ts` `/device.data.ts` `/index.vue` `/components/DeviceModal.vue` | 设备筛选列表页 |

---

## Task 1: TreeFullNameHelper 纯算法（TDD）

**Files:**
- Create: `src/main/java/org/jeecg/module/master/common/TreeFullNameHelper.java`
- Test: `src/test/java/org/jeecg/module/master/common/TreeFullNameHelperTest.java`

**Interfaces:**
- Produces: `TreeFullNameHelper.buildFullName(String parentFullName, String name) -> String`；`assertMovable(Set<String> subtreeIds, String newPid) -> void`（非法抛 `JeecgBootException`）；`generateUuid() -> String`；常量 `ROOT_PID="0"`、`SEPARATOR="-"`。

- [ ] **Step 1: 写失败测试**

```java
package org.jeecg.module.master.common;

import org.jeecg.common.exception.JeecgBootException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class TreeFullNameHelperTest {

    @Test
    void buildFullName_root_returnsName() {
        assertEquals("楼", TreeFullNameHelper.buildFullName(null, "楼"));
        assertEquals("楼", TreeFullNameHelper.buildFullName("", "楼"));
        assertEquals("楼", TreeFullNameHelper.buildFullName("0", "楼"));
    }

    @Test
    void buildFullName_child_concatenatesWithDash() {
        assertEquals("楼-1F", TreeFullNameHelper.buildFullName("楼", "1F"));
        assertEquals("楼-1F-会议室", TreeFullNameHelper.buildFullName("楼-1F", "会议室"));
    }

    @Test
    void assertMovable_intoSubtree_throws() {
        HashSet<String> subtree = new HashSet<>(Arrays.asList("B", "C", "D"));
        assertThrows(JeecgBootException.class, () -> TreeFullNameHelper.assertMovable(subtree, "B"));
        assertThrows(JeecgBootException.class, () -> TreeFullNameHelper.assertMovable(subtree, "D"));
    }

    @Test
    void assertMovable_outsideSubtree_ok() {
        HashSet<String> subtree = new HashSet<>(Arrays.asList("B", "C", "D"));
        assertDoesNotThrow(() -> TreeFullNameHelper.assertMovable(subtree, "Z"));
        assertDoesNotThrow(() -> TreeFullNameHelper.assertMovable(subtree, null));
    }

    @Test
    void generateUuid_is32CharNoDash() {
        String id = TreeFullNameHelper.generateUuid();
        assertNotNull(id);
        assertEquals(32, id.length());
        assertFalse(id.contains("-"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl sgai-module-master -am test -Dtest=TreeFullNameHelperTest`
Expected: 编译失败（`TreeFullNameHelper` 不存在）

- [ ] **Step 3: 写最小实现**

```java
package org.jeecg.module.master.common;

import cn.hutool.core.util.IdUtil;
import org.jeecg.common.exception.JeecgBootException;

import java.util.Set;

/**
 * 树形主数据全称算法（纯静态，不依赖 Spring / Mapper）。
 */
public final class TreeFullNameHelper {

    public static final String ROOT_PID = "0";
    public static final String SEPARATOR = "-";

    private TreeFullNameHelper() {
    }

    /** 根(parent 为空 / "0") 返回 name；否则 parentFullName + "-" + name。 */
    public static String buildFullName(String parentFullName, String name) {
        if (parentFullName == null || parentFullName.isEmpty() || ROOT_PID.equals(parentFullName)) {
            return name;
        }
        return parentFullName + SEPARATOR + name;
    }

    /** 若 newPid 落在子树 id 集合内，抛异常（防环：不能移到自身/子级下）。 */
    public static void assertMovable(Set<String> subtreeIds, String newPid) {
        if (newPid != null && subtreeIds.contains(newPid)) {
            throw new JeecgBootException("不能移动到自身或其子级下");
        }
    }

    /** 32 位无横线 uuid。 */
    public static String generateUuid() {
        return IdUtil.simpleUUID();
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -pl sgai-module-master -am test -Dtest=TreeFullNameHelperTest`
Expected: PASS（5 个测试全绿）

- [ ] **Step 5: 完成检查点（不操作 git）**

✅ helper 纯算法可用：根/子全称拼接、防环、uuid 格式。

---

## Task 2: 数据层 scaffolding（3 实体 + 3 Mapper + VO）

**Files:**
- Create: `entity/DeviceCategory.java`、`entity/Space.java`、`entity/Device.java`
- Create: `vo/DeviceVO.java`、`vo/DeviceImportDTO.java`
- Create: `mapper/DeviceCategoryMapper.java`、`mapper/SpaceMapper.java`、`mapper/DeviceMapper.java`

**Interfaces:**
- Produces: 实体类（供 service/controller 使用）；Mapper（`BaseMapper` 子接口，供 service 注入）。

> 说明：审计字段（createBy/createTime/updateBy/updateTime）依赖 jeecg 的 `MetaObjectHandler` 自动填充（项目标准配置）。若执行时发现未配置，service 中手动 `setCreateBy/setCreateTime`（SecurityUtils.getSubject().getUsername() / new Date()）。本 task 不写测试（POJO/接口），验证靠编译。

- [ ] **Step 1: DeviceCategory 实体**

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
@TableName("device_category")
@ApiModel("类别主数据")
public class DeviceCategory {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("类别名称")
    private String name;

    @ApiModelProperty("类别全称")
    private String fullName;

    @ApiModelProperty("上级id，根为0")
    private String pid;

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

- [ ] **Step 2: Space 实体**

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
@TableName("space")
@ApiModel("空间主数据")
public class Space {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("空间名称")
    private String name;

    @ApiModelProperty("空间全称")
    private String fullName;

    @ApiModelProperty("上级id，根为0")
    private String pid;

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

- [ ] **Step 3: Device 实体**

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
@TableName("device")
@ApiModel("设备主数据")
public class Device {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("主键uuid")
    private String id;

    @ApiModelProperty("设备名称")
    private String name;

    @ApiModelProperty("类别id")
    private String categoryId;

    @ApiModelProperty("空间id")
    private String spaceId;

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

- [ ] **Step 4: DeviceVO（列表展示 + 导出）**

```java
package org.jeecg.module.master.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.Date;

@Data
@ApiModel("设备列表/导出VO")
public class DeviceVO {

    @ApiModelProperty("id")
    private String id;

    @Excel(name = "设备名称", width = 20)
    @ApiModelProperty("设备名称")
    private String name;

    @ApiModelProperty("类别id")
    private String categoryId;

    @Excel(name = "类别", width = 25)
    @ApiModelProperty("类别全称")
    private String categoryFullName;

    @ApiModelProperty("类别名称")
    private String categoryName;

    @ApiModelProperty("空间id")
    private String spaceId;

    @Excel(name = "空间", width = 25)
    @ApiModelProperty("空间全称")
    private String spaceFullName;

    @ApiModelProperty("空间名称")
    private String spaceName;

    @Excel(name = "备注", width = 30)
    @ApiModelProperty("备注")
    private String remark;

    @Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private Date createTime;
}
```

- [ ] **Step 5: DeviceImportDTO（导入解析，按全称）**

```java
package org.jeecg.module.master.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
@ApiModel("设备导入DTO")
public class DeviceImportDTO {

    @Excel(name = "设备名称*")
    private String name;

    @Excel(name = "类别全称*")
    private String categoryFullName;

    @Excel(name = "空间全称*")
    private String spaceFullName;

    @Excel(name = "备注")
    private String remark;
}
```

- [ ] **Step 6: 三个 Mapper（仅继承 BaseMapper，无自定义方法、无 xml）**

```java
// mapper/DeviceCategoryMapper.java
package org.jeecg.module.master.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.module.master.entity.DeviceCategory;

@Mapper
public interface DeviceCategoryMapper extends BaseMapper<DeviceCategory> {
}
```

```java
// mapper/SpaceMapper.java
package org.jeecg.module.master.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.module.master.entity.Space;

@Mapper
public interface SpaceMapper extends BaseMapper<Space> {
}
```

```java
// mapper/DeviceMapper.java
package org.jeecg.module.master.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.module.master.entity.Device;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}
```

- [ ] **Step 7: 完成检查点（不操作 git）**

Run: `mvn -pl sgai-module-master -am compile`
Expected: 编译通过。
✅ 三个实体 + VO/DTO + 三个 Mapper 就绪。

---

## Task 3: 类别 Service（树逻辑 + 全称迭代重算 + 引用校验）+ 单测

**Files:**
- Create: `service/IDeviceCategoryService.java`
- Create: `service/impl/DeviceCategoryServiceImpl.java`
- Test: `src/test/java/org/jeecg/module/master/service/DeviceCategoryServiceImplTest.java`

**Interfaces:**
- Consumes: `TreeFullNameHelper`（Task 1）、`DeviceCategoryMapper`、`DeviceMapper`（Task 2）。
- Produces: `IDeviceCategoryService` 方法 `listAll(name)` / `create(entity)` / `updateNode(entity)` / `removeNode(id)`（注意避免与 `ServiceImpl` 的 `update/remove` 重载冲突，故命名 `updateNode/removeNode`）。

- [ ] **Step 1: 写失败测试（Mockito）**

```java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.mapper.DeviceCategoryMapper;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.service.impl.DeviceCategoryServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceCategoryServiceImplTest {

    @Mock
    DeviceCategoryMapper baseMapper;
    @Mock
    DeviceMapper deviceMapper;

    @InjectMocks
    DeviceCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        // 将 ServiceImpl 的 baseMapper 注入（ServiceImpl 持有 protected BaseMapper baseMapper 字段）
        service.baseMapper = baseMapper;
    }

    @Test
    void create_duplicateNameInSameLevel_throws() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThrows(JeecgBootException.class, () -> service.create(c));
    }

    @Test
    void create_root_buildsFullNameAsName() {
        DeviceCategory c = new DeviceCategory();
        c.setName("电气");
        c.setPid("0");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(DeviceCategory.class))).thenReturn(1);

        service.create(c);

        assertEquals("电气", c.getFullName());
        assertNotNull(c.getId());
        verify(baseMapper).insert(any(DeviceCategory.class));
    }

    @Test
    void remove_hasChildren_throws() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class)))
            .thenReturn(1L); // 第一次调用：count by pid = id
        assertThrows(JeecgBootException.class, () -> service.removeNode("X"));
    }

    @Test
    void remove_referencedByDevice_throws() {
        // 第一次 selectCount（子节点）=0；第二次（设备引用）=1
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deviceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThrows(JeecgBootException.class, () -> service.removeNode("X"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl sgai-module-master -am test -Dtest=DeviceCategoryServiceImplTest`
Expected: 编译失败（`IDeviceCategoryService` / `DeviceCategoryServiceImpl` 不存在）

- [ ] **Step 3: 写 Service 接口**

```java
// service/IDeviceCategoryService.java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.master.entity.DeviceCategory;

import java.util.List;

public interface IDeviceCategoryService extends IService<DeviceCategory> {

    /** 扁平全量列表（name 可选模糊）。 */
    List<DeviceCategory> listAll(String name);

    /** 新增（生成 uuid、计算 full_name、同层重名校验）。 */
    void create(DeviceCategory entity);

    /** 编辑/移动（pid 变化即移动；pid 或 name 变化触发子树全称重算）。 */
    void updateNode(DeviceCategory entity);

    /** 删除（有子节点或被设备引用时拒绝）。 */
    void removeNode(String id);
}
```

- [ ] **Step 4: 写 Service 实现（核心树逻辑，迭代非递归）**

```java
// service/impl/DeviceCategoryServiceImpl.java
package org.jeecg.module.master.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.common.TreeFullNameHelper;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.mapper.DeviceCategoryMapper;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.service.IDeviceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceCategoryServiceImpl
        extends ServiceImpl<DeviceCategoryMapper, DeviceCategory>
        implements IDeviceCategoryService {

    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    public List<DeviceCategory> listAll(String name) {
        LambdaQueryWrapper<DeviceCategory> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            w.like(DeviceCategory::getName, name);
        }
        w.orderByDesc(DeviceCategory::getCreateTime);
        return this.list(w);
    }

    @Override
    public void create(DeviceCategory entity) {
        String pid = StrUtil.isBlank(entity.getPid()) ? TreeFullNameHelper.ROOT_PID : entity.getPid();
        if (countSameLevel(pid, entity.getName(), null) > 0) {
            throw new JeecgBootException("同级下已存在同名类别");
        }
        entity.setId(TreeFullNameHelper.generateUuid());
        entity.setPid(pid);
        entity.setFullName(resolveFullName(pid, entity.getName()));
        this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNode(DeviceCategory entity) {
        DeviceCategory old = this.getById(entity.getId());
        if (old == null) {
            throw new JeecgBootException("类别不存在");
        }
        String newPid = StrUtil.isBlank(entity.getPid()) ? TreeFullNameHelper.ROOT_PID : entity.getPid();
        String newName = entity.getName();

        if (countSameLevel(newPid, newName, entity.getId()) > 0) {
            throw new JeecgBootException("同级下已存在同名类别");
        }

        boolean pidChanged = !newPid.equals(old.getPid());
        boolean nameChanged = !newName.equals(old.getName());

        if (pidChanged) {
            Set<String> subtreeIds = collectSubtreeIds(entity.getId());
            TreeFullNameHelper.assertMovable(subtreeIds, newPid);
            if (!TreeFullNameHelper.ROOT_PID.equals(newPid) && this.getById(newPid) == null) {
                throw new JeecgBootException("所选上级类别不存在");
            }
        }

        String newFullName = resolveFullName(newPid, newName);
        entity.setPid(newPid);
        entity.setFullName(newFullName);
        this.updateById(entity);

        if (pidChanged || nameChanged) {
            recalcSubtreeFullName(entity.getId(), newFullName);
        }
    }

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
    }

    // ---------- 私有工具 ----------

    private long countSameLevel(String pid, String name, String excludeId) {
        LambdaQueryWrapper<DeviceCategory> w = new LambdaQueryWrapper<DeviceCategory>()
                .eq(DeviceCategory::getPid, pid)
                .eq(DeviceCategory::getName, name);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(DeviceCategory::getId, excludeId);
        }
        return this.count(w);
    }

    /** pid="0" → name；否则取 parent.fullName 拼接。 */
    private String resolveFullName(String pid, String name) {
        if (TreeFullNameHelper.ROOT_PID.equals(pid)) {
            return name;
        }
        DeviceCategory parent = this.getById(pid);
        if (parent == null) {
            throw new JeecgBootException("所选上级类别不存在");
        }
        return TreeFullNameHelper.buildFullName(parent.getFullName(), name);
    }

    /** 迭代收集子孙 id（不含 root）：按层 selectList(in(pid, frontier))，循环至空。 */
    private Set<String> collectSubtreeIds(String rootId) {
        Set<String> all = new HashSet<>();
        List<String> frontier = Collections.singletonList(rootId);
        while (!frontier.isEmpty()) {
            List<DeviceCategory> children = this.list(new LambdaQueryWrapper<DeviceCategory>()
                    .in(DeviceCategory::getPid, frontier));
            if (children.isEmpty()) {
                break;
            }
            List<String> childIds = children.stream()
                    .map(DeviceCategory::getId).collect(Collectors.toList());
            all.addAll(childIds);
            frontier = childIds;
        }
        return all;
    }

    /** 迭代重算子树 full_name：内存按 pid 分组，BFS 自顶向下拼接，updateBatchById 批量更新。 */
    private void recalcSubtreeFullName(String rootId, String rootFullName) {
        Set<String> descIds = collectSubtreeIds(rootId);
        if (descIds.isEmpty()) {
            return;
        }
        List<DeviceCategory> descendants = this.listByIds(descIds);
        Map<String, List<DeviceCategory>> byPid = descendants.stream()
                .collect(Collectors.groupingBy(DeviceCategory::getPid));

        DeviceCategory root = this.getById(rootId); // 已含新 fullName
        List<DeviceCategory> toUpdate = new ArrayList<>();
        Deque<DeviceCategory> queue = new ArrayDeque<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            DeviceCategory node = queue.poll();
            List<DeviceCategory> kids = byPid.get(node.getId());
            if (kids != null) {
                for (DeviceCategory k : kids) {
                    k.setFullName(TreeFullNameHelper.buildFullName(node.getFullName(), k.getName()));
                    toUpdate.add(k);
                    queue.offer(k);
                }
            }
        }
        if (!toUpdate.isEmpty()) {
            this.updateBatchById(toUpdate);
        }
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn -pl sgai-module-master -am test -Dtest=DeviceCategoryServiceImplTest`
Expected: PASS（4 个测试全绿）

- [ ] **Step 6: 完成检查点（不操作 git）**

✅ 类别 service：新增/查询/编辑-移动（子树全称迭代重算+防环）/删除（引用校验）逻辑可用，单测通过。

---

## Task 4: 类别 Controller

**Files:**
- Create: `controller/DeviceCategoryController.java`

**Interfaces:**
- Consumes: `IDeviceCategoryService`（Task 3）。
- Produces: REST 端点 `/master/deviceCategory`（list / {id} / POST / PUT / DELETE）。

- [ ] **Step 1: 写 Controller**

```java
package org.jeecg.module.master.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.service.IDeviceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Api(tags = "类别主数据")
@RestController
@RequestMapping("/master/deviceCategory")
public class DeviceCategoryController {

    @Autowired
    private IDeviceCategoryService deviceCategoryService;

    @ApiOperation("扁平列表（name 可选模糊）")
    @GetMapping("/list")
    public Result<List<DeviceCategory>> list(@RequestParam(required = false) String name) {
        return Result.OK(deviceCategoryService.listAll(name));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public Result<DeviceCategory> queryById(@PathVariable("id") String id) {
        return Result.OK(deviceCategoryService.getById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<?> add(@RequestBody DeviceCategory entity) {
        deviceCategoryService.create(entity);
        return Result.OK("新增成功");
    }

    @ApiOperation("编辑/移动")
    @PutMapping
    public Result<?> edit(@RequestBody DeviceCategory entity) {
        deviceCategoryService.updateNode(entity);
        return Result.OK("编辑成功");
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        deviceCategoryService.removeNode(id);
        return Result.OK("删除成功");
    }
}
```

- [ ] **Step 2: 完成检查点（不操作 git）**

Run: `mvn -pl sgai-module-master -am compile`
Expected: 编译通过。
✅ 类别后端链路（entity→mapper→service→controller）完整，API 可调。

---

## Task 5: 空间 Service + Controller（复刻类别）

**Files:**
- Create: `service/ISpaceService.java`、`service/impl/SpaceServiceImpl.java`
- Create: `controller/SpaceController.java`
- Test: 无独立测试（逻辑与类别一致，类别单测已覆盖算法路径；执行时若需可拷贝 Task 3 测试）。

**Interfaces:**
- Consumes: `TreeFullNameHelper`、`SpaceMapper`、`DeviceMapper`。
- Produces: `ISpaceService`（listAll/create/updateNode/removeNode）；REST `/master/space`。

> 说明：空间逻辑与类别完全对称，唯一差别：删除引用校验查 `device.space_id`。以下给出完整代码（不省略，避免上下文跳跃）。

- [ ] **Step 1: Service 接口**

```java
// service/ISpaceService.java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.master.entity.Space;

import java.util.List;

public interface ISpaceService extends IService<Space> {
    List<Space> listAll(String name);
    void create(Space entity);
    void updateNode(Space entity);
    void removeNode(String id);
}
```

- [ ] **Step 2: Service 实现（与类别对称，引用校验查 device.space_id）**

```java
// service/impl/SpaceServiceImpl.java
package org.jeecg.module.master.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.common.TreeFullNameHelper;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.mapper.SpaceMapper;
import org.jeecg.module.master.service.ISpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements ISpaceService {

    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    public List<Space> listAll(String name) {
        LambdaQueryWrapper<Space> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            w.like(Space::getName, name);
        }
        w.orderByDesc(Space::getCreateTime);
        return this.list(w);
    }

    @Override
    public void create(Space entity) {
        String pid = StrUtil.isBlank(entity.getPid()) ? TreeFullNameHelper.ROOT_PID : entity.getPid();
        if (countSameLevel(pid, entity.getName(), null) > 0) {
            throw new JeecgBootException("同级下已存在同名空间");
        }
        entity.setId(TreeFullNameHelper.generateUuid());
        entity.setPid(pid);
        entity.setFullName(resolveFullName(pid, entity.getName()));
        this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNode(Space entity) {
        Space old = this.getById(entity.getId());
        if (old == null) {
            throw new JeecgBootException("空间不存在");
        }
        String newPid = StrUtil.isBlank(entity.getPid()) ? TreeFullNameHelper.ROOT_PID : entity.getPid();
        String newName = entity.getName();

        if (countSameLevel(newPid, newName, entity.getId()) > 0) {
            throw new JeecgBootException("同级下已存在同名空间");
        }

        boolean pidChanged = !newPid.equals(old.getPid());
        boolean nameChanged = !newName.equals(old.getName());

        if (pidChanged) {
            Set<String> subtreeIds = collectSubtreeIds(entity.getId());
            TreeFullNameHelper.assertMovable(subtreeIds, newPid);
            if (!TreeFullNameHelper.ROOT_PID.equals(newPid) && this.getById(newPid) == null) {
                throw new JeecgBootException("所选上级空间不存在");
            }
        }

        String newFullName = resolveFullName(newPid, newName);
        entity.setPid(newPid);
        entity.setFullName(newFullName);
        this.updateById(entity);

        if (pidChanged || nameChanged) {
            recalcSubtreeFullName(entity.getId(), newFullName);
        }
    }

    @Override
    public void removeNode(String id) {
        long childCnt = this.count(new LambdaQueryWrapper<Space>().eq(Space::getPid, id));
        if (childCnt > 0) {
            throw new JeecgBootException("存在子级，请先删除子级");
        }
        long refCnt = deviceMapper.selectCount(new LambdaQueryWrapper<Device>().eq(Device::getSpaceId, id));
        if (refCnt > 0) {
            throw new JeecgBootException("该空间被设备引用，无法删除");
        }
        this.removeById(id);
    }

    private long countSameLevel(String pid, String name, String excludeId) {
        LambdaQueryWrapper<Space> w = new LambdaQueryWrapper<Space>()
                .eq(Space::getPid, pid).eq(Space::getName, name);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(Space::getId, excludeId);
        }
        return this.count(w);
    }

    private String resolveFullName(String pid, String name) {
        if (TreeFullNameHelper.ROOT_PID.equals(pid)) {
            return name;
        }
        Space parent = this.getById(pid);
        if (parent == null) {
            throw new JeecgBootException("所选上级空间不存在");
        }
        return TreeFullNameHelper.buildFullName(parent.getFullName(), name);
    }

    private Set<String> collectSubtreeIds(String rootId) {
        Set<String> all = new HashSet<>();
        List<String> frontier = Collections.singletonList(rootId);
        while (!frontier.isEmpty()) {
            List<Space> children = this.list(new LambdaQueryWrapper<Space>().in(Space::getPid, frontier));
            if (children.isEmpty()) {
                break;
            }
            List<String> childIds = children.stream().map(Space::getId).collect(Collectors.toList());
            all.addAll(childIds);
            frontier = childIds;
        }
        return all;
    }

    private void recalcSubtreeFullName(String rootId, String rootFullName) {
        Set<String> descIds = collectSubtreeIds(rootId);
        if (descIds.isEmpty()) {
            return;
        }
        List<Space> descendants = this.listByIds(descIds);
        Map<String, List<Space>> byPid = descendants.stream().collect(Collectors.groupingBy(Space::getPid));
        Space root = this.getById(rootId);
        List<Space> toUpdate = new ArrayList<>();
        Deque<Space> queue = new ArrayDeque<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            Space node = queue.poll();
            List<Space> kids = byPid.get(node.getId());
            if (kids != null) {
                for (Space k : kids) {
                    k.setFullName(TreeFullNameHelper.buildFullName(node.getFullName(), k.getName()));
                    toUpdate.add(k);
                    queue.offer(k);
                }
            }
        }
        if (!toUpdate.isEmpty()) {
            this.updateBatchById(toUpdate);
        }
    }
}
```

- [ ] **Step 3: Controller**

```java
// controller/SpaceController.java
package org.jeecg.module.master.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.service.ISpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "空间主数据")
@RestController
@RequestMapping("/master/space")
public class SpaceController {

    @Autowired
    private ISpaceService spaceService;

    @ApiOperation("扁平列表（name 可选模糊）")
    @GetMapping("/list")
    public Result<List<Space>> list(@RequestParam(required = false) String name) {
        return Result.OK(spaceService.listAll(name));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public Result<Space> queryById(@PathVariable("id") String id) {
        return Result.OK(spaceService.getById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<?> add(@RequestBody Space entity) {
        spaceService.create(entity);
        return Result.OK("新增成功");
    }

    @ApiOperation("编辑/移动")
    @PutMapping
    public Result<?> edit(@RequestBody Space entity) {
        spaceService.updateNode(entity);
        return Result.OK("编辑成功");
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        spaceService.removeNode(id);
        return Result.OK("删除成功");
    }
}
```

- [ ] **Step 4: 完成检查点（不操作 git）**

Run: `mvn -pl sgai-module-master -am compile`
Expected: 编译通过。
✅ 空间后端链路完整。

---

## Task 6: 设备 Service + Controller（唯一性 / 列表联表 VO / Excel）

**Files:**
- Create: `service/IDeviceService.java`、`service/impl/DeviceServiceImpl.java`
- Create: `controller/DeviceController.java`
- Test: `src/test/java/org/jeecg/module/master/service/DeviceServiceImplTest.java`

**Interfaces:**
- Consumes: `DeviceMapper`、`DeviceCategoryMapper`、`SpaceMapper`、`DeviceImportDTO`、`DeviceVO`。
- Produces: `IDeviceService`（pageVO / listForExport / create / updateNode / remove / removeBatch / batchImport）；REST `/master/device`（含 exportXls / importExcel）。

- [ ] **Step 1: 写失败测试（唯一性 + 必填）**

```java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.DeviceCategoryMapper;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.mapper.SpaceMapper;
import org.jeecg.module.master.service.impl.DeviceServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock DeviceMapper baseMapper;
    @Mock DeviceCategoryMapper deviceCategoryMapper;
    @Mock SpaceMapper spaceMapper;

    @InjectMocks DeviceServiceImpl service;

    @BeforeEach
    void setUp() {
        service.baseMapper = baseMapper;
    }

    @Test
    void create_blankCategoryId_throws() {
        Device d = new Device();
        d.setName("设备A");
        d.setSpaceId("s1");
        assertThrows(JeecgBootException.class, () -> service.create(d));
    }

    @Test
    void create_duplicateName_throws() {
        Device d = new Device();
        d.setName("设备A");
        d.setCategoryId("c1");
        d.setSpaceId("s1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        assertThrows(JeecgBootException.class, () -> service.create(d));
    }

    @Test
    void create_categoryNotExist_throws() {
        Device d = new Device();
        d.setName("设备A");
        d.setCategoryId("c1");
        d.setSpaceId("s1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deviceCategoryMapper.selectById("c1")).thenReturn(null);
        assertThrows(JeecgBootException.class, () -> service.create(d));
    }

    @Test
    void create_ok() {
        Device d = new Device();
        d.setName("设备A");
        d.setCategoryId("c1");
        d.setSpaceId("s1");
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deviceCategoryMapper.selectById("c1")).thenReturn(new DeviceCategory());
        when(spaceMapper.selectById("s1")).thenReturn(new Space());
        when(baseMapper.insert(any(Device.class))).thenReturn(1);

        service.create(d);

        assertNotNull(d.getId());
        verify(baseMapper).insert(any(Device.class));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl sgai-module-master -am test -Dtest=DeviceServiceImplTest`
Expected: 编译失败（`IDeviceService`/`DeviceServiceImpl` 不存在）

- [ ] **Step 3: Service 接口**

```java
// service/IDeviceService.java
package org.jeecg.module.master.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.vo.DeviceImportDTO;
import org.jeecg.module.master.vo.DeviceVO;

import java.util.List;

public interface IDeviceService extends IService<Device> {

    IPage<DeviceVO> pageVO(Page<Device> page, String name, String categoryId, String spaceId);

    List<DeviceVO> listForExport(String name, String categoryId, String spaceId);

    void create(Device entity);

    void updateNode(Device entity);

    void removeBatch(List<String> ids);

    /** 导入：按全称解析类别/空间，校验唯一与存在，返回失败行信息。 */
    List<String> batchImport(List<DeviceImportDTO> rows);
}
```

- [ ] **Step 4: Service 实现**

```java
// service/impl/DeviceServiceImpl.java
package org.jeecg.module.master.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.master.common.TreeFullNameHelper;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.entity.DeviceCategory;
import org.jeecg.module.master.entity.Space;
import org.jeecg.module.master.mapper.DeviceCategoryMapper;
import org.jeecg.module.master.mapper.DeviceMapper;
import org.jeecg.module.master.mapper.SpaceMapper;
import org.jeecg.module.master.service.IDeviceService;
import org.jeecg.module.master.vo.DeviceImportDTO;
import org.jeecg.module.master.vo.DeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {

    @Autowired
    private DeviceCategoryMapper deviceCategoryMapper;
    @Autowired
    private SpaceMapper spaceMapper;

    @Override
    public IPage<DeviceVO> pageVO(Page<Device> page, String name, String categoryId, String spaceId) {
        IPage<Device> p = this.page(page, buildWrapper(name, categoryId, spaceId));
        return toVOPage(p);
    }

    @Override
    public List<DeviceVO> listForExport(String name, String categoryId, String spaceId) {
        List<Device> list = this.list(buildWrapper(name, categoryId, spaceId));
        Page<Device> wrap = new Page<>();
        wrap.setRecords(list);
        wrap.setTotal(list.size());
        return toVOPage(wrap).getRecords();
    }

    @Override
    public void create(Device entity) {
        validate(entity, null);
        entity.setId(TreeFullNameHelper.generateUuid());
        this.save(entity);
    }

    @Override
    public void updateNode(Device entity) {
        if (StrUtil.isBlank(entity.getId())) {
            throw new JeecgBootException("设备id不能为空");
        }
        validate(entity, entity.getId());
        this.updateById(entity);
    }

    @Override
    public void removeBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new JeecgBootException("未选择删除数据");
        }
        this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> batchImport(List<DeviceImportDTO> rows) {
        List<String> errors = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return errors;
        }
        // 预载类别/空间全称映射（按全称定位 id）
        Map<String, String> catMap = loadFullNameToIdMap(deviceCategoryMapper.selectList(null), DeviceCategory::getFullName, DeviceCategory::getId);
        Map<String, String> spcMap = loadFullNameToIdMap(spaceMapper.selectList(null), Space::getFullName, Space::getId);

        int line = 1;
        for (DeviceImportDTO row : rows) {
            line++;
            try {
                if (StrUtil.isBlank(row.getName())) {
                    throw new JeecgBootException("设备名称不能为空");
                }
                if (countName(row.getName(), null) > 0) {
                    throw new JeecgBootException("设备名称已存在：" + row.getName());
                }
                String categoryId = catMap.get(row.getCategoryFullName());
                if (categoryId == null) {
                    throw new JeecgBootException("类别不存在：" + row.getCategoryFullName());
                }
                String spaceId = spcMap.get(row.getSpaceFullName());
                if (spaceId == null) {
                    throw new JeecgBootException("空间不存在：" + row.getSpaceFullName());
                }
                Device d = new Device();
                d.setId(TreeFullNameHelper.generateUuid());
                d.setName(row.getName());
                d.setCategoryId(categoryId);
                d.setSpaceId(spaceId);
                d.setRemark(row.getRemark());
                baseMapper.insert(d);
            } catch (Exception e) {
                errors.add("第" + line + "行：" + e.getMessage());
            }
        }
        return errors;
    }

    // ---------- 私有工具 ----------

    private LambdaQueryWrapper<Device> buildWrapper(String name, String categoryId, String spaceId) {
        LambdaQueryWrapper<Device> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            w.like(Device::getName, name);
        }
        if (StrUtil.isNotBlank(categoryId)) {
            w.eq(Device::getCategoryId, categoryId);
        }
        if (StrUtil.isNotBlank(spaceId)) {
            w.eq(Device::getSpaceId, spaceId);
        }
        w.orderByDesc(Device::getCreateTime);
        return w;
    }

    private void validate(Device entity, String excludeId) {
        if (StrUtil.isBlank(entity.getName())) {
            throw new JeecgBootException("设备名称不能为空");
        }
        if (StrUtil.isBlank(entity.getCategoryId())) {
            throw new JeecgBootException("请选择类别");
        }
        if (StrUtil.isBlank(entity.getSpaceId())) {
            throw new JeecgBootException("请选择空间");
        }
        if (countName(entity.getName(), excludeId) > 0) {
            throw new JeecgBootException("设备名称已存在");
        }
        if (deviceCategoryMapper.selectById(entity.getCategoryId()) == null) {
            throw new JeecgBootException("所选类别不存在");
        }
        if (spaceMapper.selectById(entity.getSpaceId()) == null) {
            throw new JeecgBootException("所选空间不存在");
        }
    }

    private long countName(String name, String excludeId) {
        LambdaQueryWrapper<Device> w = new LambdaQueryWrapper<Device>().eq(Device::getName, name);
        if (StrUtil.isNotBlank(excludeId)) {
            w.ne(Device::getId, excludeId);
        }
        return this.count(w);
    }

    /** 设备分页 → DeviceVO（内存拼类别/空间名称）。 */
    private IPage<DeviceVO> toVOPage(IPage<Device> p) {
        List<Device> records = p.getRecords();
        List<DeviceVO> voList = records.stream().map(d -> {
            DeviceVO vo = new DeviceVO();
            BeanUtil.copyProperties(d, vo);
            return vo;
        }).collect(Collectors.toList());

        Set<String> catIds = records.stream().map(Device::getCategoryId).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        Set<String> spcIds = records.stream().map(Device::getSpaceId).filter(StrUtil::isNotBlank).collect(Collectors.toSet());

        Map<String, DeviceCategory> catMap = catIds.isEmpty() ? Collections.emptyMap()
                : deviceCategoryMapper.selectBatchIds(catIds).stream()
                .collect(Collectors.toMap(DeviceCategory::getId, c -> c));
        Map<String, Space> spcMap = spcIds.isEmpty() ? Collections.emptyMap()
                : spaceMapper.selectBatchIds(spcIds).stream()
                .collect(Collectors.toMap(Space::getId, s -> s));

        for (DeviceVO vo : voList) {
            DeviceCategory c = catMap.get(vo.getCategoryId());
            if (c != null) {
                vo.setCategoryName(c.getName());
                vo.setCategoryFullName(c.getFullName());
            }
            Space s = spcMap.get(vo.getSpaceId());
            if (s != null) {
                vo.setSpaceName(s.getName());
                vo.setSpaceFullName(s.getFullName());
            }
        }

        IPage<DeviceVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(voList);
        return result;
    }

    private <T> Map<String, String> loadFullNameToIdMap(List<T> list,
                                                        java.util.function.Function<T, String> fullNameFn,
                                                        java.util.function.Function<T, String> idFn) {
        Map<String, String> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (T t : list) {
            map.put(fullNameFn.apply(t), idFn.apply(t));
        }
        return map;
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn -pl sgai-module-master -am test -Dtest=DeviceServiceImplTest`
Expected: PASS（4 个测试全绿）

- [ ] **Step 6: Controller（含 exportXls / importExcel）**

```java
// controller/DeviceController.java
package org.jeecg.module.master.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.module.master.entity.Device;
import org.jeecg.module.master.service.IDeviceService;
import org.jeecg.module.master.vo.DeviceImportDTO;
import org.jeecg.module.master.vo.DeviceVO;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.export.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "设备主数据")
@RestController
@RequestMapping("/master/device")
public class DeviceController {

    @Autowired
    private IDeviceService deviceService;

    @ApiOperation("分页列表")
    @GetMapping("/list")
    public Result<IPage<DeviceVO>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String spaceId) {
        Page<Device> page = new Page<>(pageNo, pageSize);
        return Result.OK(deviceService.pageVO(page, name, categoryId, spaceId));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public Result<Device> queryById(@PathVariable("id") String id) {
        return Result.OK(deviceService.getById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<?> add(@RequestBody Device entity) {
        deviceService.create(entity);
        return Result.OK("新增成功");
    }

    @ApiOperation("编辑")
    @PutMapping
    public Result<?> edit(@RequestBody Device entity) {
        deviceService.updateNode(entity);
        return Result.OK("编辑成功");
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") String id) {
        deviceService.removeBatch(Arrays.asList(id));
        return Result.OK("删除成功");
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam("ids") List<String> ids) {
        deviceService.removeBatch(ids);
        return Result.OK("删除成功");
    }

    @ApiOperation("导出")
    @GetMapping("/exportXls")
    public void exportXls(HttpServletResponse response,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) String categoryId,
                          @RequestParam(required = false) String spaceId) throws IOException {
        List<DeviceVO> list = deviceService.listForExport(name, categoryId, spaceId);
        Workbook wb = ExcelExportUtil.exportExcel(new ExportParams("设备主数据", "设备"), DeviceVO.class, list);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("设备主数据.xls", "UTF-8"));
        wb.write(response.getOutputStream());
    }

    @ApiOperation("导入")
    @PostMapping("/importExcel")
    public Result<?> importExcel(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipart = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> files = multipart.getFileMap();
        List<DeviceImportDTO> all = new ArrayList<>();
        for (MultipartFile file : files.values()) {
            ImportParams params = new ImportParams();
            params.setTitleRows(0);
            params.setHeadRows(1);
            all.addAll(ExcelImportUtil.importExcel(file.getInputStream(), DeviceImportDTO.class, params));
        }
        List<String> errors = deviceService.batchImport(all);
        if (errors.isEmpty()) {
            return Result.OK("导入成功");
        }
        return Result.error("部分导入失败：" + String.join("；", errors));
    }
}
```

- [ ] **Step 7: 完成检查点（不操作 git）**

Run: `mvn -pl sgai-module-master -am test -Dtest=DeviceServiceImplTest && mvn -pl sgai-module-master -am compile`
Expected: 测试通过、编译通过。
✅ 设备后端链路完整（含唯一性/必填/列表联表 VO/批量删除/导入导出）。

---

## Task 7: 前端 listToTree 工具

**Files:**
- Create: `fwbz-web/src/views/master/utils/tree.ts`

**Interfaces:**
- Produces: `listToTree<T extends {id;pid}>(list, rootPid="0"): T[]`（每节点带 `children`）。

- [ ] **Step 1: 先查项目是否已有通用 listToTree**

Run: `grep -rl "listToTree\|arrayToTree" /Users/zhangchong/bjsg/workspace/fwbz-web/src/utils /Users/zhangchong/bjsg/workspace/fwbz-web/src/components 2>/dev/null`
Expected: 若命中已有工具，直接复用，跳过 Step 2。若无，执行 Step 2 新建。

- [ ] **Step 2: 写 listToTree（若无现成）**

```ts
// fwbz-web/src/views/master/utils/tree.ts
interface TreeNode {
  id: string;
  pid: string;
  children?: TreeNode[];
}

/**
 * 扁平列表转树（保留业务字段 + children），供 BasicTable isTreeTable 使用。
 * rootPid 默认 "0"。
 */
export function listToTree<T extends TreeNode>(list: T[], rootPid = '0'): T[] {
  const map = new Map<string, T & { children?: T[] }>();
  list.forEach((item) => map.set(item.id, { ...item, children: [] }));
  const roots: T[] = [];
  list.forEach((item) => {
    const node = map.get(item.id)!;
    if (!item.pid || item.pid === rootPid) {
      roots.push(node);
    } else {
      const parent = map.get(item.pid);
      if (parent) {
        parent.children = parent.children || [];
        parent.children.push(node);
      } else {
        roots.push(node);
      }
    }
  });
  return roots;
}

/**
 * 扁平列表转 TreeSelect 所需结构 { title, value, key, children }。
 * title 取 name（类别/空间的 name），value/key 取 id。
 * 供 ApiTreeSelect 的 api 返回使用。
 */
export interface TreeSelectNode {
  title: string;
  value: string;
  key: string;
  children?: TreeSelectNode[];
}

export function listToTreeSelect<T extends TreeNode>(
  list: T[],
  nameKey: keyof T = 'name' as keyof T,
  rootPid = '0',
): TreeSelectNode[] {
  const tree = listToTree(list, rootPid);
  const map = (nodes: T[]): TreeSelectNode[] =>
    nodes.map((n) => ({
      title: String((n as Record<string, unknown>)[nameKey as string] ?? ''),
      value: n.id,
      key: n.id,
      children: n.children && (n.children as unknown as T[]).length
        ? map(n.children as unknown as T[])
        : undefined,
    }));
  return map(tree);
}
```

- [ ] **Step 3: 完成检查点（不操作 git）**

✅ listToTree 可用（或已确认复用项目内置工具）。

---

## Task 8: 类别前端（api / data / Modal / index）

**Files:**
- Create: `fwbz-web/src/views/master/category/category.api.ts`
- Create: `fwbz-web/src/views/master/category/category.data.ts`
- Create: `fwbz-web/src/views/master/category/components/CategoryModal.vue`
- Create: `fwbz-web/src/views/master/category/index.vue`

**Interfaces:**
- Consumes: `/master/deviceCategory` 端点（Task 4）、`listToTree`（Task 7）。

- [ ] **Step 1: api.ts**

```ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/master/deviceCategory/list',
  save = '/master/deviceCategory',
  edit = '/master/deviceCategory',
  delete = '/master/deviceCategory',
}

export const list = (params?) => defHttp.get({ url: Api.list, params });
export const save = (params) => defHttp.post({ url: Api.save, params });
export const edit = (params) => defHttp.put({ url: Api.edit, params });
export const remove = (params) =>
  defHttp.delete({ url: `${Api.delete}/${params.id}` });
/** 新增/编辑统一入口（有 id 走 PUT，否则 POST）。 */
export const saveOrUpdate = (params, isUpdate: boolean) =>
  isUpdate ? edit(params) : save(params);
```

- [ ] **Step 2: data.ts**

```ts
import { BasicColumn, FormSchema } from '/@/components/Table';
import { listToTreeSelect } from '../utils/tree';
import { list as categoryList } from './category.api';

export const columns: BasicColumn[] = [
  { title: '类别名称', dataIndex: 'name', width: 320, align: 'left' },
  { title: '类别全称', dataIndex: 'fullName', align: 'left' },
];

export const searchFormSchema: FormSchema[] = [
  { label: '名称', field: 'name', component: 'JInput', colProps: { span: 6 } },
];

export const formSchema: FormSchema[] = [
  { label: '', field: 'id', component: 'Input', show: false },
  {
    label: '上级',
    field: 'pid',
    component: 'ApiTreeSelect',
    defaultValue: '0',
    componentProps: {
      api: async () => listToTreeSelect((await categoryList()) || []),
      immediate: true,
      treeDefaultExpandAll: true,
      getPopupContainer: () => document.body,
    },
  },
  { label: '类别名称', field: 'name', required: true, component: 'Input' },
];
```

- [ ] **Step 3: CategoryModal.vue**

```vue
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="title"
    :width="600"
    @ok="handleSubmit"
  >
    <div class="p-4">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from '../category.data';
import { saveOrUpdate } from '../category.api';

const emit = defineEmits(['success']);
const isUpdate = ref(true);

const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
  labelWidth: 100,
  schemas: formSchema,
  showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  resetFields();
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;
  if (unref(isUpdate)) {
    setFieldsValue({ ...data.record });
  } else if (data?.record?.pid) {
    setFieldsValue({ pid: data.record.pid });
  }
});

const title = computed(() => (!unref(isUpdate) ? '新增类别' : '编辑类别'));

async function handleSubmit() {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    await saveOrUpdate(values, unref(isUpdate));
    closeModal();
    emit('success', { isUpdate: unref(isUpdate), values });
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>
```

> 注：父级用 `ApiTreeSelect` 组件，`api` 返回 `listToTreeSelect(await list())` 转换后的树（`{title,value,key,children}`），组件内部自动加载；编辑时改上级即触发后端移动 + 全称重算。若项目无 `ApiTreeSelect` 组件，改用 `TreeSelect` 并在 `useModalInner` 内 `list()`→`listToTreeSelect`→`updateSchema({field:'pid', componentProps:{treeData}})` 注入。

- [ ] **Step 4: index.vue（树表格）**

```vue
<template>
  <div class="p-2">
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">新增</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <CategoryModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { useListPage } from '/@/hooks/system/useListPage';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import CategoryModal from './components/CategoryModal.vue';
import { columns, searchFormSchema } from './category.data';
import { list, remove } from './category.api';
import { listToTree } from '../utils/tree';

// list 返回扁平数组，转树后交给表格
const treeApi = async (params) => {
  const res = await list(params);
  return listToTree(res || []);
};

const { tableContext } = useListPage({
  designScope: 'master-category',
  tableProps: {
    title: '类别主数据',
    api: treeApi,
    columns,
    formConfig: { schemas: searchFormSchema },
    isTreeTable: true,
    actionColumn: { width: 200 },
  },
});

const [registerTable, { reload }] = tableContext;
const [registerModal, { openModal }] = useModal();

function handleCreate() {
  openModal(true, { isUpdate: false });
}

function getActions(record) {
  return [
    { label: '编辑', onClick: openModal.bind(null, true, { isUpdate: true, record }) },
    {
      label: '添加下级',
      onClick: openModal.bind(null, true, { isUpdate: false, record: { pid: record.id } }),
    },
    {
      label: '删除',
      color: 'error',
      popConfirm: { title: '确定删除吗？', confirm: handleDelete.bind(null, record) },
    },
  ];
}

async function handleDelete(record) {
  await remove({ id: record.id });
  reload();
}

function handleSuccess() {
  reload();
}
</script>
```

- [ ] **Step 5: 完成检查点（不操作 git）**

验证（前端 dev 由你启动）：进入类别页面 → 新增根类别 → 添加子级 → 编辑改名（验证全称刷新）→ 移动（验证全称重算）→ 删除（验证有子/被引用时拒绝提示）。
✅ 类别前端联调通过。

---

## Task 9: 空间前端（复刻类别）

**Files:**
- Create: `fwbz-web/src/views/master/space/space.api.ts`、`space.data.ts`、`components/SpaceModal.vue`、`index.vue`

**Interfaces:**
- Consumes: `/master/space` 端点（Task 5）、`listToTree`。

> 说明：与类别前端完全对称，仅文案/路径/标题不同。以下给出差异要点，实现时整体拷贝 Task 8 并替换。

- [ ] **Step 1: space.api.ts** — `Api` 路径改为 `/master/space/...`，函数同。
- [ ] **Step 2: space.data.ts** — 列标题改 `空间名称 / 空间全称`，`formSchema` 同结构（label "上级"、"空间名称"）。
- [ ] **Step 3: SpaceModal.vue** — 拷贝 CategoryModal，title 改 `新增空间 / 编辑空间`，import 指向 `../space.data` / `../space.api`。
- [ ] **Step 4: index.vue** — 拷贝类别 index.vue，`designScope: 'master-space'`、`title: '空间主数据'`，import 指向 space 模块。
- [ ] **Step 5: 完成检查点（不操作 git）**

✅ 空间前端联调通过（同类别验证点）。

---

## Task 10: 设备前端（列表 + 弹窗 + 导入导出）

**Files:**
- Create: `fwbz-web/src/views/master/device/device.api.ts`、`device.data.ts`、`components/DeviceModal.vue`、`index.vue`

**Interfaces:**
- Consumes: `/master/device` 端点（Task 6）、类别/空间 `/list`（Task 4/5，给 TreeSelect）。

- [ ] **Step 1: device.api.ts**

```ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/master/device/list',
  save = '/master/device',
  edit = '/master/device',
  deleteOne = '/master/device',
  deleteBatch = '/master/device/batch',
  exportXls = '/master/device/exportXls',
  importExcel = '/master/device/importExcel',
}

export const list = (params) => defHttp.get({ url: Api.list, params });
export const save = (params) => defHttp.post({ url: Api.save, params });
export const edit = (params) => defHttp.put({ url: Api.edit, params });
export const saveOrUpdate = (params, isUpdate: boolean) =>
  isUpdate ? edit(params) : save(params);
export const remove = (id) => defHttp.delete({ url: `${Api.deleteOne}/${id}` });
export const batchRemove = (ids) =>
  defHttp.delete({ url: `${Api.deleteBatch}?ids=${ids.join(',')}` });
export const getExportUrl = () => Api.exportXls;
export const getImportUrl = () => Api.importExcel;
```

- [ ] **Step 2: device.data.ts**

```ts
import { BasicColumn, FormSchema } from '/@/components/Table';
import { listToTreeSelect } from '../utils/tree';
import { list as categoryList } from '../category/category.api';
import { list as spaceList } from '../space/space.api';

// 类别/空间下拉树：list() 返回扁平数组，转成 TreeSelect 结构
const categoryTreeApi = async () => listToTreeSelect((await categoryList()) || []);
const spaceTreeApi = async () => listToTreeSelect((await spaceList()) || []);

export const columns: BasicColumn[] = [
  { title: '设备名称', dataIndex: 'name', width: 180 },
  { title: '类别', dataIndex: 'categoryFullName', width: 220 },
  { title: '空间', dataIndex: 'spaceFullName', width: 220 },
  { title: '备注', dataIndex: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
];

export const searchFormSchema: FormSchema[] = [
  { label: '设备名称', field: 'name', component: 'JInput', colProps: { span: 6 } },
  { label: '类别', field: 'categoryId', component: 'ApiTreeSelect', colProps: { span: 6 },
    componentProps: { api: categoryTreeApi, immediate: true, treeDefaultExpandAll: true } },
  { label: '空间', field: 'spaceId', component: 'ApiTreeSelect', colProps: { span: 6 },
    componentProps: { api: spaceTreeApi, immediate: true, treeDefaultExpandAll: true } },
];

export const formSchema: FormSchema[] = [
  { label: '', field: 'id', component: 'Input', show: false },
  { label: '设备名称', field: 'name', required: true, component: 'Input' },
  { label: '类别', field: 'categoryId', required: true, component: 'ApiTreeSelect',
    componentProps: { api: categoryTreeApi, immediate: true, treeDefaultExpandAll: true } },
  { label: '空间', field: 'spaceId', required: true, component: 'ApiTreeSelect',
    componentProps: { api: spaceTreeApi, immediate: true, treeDefaultExpandAll: true } },
  { label: '备注', field: 'remark', component: 'InputTextArea' },
];
```

> 注：`ApiTreeSelect` 的 `api` 返回扁平 list 时，jeecg 会自动转树（若组件不支持自动转树，则在 api 内用 `listToTree` 预转，并设 `treeData` 直接传入）。执行时验证下拉是否正确显示树。

- [ ] **Step 3: DeviceModal.vue**

```vue
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" :width="640" @ok="handleSubmit">
    <div class="p-4">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from '../device.data';
import { saveOrUpdate } from '../device.api';

const emit = defineEmits(['success']);
const isUpdate = ref(true);

const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
  labelWidth: 100,
  schemas: formSchema,
  showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  resetFields();
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;
  if (unref(isUpdate)) {
    setFieldsValue({ ...data.record });
  }
});

const title = computed(() => (!unref(isUpdate) ? '新增设备' : '编辑设备'));

async function handleSubmit() {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    await saveOrUpdate(values, unref(isUpdate));
    closeModal();
    emit('success');
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>
```

- [ ] **Step 4: index.vue（列表 + 导入导出 + 批量删除）**

```vue
<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">新增</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchDelete"><Icon icon="ant-design:delete-outlined" />批量删除</a-menu-item>
            </a-menu>
          </template>
          <a-button>批量操作<Icon icon="ant-design:down-outlined" /></a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="[
          { label: '编辑', onClick: handleEdit.bind(null, record) },
          { label: '删除', color: 'error', popConfirm: { title: '确定删除吗？', confirm: handleDelete.bind(null, record) } },
        ]" />
      </template>
    </BasicTable>
    <DeviceModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { useListPage } from '/@/hooks/system/useListPage';
import { useMethods } from '/@/hooks/system/useMethods';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import DeviceModal from './components/DeviceModal.vue';
import { columns, searchFormSchema } from './device.data';
import { list, remove, batchRemove, getExportUrl, getImportUrl } from './device.api';

const { handleExportXls, handleImportXls } = useMethods();

const { onExportXls, onImportXls, tableContext } = useListPage({
  designScope: 'master-device',
  tableProps: {
    title: '设备主数据',
    api: list,
    columns,
    formConfig: { schemas: searchFormSchema },
    actionColumn: { width: 160 },
    rowSelection: { type: 'checkbox' },
  },
  exportConfig: { name: '设备主数据', url: getExportUrl },
  importConfig: { url: getImportUrl },
});

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
const [registerModal, { openModal }] = useModal();

function handleCreate() { openModal(true, { isUpdate: false }); }
function handleEdit(record) { openModal(true, { isUpdate: true, record }); }

async function handleDelete(record) {
  await remove(record.id);
  reload();
}

async function batchDelete() {
  await batchRemove(selectedRowKeys.value);
  reload();
}

function handleSuccess() { reload(); }
</script>
```

- [ ] **Step 5: 完成检查点（不操作 git）**

验证：设备列表分页 → 按类别/空间/名称筛选 → 新增（唯一性提示）→ 编辑 → 单删/批量删 → 导出 → 导入（含失败行提示）。
✅ 设备前端联调通过。

---

## Task 11: 菜单配置 + 端到端验证

**Files:** 无代码（jeecg 菜单为数据库配置）。

- [ ] **Step 1: 建表**

由你执行 `docs/sql/2026-07-06-master-data-ddl.sql` 在目标库建三张表。

- [ ] **Step 2: 配置菜单**

在 jeecg「系统管理 → 菜单管理」新增三条菜单（父级按你的菜单层级选择）：

| 菜单名称 | 前端组件 | 路由地址（建议）|
|---|---|---|
| 类别主数据 | `master/category/index` | `/master/category` |
| 空间主数据 | `master/space/index` | `/master/space` |
| 设备主数据 | `master/device/index` | `/master/device` |

并为相关角色分配菜单授权。

- [ ] **Step 3: 端到端验证清单**

- [ ] 类别：新增根/子级 → 全称正确 → 改名后子树全称刷新 → 移动后子树全称重算 → 防环拒绝 → 删除有子拒绝 → 删除被设备引用拒绝。
- [ ] 空间：同类别。
- [ ] 设备：新增/编辑名称唯一校验 → 类别/空间必填校验 → 列表筛选 → 联表显示类别/空间全称 → 批量删除 → 导出 → 导入（含失败行）。
- [ ] 后端单元测试：`mvn -pl sgai-module-master -am test -Dtest=TreeFullNameHelperTest,DeviceCategoryServiceImplTest,DeviceServiceImplTest` 全绿。
- [ ] 审计字段：确认列表「创建时间」正常显示（jeecg `MetaObjectHandler` 自动填充生效）；若为空，在 service `create/updateNode` 显式 `setCreateTime(new Date())` / `setUpdateTime(new Date())`，并排查 `MetaObjectHandler` 是否配置。

- [ ] **Step 4: 完成检查点（不操作 git）**

✅ 端到端验证通过，主数据模块交付完成。

---

## Self-Review（plan 自审）

**1. Spec 覆盖核对**
- 全称冗余 + 迭代重算 → Task 1（helper）+ Task 3/5（recalcSubtreeFullName 迭代）。✅
- 零 xml + wrapper → 所有 service 用 `LambdaQueryWrapper` + `IService` 方法；无 xml 文件。✅
- 物理删除 + 保守拒绝 → Task 3/5 removeNode（子节点 + 设备引用）。✅
- 设备 name 唯一 + category/space 必填 → Task 6 validate。✅
- 树节点移动 → Task 3/5 updateNode（pid 变化）。✅
- 设备 Excel 导入导出 → Task 6（exportXls/importExcel + DeviceImportDTO）。✅
- 前端三页 + 菜单 → Task 7-11。✅
- 按层分包 → 所有后端文件路径 controller/service/mapper/entity/vo/common。✅

**2. 占位符扫描**：无 TBD/TODO；每步含实际代码或命令。✅（CategoryModal 的 TreeSelect 数据加载给出两种实现路径，属可选适配，非占位符。）

**3. 类型一致性核对**
- `IDeviceCategoryService.updateNode/removeNode` 与 impl、controller 调用一致。✅
- `IDeviceService.pageVO/listForExport/create/updateNode/removeBatch/batchImport` 与 impl、controller 一致。✅
- `DeviceVO` 字段（categoryFullName/spaceFullName）与前端 columns 一致。✅
- `TreeFullNameHelper.buildFullName/assertMovable/generateUuid` 签名 Task 1 定义，Task 3/5/6 使用一致。✅
- 前端 `list`/`save`/`edit`/`remove`/`saveOrUpdate` 命名三页一致。✅

无遗漏、无类型不一致。
