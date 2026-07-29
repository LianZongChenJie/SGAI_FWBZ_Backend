/*
 Navicat Premium Dump SQL

 Source Server         : localhost_5238
 Source Server Type    : Dameng
 Source Server Version : 801560 (08.01.560)
 Source Host           : localhost:5238
 Source Schema         : FWBZ

 Target Server Type    : Dameng
 Target Server Version : 801560 (08.01.560)
 File Encoding         : 65001

 Date: 29/07/2026 16:14:45
*/


-- ----------------------------
-- Table structure for alarm_category
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."alarm_category";
CREATE TABLE "FWBZ"."alarm_category" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "alarm_category_name" VARCHAR(255 CHAR) NOT NULL,
  "alarm_category_code" VARCHAR(255 CHAR) NOT NULL,
  "sort" INT NOT NULL,
  "status" VARCHAR(2 CHAR) NOT NULL
)
;
COMMENT ON COLUMN "FWBZ"."alarm_category"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."alarm_category"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."alarm_category"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."alarm_category"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."alarm_category"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."alarm_category"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."alarm_category"."alarm_category_name" IS '类别名称';
COMMENT ON COLUMN "FWBZ"."alarm_category"."alarm_category_code" IS '类别编号';
COMMENT ON COLUMN "FWBZ"."alarm_category"."sort" IS '排序字段';
COMMENT ON COLUMN "FWBZ"."alarm_category"."status" IS '状态。启用：1；禁用：0；';
COMMENT ON TABLE "FWBZ"."alarm_category" IS '报警类别';

-- ----------------------------
-- Table structure for alarm_level
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."alarm_level";
CREATE TABLE "FWBZ"."alarm_level" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "alarm_level_code" VARCHAR(255 CHAR) NOT NULL,
  "alarm_level_name" VARCHAR(255 CHAR) NOT NULL,
  "sort" INT NOT NULL,
  "status" VARCHAR(2 CHAR) NOT NULL,
  "alarm_level_color" VARCHAR(50)
)
;
COMMENT ON COLUMN "FWBZ"."alarm_level"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."alarm_level"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."alarm_level"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."alarm_level"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."alarm_level"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."alarm_level"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."alarm_level"."alarm_level_code" IS '等级编号';
COMMENT ON COLUMN "FWBZ"."alarm_level"."alarm_level_name" IS '等级名称';
COMMENT ON COLUMN "FWBZ"."alarm_level"."sort" IS '排序字段';
COMMENT ON COLUMN "FWBZ"."alarm_level"."status" IS '状态。启用：1；禁用：0';
COMMENT ON COLUMN "FWBZ"."alarm_level"."alarm_level_color" IS '告警颜色';
COMMENT ON TABLE "FWBZ"."alarm_level" IS '报警级别';

-- ----------------------------
-- Table structure for alarm_record
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."alarm_record";
CREATE TABLE "FWBZ"."alarm_record" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "alarm_rule_id" BIGINT,
  "device_id" BIGINT,
  "device_name" VARCHAR(255 CHAR),
  "space_id" BIGINT,
  "space_name" VARCHAR(255 CHAR),
  "alarm_content" TEXT,
  "alarm_time" TIMESTAMP,
  "alarm_category_id" BIGINT,
  "alarm_category_name" VARCHAR(255 CHAR),
  "alarm_level_id" BIGINT,
  "alarm_level_name" VARCHAR(255 CHAR),
  "charge_person" BIGINT,
  "charge_person_name" VARCHAR(255 CHAR),
  "alarm_status" VARCHAR(1 CHAR),
  "point_id" BIGINT,
  "point_name" VARCHAR(255 CHAR),
  "value" VARCHAR(255 CHAR),
  "condition_value" VARCHAR(255 CHAR),
  "operator" VARCHAR(255 CHAR),
  "time_granularity" VARCHAR(255 CHAR),
  "alarm_rule_point_id" BIGINT,
  "device_category_id" BIGINT,
  "alarm_level_color" VARCHAR(50),
  "event_id" VARCHAR(50)
)
;
COMMENT ON COLUMN "FWBZ"."alarm_record"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."alarm_record"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."alarm_record"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."alarm_record"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."alarm_record"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."alarm_record"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_rule_id" IS '告警规则ID';
COMMENT ON COLUMN "FWBZ"."alarm_record"."device_id" IS '设备ID';
COMMENT ON COLUMN "FWBZ"."alarm_record"."device_name" IS '设备名称';
COMMENT ON COLUMN "FWBZ"."alarm_record"."space_id" IS '空间ID';
COMMENT ON COLUMN "FWBZ"."alarm_record"."space_name" IS '空间名称';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_content" IS '告警内容';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_time" IS '告警时间';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_category_id" IS '告警类别ID';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_category_name" IS '告警类别名称';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_level_id" IS '告警级别ID';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_level_name" IS '告警级别名称';
COMMENT ON COLUMN "FWBZ"."alarm_record"."charge_person" IS '负责人ID';
COMMENT ON COLUMN "FWBZ"."alarm_record"."charge_person_name" IS '负责人名称';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_status" IS '告警状态【1-未处理，2-已消除】';
COMMENT ON COLUMN "FWBZ"."alarm_record"."point_id" IS '点位id';
COMMENT ON COLUMN "FWBZ"."alarm_record"."point_name" IS '点位名称';
COMMENT ON COLUMN "FWBZ"."alarm_record"."value" IS '告警值';
COMMENT ON COLUMN "FWBZ"."alarm_record"."condition_value" IS '阈值';
COMMENT ON COLUMN "FWBZ"."alarm_record"."operator" IS '条件';
COMMENT ON COLUMN "FWBZ"."alarm_record"."time_granularity" IS '时间粒度';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_rule_point_id" IS '告警规则点位id';
COMMENT ON COLUMN "FWBZ"."alarm_record"."device_category_id" IS '设备类别id';
COMMENT ON COLUMN "FWBZ"."alarm_record"."alarm_level_color" IS '报警级别颜色';
COMMENT ON COLUMN "FWBZ"."alarm_record"."event_id" IS '事件id';
COMMENT ON TABLE "FWBZ"."alarm_record" IS '告警记录表';

-- ----------------------------
-- Table structure for alarm_rule_point
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."alarm_rule_point";
CREATE TABLE "FWBZ"."alarm_rule_point" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "alarm_rule_id" BIGINT,
  "device_id" BIGINT NOT NULL,
  "device_name" VARCHAR(255 CHAR),
  "point_id" BIGINT,
  "point_name" VARCHAR(255 CHAR),
  "time_granularity" VARCHAR(50 CHAR),
  "operator" VARCHAR(10 CHAR) NOT NULL,
  "condition_value" VARCHAR(255 CHAR) NOT NULL
)
;
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."id" IS '主键ID';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."alarm_rule_id" IS '告警规则ID';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."device_id" IS '设备ID';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."device_name" IS '设备名称';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."point_id" IS '点位ID';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."point_name" IS '点位名称';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."time_granularity" IS '时间粒度（hour/day/month/year）';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."operator" IS '条件运算符（如 >, <, = 等）';
COMMENT ON COLUMN "FWBZ"."alarm_rule_point"."condition_value" IS '条件值';
COMMENT ON TABLE "FWBZ"."alarm_rule_point" IS '告警规则设备点位配置表';

-- ----------------------------
-- Table structure for alarm_rules
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."alarm_rules";
CREATE TABLE "FWBZ"."alarm_rules" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "rule_code" VARCHAR(255 CHAR),
  "rule_name" VARCHAR(255 CHAR),
  "alarm_category_id" BIGINT,
  "alarm_level_id" BIGINT,
  "frequency" INT,
  "frequency_unit" VARCHAR(50 CHAR),
  "point_type" VARCHAR(50 CHAR),
  "notice_user" VARCHAR(2000 CHAR),
  "enabled_status" VARCHAR(2 CHAR),
  "alarm_category_name" VARCHAR(255 CHAR),
  "alarm_level_name" VARCHAR(255 CHAR),
  "alarm_level_color" VARCHAR(50)
)
;
COMMENT ON COLUMN "FWBZ"."alarm_rules"."id" IS '主键ID';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."rule_code" IS '规则编号';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."rule_name" IS '规则名称';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."alarm_category_id" IS '报警类别';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."alarm_level_id" IS '报警等级';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."frequency" IS '频率';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."frequency_unit" IS '频率单位';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."point_type" IS '报警点位类型（instant/accumulate）';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."notice_user" IS '通知用户ID';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."enabled_status" IS '启用状态【0-禁用，1-启用】';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."alarm_category_name" IS '报警类别名称';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."alarm_level_name" IS '报警级别名称';
COMMENT ON COLUMN "FWBZ"."alarm_rules"."alarm_level_color" IS '报警颜色';
COMMENT ON TABLE "FWBZ"."alarm_rules" IS '告警规则表';

-- ----------------------------
-- Table structure for building_control_point
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."building_control_point";
CREATE TABLE "FWBZ"."building_control_point" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "gateway_adr" VARCHAR(255 CHAR) NOT NULL,
  "bacnet_adr" VARCHAR(255 CHAR) NOT NULL,
  "value" VARCHAR(255 CHAR),
  "collection_time" TIMESTAMP,
  "content" VARCHAR(2000 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."building_control_point"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."building_control_point"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."building_control_point"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."building_control_point"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."building_control_point"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."building_control_point"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."building_control_point"."value" IS '采集值';
COMMENT ON COLUMN "FWBZ"."building_control_point"."collection_time" IS '采集时间';
COMMENT ON TABLE "FWBZ"."building_control_point" IS '楼宇控制点表';

-- ----------------------------
-- Table structure for building_control_point_history
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."building_control_point_history";
CREATE TABLE "FWBZ"."building_control_point_history" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "point_id" BIGINT NOT NULL,
  "value" VARCHAR(255 CHAR),
  "collection_time" TIMESTAMP
)
;

