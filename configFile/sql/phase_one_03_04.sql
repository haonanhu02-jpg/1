-- Phase one 03/04 schema and data migration.
-- application.yml uses spring.jpa.hibernate.ddl-auto=none, so run this script manually after phase_one_01_02.sql.

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iyque_phase_group_route_rule'
    AND COLUMN_NAME = 'rule_type'
);
SET @sql = IF(
  @column_exists = 0,
  'ALTER TABLE iyque_phase_group_route_rule ADD COLUMN rule_type VARCHAR(32) DEFAULT NULL COMMENT ''CUSTOMER_TAG CHANNEL_TAG CHANNEL_DEFAULT GLOBAL_DEFAULT'' AFTER rule_name',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS iyque_phase_customer_route_log (
  id BIGINT NOT NULL PRIMARY KEY,
  external_userid VARCHAR(128) DEFAULT NULL COMMENT 'external user id',
  customer_name VARCHAR(255) DEFAULT NULL COMMENT 'customer name',
  user_id VARCHAR(128) DEFAULT NULL COMMENT 'employee user id',
  user_name VARCHAR(255) DEFAULT NULL COMMENT 'employee name',
  channel_code_id VARCHAR(64) DEFAULT NULL COMMENT 'channel code id',
  channel_code_name VARCHAR(255) DEFAULT NULL COMMENT 'channel code name',
  channel_state VARCHAR(255) DEFAULT NULL COMMENT 'wechat state',
  tag_ids VARCHAR(1024) DEFAULT NULL COMMENT 'input tag ids',
  rule_type VARCHAR(32) DEFAULT NULL COMMENT 'matched rule type',
  matched_rule_id BIGINT DEFAULT NULL COMMENT 'matched rule id',
  matched_rule_name VARCHAR(255) DEFAULT NULL COMMENT 'matched rule name',
  match_path VARCHAR(255) DEFAULT NULL COMMENT 'match path',
  target_pool_id BIGINT DEFAULT NULL COMMENT 'target pool id',
  target_pool_name VARCHAR(255) DEFAULT NULL COMMENT 'target pool name',
  target_chat_id VARCHAR(128) DEFAULT NULL COMMENT 'target chat id',
  target_chat_name VARCHAR(255) DEFAULT NULL COMMENT 'target chat name',
  route_status VARCHAR(32) DEFAULT NULL COMMENT 'MATCHED NO_ROUTE_RULE TARGET_POOL_DISABLED NO_AVAILABLE_CHAT',
  failure_reason TEXT DEFAULT NULL COMMENT 'failure reason',
  event_time DATETIME DEFAULT NULL COMMENT 'event time',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_route_log_customer_time (external_userid, event_time, del_flag),
  KEY idx_route_log_channel_time (channel_code_id, event_time, del_flag),
  KEY idx_route_log_status_time (route_status, event_time, del_flag),
  KEY idx_route_log_rule_type (rule_type, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase customer route log';

UPDATE iyque_phase_group_route_rule
SET rule_type = CASE
  WHEN fallback_flag = b'1' THEN 'GLOBAL_DEFAULT'
  WHEN required_tag_ids IS NOT NULL AND required_tag_ids <> '' THEN 'CUSTOMER_TAG'
  WHEN channel_code_id IS NOT NULL AND channel_code_id <> '' THEN 'CHANNEL_DEFAULT'
  ELSE 'GLOBAL_DEFAULT'
END
WHERE rule_type IS NULL OR rule_type = '';

UPDATE iyque_phase_group_route_rule
SET fallback_flag = CASE WHEN rule_type = 'GLOBAL_DEFAULT' THEN b'1' ELSE b'0' END;

UPDATE iyque_phase_alert_log
SET alert_level = 'HIGH'
WHERE alert_level = 'ERROR';
