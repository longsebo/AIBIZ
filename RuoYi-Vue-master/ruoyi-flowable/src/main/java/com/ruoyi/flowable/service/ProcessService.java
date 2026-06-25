package com.ruoyi.flowable.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flowable.domain.ProcessDefinition;
import com.ruoyi.flowable.domain.ProcessInstance;
import com.ruoyi.flowable.domain.TaskInfo;

import java.util.List;
import java.util.Map;

/**
 * 流程服务接口
 *
 * @author aibiz
 */
public interface ProcessService {

    /**
     * 查询流程定义列表
     */
    List<ProcessDefinition> listProcessDefinition();

    /**
     * 部署流程定义
     *
     * @param name 流程名称
     * @param category 流程分类
     * @param bpmnXml BPMN XML内容
     */
    void deployProcessDefinition(String name, String category, String bpmnXml);

    /**
     * 删除流程定义
     *
     * @param deploymentId 部署ID
     */
    void deleteProcessDefinition(String deploymentId);

    /**
     * 启动流程实例
     *
     * @param processDefinitionKey 流程定义Key
     * @param businessKey 业务键
     * @param variables 流程变量
     */
    ProcessInstance startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    /**
     * 查询流程实例详情
     *
     * @param processInstanceId 流程实例ID
     */
    ProcessInstance getProcessInstance(String processInstanceId);

    /**
     * 查询我发起的流程
     *
     * @param userId 用户ID
     */
    List<ProcessInstance> listMyStartedProcess(String userId);

    /**
     * 查询我的待办任务
     *
     * @param userId 用户ID
     */
    List<TaskInfo> listMyTodoTask(String userId);

    /**
     * 查询我的已办任务
     *
     * @param userId 用户ID
     */
    List<TaskInfo> listMyDoneTask(String userId);

    /**
     * 完成任务（通过）
     *
     * @param taskId 任务ID
     * @param variables 流程变量
     */
    void completeTask(String taskId, Map<String, Object> variables);

    /**
     * 驳回任务
     *
     * @param taskId 任务ID
     * @param reason 驳回原因
     */
    void rejectTask(String taskId, String reason);

    /**
     * 委派任务
     *
     * @param taskId 任务ID
     * @param userId 被委派人ID
     */
    void delegateTask(String taskId, String userId);

    /**
     * 认领任务
     *
     * @param taskId 任务ID
     * @param userId 认领人ID
     */
    void claimTask(String taskId, String userId);

    /**
     * 获取流程图XML
     *
     * @param processDefinitionId 流程定义ID
     */
    String getProcessDiagram(String processDefinitionId);
}
