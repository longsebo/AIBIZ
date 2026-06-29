-- ============================================================
-- Flowable 8 菜单和权限 SQL
-- ============================================================

-- 一级菜单：流程管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程管理', 0, 5, 'flowable', null, 1, 0, 'M', '0', '0', '', 'tree', 'admin', NOW(), '流程管理目录');

-- 获取上一步插入的菜单ID
SET @parentId = LAST_INSERT_ID();

-- 流程定义
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程定义', @parentId, 1, 'process', 'flowable/process/index', 1, 0, 'C', '0', '0', 'flowable:process:list', 'documentation', 'admin', NOW(), '流程定义菜单');

SET @processId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程定义查询', @processId, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:query', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程部署', @processId, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:deploy', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('启动流程', @processId, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:start', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程删除', @processId, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:remove', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程设计', @processId, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:design', '#', 'admin', NOW(), '');

-- 流程分类
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程分类', @parentId, 1, 'category', 'flowable/category/index', 1, 0, 'C', '0', '0', 'flowable:category:list', 'tree', 'admin', NOW(), '流程分类菜单');

SET @categoryId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程分类查询', @categoryId, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:query', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程分类新增', @categoryId, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:add', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程分类修改', @categoryId, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:edit', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程分类删除', @categoryId, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:remove', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程分类导出', @categoryId, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:export', '#', 'admin', NOW(), '');

-- 我的任务
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('我的任务', @parentId, 2, 'task', 'flowable/task/index', 1, 0, 'C', '0', '0', 'flowable:task:list', 'todo', 'admin', NOW(), '我的任务菜单');

SET @taskId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('任务查询', @taskId, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:task:query', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('任务办理', @taskId, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:task:complete', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('任务驳回', @taskId, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:task:reject', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('任务委派', @taskId, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:task:delegate', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('任务认领', @taskId, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:task:claim', '#', 'admin', NOW(), '');

-- 我的抄送
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('我的抄送', @parentId, 3, 'cc', 'flowable/cc/index', 1, 0, 'C', '0', '0', 'flowable:cc:list', 'user', 'admin', NOW(), '我的抄送菜单');

SET @ccId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('抄送列表', @ccId, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:cc:list', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('标记已读', @ccId, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:cc:read', '#', 'admin', NOW(), '');

-- 表单管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('表单管理', @parentId, 4, 'form', 'flowable/form/index', 1, 0, 'C', '0', '0', 'flowable:form:list', 'edit', 'admin', NOW(), '表单管理菜单');

SET @formId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('表单查询', @formId, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:query', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('表单新增', @formId, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:add', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('表单修改', @formId, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:edit', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('表单删除', @formId, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:remove', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('表单导出', @formId, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:export', '#', 'admin', NOW(), '');

-- AI指令映射 - Flowable相关指令
INSERT INTO ai_command_map (command, command_type, menu_path, menu_name, sort, status, remark) VALUES
('流程列表', 'list', 'flowable/process/index', '流程定义', 100, '0', '查询流程定义列表'),
('流程定义', 'list', 'flowable/process/index', '流程定义', 101, '0', '查询流程定义'),
('部署流程', 'add', 'flowable/process/index', '流程定义', 102, '0', '部署BPMN流程'),
('我的待办', 'list', 'flowable/task/index', '我的任务', 103, '0', '查询我的待办任务'),
('待办任务', 'list', 'flowable/task/index', '我的任务', 104, '0', '查询我的待办任务'),
('我的已办', 'list', 'flowable/task/index', '我的任务', 105, '0', '查询我的已办任务'),
('已办任务', 'list', 'flowable/task/index', '我的任务', 106, '0', '查询我的已办任务'),
('请假流程', 'start', 'flowable/process/index', '流程定义', 107, '0', '启动请假流程'),
('表单设计', 'list', 'flowable/form/index', '表单管理', 108, '0', '打开表单管理页面'),
('设计表单', 'list', 'flowable/form/index', '表单管理', 109, '0', '打开表单管理页面'),
('表单列表', 'list', 'flowable/form/index', '表单管理', 110, '0', '查询表单列表'),
('表单管理', 'list', 'flowable/form/index', '表单管理', 111, '0', '打开表单管理页面'),
('流程分类', 'list', 'flowable/category/index', '流程分类', 112, '0', '查询流程分类列表'),
('分类管理', 'list', 'flowable/category/index', '流程分类', 113, '0', '打开流程分类管理');
