-- Phase one 05/06/07 schema.
-- Run after phase_one_01_02.sql and phase_one_03_04.sql.

CREATE TABLE IF NOT EXISTS iyque_phase_business_log (
  id BIGINT NOT NULL PRIMARY KEY,
  log_type VARCHAR(64) DEFAULT NULL COMMENT 'business log type',
  business_id VARCHAR(255) DEFAULT NULL COMMENT 'business object id',
  external_userid VARCHAR(128) DEFAULT NULL COMMENT 'external user id',
  user_id VARCHAR(128) DEFAULT NULL COMMENT 'employee user id',
  chat_id VARCHAR(128) DEFAULT NULL COMMENT 'chat id',
  channel_code_id VARCHAR(64) DEFAULT NULL COMMENT 'channel code id',
  request_summary TEXT DEFAULT NULL COMMENT 'request summary',
  response_summary TEXT DEFAULT NULL COMMENT 'response summary',
  error_msg TEXT DEFAULT NULL COMMENT 'error message',
  success BIT(1) DEFAULT NULL COMMENT 'success flag',
  alert_id BIGINT DEFAULT NULL COMMENT 'related alert id',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_phase_log_type_time (log_type, create_time, del_flag),
  KEY idx_phase_log_customer_time (external_userid, create_time, del_flag),
  KEY idx_phase_log_user_time (user_id, create_time, del_flag),
  KEY idx_phase_log_channel_time (channel_code_id, create_time, del_flag),
  KEY idx_phase_log_success_time (success, create_time, del_flag),
  KEY idx_phase_log_alert (alert_id, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase business log';

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iyque_phase_alert_log'
    AND COLUMN_NAME = 'notify_channels'
);
SET @sql = IF(
  @column_exists = 0,
  'ALTER TABLE iyque_phase_alert_log ADD COLUMN notify_channels VARCHAR(128) DEFAULT NULL COMMENT ''notification channels'' AFTER handled_time',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iyque_phase_alert_log'
    AND COLUMN_NAME = 'notify_status'
);
SET @sql = IF(
  @column_exists = 0,
  'ALTER TABLE iyque_phase_alert_log ADD COLUMN notify_status VARCHAR(64) DEFAULT NULL COMMENT ''notification status'' AFTER notify_channels',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iyque_phase_alert_log'
    AND COLUMN_NAME = 'notify_error'
);
SET @sql = IF(
  @column_exists = 0,
  'ALTER TABLE iyque_phase_alert_log ADD COLUMN notify_error TEXT DEFAULT NULL COMMENT ''notification error'' AFTER notify_status',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE iyque_phase_alert_log
SET notify_channels = IF(alert_level = 'HIGH', 'BACKEND,WECHAT_ROBOT,EMAIL', 'BACKEND'),
    notify_status = IF(alert_level = 'HIGH', 'PENDING_CONFIG', 'BACKEND_ONLY')
WHERE notify_status IS NULL OR notify_status = '';
