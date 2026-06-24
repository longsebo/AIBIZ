-- AI指令映射表
DROP TABLE IF EXISTS `ai_command_map`;
CREATE TABLE `ai_command_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `command` varchar(100) NOT NULL COMMENT '指令关键词',
  `command_type` varchar(20) DEFAULT 'view' COMMENT '指令类型：view=查看, add=新增, edit=编辑, delete=删除',
  `menu_path` varchar(200) NOT NULL COMMENT '菜单路径',
  `menu_name` varchar(100) DEFAULT NULL COMMENT '菜单名称',
  `sort` int(4) DEFAULT 0 COMMENT '排序',
  `status` char(1) DEFAULT '0' COMMENT '状态：0正常，1停用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_command` (`command`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI指令映射表';

-- 初始化所有菜单的指令映射
INSERT INTO `ai_command_map` (`command`, `command_type`, `menu_path`, `menu_name`, `sort`, `status`, `remark`) VALUES
-- 用户管理
('用户列表', 'view', 'system/user/index', '用户管理', 1, '0', '查看用户列表'),
('用户管理', 'view', 'system/user/index', '用户管理', 1, '0', '查看用户列表'),
('新增用户', 'add', 'system/user/index', '用户管理', 2, '0', '新增用户'),
('添加用户', 'add', 'system/user/index', '用户管理', 2, '0', '新增用户'),
-- 角色管理
('角色列表', 'view', 'system/role/index', '角色管理', 3, '0', '查看角色列表'),
('角色管理', 'view', 'system/role/index', '角色管理', 3, '0', '查看角色列表'),
('新增角色', 'add', 'system/role/index', '角色管理', 4, '0', '新增角色'),
-- 菜单管理
('菜单列表', 'view', 'system/menu/index', '菜单管理', 5, '0', '查看菜单列表'),
('菜单管理', 'view', 'system/menu/index', '菜单管理', 5, '0', '查看菜单列表'),
-- 部门管理
('部门列表', 'view', 'system/dept/index', '部门管理', 6, '0', '查看部门列表'),
('部门管理', 'view', 'system/dept/index', '部门管理', 6, '0', '查看部门列表'),
('新增部门', 'add', 'system/dept/index', '部门管理', 7, '0', '新增部门'),
-- 岗位管理
('岗位列表', 'view', 'system/post/index', '岗位管理', 8, '0', '查看岗位列表'),
('岗位管理', 'view', 'system/post/index', '岗位管理', 8, '0', '查看岗位列表'),
('新增岗位', 'add', 'system/post/index', '岗位管理', 9, '0', '新增岗位'),
-- 字典管理
('字典列表', 'view', 'system/dict/index', '字典管理', 10, '0', '查看字典列表'),
('字典管理', 'view', 'system/dict/index', '字典管理', 10, '0', '查看字典列表'),
('新增字典', 'add', 'system/dict/index', '字典管理', 11, '0', '新增字典'),
-- 参数设置
('参数列表', 'view', 'system/config/index', '参数设置', 12, '0', '查看参数列表'),
('参数设置', 'view', 'system/config/index', '参数设置', 12, '0', '查看参数设置'),
('新增参数', 'add', 'system/config/index', '参数设置', 13, '0', '新增参数'),
-- 通知公告
('公告列表', 'view', 'system/notice/index', '通知公告', 14, '0', '查看公告列表'),
('通知公告', 'view', 'system/notice/index', '通知公告', 14, '0', '查看通知公告'),
('新增公告', 'add', 'system/notice/index', '通知公告', 15, '0', '新增公告'),
-- 操作日志
('操作日志', 'view', 'monitor/operlog/index', '操作日志', 16, '0', '查看操作日志'),
-- 登录日志
('登录日志', 'view', 'monitor/logininfor/index', '登录日志', 17, '0', '查看登录日志'),
-- 在线用户
('在线用户', 'view', 'monitor/online/index', '在线用户', 18, '0', '查看在线用户'),
-- 定时任务
('定时任务', 'view', 'monitor/job/index', '定时任务', 19, '0', '查看定时任务'),
('新增任务', 'add', 'monitor/job/index', '定时任务', 20, '0', '新增定时任务'),
-- 数据监控
('数据监控', 'view', 'monitor/druid/index', '数据监控', 21, '0', '查看数据监控'),
-- 服务监控
('系统监控', 'view', 'monitor/server/index', '服务监控', 22, '0', '查看系统监控'),
('服务器监控', 'view', 'monitor/server/index', '服务监控', 22, '0', '查看服务器监控'),
-- 缓存监控
('缓存监控', 'view', 'monitor/cache/index', '缓存监控', 23, '0', '查看缓存监控'),
('缓存列表', 'view', 'monitor/cache/list', '缓存列表', 24, '0', '查看缓存列表'),
-- 表单构建
('表单构建', 'view', 'tool/build/index', '表单构建', 25, '0', '查看表单构建'),
-- 代码生成
('代码生成', 'view', 'tool/gen/index', '代码生成', 26, '0', '查看代码生成'),
-- 系统接口
('系统接口', 'view', 'tool/swagger/index', '系统接口', 27, '0', '查看系统接口');
