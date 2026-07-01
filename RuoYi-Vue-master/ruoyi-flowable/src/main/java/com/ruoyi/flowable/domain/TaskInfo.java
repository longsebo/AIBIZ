package com.ruoyi.flowable.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务对象
 *
 * @author aibiz
 */
@Data
public class TaskInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private String id;

    /** 任务名称 */
    private String name;

    /** 流程定义ID */
    private String processDefinitionId;

    /** 流程实例ID */
    private String processInstanceId;

    /** 执行实例ID */
    private String executionId;

    /** 流程定义Key */
    private String processDefinitionKey;

    /** 流程定义名称 */
    private String processDefinitionName;

    /** 业务关联键 */
    private String businessKey;

    /** 任务办理人 */
    private String assignee;

    /** 任务办理人名称 */
    private String assigneeName;

    /** 候选用户 */
    private String candidateUser;

    /** 候选组 */
    private String candidateGroups;

    /** 任务创建时间 */
    private Date createTime;

    /** 任务办理期限 */
    private Date dueDate;

    /** 任务优先级 */
    private Integer priority;

    /** 任务描述 */
    private String description;

    /** 任务所属人(委托人) */
    private String owner;

    /** 流程启动人 */
    private String startUserId;
}
