-- 类别 / 空间 增加排序字段 sort
-- 由用户在目标库手动执行；老数据 sort 保持 NULL。
-- 排序规则：ORDER BY sort ASC（MySQL 升序下 NULL 视为最小值，排在最前）。
-- 新增节点 sort 为空时，后端自动取「同 pid 下 max(sort)+1」赋值。

-- 类别
ALTER TABLE device_category ADD COLUMN sort INT NULL COMMENT '同级内排序，升序，小在前';

-- 空间
ALTER TABLE space ADD COLUMN sort INT NULL COMMENT '同级内排序，升序，小在前';
