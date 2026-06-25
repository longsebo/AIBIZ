package com.ruoyi.flowable.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程定义对象
 *
 * @author aibiz
 */
@Data
public class ProcessDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 流程定义ID */
    private String id;

    /** 流程名称 */
    private String name;

    /** 流程Key */
    private String key;

    /** 版本号 */
    private Integer version;

    /** 分类 */
    private String category;

    /** 流程定义文件名称 */
    private String resourceName;

    /** 部署ID */
    private String deploymentId;

    /** 部署时间 */
    private Date deploymentTime;

    /** 流程图资源名称 */
    private String diagramResourceName;

    /** 是否挂起 */
    private Boolean suspended;

    /** 租户ID */
    private String tenantId;
}
