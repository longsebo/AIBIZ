CREATE TABLE `sys_process_cc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例ID',
  `process_definition_id` varchar(64) NOT NULL COMMENT '流程定义ID',
  `process_key` varchar(64) NOT NULL COMMENT '流程定义Key',
  `process_name` varchar(128) NOT NULL COMMENT '流程名称',
  `cc_user_id` bigint(20) NOT NULL COMMENT '抄送用户ID',
  `cc_user_name` varchar(64) NOT NULL COMMENT '抄送用户姓名',
  `cc_user_nick_name` varchar(64) DEFAULT NULL COMMENT '抄送用户昵称',
  `status` char(1) DEFAULT '0' COMMENT '状态（0未读，1已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime DEFAULT NULL COMMENT '读取时间',
  PRIMARY KEY (`id`),
  KEY `idx_process_instance_id` (`process_instance_id`),
  KEY `idx_cc_user_id` (`cc_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程抄送表';