-- ----------------------------
-- Table structure for business_config
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."business_config";
CREATE TABLE "FWBZ"."business_config" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "name" VARCHAR(255 CHAR),
  "config_key" VARCHAR(255 CHAR),
  "config_value" VARCHAR(2000 CHAR),
  "remark" VARCHAR(200)
)
;
COMMENT ON COLUMN "FWBZ"."business_config"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."business_config"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."business_config"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."business_config"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."business_config"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."business_config"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."business_config"."name" IS '业务说明';
COMMENT ON COLUMN "FWBZ"."business_config"."config_key" IS '唯一标识';
COMMENT ON COLUMN "FWBZ"."business_config"."config_value" IS '值';
COMMENT ON COLUMN "FWBZ"."business_config"."remark" IS '备注';
COMMENT ON TABLE "FWBZ"."business_config" IS '业务配置表';

-- ----------------------------
-- Table structure for carbon_emission_factor
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."carbon_emission_factor";
CREATE TABLE "FWBZ"."carbon_emission_factor" (
  "id" VARCHAR(36 CHAR) NOT NULL,
  "create_by" VARCHAR(50 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(50 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(64 CHAR),
  "carbon_factor_name" VARCHAR(32 CHAR),
  "coefficient" VARCHAR(32 CHAR),
  "unit" VARCHAR(32 CHAR),
  "sort" INT,
  "remark" VARCHAR(32 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."carbon_factor_name" IS '碳因子名称';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."coefficient" IS '系数';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."unit" IS '单位';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."carbon_emission_factor"."remark" IS '说明';

-- ----------------------------
-- Table structure for data_amend_log
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."data_amend_log";
CREATE TABLE "FWBZ"."data_amend_log" (
  "id" "bigint" NOT NULL,
  "device_id" "bigint",
  "hour_data_id" "bigint",
  "time" TIMESTAMP,
  "start_value" DECIMAL(22,6),
  "end_value" DECIMAL(22,6),
  "compute_value" DECIMAL(22,6),
  "original_value" DECIMAL(22,6),
  "value" DECIMAL(22,6),
  "update_by" VARCHAR(50),
  "update_time" TIMESTAMP
)
;
COMMENT ON COLUMN "FWBZ"."data_amend_log"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."device_id" IS '设备id';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."hour_data_id" IS '小时能耗id';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."time" IS '时间';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."start_value" IS '起始值';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."end_value" IS '结束值';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."compute_value" IS '计算值';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."original_value" IS '修正前';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."value" IS '修正后';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."update_by" IS '修正人';
COMMENT ON COLUMN "FWBZ"."data_amend_log"."update_time" IS '修正时间';
COMMENT ON TABLE "FWBZ"."data_amend_log" IS '数据修正日志';

-- ----------------------------
-- Table structure for data_day
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."data_day";
CREATE TABLE "FWBZ"."data_day" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "device_id" BIGINT,
  "value" DECIMAL(38,4),
  "time" TIMESTAMP
)
;
COMMENT ON COLUMN "FWBZ"."data_day"."device_id" IS '设备id';
COMMENT ON COLUMN "FWBZ"."data_day"."value" IS '数值';
COMMENT ON COLUMN "FWBZ"."data_day"."time" IS '时间';
COMMENT ON TABLE "FWBZ"."data_day" IS '日数据';

-- ----------------------------
-- Table structure for data_hour
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."data_hour";
CREATE TABLE "FWBZ"."data_hour" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "device_id" BIGINT,
  "value" DECIMAL(38,4),
  "time" TIMESTAMP,
  "start_value" DECIMAL(38,4),
  "end_value" DECIMAL(38,4),
  "compute_value" DECIMAL(38,4),
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP
)
;

-- ----------------------------
-- Table structure for data_minute
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."data_minute";
CREATE TABLE "FWBZ"."data_minute" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "device_id" BIGINT,
  "time" TIMESTAMP,
  "start_value" DECIMAL(19,2),
  "end_value" DECIMAL(19,2),
  "value" DECIMAL(10,2)
)
;

-- ----------------------------
-- Table structure for data_month
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."data_month";
CREATE TABLE "FWBZ"."data_month" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "device_id" BIGINT,
  "value" DECIMAL(38,4),
  "time" TIMESTAMP
)
;
COMMENT ON COLUMN "FWBZ"."data_month"."device_id" IS '设备id';
COMMENT ON COLUMN "FWBZ"."data_month"."value" IS '数值';
COMMENT ON COLUMN "FWBZ"."data_month"."time" IS '时间';
COMMENT ON TABLE "FWBZ"."data_month" IS '月数据';

-- ----------------------------
-- Table structure for data_real
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."data_real";
CREATE TABLE "FWBZ"."data_real" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "device_id" BIGINT,
  "value" DECIMAL(38,4),
  "time" TIMESTAMP
)
;

-- ----------------------------
-- Table structure for data_year
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."data_year";
CREATE TABLE "FWBZ"."data_year" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "device_id" BIGINT,
  "value" DECIMAL(38,4),
  "time" TIMESTAMP
)
;
COMMENT ON COLUMN "FWBZ"."data_year"."device_id" IS '设备id';
COMMENT ON COLUMN "FWBZ"."data_year"."value" IS '数值';
COMMENT ON COLUMN "FWBZ"."data_year"."time" IS '时间';
COMMENT ON TABLE "FWBZ"."data_year" IS '年数据';

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device";
CREATE TABLE "FWBZ"."device" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "device_code" VARCHAR(255 CHAR),
  "device_name" VARCHAR(255 CHAR),
  "category_id" BIGINT,
  "space_id" BIGINT,
  "magnification" DECIMAL(19,4),
  "automatic_algorithm" VARCHAR(255 CHAR),
  "sort" INT,
  "remark" TEXT,
  "run_state" VARCHAR(255 CHAR),
  "model_id" BIGINT,
  "device_type" VARCHAR(2 CHAR),
  "last_gather_time" TIMESTAMP
)
;
COMMENT ON COLUMN "FWBZ"."device"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device"."device_code" IS '设备编号';
COMMENT ON COLUMN "FWBZ"."device"."device_name" IS '设备名称';
COMMENT ON COLUMN "FWBZ"."device"."category_id" IS '设备类别id';
COMMENT ON COLUMN "FWBZ"."device"."space_id" IS '空间位置id';
COMMENT ON COLUMN "FWBZ"."device"."magnification" IS '倍率';
COMMENT ON COLUMN "FWBZ"."device"."automatic_algorithm" IS '自动算法';
COMMENT ON COLUMN "FWBZ"."device"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."device"."remark" IS '备注';
COMMENT ON COLUMN "FWBZ"."device"."run_state" IS '运行状态';
COMMENT ON COLUMN "FWBZ"."device"."model_id" IS '设备模型id';
COMMENT ON COLUMN "FWBZ"."device"."device_type" IS '设备分类。仪表：1；设备：2；';
COMMENT ON COLUMN "FWBZ"."device"."last_gather_time" IS '最后采集时间';
COMMENT ON TABLE "FWBZ"."device" IS '设备基础信息';

-- ----------------------------
-- Table structure for device_251126
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_251126";
CREATE TABLE "FWBZ"."device_251126" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "device_code" VARCHAR(255 CHAR),
  "device_name" VARCHAR(255 CHAR),
  "category_id" BIGINT,
  "space_id" BIGINT,
  "magnification" DECIMAL(19,4),
  "automatic_algorithm" VARCHAR(255 CHAR),
  "sort" INT,
  "remark" TEXT,
  "run_state" VARCHAR(255 CHAR),
  "model_id" BIGINT,
  "device_type" VARCHAR(2 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."device_251126"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_251126"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_251126"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_251126"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device_251126"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device_251126"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device_251126"."device_code" IS '设备编号';
COMMENT ON COLUMN "FWBZ"."device_251126"."device_name" IS '设备名称';
COMMENT ON COLUMN "FWBZ"."device_251126"."category_id" IS '设备类别id';
COMMENT ON COLUMN "FWBZ"."device_251126"."space_id" IS '空间位置id';
COMMENT ON COLUMN "FWBZ"."device_251126"."magnification" IS '倍率';
COMMENT ON COLUMN "FWBZ"."device_251126"."automatic_algorithm" IS '自动算法';
COMMENT ON COLUMN "FWBZ"."device_251126"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."device_251126"."remark" IS '备注';
COMMENT ON COLUMN "FWBZ"."device_251126"."run_state" IS '运行状态';
COMMENT ON COLUMN "FWBZ"."device_251126"."model_id" IS '设备模型id';
COMMENT ON COLUMN "FWBZ"."device_251126"."device_type" IS '设备分类。仪表：1；设备：2；';
COMMENT ON TABLE "FWBZ"."device_251126" IS '设备基础信息';

-- ----------------------------
-- Table structure for device_attribute
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_attribute";
CREATE TABLE "FWBZ"."device_attribute" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "device_id" BIGINT,
  "attribute_name" VARCHAR(255 CHAR),
  "attribute_code" VARCHAR(255 CHAR),
  "unit" VARCHAR(255 CHAR),
  "readwrite_level" VARCHAR(255 CHAR),
  "sort" INT,
  "value" DECIMAL(19,4),
  "gather_time" TIMESTAMP,
  "acquisition_coding" VARCHAR(255 CHAR),
  "value_type" VARCHAR(50),
  "value_config" VARCHAR(2000)
)
;
COMMENT ON COLUMN "FWBZ"."device_attribute"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_attribute"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_attribute"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_attribute"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device_attribute"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device_attribute"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device_attribute"."device_id" IS '设备id';
COMMENT ON COLUMN "FWBZ"."device_attribute"."attribute_name" IS '属性名称';
COMMENT ON COLUMN "FWBZ"."device_attribute"."attribute_code" IS '属性编码';
COMMENT ON COLUMN "FWBZ"."device_attribute"."unit" IS '单位';
COMMENT ON COLUMN "FWBZ"."device_attribute"."readwrite_level" IS '读写等级';
COMMENT ON COLUMN "FWBZ"."device_attribute"."sort" IS '排序字段';
COMMENT ON COLUMN "FWBZ"."device_attribute"."value" IS '采集值';
COMMENT ON COLUMN "FWBZ"."device_attribute"."gather_time" IS '采集时间';
COMMENT ON COLUMN "FWBZ"."device_attribute"."acquisition_coding" IS '采集编码';
COMMENT ON COLUMN "FWBZ"."device_attribute"."value_type" IS '属性值类型';
COMMENT ON COLUMN "FWBZ"."device_attribute"."value_config" IS '属性值配置';
COMMENT ON TABLE "FWBZ"."device_attribute" IS '设备基础信息';

