-- 流程分类表
DROP TABLE IF EXISTS sys_flow_category;
CREATE TABLE sys_flow_category (
  category_id      BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  category_name    VARCHAR(100)    NOT NULL COMMENT '分类名称',
  category_code    VARCHAR(50)     NOT NULL COMMENT '分类编码',
  parent_id        BIGINT(20)      DEFAULT 0 COMMENT '父分类ID',
  order_num        INT(4)          DEFAULT 0 COMMENT '显示顺序',
  status           CHAR(1)         DEFAULT '0' COMMENT '状态（0正常 1停用）',
  create_by        VARCHAR(64)     DEFAULT '' COMMENT '创建者',
  create_time      DATETIME        DEFAULT NULL COMMENT '创建时间',
  update_by        VARCHAR(64)     DEFAULT '' COMMENT '更新者',
  update_time      DATETIME        DEFAULT NULL COMMENT '更新时间',
  remark           VARCHAR(500)    DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (category_id),
  UNIQUE KEY uk_category_code (category_code)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='流程分类表';

-- 初始化流程分类数据
INSERT INTO sys_flow_category(category_name, category_code, parent_id, order_num, status, create_time, remark) VALUES
('行政审批', 'admin', 0, 1, '0', sysdate(), '行政审批流程'),
('人事管理', 'hr', 0, 2, '0', sysdate(), '人事管理流程'),
('财务管理', 'finance', 0, 3, '0', sysdate(), '财务管理流程'),
('业务审批', 'business', 0, 4, '0', sysdate(), '业务审批流程'),
('其他流程', 'other', 0, 5, '0', sysdate(), '其他流程');