-- 流程草稿表
CREATE TABLE sys_process_draft (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    process_key VARCHAR(255) NOT NULL COMMENT '流程定义Key',
    process_name VARCHAR(255) COMMENT '流程名称',
    form_data TEXT COMMENT '表单数据(JSON格式)',
    attachments TEXT COMMENT '附件列表(JSON格式)',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
    INDEX idx_process_key (process_key),
    INDEX idx_create_by (create_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程草稿表';