-- ----------------------------
-- Table structure for device_attribute_251201
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_attribute_251201";
CREATE TABLE "FWBZ"."device_attribute_251201" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "device_id" BIGINT,
  "attribute_name" VARCHAR(255 CHAR),
  "attribute_code" VARCHAR(255 CHAR),
  "unit" VARCHAR(255 CHAR),
  "readwrite_level" VARCHAR(255 CHAR),
  "sort" INT,
  "value" DECIMAL(19,4),
  "gather_time" TIMESTAMP,
  "acquisition_coding" VARCHAR(255 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."device_attribute_251201"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_attribute_251201"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_attribute_251201"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_attribute_251201"."device_id" IS '设备id';
COMMENT ON COLUMN "FWBZ"."device_attribute_251201"."attribute_name" IS '属性名称';
COMMENT ON COLUMN "FWBZ"."device_attribute_251201"."attribute_code" IS '属性编码';
COMMENT ON COLUMN "FWBZ"."device_attribute_251201"."gather_time" IS '采集时间';
COMMENT ON COLUMN "FWBZ"."device_attribute_251201"."acquisition_coding" IS '采集编码';
COMMENT ON TABLE "FWBZ"."device_attribute_251201" IS '设备基础信息';

-- ----------------------------
-- Table structure for device_attribute_251209
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_attribute_251209";
CREATE TABLE "FWBZ"."device_attribute_251209" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "device_id" BIGINT,
  "attribute_name" VARCHAR(255 CHAR),
  "attribute_code" VARCHAR(255 CHAR),
  "unit" VARCHAR(255 CHAR),
  "readwrite_level" VARCHAR(255 CHAR),
  "sort" INT,
  "value" DECIMAL(19,4),
  "gather_time" TIMESTAMP,
  "acquisition_coding" VARCHAR(255 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."device_id" IS '设备id';
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."attribute_name" IS '属性名称';
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."attribute_code" IS '属性编码';
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."readwrite_level" IS '读写等级';
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."gather_time" IS '采集时间';
COMMENT ON COLUMN "FWBZ"."device_attribute_251209"."acquisition_coding" IS '采集编码';
COMMENT ON TABLE "FWBZ"."device_attribute_251209" IS '设备基础信息';

-- ----------------------------
-- Table structure for device_attribute_config
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_attribute_config";
CREATE TABLE "FWBZ"."device_attribute_config" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "label" VARCHAR(255 CHAR),
  "code" VARCHAR(255 CHAR),
  "sort" INT
)
;
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."label" IS '属性名称';
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."code" IS '属性key';
COMMENT ON COLUMN "FWBZ"."device_attribute_config"."sort" IS '排序';
COMMENT ON TABLE "FWBZ"."device_attribute_config" IS '设备采集点位配置';

-- ----------------------------
-- Table structure for device_attribute_data
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_attribute_data";
CREATE TABLE "FWBZ"."device_attribute_data" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "device_id" BIGINT,
  "attribute_id" BIGINT,
  "value" VARCHAR(255 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."device_id" IS '设备ID';
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."attribute_id" IS '属性ID';
COMMENT ON COLUMN "FWBZ"."device_attribute_data"."value" IS '值';
COMMENT ON TABLE "FWBZ"."device_attribute_data" IS '设备采集点位数据';

-- ----------------------------
-- Table structure for device_data_temp
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_data_temp";
CREATE TABLE "FWBZ"."device_data_temp" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "time" VARCHAR(50),
  "device_code" BIGINT,
  "value" DECIMAL(38,4)
)
;

-- ----------------------------
-- Table structure for device_energy_consumption
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_energy_consumption";
CREATE TABLE "FWBZ"."device_energy_consumption" (
  "device_code" VARCHAR(50),
  "device_name" VARCHAR(100),
  "date" VARCHAR(50),
  "value" DECIMAL(22,2),
  "device_id" "bigint"
)
;
COMMENT ON TABLE "FWBZ"."device_energy_consumption" IS '临时历史数据导入';

-- ----------------------------
-- Table structure for device_model
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_model";
CREATE TABLE "FWBZ"."device_model" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "model_name" VARCHAR(255 CHAR),
  "category_id" BIGINT
)
;
COMMENT ON COLUMN "FWBZ"."device_model"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_model"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_model"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_model"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device_model"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device_model"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device_model"."model_name" IS '模型名称';
COMMENT ON COLUMN "FWBZ"."device_model"."category_id" IS '专业id';
COMMENT ON TABLE "FWBZ"."device_model" IS '设备模型';

-- ----------------------------
-- Table structure for device_model_attribute
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_model_attribute";
CREATE TABLE "FWBZ"."device_model_attribute" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "model_id" BIGINT,
  "attribute_name" VARCHAR(255 CHAR),
  "unit" VARCHAR(255 CHAR),
  "attribute_code" VARCHAR(255 CHAR),
  "readwrite_level" VARCHAR(2 CHAR),
  "sort" INT,
  "value_type" VARCHAR(50),
  "value_config" VARCHAR(2000)
)
;
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."model_id" IS '模型id';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."attribute_name" IS '属性名称';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."unit" IS '单位';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."attribute_code" IS '属性编码';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."readwrite_level" IS '读写等级';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."sort" IS '排序字段';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."value_type" IS '属性值类型';
COMMENT ON COLUMN "FWBZ"."device_model_attribute"."value_config" IS '属性值配置';
COMMENT ON TABLE "FWBZ"."device_model_attribute" IS '设备模型属性';

-- ----------------------------
-- Table structure for device_static_data
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_static_data";
CREATE TABLE "FWBZ"."device_static_data" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "device_id" BIGINT,
  "config_id" BIGINT,
  "value" TEXT
)
;
COMMENT ON COLUMN "FWBZ"."device_static_data"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_static_data"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_static_data"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_static_data"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device_static_data"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device_static_data"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device_static_data"."device_id" IS '设备id';
COMMENT ON COLUMN "FWBZ"."device_static_data"."config_id" IS '配置id';
COMMENT ON COLUMN "FWBZ"."device_static_data"."value" IS '值';
COMMENT ON TABLE "FWBZ"."device_static_data" IS '设备静态数据';

-- ----------------------------
-- Table structure for device_static_data_config
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_static_data_config";
CREATE TABLE "FWBZ"."device_static_data_config" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "type" VARCHAR(255 CHAR),
  "label" VARCHAR(255 CHAR),
  "value_type" VARCHAR(255 CHAR),
  "value_data" TEXT,
  "sort" INT
)
;
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."type" IS '类型。基本信息：base；技术参数：tech；服务厂商：vendor';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."label" IS '标签';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."value_type" IS '数据类型。文本输入框：input；下拉框：select；日期选择框：datePicker';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."value_data" IS '数据源';
COMMENT ON COLUMN "FWBZ"."device_static_data_config"."sort" IS '排序字段';
COMMENT ON TABLE "FWBZ"."device_static_data_config" IS '设备静态数据配置';

-- ----------------------------
-- Table structure for device_temp
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_temp";
CREATE TABLE "FWBZ"."device_temp" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "device_name" VARCHAR(200),
  "device_code" VARCHAR(200),
  "device_new_code" VARCHAR(200),
  "device_id" BIGINT
)
;

-- ----------------------------
-- Table structure for device_temp_251126
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."device_temp_251126";
CREATE TABLE "FWBZ"."device_temp_251126" (
  "id" "bigint" NOT NULL,
  "device_name" VARCHAR(50)
)
;
COMMENT ON TABLE "FWBZ"."device_temp_251126" IS '设备信息临时表';

