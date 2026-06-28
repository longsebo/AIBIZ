package com.ruoyi.flowable.service.impl;

import com.ruoyi.flowable.domain.ProcessDefinition;
import com.ruoyi.flowable.domain.ProcessInstance;
import com.ruoyi.flowable.domain.TaskInfo;
import com.ruoyi.flowable.domain.SysFlowCategory;
import com.ruoyi.flowable.service.ISysFlowCategoryService;
import com.ruoyi.flowable.service.ProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流程服务实现
 *
 * @author aibiz
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final ISysFlowCategoryService categoryService;

    @Override
    public List<ProcessDefinition> listProcessDefinition() {
        List<ProcessDefinition> result = new ArrayList<>();
        List<SysFlowCategory> categoryList = categoryService.selectCategoryAll();
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().latestVersion();
        List<org.flowable.engine.repository.ProcessDefinition> list = query.list();
        for (org.flowable.engine.repository.ProcessDefinition pd : list) {
            ProcessDefinition d = new ProcessDefinition();
            d.setId(pd.getId());
            d.setName(pd.getName());
            d.setKey(pd.getKey());
            d.setVersion(pd.getVersion());
            d.setCategory(pd.getCategory());
            d.setCategoryName(getCategoryName(pd.getCategory(), categoryList));
            d.setResourceName(pd.getResourceName());
            d.setDeploymentId(pd.getDeploymentId());
            d.setDiagramResourceName(pd.getDiagramResourceName());
            d.setSuspended(pd.isSuspended());
            d.setTenantId(pd.getTenantId());
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(pd.getDeploymentId()).singleResult();
            if (deployment != null) {
                d.setDeploymentTime(deployment.getDeploymentTime());
            }
            result.add(d);
        }
        return result;
    }

    private String getCategoryName(String categoryCode, List<SysFlowCategory> categoryList) {
        if (categoryCode == null || categoryCode.isEmpty()) {
            return "-";
        }
        if (categoryCode.startsWith("http://") || categoryCode.startsWith("https://")) {
            return "其他";
        }
        for (SysFlowCategory category : categoryList) {
            if (categoryCode.equals(category.getCategoryCode())) {
                return category.getCategoryName();
            }
        }
        return categoryCode;
    }

    @Override
    public void deployProcessDefinition(String name, String category, String bpmnXml) {
        repositoryService.createDeployment()
                .name(name)
                .category(category)
                .addString(name + ".bpmn20.xml", bpmnXml)
                .deploy();
    }

    @Override
    public void deleteProcessDefinition(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    @Override
    public ProcessInstance startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        org.flowable.engine.runtime.ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        return convertProcessInstance(pi);
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        org.flowable.engine.runtime.ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (pi == null) {
            // 已结束的流程，从历史中查
            HistoricProcessInstance hi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (hi != null) {
                ProcessInstance result = new ProcessInstance();
                result.setId(hi.getId());
                result.setProcessDefinitionId(hi.getProcessDefinitionId());
                result.setProcessDefinitionKey(hi.getProcessDefinitionKey());
                result.setProcessDefinitionName(hi.getProcessDefinitionName());
                result.setBusinessKey(hi.getBusinessKey());
                result.setStartUserId(hi.getStartUserId());
                result.setStartTime(hi.getStartTime());
                result.setEndTime(hi.getEndTime());
                result.setDurationInMillis(hi.getDurationInMillis());
                result.setEnded(true);
                return result;
            }
            return null;
        }
        return convertProcessInstance(pi);
    }

    @Override
    public List<ProcessInstance> listMyStartedProcess(String userId) {
        List<ProcessInstance> result = new ArrayList<>();
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
                .orderByProcessInstanceStartTime().desc();
        List<HistoricProcessInstance> list = query.list();
        for (HistoricProcessInstance hi : list) {
            ProcessInstance pi = new ProcessInstance();
            pi.setId(hi.getId());
            pi.setProcessDefinitionId(hi.getProcessDefinitionId());
            pi.setProcessDefinitionKey(hi.getProcessDefinitionKey());
            pi.setProcessDefinitionName(hi.getProcessDefinitionName());
            pi.setBusinessKey(hi.getBusinessKey());
            pi.setStartUserId(hi.getStartUserId());
            pi.setStartTime(hi.getStartTime());
            pi.setEndTime(hi.getEndTime());
            pi.setDurationInMillis(hi.getDurationInMillis());
            pi.setEnded(hi.getEndTime() != null);
            result.add(pi);
        }
        return result;
    }

    @Override
    public List<TaskInfo> listMyTodoTask(String userId) {
        List<TaskInfo> result = new ArrayList<>();
        TaskQuery query = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc();
        List<Task> list = query.list();
        for (Task t : list) {
            result.add(convertTask(t));
        }
        return result;
    }

    @Override
    public List<TaskInfo> listMyDoneTask(String userId) {
        List<TaskInfo> result = new ArrayList<>();
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc();
        List<HistoricTaskInstance> list = query.list();
        for (HistoricTaskInstance t : list) {
            TaskInfo info = new TaskInfo();
            info.setId(t.getId());
            info.setName(t.getName());
            info.setProcessDefinitionId(t.getProcessDefinitionId());
            info.setProcessInstanceId(t.getProcessInstanceId());
            info.setAssignee(t.getAssignee());
            info.setCreateTime(t.getCreateTime());
            info.setDueDate(t.getDueDate());
            info.setPriority(t.getPriority());
            info.setDescription(t.getDescription());
            // 从历史流程实例获取额外信息
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId()).singleResult();
            if (hpi != null) {
                info.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
                info.setProcessDefinitionName(hpi.getProcessDefinitionName());
                info.setBusinessKey(hpi.getBusinessKey());
                info.setStartUserId(hpi.getStartUserId());
            }
            result.add(info);
        }
        return result;
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        if (variables != null && !variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
    }

    @Override
    public void rejectTask(String taskId, String reason) {
        // Flowable 8 通过设置流程变量 + 跳转到开始节点实现驳回
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return;
        }
        // 简单驳回：删除当前任务并跳回发起人
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveActivityIdTo(task.getTaskDefinitionKey(), "startEvent")
                .changeState();
        taskService.deleteTask(taskId, reason);
    }

    @Override
    public void delegateTask(String taskId, String userId) {
        taskService.delegateTask(taskId, userId);
    }

    @Override
    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    @Override
    public String getProcessDiagram(String processDefinitionId) {
        org.flowable.engine.repository.ProcessDefinition pd = repositoryService.getProcessDefinition(processDefinitionId);
        if (pd == null) {
            return null;
        }
        InputStream is = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDiagramResourceName());
        if (is == null) {
            return null;
        }
        try {
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取流程图失败", e);
            return null;
        }
    }

    @Override
    public String getProcessXmlByKey(String processDefinitionKey) {
        org.flowable.engine.repository.ProcessDefinition pd = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
        if (pd == null) {
            return null;
        }
        InputStream is = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
        if (is == null) {
            return null;
        }
        try {
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取流程XML失败", e);
            return null;
        }
    }

    private ProcessInstance convertProcessInstance(org.flowable.engine.runtime.ProcessInstance pi) {
        ProcessInstance result = new ProcessInstance();
        result.setId(pi.getId());
        result.setProcessDefinitionId(pi.getProcessDefinitionId());
        result.setProcessDefinitionKey(pi.getProcessDefinitionKey());
        result.setProcessDefinitionName(pi.getProcessDefinitionName());
        result.setBusinessKey(pi.getBusinessKey());
        result.setStartUserId(pi.getStartUserId());
        result.setStartTime(pi.getStartTime());
        result.setEnded(false);
        result.setSuspended(pi.isSuspended());
        result.setProcessVariables(pi.getProcessVariables());
        return result;
    }

    private TaskInfo convertTask(Task t) {
        TaskInfo info = new TaskInfo();
        info.setId(t.getId());
        info.setName(t.getName());
        info.setProcessDefinitionId(t.getProcessDefinitionId());
        info.setProcessInstanceId(t.getProcessInstanceId());
        info.setExecutionId(t.getExecutionId());
        info.setAssignee(t.getAssignee());
        info.setCreateTime(t.getCreateTime());
        info.setDueDate(t.getDueDate());
        info.setPriority(t.getPriority());
        info.setDescription(t.getDescription());
        info.setOwner(t.getOwner());
        // 从流程实例获取额外信息
        org.flowable.engine.runtime.ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(t.getProcessInstanceId()).singleResult();
        if (pi != null) {
            info.setProcessDefinitionKey(pi.getProcessDefinitionKey());
            info.setProcessDefinitionName(pi.getProcessDefinitionName());
            info.setBusinessKey(pi.getBusinessKey());
            info.setStartUserId(pi.getStartUserId());
        }
        return info;
    }

    @Override
    public void updateAndDeployProcess(String processDefinitionKey, String name, String category, String bpmnXml) {
        // 先查询已有流程定义
        org.flowable.engine.repository.ProcessDefinition existingDef = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();

        if (existingDef != null) {
            // 如果已存在，先删除旧版本
            List<org.flowable.engine.repository.ProcessDefinition> versions = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .list();
            for (org.flowable.engine.repository.ProcessDefinition pd : versions) {
                repositoryService.deleteDeployment(pd.getDeploymentId(), true);
            }
        }

        // 部署新版本
        repositoryService.createDeployment()
                .name(name)
                .key(processDefinitionKey)
                .category(category)
                .addString(name + ".bpmn20.xml", bpmnXml)
                .deploy();
    }
}
