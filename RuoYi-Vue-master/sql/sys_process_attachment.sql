CREATE TABLE `sys_process_attachment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例ID',
  `process_definition_id` varchar(64) NOT NULL COMMENT '流程定义ID',
  `process_key` varchar(64) NOT NULL COMMENT '流程定义Key',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_process_instance_id` (`process_instance_id`),
  KEY `idx_process_key` (`process_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程附件表';