-- ----------------------------
-- Table structure for energy_analysis_benchmark
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."energy_analysis_benchmark";
CREATE TABLE "FWBZ"."energy_analysis_benchmark" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "config_id" BIGINT NOT NULL,
  "label" VARCHAR(255 CHAR) NOT NULL,
  "value" VARCHAR(255 CHAR) NOT NULL,
  "operator" VARCHAR(10 CHAR) NOT NULL,
  "content" VARCHAR(255 CHAR),
  "sort" INT
)
;
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."id" IS '主键ID';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."config_id" IS '能效分析配置Id';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."label" IS '文本';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."value" IS '基准值';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."operator" IS '运算符';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."content" IS '提示信息';
COMMENT ON COLUMN "FWBZ"."energy_analysis_benchmark"."sort" IS '排序字段';

-- ----------------------------
-- Table structure for energy_analysis_chart
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."energy_analysis_chart";
CREATE TABLE "FWBZ"."energy_analysis_chart" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "config_id" BIGINT NOT NULL,
  "chart_name" VARCHAR(255 CHAR) NOT NULL,
  "chart_type" VARCHAR(50 CHAR) NOT NULL,
  "point_id" BIGINT NOT NULL,
  "sort" BIGINT,
  "unit" VARCHAR(255 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."config_id" IS '能效分析配置id';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."chart_name" IS '图标名称';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."chart_type" IS '图表类型。饼：pie；柱状：bar；折线：line；堆叠柱状：stackedColumn；';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."point_id" IS '计量规则id';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."sort" IS '排序字段';
COMMENT ON COLUMN "FWBZ"."energy_analysis_chart"."unit" IS '单位';

-- ----------------------------
-- Table structure for energy_analysis_config
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."energy_analysis_config";
CREATE TABLE "FWBZ"."energy_analysis_config" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "name" VARCHAR(255 CHAR) NOT NULL,
  "remark" VARCHAR(255 CHAR),
  "sort" INT,
  "status" VARCHAR(2 CHAR) NOT NULL
)
;
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."name" IS '名称';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."remark" IS '备注';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."energy_analysis_config"."status" IS '状态。启用：1；禁用：0';

-- ----------------------------
-- Table structure for energy_attribute_management
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."energy_attribute_management";
CREATE TABLE "FWBZ"."energy_attribute_management" (
  "id" VARCHAR(32 CHAR) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "attribute_name" VARCHAR(255 CHAR),
  "attribute_type" VARCHAR(255 CHAR),
  "sort" INT,
  "remark" TEXT
)
;
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."attribute_name" IS '属性名称';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."attribute_type" IS '属性类别';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."energy_attribute_management"."remark" IS '说明';
COMMENT ON TABLE "FWBZ"."energy_attribute_management" IS '能源属性管理';

-- ----------------------------
-- Table structure for energy_flow_diagram_config
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."energy_flow_diagram_config";
CREATE TABLE "FWBZ"."energy_flow_diagram_config" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "type" VARCHAR(255 CHAR),
  "node_name" VARCHAR(255 CHAR),
  "parent_id" BIGINT,
  "metering_point_id" BIGINT,
  "sort" INT
)
;
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."type" IS '类型。数据字典：energy_flow_type';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."node_name" IS '节点名称';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."parent_id" IS '父节点';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."metering_point_id" IS '计量点位';
COMMENT ON COLUMN "FWBZ"."energy_flow_diagram_config"."sort" IS '排序';
COMMENT ON TABLE "FWBZ"."energy_flow_diagram_config" IS '能流图配置';

-- ----------------------------
-- Table structure for energy_medium_manage
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."energy_medium_manage";
CREATE TABLE "FWBZ"."energy_medium_manage" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "pid" BIGINT,
  "has_child" VARCHAR(10 CHAR),
  "code" VARCHAR(255 CHAR),
  "name" VARCHAR(255 CHAR),
  "standard_unit" BIGINT,
  "sort" INT,
  "time_sharing" VARCHAR(255 CHAR),
  "remark" TEXT
)
;
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."pid" IS '父级节点';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."has_child" IS '是否有子节点';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."code" IS '能介编码';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."name" IS '能介名称';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."standard_unit" IS '标准单位';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."time_sharing" IS '分时计量';
COMMENT ON COLUMN "FWBZ"."energy_medium_manage"."remark" IS '说明';
COMMENT ON TABLE "FWBZ"."energy_medium_manage" IS '能介管理';

