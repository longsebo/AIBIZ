-- ============================================================
-- 流程分类管理菜单 SQL（单独执行）
-- ============================================================

-- 查询流程管理目录的ID（假设已存在）
-- 如果流程管理目录不存在，先创建
SELECT @parentId := menu_id FROM sys_menu WHERE menu_name = '流程管理' AND parent_id = 0 LIMIT 1;

-- 如果找不到，则创建流程管理目录
-- INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
-- VALUES('流程管理', 0, 5, 'flowable', null, 1, 0, 'M', '0', '0', '', 'tree', 'admin', NOW(), '流程管理目录');
-- SET @parentId = LAST_INSERT_ID();

-- 流程分类菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('流程分类', @parentId, 1, 'category', 'flowable/category/index', 1, 0, 'C', '0', '0', 'flowable:category:list', 'tree', 'admin', NOW(), '流程分类菜单');

SET @categoryId = LAST_INSERT_ID();

-- 流程分类权限按钮
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

-- AI指令映射
INSERT INTO ai_command_map (command, command_type, menu_path, menu_name, sort, status, remark) VALUES
('流程分类', 'list', 'flowable/category/index', '流程分类', 110, '0', '查询流程分类列表'),
('分类管理', 'list', 'flowable/category/index', '流程分类', 111, '0', '打开流程分类管理');