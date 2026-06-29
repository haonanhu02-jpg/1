-- Phase one 01/02 schema.
-- application.yml uses spring.jpa.hibernate.ddl-auto=none, so run this script manually.

CREATE TABLE IF NOT EXISTS iyque_phase_employee_pool (
  id BIGINT NOT NULL PRIMARY KEY,
  employee_id VARCHAR(128) DEFAULT NULL COMMENT 'employee user id',
  employee_name VARCHAR(255) DEFAULT NULL COMMENT 'employee name',
  sort INT DEFAULT 0 COMMENT 'sort order',
  enabled BIT(1) DEFAULT b'1' COMMENT 'enabled flag',
  remark VARCHAR(512) DEFAULT NULL COMMENT 'remark',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_employee_enabled (enabled, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase employee pool';

CREATE TABLE IF NOT EXISTS iyque_phase_group_pool (
  id BIGINT NOT NULL PRIMARY KEY,
  pool_name VARCHAR(255) NOT NULL COMMENT 'pool name',
  channel_code_id VARCHAR(64) DEFAULT NULL COMMENT 'channel code id',
  channel_code_name VARCHAR(255) DEFAULT NULL COMMENT 'channel code name',
  enabled BIT(1) DEFAULT b'1' COMMENT 'enabled flag',
  remark VARCHAR(512) DEFAULT NULL COMMENT 'remark',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_group_pool_enabled (enabled, del_flag),
  KEY idx_group_pool_channel (channel_code_id, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase group pool';

CREATE TABLE IF NOT EXISTS iyque_phase_group_pool_chat (
  id BIGINT NOT NULL PRIMARY KEY,
  group_pool_id BIGINT NOT NULL COMMENT 'group pool id',
  chat_id VARCHAR(128) NOT NULL COMMENT 'chat id',
  chat_name VARCHAR(255) DEFAULT NULL COMMENT 'chat name',
  capacity_threshold INT DEFAULT 180 COMMENT 'capacity threshold',
  current_member_count INT DEFAULT 0 COMMENT 'current member count',
  backup_flag BIT(1) DEFAULT b'0' COMMENT 'backup flag',
  sort INT DEFAULT 0 COMMENT 'sort order',
  enabled BIT(1) DEFAULT b'1' COMMENT 'enabled flag',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_pool_chat_pool_sort (group_pool_id, backup_flag, sort, del_flag),
  KEY idx_pool_chat_chat (chat_id, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase group pool chat';

CREATE TABLE IF NOT EXISTS iyque_phase_group_route_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  rule_name VARCHAR(255) NOT NULL COMMENT 'rule name',
  channel_code_id VARCHAR(64) DEFAULT NULL COMMENT 'channel code id',
  channel_code_name VARCHAR(255) DEFAULT NULL COMMENT 'channel code name',
  required_tag_ids VARCHAR(1024) DEFAULT NULL COMMENT 'required tag ids',
  target_pool_id BIGINT DEFAULT NULL COMMENT 'target pool id',
  priority INT DEFAULT 0 COMMENT 'priority',
  fallback_flag BIT(1) DEFAULT b'0' COMMENT 'fallback flag',
  enabled BIT(1) DEFAULT b'1' COMMENT 'enabled flag',
  remark VARCHAR(512) DEFAULT NULL COMMENT 'remark',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_route_rule_match (enabled, priority, del_flag),
  KEY idx_route_rule_channel (channel_code_id, del_flag),
  KEY idx_route_rule_pool (target_pool_id, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase group route rule';

CREATE TABLE IF NOT EXISTS iyque_phase_customer_chat_relation (
  id BIGINT NOT NULL PRIMARY KEY,
  external_userid VARCHAR(128) DEFAULT NULL COMMENT 'external user id',
  customer_name VARCHAR(255) DEFAULT NULL COMMENT 'customer name',
  user_id VARCHAR(128) DEFAULT NULL COMMENT 'employee user id',
  chat_id VARCHAR(128) DEFAULT NULL COMMENT 'chat id',
  chat_name VARCHAR(255) DEFAULT NULL COMMENT 'chat name',
  group_pool_id BIGINT DEFAULT NULL COMMENT 'group pool id',
  group_pool_name VARCHAR(255) DEFAULT NULL COMMENT 'group pool name',
  channel_code_id VARCHAR(64) DEFAULT NULL COMMENT 'channel code id',
  channel_state VARCHAR(255) DEFAULT NULL COMMENT 'wechat state',
  event_type VARCHAR(32) DEFAULT NULL COMMENT 'JOIN LEAVE UPDATE',
  relation_status VARCHAR(32) DEFAULT NULL COMMENT 'IN OUT UNKNOWN',
  event_time DATETIME DEFAULT NULL COMMENT 'event time',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_relation_customer_chat_time (external_userid, chat_id, event_time, del_flag),
  KEY idx_relation_chat_time (chat_id, event_time, del_flag),
  KEY idx_relation_pool_time (group_pool_id, event_time, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase customer chat relation';

CREATE TABLE IF NOT EXISTS iyque_phase_callback_idempotent (
  id BIGINT NOT NULL PRIMARY KEY,
  event_key VARCHAR(128) NOT NULL COMMENT 'callback event key',
  event_type VARCHAR(64) DEFAULT NULL COMMENT 'event type',
  change_type VARCHAR(64) DEFAULT NULL COMMENT 'change type',
  process_status VARCHAR(32) DEFAULT NULL COMMENT 'PROCESSING SUCCESS FAILED',
  raw_digest VARCHAR(64) DEFAULT NULL COMMENT 'raw digest',
  error_msg TEXT DEFAULT NULL COMMENT 'error message',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  UNIQUE KEY uk_callback_event_key (event_key),
  KEY idx_callback_status (process_status, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase callback idempotent';

CREATE TABLE IF NOT EXISTS iyque_phase_api_call_log (
  id BIGINT NOT NULL PRIMARY KEY,
  api_name VARCHAR(255) DEFAULT NULL COMMENT 'api name',
  request_summary TEXT DEFAULT NULL COMMENT 'request summary',
  response_summary TEXT DEFAULT NULL COMMENT 'response summary',
  success BIT(1) DEFAULT NULL COMMENT 'success flag',
  cost_ms BIGINT DEFAULT NULL COMMENT 'cost milliseconds',
  error_msg TEXT DEFAULT NULL COMMENT 'error message',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_api_log_name_time (api_name, create_time, del_flag),
  KEY idx_api_log_success_time (success, create_time, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase api call log';

CREATE TABLE IF NOT EXISTS iyque_phase_alert_log (
  id BIGINT NOT NULL PRIMARY KEY,
  alert_type VARCHAR(64) DEFAULT NULL COMMENT 'alert type',
  alert_level VARCHAR(32) DEFAULT NULL COMMENT 'alert level',
  business_key VARCHAR(255) DEFAULT NULL COMMENT 'business key',
  alert_title VARCHAR(255) DEFAULT NULL COMMENT 'alert title',
  alert_content TEXT DEFAULT NULL COMMENT 'alert content',
  status VARCHAR(32) DEFAULT 'PENDING' COMMENT 'PENDING HANDLED',
  handled_by VARCHAR(128) DEFAULT NULL COMMENT 'handled by',
  handled_remark VARCHAR(512) DEFAULT NULL COMMENT 'handled remark',
  handled_time DATETIME DEFAULT NULL COMMENT 'handled time',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_alert_status_time (status, create_time, del_flag),
  KEY idx_alert_type_time (alert_type, create_time, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase alert log';

CREATE TABLE IF NOT EXISTS iyque_phase_failed_task (
  id BIGINT NOT NULL PRIMARY KEY,
  task_type VARCHAR(64) DEFAULT NULL COMMENT 'task type',
  business_key VARCHAR(255) DEFAULT NULL COMMENT 'business key',
  request_body TEXT DEFAULT NULL COMMENT 'request body',
  error_msg TEXT DEFAULT NULL COMMENT 'error message',
  status VARCHAR(32) DEFAULT 'PENDING' COMMENT 'PENDING FAILED EXHAUSTED SUCCESS',
  retry_count INT DEFAULT 0 COMMENT 'retry count',
  max_retry_count INT DEFAULT 3 COMMENT 'max retry count',
  next_retry_time DATETIME DEFAULT NULL COMMENT 'next retry time',
  last_retry_time DATETIME DEFAULT NULL COMMENT 'last retry time',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  del_flag INT DEFAULT 0,
  KEY idx_failed_task_retry (status, next_retry_time, del_flag),
  KEY idx_failed_task_business (business_key, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='phase failed task';