-- ----------------------------
-- Table structure for energy_price
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."energy_price";
CREATE TABLE "FWBZ"."energy_price" (
  "id" VARCHAR(36 CHAR) NOT NULL,
  "create_by" VARCHAR(50 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(50 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(64 CHAR),
  "energy_medium" VARCHAR(32 CHAR),
  "unit_price" DECIMAL(10,5),
  "unit" VARCHAR(32 CHAR),
  "sort" INT,
  "remark" VARCHAR(32 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."energy_price"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."energy_price"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."energy_price"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."energy_price"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."energy_price"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."energy_price"."energy_medium" IS '能源介质';
COMMENT ON COLUMN "FWBZ"."energy_price"."unit_price" IS '单价';
COMMENT ON COLUMN "FWBZ"."energy_price"."unit" IS '单位';
COMMENT ON COLUMN "FWBZ"."energy_price"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."energy_price"."remark" IS '说明';

-- ----------------------------
-- Table structure for energy_pricing_config
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."energy_pricing_config";
CREATE TABLE "FWBZ"."energy_pricing_config" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "category_id" BIGINT,
  "category" VARCHAR(255 CHAR),
  "billing_way" VARCHAR(255 CHAR),
  "fixed_unit_price" DECIMAL(18,2),
  "step1_max" DECIMAL(18,2),
  "step1_unit_price" DECIMAL(18,2),
  "step2_max" DECIMAL(18,2),
  "step2_min" DECIMAL(18,2),
  "step2_unit_price" DECIMAL(18,2),
  "step3_min" DECIMAL(18,2),
  "step3_unit_price" DECIMAL(18,2),
  "tip_price" DECIMAL(18,2),
  "peak_price" DECIMAL(18,2),
  "flat_price" DECIMAL(18,2),
  "valley_price" DECIMAL(18,2),
  "apply_months1" VARCHAR(255 CHAR),
  "tip_time_slot1" VARCHAR(255 CHAR),
  "peak_time_slot1" VARCHAR(255 CHAR),
  "flat_time_slot1" VARCHAR(255 CHAR),
  "valley_time_slot1" VARCHAR(255 CHAR),
  "apply_months2" VARCHAR(255 CHAR),
  "tip_time_slot2" VARCHAR(255 CHAR),
  "peak_time_slot2" VARCHAR(255 CHAR),
  "flat_time_slot2" VARCHAR(255 CHAR),
  "valley_time_slot2" VARCHAR(255 CHAR),
  "status" VARCHAR(255 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."category_id" IS '仪表类别id';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."category" IS '类别。电：electricity；水：water；热：heating';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."billing_way" IS '计价方式 1-峰谷分时计价 2-固定计价 3-阶梯计价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."fixed_unit_price" IS '固定单价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."step1_max" IS '阶梯计价-第一阶段-最大值';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."step1_unit_price" IS '阶梯计价-第一阶段-单价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."step2_max" IS '阶梯计价-第二阶段-最大值';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."step2_min" IS '阶梯计价-第二阶段-最小值';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."step2_unit_price" IS '阶梯计价-第二阶段-单价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."step3_min" IS '阶梯计价-第三阶段-最小值';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."step3_unit_price" IS '阶梯计价-第三阶段-单价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."tip_price" IS '峰谷分时计价-尖电价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."peak_price" IS '峰谷分时计价-峰电价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."flat_price" IS '峰谷分时计价-平电价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."valley_price" IS '峰谷分时计价-谷电价';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."apply_months1" IS '峰谷分时计价-适用月份1';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."tip_time_slot1" IS '峰谷分时计价-尖时段1';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."peak_time_slot1" IS '峰谷分时计价-峰时段1';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."flat_time_slot1" IS '峰谷分时计价-平时段1';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."valley_time_slot1" IS '峰谷分时计价-谷时段1';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."apply_months2" IS '峰谷分时计价-适用月份2';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."tip_time_slot2" IS '峰谷分时计价-尖时段2';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."peak_time_slot2" IS '峰谷分时计价-峰时段2';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."flat_time_slot2" IS '峰谷分时计价-平时段2';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."valley_time_slot2" IS '峰谷分时计价-谷时段2';
COMMENT ON COLUMN "FWBZ"."energy_pricing_config"."status" IS '启用：1；禁用：0';

-- ----------------------------
-- Table structure for equipment_category
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."equipment_category";
CREATE TABLE "FWBZ"."equipment_category" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "pid" BIGINT,
  "has_child" VARCHAR(10 CHAR),
  "category_name" VARCHAR(255 CHAR),
  "sort" INT,
  "remark" TEXT,
  "full_name" VARCHAR(255 CHAR),
  "full_id" VARCHAR(255 CHAR),
  "type" VARCHAR(2 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."equipment_category"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."equipment_category"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."equipment_category"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."equipment_category"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."equipment_category"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."equipment_category"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."equipment_category"."pid" IS '父级节点';
COMMENT ON COLUMN "FWBZ"."equipment_category"."has_child" IS '是否有子节点';
COMMENT ON COLUMN "FWBZ"."equipment_category"."category_name" IS '类别名称';
COMMENT ON COLUMN "FWBZ"."equipment_category"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."equipment_category"."remark" IS '备注';
COMMENT ON COLUMN "FWBZ"."equipment_category"."full_name" IS '全称';
COMMENT ON COLUMN "FWBZ"."equipment_category"."full_id" IS '父级id';
COMMENT ON COLUMN "FWBZ"."equipment_category"."type" IS '分类。仪表：1；设备：2；';
COMMENT ON TABLE "FWBZ"."equipment_category" IS '设备类别';

-- ----------------------------
-- Table structure for gather_rule_config
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."gather_rule_config";
CREATE TABLE "FWBZ"."gather_rule_config" (
  "id" VARCHAR(32 CHAR) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "gateway_code" VARCHAR(255 CHAR),
  "gateway_name" VARCHAR(255 CHAR),
  "gateway_type" VARCHAR(255 CHAR),
  "install_addr" BIGINT,
  "ip" VARCHAR(255 CHAR),
  "protocol" VARCHAR(255 CHAR),
  "state" VARCHAR(255 CHAR),
  "last_collection_time" TIMESTAMP,
  "frequency" INT
)
;
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."gateway_code" IS '网关编号';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."gateway_name" IS '网关名称';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."gateway_type" IS '网关类型';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."install_addr" IS '安装位置';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."ip" IS 'ip';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."protocol" IS '通讯协议';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."state" IS '状态';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."last_collection_time" IS '最后采集时间';
COMMENT ON COLUMN "FWBZ"."gather_rule_config"."frequency" IS '采集频率/s';
COMMENT ON TABLE "FWBZ"."gather_rule_config" IS '采集管理-规则标准';

-- ----------------------------
-- Table structure for lighting_area
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."lighting_area";
CREATE TABLE "FWBZ"."lighting_area" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "area_name" VARCHAR(200),
  "area_code" VARCHAR(200),
  "status" VARCHAR(50),
  "sort" INT,
  "space" VARCHAR(50),
  "location" VARCHAR(200),
  "monitor_adr" VARCHAR(200),
  "remark" VARCHAR(50),
  "type" VARCHAR(50),
  "space_name" VARCHAR(50),
  "start_time" TIMESTAMP,
  "closing_time" VARCHAR(50),
  "all_duration" BIGINT,
  "open_code" VARCHAR(50),
  "close_code" VARCHAR(50),
  "rel_name" VARCHAR(50)
)
;
COMMENT ON COLUMN "FWBZ"."lighting_area"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."lighting_area"."area_name" IS '区域名称';
COMMENT ON COLUMN "FWBZ"."lighting_area"."area_code" IS '区域编码';
COMMENT ON COLUMN "FWBZ"."lighting_area"."status" IS '状态';
COMMENT ON COLUMN "FWBZ"."lighting_area"."sort" IS '排序字段';
COMMENT ON COLUMN "FWBZ"."lighting_area"."space" IS '金安桥：1；一高炉：2';
COMMENT ON COLUMN "FWBZ"."lighting_area"."location" IS '位置信息';
COMMENT ON COLUMN "FWBZ"."lighting_area"."monitor_adr" IS '监控信息';
COMMENT ON COLUMN "FWBZ"."lighting_area"."remark" IS '备注';
COMMENT ON COLUMN "FWBZ"."lighting_area"."type" IS '建筑：1、区域：2';
COMMENT ON COLUMN "FWBZ"."lighting_area"."space_name" IS '空间名称';
COMMENT ON COLUMN "FWBZ"."lighting_area"."start_time" IS '场景启动时间';
COMMENT ON COLUMN "FWBZ"."lighting_area"."closing_time" IS '场景关闭时间';
COMMENT ON COLUMN "FWBZ"."lighting_area"."all_duration" IS '开启时长，单位：秒';
COMMENT ON COLUMN "FWBZ"."lighting_area"."open_code" IS '场景开启码';
COMMENT ON COLUMN "FWBZ"."lighting_area"."close_code" IS '场景关闭码';
COMMENT ON COLUMN "FWBZ"."lighting_area"."rel_name" IS '关联名称';
COMMENT ON TABLE "FWBZ"."lighting_area" IS '照明-区域';

-- ----------------------------
-- Table structure for lighting_circuit
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."lighting_circuit";
CREATE TABLE "FWBZ"."lighting_circuit" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "circuit_name" VARCHAR(50),
  "circuit_code" VARCHAR(50),
  "status" VARCHAR(50),
  "area_id" BIGINT,
  "start_time" TIMESTAMP,
  "closing_time" TIMESTAMP,
  "all_duration" BIGINT,
  "operator_by" VARCHAR(50),
  "operator_time" TIMESTAMP,
  "area_code" VARCHAR(50),
  "comstat" VARCHAR(50)
)
;
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."circuit_name" IS '回路名称';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."circuit_code" IS '回路编码';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."status" IS '状态。开启、关闭';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."area_id" IS '所在区域';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."start_time" IS '开启时间';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."closing_time" IS '关闭时间';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."all_duration" IS '开启总时长';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."operator_by" IS '操作人';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."operator_time" IS '操作时间';
COMMENT ON COLUMN "FWBZ"."lighting_circuit"."comstat" IS '通讯状态';
COMMENT ON TABLE "FWBZ"."lighting_circuit" IS '照明-回路';

-- ----------------------------
-- Table structure for lighting_operation_log
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."lighting_operation_log";
CREATE TABLE "FWBZ"."lighting_operation_log" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "rel_type" VARCHAR(50) NOT NULL,
  "rel_id" BIGINT NOT NULL,
  "name" VARCHAR(200),
  "operation_type" VARCHAR(50),
  "operation_time" TIMESTAMP,
  "operation_by" VARCHAR(200)
)
;
COMMENT ON TABLE "FWBZ"."lighting_operation_log" IS '照明控制记录';

-- ----------------------------
-- Table structure for lighting_plan
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."lighting_plan";
CREATE TABLE "FWBZ"."lighting_plan" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "plan_name" VARCHAR(50),
  "rel_type" VARCHAR(50),
  "rel_ids" VARCHAR(2000),
  "execution_time" TIME,
  "operation_type" VARCHAR(50),
  "status" VARCHAR(50),
  "version" BIGINT,
  "create_by" VARCHAR(50),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(50),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(50),
  "sort" INT
)
;
COMMENT ON COLUMN "FWBZ"."lighting_plan"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."lighting_plan"."plan_name" IS '计划名称';
COMMENT ON COLUMN "FWBZ"."lighting_plan"."rel_type" IS '区域、回路';
COMMENT ON COLUMN "FWBZ"."lighting_plan"."rel_ids" IS '关联id，多个以英文逗号分隔';
COMMENT ON COLUMN "FWBZ"."lighting_plan"."execution_time" IS '执行时间';
COMMENT ON COLUMN "FWBZ"."lighting_plan"."operation_type" IS '操作类型。开启、关闭';
COMMENT ON COLUMN "FWBZ"."lighting_plan"."sort" IS '排序字段，升序排列';
COMMENT ON TABLE "FWBZ"."lighting_plan" IS '照明计划';

-- ----------------------------
-- Table structure for lighting_plan_execution_time
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."lighting_plan_execution_time";
CREATE TABLE "FWBZ"."lighting_plan_execution_time" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "plan_id" BIGINT NOT NULL,
  "execution_time" TIME,
  "start_date" DATE,
  "end_date" DATE,
  "enabled_week" VARCHAR(50),
  "version" VARCHAR(50)
)
;

-- ----------------------------
-- Table structure for linkage_front_point
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."linkage_front_point";
CREATE TABLE "FWBZ"."linkage_front_point" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "linkage_strategy_id" BIGINT NOT NULL,
  "device_id" BIGINT NOT NULL,
  "device_name" VARCHAR(255 CHAR),
  "space_name" VARCHAR(255 CHAR),
  "point_id" BIGINT NOT NULL,
  "point_name" VARCHAR(255 CHAR),
  "operator" VARCHAR(10 CHAR) NOT NULL,
  "condition_value" VARCHAR(255 CHAR) NOT NULL
)
;
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."linkage_strategy_id" IS '联动控制策略主键';
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."device_id" IS '设备主键';
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."device_name" IS '设备名称';
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."space_name" IS '空间名称';
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."point_id" IS '点位id';
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."point_name" IS '点位名称';
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."operator" IS '运算符';
COMMENT ON COLUMN "FWBZ"."linkage_front_point"."condition_value" IS '条件值';
COMMENT ON TABLE "FWBZ"."linkage_front_point" IS '联动控制策略前置点位';

-- ----------------------------
-- Table structure for linkage_rear_point
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."linkage_rear_point";
CREATE TABLE "FWBZ"."linkage_rear_point" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "linkage_strategy_id" BIGINT NOT NULL,
  "device_id" BIGINT NOT NULL,
  "device_name" VARCHAR(255 CHAR),
  "space_name" VARCHAR(255 CHAR),
  "point_id" BIGINT NOT NULL,
  "point_name" VARCHAR(255 CHAR),
  "condition_value" VARCHAR(255 CHAR) NOT NULL
)
;
COMMENT ON COLUMN "FWBZ"."linkage_rear_point"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."linkage_rear_point"."linkage_strategy_id" IS '联动控制策略主键';
COMMENT ON COLUMN "FWBZ"."linkage_rear_point"."device_id" IS '设备主键';
COMMENT ON COLUMN "FWBZ"."linkage_rear_point"."device_name" IS '设备名称';
COMMENT ON COLUMN "FWBZ"."linkage_rear_point"."space_name" IS '空间名称';
COMMENT ON COLUMN "FWBZ"."linkage_rear_point"."point_id" IS '点位id';
COMMENT ON COLUMN "FWBZ"."linkage_rear_point"."point_name" IS '点位名称';
COMMENT ON COLUMN "FWBZ"."linkage_rear_point"."condition_value" IS '条件值';
COMMENT ON TABLE "FWBZ"."linkage_rear_point" IS '联动控制策略后置点位';

