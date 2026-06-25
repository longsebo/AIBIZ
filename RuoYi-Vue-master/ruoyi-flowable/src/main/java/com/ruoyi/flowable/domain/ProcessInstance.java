package com.ruoyi.flowable.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 流程实例对象
 *
 * @author aibiz
 */
@Data
public class ProcessInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 流程实例ID */
    private String id;

    /** 流程定义ID */
    private String processDefinitionId;

    /** 流程定义Key */
    private String processDefinitionKey;

    /** 流程定义名称 */
    private String processDefinitionName;

    /** 业务关联键 */
    private String businessKey;

    /** 启动人 */
    private String startUserId;

    /** 启动人姓名 */
    private String startUserName;

    /** 启动时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 流程持续时长(毫秒) */
    private Long durationInMillis;

    /** 是否结束 */
    private Boolean ended;

    /** 是否挂起 */
    private Boolean suspended;

    /** 当前活动ID */
    private String activityId;

    /** 流程变量 */
    private Map<String, Object> processVariables;
}
