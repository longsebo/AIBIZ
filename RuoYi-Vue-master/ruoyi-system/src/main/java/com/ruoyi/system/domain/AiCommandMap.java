package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.TreeEntity;

/**
 * AI指令映射表 ai_command_map
 *
 * @author ruoyi
 */
public class AiCommandMap extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 指令关键词 */
    private String command;

    /** 指令类型：view=查看, add=新增, edit=编辑, delete=删除 */
    private String commandType;

    /** 菜单路径 */
    private String menuPath;

    /** 菜单名称 */
    private String menuName;

    /** 排序 */
    private Integer sort;

    /** 状态：0正常，1停用 */
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getMenuPath() {
        return menuPath;
    }

    public void setMenuPath(String menuPath) {
        this.menuPath = menuPath;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AiCommandMap{" +
                "id=" + id +
                ", command='" + command + '\'' +
                ", commandType='" + commandType + '\'' +
                ", menuPath='" + menuPath + '\'' +
                ", menuName='" + menuName + '\'' +
                ", sort=" + sort +
                ", status='" + status + '\'' +
                '}';
    }
}