-- ----------------------------
-- Table structure for linkage_strategy
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."linkage_strategy";
CREATE TABLE "FWBZ"."linkage_strategy" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "strategy_code" VARCHAR(255 CHAR) NOT NULL,
  "strategy_name" VARCHAR(255 CHAR) NOT NULL,
  "strategy_target" VARCHAR(255 CHAR) NOT NULL,
  "front_device" VARCHAR(2000 CHAR) NOT NULL,
  "rear_device" VARCHAR(2000 CHAR) NOT NULL,
  "enabled_status" VARCHAR(2 CHAR) NOT NULL
)
;
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."strategy_code" IS '策略编码';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."strategy_name" IS '策略名称';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."strategy_target" IS '策略目标';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."front_device" IS '前置设备';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."rear_device" IS '后置设备';
COMMENT ON COLUMN "FWBZ"."linkage_strategy"."enabled_status" IS '启用状态。启用：1；禁用：0';
COMMENT ON TABLE "FWBZ"."linkage_strategy" IS '联动控制策略';

-- ----------------------------
-- Table structure for log_point_execute_record
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."log_point_execute_record";
CREATE TABLE "FWBZ"."log_point_execute_record" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "strategy_execute_id" BIGINT,
  "point_id" BIGINT,
  "executed_time" TIMESTAMP,
  "device_id" BIGINT,
  "device_name" VARCHAR(255 CHAR),
  "condition_value" VARCHAR(255 CHAR),
  "point_name" VARCHAR(255 CHAR),
  "success_flag" VARCHAR(10 CHAR),
  "condition_remark" VARCHAR(50)
)
;
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."id" IS '主键ID';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."strategy_execute_id" IS '策略执行记录主键';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."point_id" IS '点位ID';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."executed_time" IS '执行时间';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."device_id" IS '设备ID';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."device_name" IS '设备名称';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."condition_value" IS '条件值';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."point_name" IS '点位名称';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."success_flag" IS '是否执行成功';
COMMENT ON COLUMN "FWBZ"."log_point_execute_record"."condition_remark" IS '条件值备注';
COMMENT ON TABLE "FWBZ"."log_point_execute_record" IS '点位执行记录表';

-- ----------------------------
-- Table structure for log_strategy_execute_record
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."log_strategy_execute_record";
CREATE TABLE "FWBZ"."log_strategy_execute_record" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "business_type" VARCHAR(1 CHAR),
  "business_key" BIGINT,
  "success_flag" VARCHAR(50 CHAR),
  "description" TEXT,
  "executed_time" TIMESTAMP,
  "executed_by" VARCHAR(50)
)
;
COMMENT ON COLUMN "FWBZ"."log_strategy_execute_record"."id" IS '主键ID';
COMMENT ON COLUMN "FWBZ"."log_strategy_execute_record"."business_type" IS '执行业务类型【0-模式化管理，1-联动策略】';
COMMENT ON COLUMN "FWBZ"."log_strategy_execute_record"."business_key" IS '执行业务主键';
COMMENT ON COLUMN "FWBZ"."log_strategy_execute_record"."success_flag" IS '是否执行成功【成功/失败/执行中】';
COMMENT ON COLUMN "FWBZ"."log_strategy_execute_record"."description" IS '描述信息';
COMMENT ON COLUMN "FWBZ"."log_strategy_execute_record"."executed_time" IS '执行时间';
COMMENT ON COLUMN "FWBZ"."log_strategy_execute_record"."executed_by" IS '执行人';
COMMENT ON TABLE "FWBZ"."log_strategy_execute_record" IS '策略执行记录表';

-- ----------------------------
-- Table structure for metering_point_2511201615
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_2511201615";
CREATE TABLE "FWBZ"."metering_point_2511201615" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "type" VARCHAR(255 CHAR),
  "node_code" VARCHAR(255 CHAR),
  "node_name" VARCHAR(255 CHAR),
  "parent_id" BIGINT,
  "sort" INT,
  "category_id" BIGINT,
  "space_id" BIGINT,
  "metering_unit" BIGINT,
  "formula" TEXT,
  "true_formula" TEXT
)
;
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."type" IS '类型。数据字典：energy_flow_type';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."node_code" IS '节点编号';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."node_name" IS '节点名称';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."parent_id" IS '父节点';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."category_id" IS '设备类别';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."space_id" IS '空间位置';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."metering_unit" IS '计量单位';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."formula" IS '公式';
COMMENT ON COLUMN "FWBZ"."metering_point_2511201615"."true_formula" IS '解析后公式（将点位编码替换为设备编码）';
COMMENT ON TABLE "FWBZ"."metering_point_2511201615" IS '计量点位配置';

-- ----------------------------
-- Table structure for metering_point_cost_data_day
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_cost_data_day";
CREATE TABLE "FWBZ"."metering_point_cost_data_day" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT,
  "time" TIMESTAMP,
  "value" DECIMAL(18,2),
  "cost" DECIMAL(18,2)
)
;

-- ----------------------------
-- Table structure for metering_point_cost_data_hour
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_cost_data_hour";
CREATE TABLE "FWBZ"."metering_point_cost_data_hour" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT,
  "time" TIMESTAMP,
  "value" DECIMAL(18,2),
  "cost" DECIMAL(18,2)
)
;

-- ----------------------------
-- Table structure for metering_point_cost_data_month
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_cost_data_month";
CREATE TABLE "FWBZ"."metering_point_cost_data_month" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT,
  "time" TIMESTAMP,
  "value" DECIMAL(18,2),
  "cost" DECIMAL(18,2)
)
;

-- ----------------------------
-- Table structure for metering_point_cost_data_year
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_cost_data_year";
CREATE TABLE "FWBZ"."metering_point_cost_data_year" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT,
  "time" TIMESTAMP,
  "value" DECIMAL(18,2),
  "cost" DECIMAL(18,2)
)
;

-- ----------------------------
-- Table structure for metering_point_data_day
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_data_day";
CREATE TABLE "FWBZ"."metering_point_data_day" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT,
  "time" TIMESTAMP,
  "value" DECIMAL(30,4)
)
;
COMMENT ON COLUMN "FWBZ"."metering_point_data_day"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."metering_point_data_day"."metering_point_id" IS '点位ID';
COMMENT ON COLUMN "FWBZ"."metering_point_data_day"."time" IS '时间';
COMMENT ON COLUMN "FWBZ"."metering_point_data_day"."value" IS '值';
COMMENT ON TABLE "FWBZ"."metering_point_data_day" IS '计量点日数据';

-- ----------------------------
-- Table structure for metering_point_data_hour
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_data_hour";
CREATE TABLE "FWBZ"."metering_point_data_hour" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT,
  "time" TIMESTAMP,
  "value" DECIMAL(19,4)
)
;

-- ----------------------------
-- Table structure for metering_point_data_month
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_data_month";
CREATE TABLE "FWBZ"."metering_point_data_month" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT,
  "time" TIMESTAMP,
  "value" DECIMAL(30,4)
)
;
COMMENT ON COLUMN "FWBZ"."metering_point_data_month"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."metering_point_data_month"."metering_point_id" IS '点位ID';
COMMENT ON COLUMN "FWBZ"."metering_point_data_month"."time" IS '时间';
COMMENT ON COLUMN "FWBZ"."metering_point_data_month"."value" IS '值';
COMMENT ON TABLE "FWBZ"."metering_point_data_month" IS '计量点月数据';

-- ----------------------------
-- Table structure for metering_point_data_year
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_data_year";
CREATE TABLE "FWBZ"."metering_point_data_year" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT,
  "time" TIMESTAMP,
  "value" DECIMAL(38,4)
)
;
COMMENT ON COLUMN "FWBZ"."metering_point_data_year"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."metering_point_data_year"."metering_point_id" IS '点位ID';
COMMENT ON COLUMN "FWBZ"."metering_point_data_year"."time" IS '时间';
COMMENT ON COLUMN "FWBZ"."metering_point_data_year"."value" IS '值';
COMMENT ON TABLE "FWBZ"."metering_point_data_year" IS '计量点年数据';

-- ----------------------------
-- Table structure for metering_point_rel
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."metering_point_rel";
CREATE TABLE "FWBZ"."metering_point_rel" (
  "id" BIGINT IDENTITY(0,0) NOT NULL,
  "metering_point_id" BIGINT NOT NULL,
  "rel_id" BIGINT NOT NULL,
  "rel_type" VARCHAR(2 CHAR) NOT NULL
)
;
COMMENT ON COLUMN "FWBZ"."metering_point_rel"."id" IS '主键ID';
COMMENT ON COLUMN "FWBZ"."metering_point_rel"."metering_point_id" IS '计量点ID';
COMMENT ON COLUMN "FWBZ"."metering_point_rel"."rel_id" IS '关联ID';
COMMENT ON COLUMN "FWBZ"."metering_point_rel"."rel_type" IS '关联类型。设备：1；计量点：2';
COMMENT ON TABLE "FWBZ"."metering_point_rel" IS '计量点关联设备点位';

