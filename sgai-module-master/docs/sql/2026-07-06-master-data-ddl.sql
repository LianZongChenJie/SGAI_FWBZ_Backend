-- =====================================================================
-- 主数据模块 建表 DDL
-- 日期：2026-07-06
-- 关联设计：docs/superpowers/specs/2026-07-06-master-data-design.md
-- 说明：
--   1) 类别/空间为树形主数据，full_name 冗余存储，由后端迭代维护（非递归）。
--   2) 根节点 pid = '0'；全称分隔符 '-'；根节点 full_name = name 自身。
--   3) 设备名称系统唯一；category_id / space_id 必填。
--   4) 主键 uuid（32 位无横线，后端 IdUtil.simpleUUID() 生成）。
-- 执行：由用户在目标库手动执行。
-- =====================================================================

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
