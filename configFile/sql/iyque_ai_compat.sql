-- Original Iyque AI chat compatibility tables.
-- The phase-one project does not implement AI business, but the original frontend
-- still loads the AI chat widget after login, so these empty tables prevent
-- startup-page database errors.

CREATE TABLE IF NOT EXISTS iyque_ai_conversation (
  id BIGINT NOT NULL PRIMARY KEY,
  conversationId VARCHAR(128) DEFAULT NULL,
  title VARCHAR(255) DEFAULT NULL,
  mode VARCHAR(64) DEFAULT NULL,
  userId BIGINT DEFAULT NULL,
  modelName VARCHAR(128) DEFAULT NULL,
  role TEXT DEFAULT NULL,
  temperature DOUBLE DEFAULT NULL,
  topP DOUBLE DEFAULT NULL,
  maxHistoryRounds INT DEFAULT NULL,
  createTime DATETIME DEFAULT NULL,
  updateTime DATETIME DEFAULT NULL,
  deleted INT DEFAULT 0,
  deviceType INT DEFAULT NULL,
  KEY idx_ai_conversation_device_update (deviceType, deleted, updateTime),
  KEY idx_ai_conversation_id (conversationId, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Iyque AI conversation compatibility';

CREATE TABLE IF NOT EXISTS iyque_ai_conversation_message (
  id BIGINT NOT NULL PRIMARY KEY,
  conversationId VARCHAR(128) DEFAULT NULL,
  type VARCHAR(64) DEFAULT NULL,
  content LONGTEXT DEFAULT NULL,
  timestamp VARCHAR(64) DEFAULT NULL,
  createTime DATETIME DEFAULT NULL,
  KEY idx_ai_message_conversation_time (conversationId, createTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Iyque AI conversation message compatibility';