-- ----------------------------
-- Table structure for patterning_execution_time
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."patterning_execution_time";
CREATE TABLE "FWBZ"."patterning_execution_time" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "patterning_id" BIGINT,
  "begin_date" DATE,
  "begin_time" TIME,
  "enabled_week" VARCHAR(255 CHAR),
  "end_date" DATE,
  "version" VARCHAR(50)
)
;
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."patterning_id" IS '模式化管理策略ID';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."begin_date" IS '策略起始日期';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."begin_time" IS '策略执行时间';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."enabled_week" IS '周策略执行日，例如：1,2,3 表示周一、周二、周三';
COMMENT ON COLUMN "FWBZ"."patterning_execution_time"."end_date" IS '策略结束日期';
COMMENT ON TABLE "FWBZ"."patterning_execution_time" IS '场景策略执行时间配置表';

-- ----------------------------
-- Table structure for patterning_point
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."patterning_point";
CREATE TABLE "FWBZ"."patterning_point" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "pattern_strategy_id" BIGINT,
  "device_id" BIGINT,
  "device_code" VARCHAR(255 CHAR),
  "device_name" VARCHAR(255 CHAR),
  "space_id" BIGINT,
  "space_name" VARCHAR(255 CHAR),
  "condition_value" VARCHAR(255 CHAR),
  "point_name" VARCHAR(255 CHAR),
  "point_id" BIGINT
)
;
COMMENT ON COLUMN "FWBZ"."patterning_point"."id" IS '主键ID';
COMMENT ON COLUMN "FWBZ"."patterning_point"."pattern_strategy_id" IS '模式化策略主键';
COMMENT ON COLUMN "FWBZ"."patterning_point"."device_id" IS '设备主键';
COMMENT ON COLUMN "FWBZ"."patterning_point"."device_code" IS '设备编码';
COMMENT ON COLUMN "FWBZ"."patterning_point"."device_name" IS '设备名称';
COMMENT ON COLUMN "FWBZ"."patterning_point"."space_id" IS '空间主键';
COMMENT ON COLUMN "FWBZ"."patterning_point"."space_name" IS '空间名称';
COMMENT ON COLUMN "FWBZ"."patterning_point"."condition_value" IS '条件值';
COMMENT ON COLUMN "FWBZ"."patterning_point"."point_name" IS '点位名称';
COMMENT ON COLUMN "FWBZ"."patterning_point"."point_id" IS '点位ID';
COMMENT ON TABLE "FWBZ"."patterning_point" IS '场景策略设备点位表';

