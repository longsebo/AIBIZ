-- ============================================================
-- 表单表 - 保存表单设计器设计的表单
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_form` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `form_name` VARCHAR(100) NOT NULL COMMENT '表单名称',
  `form_code` VARCHAR(100) NOT NULL COMMENT '表单编码',
  `form_json` LONGTEXT COMMENT '表单JSON定义',
  `form_desc` VARCHAR(500) DEFAULT '' COMMENT '表单描述',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_form_code` (`form_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单表';

-- ============================================================
-- 流程表单关联表 - 流程和表单一对一关联
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_process_form` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_key` VARCHAR(255) NOT NULL COMMENT '流程定义Key',
  `process_name` VARCHAR(255) DEFAULT '' COMMENT '流程名称',
  `form_id` BIGINT(20) NOT NULL COMMENT '表单ID',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_key` (`process_key`),
  KEY `idx_form_id` (`form_id`),
  CONSTRAINT `fk_process_form_form` FOREIGN KEY (`form_id`) REFERENCES `sys_form` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程表单关联表';
