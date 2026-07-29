-- 设备管理demo表结构
CREATE TABLE `fwbz_device` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `device_code` varchar(50) NOT NULL COMMENT '设备编号',
  `device_type` int(2) DEFAULT NULL COMMENT '设备类型 1-电表 2-水表 3-气表 4-温控器',
  `location` varchar(200) DEFAULT NULL COMMENT '安装位置',
  `status` int(2) DEFAULT '1' COMMENT '设备状态 0-离线 1-在线 2-故障',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_code` (`device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FWBZ设备管理demo表';

-- 插入字典数据
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`)
VALUES ('1876543210987654321', '设备类型', 'device_type', 'FWBZ设备类型字典', '0', 'admin', NOW(), NULL, NULL, '0');

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
('1876543210987654322', '1876543210987654321', '电表', '1', 1, '1', 'admin', NOW(), NULL, NULL, NULL),
('1876543210987654323', '1876543210987654321', '水表', '2', 2, '1', 'admin', NOW(), NULL, NULL, NULL),
('1876543210987654324', '1876543210987654321', '气表', '3', 3, '1', 'admin', NOW(), NULL, NULL, NULL),
('1876543210987654325', '1876543210987654321', '温控器', '4', 4, '1', 'admin', NOW(), NULL, NULL, NULL);

INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`)
VALUES ('1876543210987654326', '设备状态', 'device_status', 'FWBZ设备状态字典', '0', 'admin', NOW(), NULL, NULL, '0');

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
('1876543210987654327', '1876543210987654326', '离线', '0', 1, '1', 'admin', NOW(), NULL, NULL, NULL),
('1876543210987654328', '1876543210987654326', '在线', '1', 2, '1', 'admin', NOW(), NULL, NULL, NULL),
('1876543210987654329', '1876543210987654326', '故障', '2', 3, '1', 'admin', NOW(), NULL, NULL, NULL);

-- 菜单配置SQL（一级菜单：Demo管理，二级菜单：设备管理）
-- 注意：父级菜单ID请根据你实际的FWBZ菜单ID调整，这里只是示例
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `is_leaf`, `menu_type`, `icon`, `sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `rule_flag`)
VALUES ('1876543210987654330', '你的FWBZ根菜单ID', 'Demo管理', '/fwbz/demo', 'layouts/RouteView', '1', '0', '0', 'appstore', 99, '1', 'admin', NOW(), NULL, NULL, '0', '0');

INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `is_leaf`, `menu_type`, `icon`, `sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `rule_flag`)
VALUES ('1876543210987654331', '1876543210987654330', '设备管理', '/fwbz/demo/device', '/fwbz/demo/device/index', '1', '1', '0', 'control', 1, '1', 'admin', NOW(), NULL, NULL, '0', '0');

-- 按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `perms`, `menu_type`, `sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`)
VALUES
('1876543210987654332', '1876543210987654331', '新增', 'fwbz:demo:device:add', '1', 1, '1', 'admin', NOW(), NULL, NULL, '0'),
('1876543210987654333', '1876543210987654331', '编辑', 'fwbz:demo:device:edit', '1', 2, '1', 'admin', NOW(), NULL, NULL, '0'),
('1876543210987654334', '1876543210987654331', '删除', 'fwbz:demo:device:delete', '1', 3, '1', 'admin', NOW(), NULL, NULL, '0'),
('1876543210987654335', '1876543210987654331', '批量删除', 'fwbz:demo:device:deleteBatch', '1', 4, '1', 'admin', NOW(), NULL, NULL, '0'),
('1876543210987654336', '1876543210987654331', '导出', 'fwbz:demo:device:exportXls', '1', 5, '1', 'admin', NOW(), NULL, NULL, '0'),
('1876543210987654337', '1876543210987654331', '导入', 'fwbz:demo:device:importExcel', '1', 6, '1', 'admin', NOW(), NULL, NULL, '0');