-- ----------------------------
-- Table structure for patterning_related
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."patterning_related";
CREATE TABLE "FWBZ"."patterning_related" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "pre_association_id" BIGINT,
  "post_association_id" BIGINT,
  "post_association_name" VARCHAR(255 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."patterning_related"."id" IS '主键ID';
COMMENT ON COLUMN "FWBZ"."patterning_related"."pre_association_id" IS '前关联主键';
COMMENT ON COLUMN "FWBZ"."patterning_related"."post_association_id" IS '后关联主键';
COMMENT ON COLUMN "FWBZ"."patterning_related"."post_association_name" IS '后关联策略名称';
COMMENT ON TABLE "FWBZ"."patterning_related" IS '场景策略关联关系表';

-- ----------------------------
-- Table structure for patterning_strategy
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."patterning_strategy";
CREATE TABLE "FWBZ"."patterning_strategy" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "strategy_code" VARCHAR(255 CHAR),
  "strategy_name" VARCHAR(255 CHAR),
  "strategy_scene" VARCHAR(255 CHAR),
  "strategy_target" VARCHAR(255 CHAR),
  "execute_device" VARCHAR(255 CHAR),
  "enabled_status" VARCHAR(1 CHAR),
  "composite_specialty_flag" VARCHAR(1 CHAR),
  "space_id" BIGINT,
  "space_name" VARCHAR(255 CHAR),
  "group_name" VARCHAR(255 CHAR),
  "group_id" BIGINT,
  "model_type" VARCHAR(50 CHAR),
  "professional_id" BIGINT,
  "professional_name" VARCHAR(255 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."strategy_code" IS '策略编号';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."strategy_name" IS '策略名称';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."strategy_scene" IS '应用场景';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."strategy_target" IS '策略目的';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."execute_device" IS '执行设备/参数，描述';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."enabled_status" IS '启动状态【0禁用 ，1启用】';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."composite_specialty_flag" IS '是否为复合专业【0-否，1-是】';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."space_id" IS '空间主键';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."space_name" IS '空间名称';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."group_name" IS '分组名称';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."group_id" IS '分组主键';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."model_type" IS '模式类型【手动/自动】';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."professional_id" IS '专业ID';
COMMENT ON COLUMN "FWBZ"."patterning_strategy"."professional_name" IS '专业名称';
COMMENT ON TABLE "FWBZ"."patterning_strategy" IS '场景控制';

-- ----------------------------
-- Table structure for project
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."project";
CREATE TABLE "FWBZ"."project" (
  "id" VARCHAR(36 CHAR) NOT NULL,
  "project_name" VARCHAR(250 CHAR) NOT NULL,
  "project_establishment_time" TIMESTAMP,
  "project_cycle" INT,
  "project_budget" DECIMAL(10,0),
  "project_subject" VARCHAR(32 CHAR),
  "project_files" CLOB,
  "project_goal" CLOB,
  "point_id" BIGINT,
  "project_type" VARCHAR(255 CHAR),
  "full_point_id" VARCHAR(255 CHAR),
  "measurement_time" TIMESTAMP,
  "create_by" VARCHAR(50 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(50 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(64 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."project"."project_name" IS '项目名称';
COMMENT ON COLUMN "FWBZ"."project"."project_establishment_time" IS '立项时间';
COMMENT ON COLUMN "FWBZ"."project"."project_cycle" IS '项目周期（单位可以根据实际情况确定，月';
COMMENT ON COLUMN "FWBZ"."project"."project_budget" IS '项目预算';
COMMENT ON COLUMN "FWBZ"."project"."project_subject" IS '项目主体';
COMMENT ON COLUMN "FWBZ"."project"."project_files" IS '项目文件（可存储文件相关信息或路径等）';
COMMENT ON COLUMN "FWBZ"."project"."project_goal" IS '项目目标';
COMMENT ON COLUMN "FWBZ"."project"."point_id" IS '关联计量点位id';
COMMENT ON COLUMN "FWBZ"."project"."project_type" IS '项目类型';
COMMENT ON COLUMN "FWBZ"."project"."measurement_time" IS '节能计量启动时间';
COMMENT ON COLUMN "FWBZ"."project"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."project"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."project"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."project"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."project"."sys_org_code" IS '所属部门';

-- ----------------------------
-- Table structure for space
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."space";
CREATE TABLE "FWBZ"."space" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "pid" BIGINT,
  "has_child" VARCHAR(10 CHAR),
  "space_name" VARCHAR(255 CHAR),
  "sort" INT,
  "remark" TEXT,
  "full_name" VARCHAR(255 CHAR),
  "full_id" VARCHAR(255 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."space"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."space"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."space"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."space"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."space"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."space"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."space"."pid" IS '父级节点';
COMMENT ON COLUMN "FWBZ"."space"."has_child" IS '是否有子节点';
COMMENT ON COLUMN "FWBZ"."space"."space_name" IS '名称';
COMMENT ON COLUMN "FWBZ"."space"."sort" IS '排序字段';
COMMENT ON COLUMN "FWBZ"."space"."remark" IS '备注';
COMMENT ON COLUMN "FWBZ"."space"."full_name" IS '空间全称';
COMMENT ON COLUMN "FWBZ"."space"."full_id" IS '父级id';
COMMENT ON TABLE "FWBZ"."space" IS '空间位置';

-- ----------------------------
-- Table structure for standard_coal_coefficient
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."standard_coal_coefficient";
CREATE TABLE "FWBZ"."standard_coal_coefficient" (
  "id" VARCHAR(36 CHAR) NOT NULL,
  "create_by" VARCHAR(50 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(50 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(64 CHAR),
  "energy_medium" VARCHAR(32 CHAR),
  "unit" VARCHAR(32 CHAR),
  "eccsc" VARCHAR(32 CHAR),
  "ecf" VARCHAR(32 CHAR),
  "sort" INT,
  "remark" VARCHAR(32 CHAR)
)
;
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."energy_medium" IS '能源介质';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."unit" IS '单位';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."eccsc" IS '当量折算系数';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."ecf" IS '等价折算系数';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."standard_coal_coefficient"."remark" IS '说明';

-- ----------------------------
-- Table structure for table_venue_info
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."table_venue_info";
CREATE TABLE "FWBZ"."table_venue_info" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "venue_name" VARCHAR2(50),
  "location" VARCHAR2(50),
  "orientation" VARCHAR2(20),
  "area" VARCHAR2(20),
  "ceiling_h" VARCHAR2(255),
  "lighting" VARCHAR2(255),
  "basic_facility" NVARCHAR2(510),
  "buildable" TINYINT NOT NULL
)
;
COMMENT ON COLUMN "FWBZ"."table_venue_info"."venue_name" IS '场馆名称';
COMMENT ON COLUMN "FWBZ"."table_venue_info"."location" IS '位置';
COMMENT ON COLUMN "FWBZ"."table_venue_info"."orientation" IS '朝向';
COMMENT ON COLUMN "FWBZ"."table_venue_info"."area" IS '建筑面积';
COMMENT ON COLUMN "FWBZ"."table_venue_info"."ceiling_h" IS '层高';
COMMENT ON COLUMN "FWBZ"."table_venue_info"."lighting" IS '采光条件';
COMMENT ON COLUMN "FWBZ"."table_venue_info"."basic_facility" IS '基础情况';
COMMENT ON COLUMN "FWBZ"."table_venue_info"."buildable" IS '可施工 1=是 0=否';
COMMENT ON TABLE "FWBZ"."table_venue_info" IS '场馆基本信息';

-- ----------------------------
-- Table structure for unit_management
-- ----------------------------
DROP TABLE IF EXISTS "FWBZ"."unit_management";
CREATE TABLE "FWBZ"."unit_management" (
  "id" BIGINT IDENTITY(1,1) NOT NULL,
  "create_by" VARCHAR(255 CHAR),
  "create_time" TIMESTAMP,
  "update_by" VARCHAR(255 CHAR),
  "update_time" TIMESTAMP,
  "sys_org_code" VARCHAR(255 CHAR),
  "code" VARCHAR(255 CHAR),
  "name" VARCHAR(255 CHAR),
  "english_ame" VARCHAR(255 CHAR),
  "sort" INT,
  "remark" TEXT
)
;
COMMENT ON COLUMN "FWBZ"."unit_management"."id" IS '主键';
COMMENT ON COLUMN "FWBZ"."unit_management"."create_by" IS '创建人';
COMMENT ON COLUMN "FWBZ"."unit_management"."create_time" IS '创建日期';
COMMENT ON COLUMN "FWBZ"."unit_management"."update_by" IS '更新人';
COMMENT ON COLUMN "FWBZ"."unit_management"."update_time" IS '更新日期';
COMMENT ON COLUMN "FWBZ"."unit_management"."sys_org_code" IS '所属部门';
COMMENT ON COLUMN "FWBZ"."unit_management"."code" IS '单位代码';
COMMENT ON COLUMN "FWBZ"."unit_management"."name" IS '单位名称';
COMMENT ON COLUMN "FWBZ"."unit_management"."english_ame" IS '英文名称';
COMMENT ON COLUMN "FWBZ"."unit_management"."sort" IS '排序';
COMMENT ON COLUMN "FWBZ"."unit_management"."remark" IS '说明';
COMMENT ON TABLE "FWBZ"."unit_management" IS '计量单位管理';

-- ----------------------------
-- Primary Key structure for table alarm_category
-- ----------------------------
ALTER TABLE "FWBZ"."alarm_category" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table alarm_level
-- ----------------------------
ALTER TABLE "FWBZ"."alarm_level" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table alarm_record
-- ----------------------------
ALTER TABLE "FWBZ"."alarm_record" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table alarm_rule_point
-- ----------------------------
ALTER TABLE "FWBZ"."alarm_rule_point" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table alarm_rules
-- ----------------------------
ALTER TABLE "FWBZ"."alarm_rules" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table building_control_point
-- ----------------------------
ALTER TABLE "FWBZ"."building_control_point" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table building_control_point
-- ----------------------------
ALTER TABLE "FWBZ"."building_control_point" ADD UNIQUE ("gateway_adr", "bacnet_adr");

-- ----------------------------
-- Primary Key structure for table building_control_point_history
-- ----------------------------
ALTER TABLE "FWBZ"."building_control_point_history" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table business_config
-- ----------------------------
ALTER TABLE "FWBZ"."business_config" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table business_config
-- ----------------------------
ALTER TABLE "FWBZ"."business_config" ADD UNIQUE ("config_key");

-- ----------------------------
-- Primary Key structure for table carbon_emission_factor
-- ----------------------------
ALTER TABLE "FWBZ"."carbon_emission_factor" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table data_amend_log
-- ----------------------------
ALTER TABLE "FWBZ"."data_amend_log" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table data_day
-- ----------------------------
ALTER TABLE "FWBZ"."data_day" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table data_hour
-- ----------------------------
ALTER TABLE "FWBZ"."data_hour" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table data_minute
-- ----------------------------
ALTER TABLE "FWBZ"."data_minute" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table data_month
-- ----------------------------
ALTER TABLE "FWBZ"."data_month" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table data_year
-- ----------------------------
ALTER TABLE "FWBZ"."data_year" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device
-- ----------------------------
ALTER TABLE "FWBZ"."device" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_251126
-- ----------------------------
ALTER TABLE "FWBZ"."device_251126" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_attribute
-- ----------------------------
ALTER TABLE "FWBZ"."device_attribute" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_attribute_251201
-- ----------------------------
ALTER TABLE "FWBZ"."device_attribute_251201" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_attribute_251209
-- ----------------------------
ALTER TABLE "FWBZ"."device_attribute_251209" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_attribute_config
-- ----------------------------
ALTER TABLE "FWBZ"."device_attribute_config" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_attribute_data
-- ----------------------------
ALTER TABLE "FWBZ"."device_attribute_data" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_data_temp
-- ----------------------------
ALTER TABLE "FWBZ"."device_data_temp" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_model
-- ----------------------------
ALTER TABLE "FWBZ"."device_model" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_model_attribute
-- ----------------------------
ALTER TABLE "FWBZ"."device_model_attribute" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_static_data
-- ----------------------------
ALTER TABLE "FWBZ"."device_static_data" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_static_data_config
-- ----------------------------
ALTER TABLE "FWBZ"."device_static_data_config" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_temp
-- ----------------------------
ALTER TABLE "FWBZ"."device_temp" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table device_temp_251126
-- ----------------------------
ALTER TABLE "FWBZ"."device_temp_251126" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table energy_analysis_benchmark
-- ----------------------------
ALTER TABLE "FWBZ"."energy_analysis_benchmark" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table energy_analysis_chart
-- ----------------------------
ALTER TABLE "FWBZ"."energy_analysis_chart" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table energy_analysis_config
-- ----------------------------
ALTER TABLE "FWBZ"."energy_analysis_config" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table energy_attribute_management
-- ----------------------------
ALTER TABLE "FWBZ"."energy_attribute_management" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table energy_flow_diagram_config
-- ----------------------------
ALTER TABLE "FWBZ"."energy_flow_diagram_config" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table energy_medium_manage
-- ----------------------------
ALTER TABLE "FWBZ"."energy_medium_manage" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table energy_price
-- ----------------------------
ALTER TABLE "FWBZ"."energy_price" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table energy_pricing_config
-- ----------------------------
ALTER TABLE "FWBZ"."energy_pricing_config" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table equipment_category
-- ----------------------------
ALTER TABLE "FWBZ"."equipment_category" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table gather_rule_config
-- ----------------------------
ALTER TABLE "FWBZ"."gather_rule_config" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table lighting_area
-- ----------------------------
ALTER TABLE "FWBZ"."lighting_area" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table lighting_area
-- ----------------------------
ALTER TABLE "FWBZ"."lighting_area" ADD UNIQUE ("area_code", "space");

-- ----------------------------
-- Primary Key structure for table lighting_circuit
-- ----------------------------
ALTER TABLE "FWBZ"."lighting_circuit" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table lighting_operation_log
-- ----------------------------
ALTER TABLE "FWBZ"."lighting_operation_log" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table lighting_plan
-- ----------------------------
ALTER TABLE "FWBZ"."lighting_plan" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table lighting_plan_execution_time
-- ----------------------------
ALTER TABLE "FWBZ"."lighting_plan_execution_time" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table linkage_front_point
-- ----------------------------
ALTER TABLE "FWBZ"."linkage_front_point" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table linkage_rear_point
-- ----------------------------
ALTER TABLE "FWBZ"."linkage_rear_point" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table linkage_strategy
-- ----------------------------
ALTER TABLE "FWBZ"."linkage_strategy" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table log_point_execute_record
-- ----------------------------
ALTER TABLE "FWBZ"."log_point_execute_record" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table log_strategy_execute_record
-- ----------------------------
ALTER TABLE "FWBZ"."log_strategy_execute_record" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_2511201615
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_2511201615" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_cost_data_day
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_cost_data_day" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_cost_data_hour
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_cost_data_hour" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_cost_data_month
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_cost_data_month" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_cost_data_year
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_cost_data_year" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_data_day
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_data_day" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_data_hour
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_data_hour" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_data_month
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_data_month" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_data_year
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_data_year" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table metering_point_rel
-- ----------------------------
ALTER TABLE "FWBZ"."metering_point_rel" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table patterning_execution_time
-- ----------------------------
ALTER TABLE "FWBZ"."patterning_execution_time" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table patterning_execution_time
-- ----------------------------
ALTER TABLE "FWBZ"."patterning_execution_time" ADD UNIQUE ("patterning_id");

-- ----------------------------
-- Primary Key structure for table patterning_point
-- ----------------------------
ALTER TABLE "FWBZ"."patterning_point" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table patterning_related
-- ----------------------------
ALTER TABLE "FWBZ"."patterning_related" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table patterning_strategy
-- ----------------------------
ALTER TABLE "FWBZ"."patterning_strategy" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table project
-- ----------------------------
ALTER TABLE "FWBZ"."project" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table space
-- ----------------------------
ALTER TABLE "FWBZ"."space" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table standard_coal_coefficient
-- ----------------------------
ALTER TABLE "FWBZ"."standard_coal_coefficient" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table table_venue_info
-- ----------------------------
ALTER TABLE "FWBZ"."table_venue_info" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table unit_management
-- ----------------------------
ALTER TABLE "FWBZ"."unit_management" ADD PRIMARY KEY ("id");
