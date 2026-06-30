CREATE TABLE IF NOT EXISTS `ai_agent_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_no` VARCHAR(64) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_agent_session_no` (`session_no`),
  KEY `idx_ai_agent_session_user` (`user_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_agent_session_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `session_no` VARCHAR(64) NOT NULL,
  `report_id` BIGINT NOT NULL,
  `report_no` VARCHAR(64) NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_agent_session_report` (`session_id`, `report_id`, `is_deleted`),
  KEY `idx_ai_agent_report_session` (`session_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `system_config` (`config_key`, `config_value`, `data_type`, `config_group`, `remark`, `is_deleted`)
VALUES
('ai.api.enabled','false','BOOLEAN','AI_API','是否启用AI报告智能助手',0),
('ai.api.base_url','https://api.xiaomimimo.com/v1','STRING','AI_API','mimoapi OpenAI兼容接口地址',0),
('ai.api.model','','STRING','AI_API','mimoapi模型名称',0),
('ai.api.key','','SECRET','AI_API','mimoapi API Key',0),
('ai.api.timeout_seconds','30','INT','AI_API','AI接口调用超时时间(秒)',0),
('ai.agent.system_prompt','','TEXT','AI_API','报告智能体系统提示词补充说明',0)
ON DUPLICATE KEY UPDATE
  `data_type` = VALUES(`data_type`),
  `config_group` = VALUES(`config_group`),
  `remark` = VALUES(`remark`),
  `is_deleted` = 0;
