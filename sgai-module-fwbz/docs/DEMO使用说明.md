# FWBZ设备管理Demo使用说明

## 一、开发模式说明
这是标准的JeecgBoot 3.7 + Vue3前后端分离开发模式，三个项目的分工：
1. **sgai-boot-3.7**：JeecgBoot平台基础框架，一般不需要修改，提供系统管理、权限、字典等基础能力
2. **sgai-module-fwbz**：FWBZ业务后端模块，所有业务代码都在这里写
   - `sgai-module-fwbz-api`：对外暴露的接口DTO/VO，微服务间调用时用，单体开发一般不用
   - `sgai-module-fwbz-biz`：**核心业务代码都在这里**，按业务分子包（entity/mapper/service/controller）
   - `sgai-module-fwbz-start`：启动类模块，直接运行这个模块下的启动类即可启动后端
3. **fwbz-web**：前端Vue3项目，所有页面、api都在这里写

## 二、Demo部署步骤
### 1. 执行数据库脚本
在你的MySQL数据库中执行 `docs/demo_device.sql` 脚本：
- 创建fwbz_device表
- 创建设备类型、设备状态两个数据字典
- 菜单和权限SQL需要你自己调整父菜单ID后执行（或者直接在系统管理-菜单管理里手动加）

### 2. 后端启动
- IDEA中导入sgai-module-fwbz项目，Maven刷新依赖
- 找到 `sgai-module-fwbz-start` 模块下的启动类（一般是SgaiFwbzApplication.java）
- 直接启动即可，不需要重启sgai-boot-3.7，fwbz是独立启动的业务模块

### 3. 前端启动
- 进入fwbz-web目录，执行 `npm install`（如果没装过依赖）
- 执行 `npm run serve` 启动前端
- 菜单配置：
  - 方式1：执行SQL里的菜单语句（记得改parent_id为你FWBZ系统的根菜单ID）
  - 方式2：登录系统后在「系统管理-菜单管理」里手动添加菜单：
    - 一级菜单：名称「Demo管理」，路径`/fwbz/demo`，组件`layouts/RouteView`
    - 二级菜单：名称「设备管理」，路径`/fwbz/demo/device`，组件`/fwbz/demo/device/index`
- 给你的角色分配这个菜单和按钮权限，刷新页面就能看到菜单了

## 三、代码结构说明
### 后端代码位置
```
sgai-module-fwbz-biz/src/main/java/org/jeecg/modules/fwbz/demo/
├── entity/
│   └── FwbzDevice.java          # 实体类，对应数据库表
├── mapper/
│   └── FwbzDeviceMapper.java    # MyBatis-Plus Mapper接口
├── service/
│   ├── IFwbzDeviceService.java  # Service接口
│   └── impl/
│       └── FwbzDeviceServiceImpl.java  # Service实现
└── controller/
    └── FwbzDeviceController.java       # Controller接口，提供REST API
```

### 前端代码位置
```
fwbz-web/src/views/fwbz/demo/device/
├── device.api.ts                # 接口请求封装
├── index.vue                    # 设备列表主页面
└── components/                  # 子组件（弹窗等可以放这里）
```

## 四、标准开发流程（以后写新功能就按这个来）
1. **建表**：先设计数据库表，字段加好注释
2. **后端代码**：
   - 在对应业务包下建entity类，加@TableName、@TableId等注解
   - 建Mapper接口继承BaseMapper
   - 建Service接口继承IService，建ServiceImpl继承ServiceImpl
   - 建Controller继承JeecgController，写CRUD接口，路径统一用`/fwbz/业务模块/xxx`
   - 有自定义SQL就写mapper xml，放在resources/mapper目录下
3. **前端代码**：
   - 在views/fwbz下建对应业务目录
   - 先写api.ts封装后端接口
   - 写index.vue列表页，用BasicTable做表格，用a-modal做新增编辑弹窗
   - 字典直接调用`/sys/dict/getDictItems/字典编码`获取
4. **配置菜单权限**：在系统管理里加菜单、按钮权限，给角色分配权限
5. **测试**：启动前后端，测试功能

## 五、Demo功能清单
- ✅ 设备分页列表查询，支持按设备名称、编号、类型、状态搜索
- ✅ 新增设备
- ✅ 编辑设备
- ✅ 单个删除/批量删除设备
- ✅ 字典自动翻译（设备类型、状态）
- ✅ 表单校验
- ✅ 标准权限控制（按钮权限注解）
- ✅ 自带导入导出Excel能力（JeecgController父类自带）
