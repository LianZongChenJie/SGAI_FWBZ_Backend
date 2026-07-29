-- 数据对接管理 建表 DDL（用户在目标库手动执行，不要自动跑迁移）
CREATE TABLE integration_system (
  id              varchar(32)  NOT NULL COMMENT '主键uuid',
  name            varchar(100) NOT NULL COMMENT '系统名称',
  code            varchar(50)  NOT NULL COMMENT '系统编码(唯一,日志冗余追溯)',
  push_enabled    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否启用推送 0否1是',
  push_url        varchar(500) NULL COMMENT '推送目标URL',
  receive_enabled tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否启用接收 0否1是',
  token           varchar(100) NULL COMMENT '共享令牌(推送请求头/接收反查,唯一)',
  remark          varchar(500) NULL COMMENT '备注',
  create_by       varchar(50)  NULL,
  create_time     datetime     NULL,
  update_by       varchar(50)  NULL,
  update_time     datetime     NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code),
  UNIQUE KEY uk_token (token)
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
