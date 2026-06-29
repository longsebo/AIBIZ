package com.ruoyi.flowable.service.impl;

import com.ruoyi.flowable.domain.ProcessDefinition;
import com.ruoyi.flowable.domain.ProcessInstance;
import com.ruoyi.flowable.domain.TaskInfo;
import com.ruoyi.flowable.domain.SysFlowCategory;
import com.ruoyi.flowable.service.ISysFlowCategoryService;
import com.ruoyi.flowable.service.ISysProcessCcService;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final ISysProcessCcService ccService;
    private final JdbcTemplate jdbcTemplate;

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
        if (variables == null) {
            variables = new java.util.HashMap<>();
        }

        resolveCountersignAssignees(processDefinitionKey, variables);

        org.flowable.engine.runtime.ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        
        assignFirstTask(processDefinitionKey, pi.getId(), variables);
        
        saveCcRecordsOnStart(pi);
        
        return convertProcessInstance(pi);
    }

    /**
     * 解析会签用户任务的审批人配置，计算 assigneeList 变量
     */
    private void resolveCountersignAssignees(String processDefinitionKey, Map<String, Object> variables) {
        try {
            log.info("开始解析会签审批人配置, processDefinitionKey={}", processDefinitionKey);

            org.flowable.engine.repository.ProcessDefinition pd = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .latestVersion()
                    .singleResult();
            if (pd == null) {
                log.warn("未找到流程定义, processDefinitionKey={}", processDefinitionKey);
                return;
            }
            log.info("找到流程定义, id={}, name={}, resourceName={}", pd.getId(), pd.getName(), pd.getResourceName());

            InputStream is = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
            if (is == null) {
                log.warn("无法获取流程XML资源, deploymentId={}, resourceName={}", pd.getDeploymentId(), pd.getResourceName());
                return;
            }
            String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            log.info("流程XML长度={}", xml.length());

            String startEventId = extractStartEventId(xml);
            log.info("找到startEvent节点id={}", startEventId);
            if (startEventId == null || startEventId.isEmpty()) {
                log.warn("未找到startEvent节点");
                return;
            }

            List<String> nextNodeIds = findNextNodeIds(xml, startEventId);
            log.info("startEvent的后续节点ids={}", nextNodeIds);
            if (nextNodeIds.isEmpty()) {
                log.warn("startEvent没有后续节点");
                return;
            }

            Map<String, String> targetTask = null;
            for (String nodeId : nextNodeIds) {
                targetTask = extractUserTaskById(xml, nodeId);
                if (targetTask != null) {
                    log.info("找到后续用户任务节点id={}", nodeId);
                    break;
                }
            }

            if (targetTask == null) {
                log.warn("startEvent的后续节点中未找到用户任务");
                return;
            }

            String userTaskConfig = targetTask.get("config");
            String innerContent = targetTask.get("content");
            log.info("后续用户任务 - config原始值={}, content长度={}", userTaskConfig, innerContent != null ? innerContent.length() : 0);

            boolean hasCountersign = innerContent != null && innerContent.contains("multiInstanceLoopCharacteristics");
            log.info("后续用户任务是否配置会签={}", hasCountersign);

            if (!hasCountersign) {
                log.info("后续用户任务未配置会签，无需构造assigneeList");
                return;
            }

            if (userTaskConfig == null || userTaskConfig.isEmpty()) {
                log.warn("后续用户任务的userTaskConfig为空");
                return;
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String decodedConfig = userTaskConfig.replace("&#34;", "\"").replace("&quot;", "\"");
            Map<String, Object> config = mapper.readValue(decodedConfig, Map.class);
            log.info("解析后的config={}", config);

            String assigneeType = (String) config.get("assigneeType");
            log.info("assigneeType={}", assigneeType);
            List<String> assigneeList = new ArrayList<>();

            if ("user".equals(assigneeType)) {
                List<Object> assigneeUsers = (List<Object>) config.get("assigneeUsers");
                log.info("指定用户模式, assigneeUsers={}", assigneeUsers);
                if (assigneeUsers != null) {
                    for (Object userId : assigneeUsers) {
                        if (userId != null) {
                            assigneeList.add(userId.toString());
                        }
                    }
                }
            } else if ("dept".equals(assigneeType)) {
                List<Object> assigneeDepts = (List<Object>) config.get("assigneeDepts");
                log.info("部门模式, assigneeDepts={}", assigneeDepts);
                if (assigneeDepts != null && !assigneeDepts.isEmpty()) {
                    Set<String> userIds = new HashSet<>();
                    StringBuilder sql = new StringBuilder(
                        "select distinct u.user_id from sys_user u " +
                        "inner join sys_dept d on u.dept_id = d.dept_id " +
                        "where u.del_flag = '0' and (");
                    List<Long> deptIds = new ArrayList<>();
                    for (int i = 0; i < assigneeDepts.size(); i++) {
                        if (i > 0) sql.append(" or ");
                        sql.append("d.ancestors like concat(?, '%') or u.dept_id = ?");
                        Long did = Long.valueOf(assigneeDepts.get(i).toString());
                        deptIds.add(did);
                        deptIds.add(did);
                    }
                    sql.append(")");
                    log.info("部门查询SQL={}, 参数={}", sql, deptIds);
                    List<Long> userIdList = jdbcTemplate.queryForList(sql.toString(), Long.class, deptIds.toArray());
                    log.info("查询到部门用户数量={}", userIdList.size());
                    for (Long uid : userIdList) {
                        userIds.add(uid.toString());
                    }
                    assigneeList.addAll(userIds);
                }
            } else if ("role".equals(assigneeType)) {
                List<Object> assigneeRoles = (List<Object>) config.get("assigneeRoles");
                log.info("角色模式, assigneeRoles={}", assigneeRoles);
                if (assigneeRoles != null && !assigneeRoles.isEmpty()) {
                    Set<String> userIds = new HashSet<>();
                    StringBuilder sql = new StringBuilder(
                        "select distinct u.user_id from sys_user u " +
                        "inner join sys_user_role ur on u.user_id = ur.user_id " +
                        "where u.del_flag = '0' and ur.role_id in (");
                    List<Long> roleIds = new ArrayList<>();
                    for (int i = 0; i < assigneeRoles.size(); i++) {
                        if (i > 0) sql.append(",");
                        sql.append("?");
                        roleIds.add(Long.valueOf(assigneeRoles.get(i).toString()));
                    }
                    sql.append(")");
                    log.info("角色查询SQL={}, 参数={}", sql, roleIds);
                    List<Long> userIdList = jdbcTemplate.queryForList(sql.toString(), Long.class, roleIds.toArray());
                    log.info("查询到角色用户数量={}", userIdList.size());
                    for (Long uid : userIdList) {
                        userIds.add(uid.toString());
                    }
                    assigneeList.addAll(userIds);
                }
            } else if ("formComponent".equals(assigneeType)) {
                String formField = (String) config.get("assigneeFormComponent");
                log.info("表单组件模式, formField={}, variables中是否存在={}", formField, variables.containsKey(formField));
                if (formField != null && variables.containsKey(formField)) {
                    Object fieldValue = variables.get(formField);
                    log.info("表单字段值={}, 类型={}", fieldValue, fieldValue.getClass().getName());
                    if (fieldValue instanceof String) {
                        String[] ids = ((String) fieldValue).split(",");
                        for (String id : ids) {
                            if (id != null && !id.trim().isEmpty()) {
                                assigneeList.add(id.trim());
                            }
                        }
                    } else if (fieldValue instanceof List) {
                        for (Object item : (List<?>) fieldValue) {
                            if (item != null) {
                                assigneeList.add(item.toString());
                            }
                        }
                    }
                }
            } else {
                log.warn("未知的审批人类型={}", assigneeType);
            }

            log.info("计算得到assigneeList={}, size={}", assigneeList, assigneeList.size());
            if (!assigneeList.isEmpty()) {
                variables.put("assigneeList", assigneeList);
                log.info("已将assigneeList放入流程变量");
            } else {
                log.warn("assigneeList为空，未放入流程变量");
            }
        } catch (Exception e) {
            log.error("解析会签审批人配置失败, processDefinitionKey={}", processDefinitionKey, e);
        }
    }

    /**
     * 为流程实例的第一个用户任务分配审批人
     */
    private void assignFirstTask(String processDefinitionKey, String processInstanceId, Map<String, Object> variables) {
        try {
            log.info("开始为流程实例分配第一个任务审批人, processDefinitionKey={}, processInstanceId={}", processDefinitionKey, processInstanceId);

            List<Task> unassignedTasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .taskAssignee(null)
                    .list();
            log.info("找到未分配任务数量={}", unassignedTasks.size());

            if (unassignedTasks.isEmpty()) {
                log.info("没有未分配的任务");
                return;
            }

            org.flowable.engine.repository.ProcessDefinition pd = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .latestVersion()
                    .singleResult();
            if (pd == null) {
                log.warn("未找到流程定义");
                return;
            }

            InputStream is = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
            if (is == null) {
                log.warn("无法获取流程XML资源");
                return;
            }
            String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            for (Task task : unassignedTasks) {
                String taskDefinitionKey = task.getTaskDefinitionKey();
                log.info("处理未分配任务, taskId={}, taskDefinitionKey={}", task.getId(), taskDefinitionKey);

                Map<String, String> userTask = extractUserTaskById(xml, taskDefinitionKey);
                if (userTask == null) {
                    log.warn("未找到任务配置, taskDefinitionKey={}", taskDefinitionKey);
                    continue;
                }

                String userTaskConfig = userTask.get("config");
                String innerContent = userTask.get("content");

                boolean hasCountersign = innerContent != null && innerContent.contains("multiInstanceLoopCharacteristics");
                log.info("任务是否配置会签={}", hasCountersign);

                if (userTaskConfig == null || userTaskConfig.isEmpty()) {
                    log.warn("用户任务的userTaskConfig为空");
                    continue;
                }

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String decodedConfig = userTaskConfig.replace("&#34;", "\"").replace("&quot;", "\"");
                Map<String, Object> config = mapper.readValue(decodedConfig, Map.class);

                String assigneeType = (String) config.get("assigneeType");
                List<String> assigneeList = new ArrayList<>();

                if ("user".equals(assigneeType)) {
                    List<Object> assigneeUsers = (List<Object>) config.get("assigneeUsers");
                    if (assigneeUsers != null) {
                        for (Object userId : assigneeUsers) {
                            if (userId != null) {
                                assigneeList.add(userId.toString());
                            }
                        }
                    }
                } else if ("dept".equals(assigneeType)) {
                    List<Object> assigneeDepts = (List<Object>) config.get("assigneeDepts");
                    if (assigneeDepts != null && !assigneeDepts.isEmpty()) {
                        Set<String> userIds = new HashSet<>();
                        StringBuilder sql = new StringBuilder(
                            "select distinct u.user_id from sys_user u " +
                            "inner join sys_dept d on u.dept_id = d.dept_id " +
                            "where u.del_flag = '0' and (");
                        List<Long> deptIds = new ArrayList<>();
                        for (int i = 0; i < assigneeDepts.size(); i++) {
                            if (i > 0) sql.append(" or ");
                            sql.append("d.ancestors like concat(?, '%') or u.dept_id = ?");
                            Long did = Long.valueOf(assigneeDepts.get(i).toString());
                            deptIds.add(did);
                            deptIds.add(did);
                        }
                        sql.append(")");
                        List<Long> userIdList = jdbcTemplate.queryForList(sql.toString(), Long.class, deptIds.toArray());
                        for (Long uid : userIdList) {
                            userIds.add(uid.toString());
                        }
                        assigneeList.addAll(userIds);
                    }
                } else if ("role".equals(assigneeType)) {
                    List<Object> assigneeRoles = (List<Object>) config.get("assigneeRoles");
                    if (assigneeRoles != null && !assigneeRoles.isEmpty()) {
                        Set<String> userIds = new HashSet<>();
                        StringBuilder sql = new StringBuilder(
                            "select distinct u.user_id from sys_user u " +
                            "inner join sys_user_role ur on u.user_id = ur.user_id " +
                            "where u.del_flag = '0' and ur.role_id in (");
                        List<Long> roleIds = new ArrayList<>();
                        for (int i = 0; i < assigneeRoles.size(); i++) {
                            if (i > 0) sql.append(",");
                            sql.append("?");
                            roleIds.add(Long.valueOf(assigneeRoles.get(i).toString()));
                        }
                        sql.append(")");
                        List<Long> userIdList = jdbcTemplate.queryForList(sql.toString(), Long.class, roleIds.toArray());
                        for (Long uid : userIdList) {
                            userIds.add(uid.toString());
                        }
                        assigneeList.addAll(userIds);
                    }
                } else if ("formComponent".equals(assigneeType)) {
                    String fieldKey = (String) config.get("fieldKey");
                    if (fieldKey != null) {
                        Object fieldValue = variables.get(fieldKey);
                        if (fieldValue instanceof String) {
                            String[] ids = fieldValue.toString().split(",");
                            for (String id : ids) {
                                if (id != null && !id.trim().isEmpty()) {
                                    assigneeList.add(id.trim());
                                }
                            }
                        } else if (fieldValue instanceof List) {
                            for (Object item : (List<?>) fieldValue) {
                                if (item != null) {
                                    assigneeList.add(item.toString());
                                }
                            }
                        }
                    }
                }

                if (!assigneeList.isEmpty()) {
                    if (hasCountersign) {
                        String assignee = assigneeList.get(0);
                        taskService.setAssignee(task.getId(), assignee);
                        log.info("已为会签任务分配审批人, taskId={}, assignee={}", task.getId(), assignee);
                    } else {
                        String assignee = assigneeList.get(0);
                        taskService.setAssignee(task.getId(), assignee);
                        log.info("已为普通任务分配审批人, taskId={}, assignee={}", task.getId(), assignee);
                        if (assigneeList.size() > 1) {
                            for (int i = 1; i < assigneeList.size(); i++) {
                                taskService.addCandidateUser(task.getId(), assigneeList.get(i));
                            }
                        }
                    }
                } else {
                    log.warn("未计算出审批人, taskId={}", task.getId());
                }
            }
        } catch (Exception e) {
            log.error("为第一个任务分配审批人失败", e);
        }
    }

    /**
     * 从BPMN XML中提取所有会签用户任务的配置
     */
    private List<Map<String, String>> extractCountersignUserTasks(String xml) {
        List<Map<String, String>> result = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<bpmn:userTask\\s+([^>]*flowable:userTaskConfig=\"[^\"]*\"[^>]*)>([\\s\\S]*?)</bpmn:userTask>",
            java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        
        int userTaskCount = 0;
        int countersignCount = 0;
        
        while (matcher.find()) {
            userTaskCount++;
            String openTagAttrs = matcher.group(1);
            String innerContent = matcher.group(2);
            log.debug("找到用户任务, attrs长度={}, content长度={}", openTagAttrs.length(), innerContent.length());
            
            boolean hasCountersign = innerContent.contains("multiInstanceLoopCharacteristics");
            log.debug("是否会签={}", hasCountersign);
            
            if (hasCountersign) {
                countersignCount++;
                java.util.regex.Pattern configPattern = java.util.regex.Pattern.compile("flowable:userTaskConfig=\"([^\"]*)\"");
                java.util.regex.Matcher configMatcher = configPattern.matcher(openTagAttrs);
                if (configMatcher.find()) {
                    Map<String, String> task = new java.util.HashMap<>();
                    task.put("config", configMatcher.group(1));
                    result.add(task);
                    log.debug("提取到会签配置, config长度={}", configMatcher.group(1).length());
                } else {
                    log.debug("未找到flowable:userTaskConfig属性");
                }
            }
        }
        
        log.info("扫描完成: 用户任务总数={}, 会签任务数={}, 提取到配置数={}", userTaskCount, countersignCount, result.size());
        return result;
    }

    /**
     * 从BPMN XML中提取startEvent的id
     */
    private String extractStartEventId(String xml) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<bpmn:startEvent\\s+id=\"([^\"]*)\"");
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 通过sequenceFlow找到指定节点的后续节点ids
     */
    private List<String> findNextNodeIds(String xml, String sourceNodeId) {
        List<String> result = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<bpmn:sequenceFlow\\s+[^>]*sourceRef=\"" + sourceNodeId + "\"[^>]*targetRef=\"([^\"]*)\"");
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    /**
     * 通过id提取用户任务的配置和内容
     */
    private Map<String, String> extractUserTaskById(String xml, String taskId) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<bpmn:userTask\\s+id=\"" + java.util.regex.Pattern.quote(taskId) + "\"([^>]*)>([\\s\\S]*?)</bpmn:userTask>");
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            Map<String, String> task = new java.util.HashMap<>();
            String attrs = matcher.group(1);
            String content = matcher.group(2);
            
            java.util.regex.Pattern configPattern = java.util.regex.Pattern.compile("flowable:userTaskConfig=\"([^\"]*)\"");
            java.util.regex.Matcher configMatcher = configPattern.matcher(attrs);
            if (configMatcher.find()) {
                task.put("config", configMatcher.group(1));
            }
            task.put("content", content);
            return task;
        }
        return null;
    }
    
    private void saveCcRecordsOnStart(org.flowable.engine.runtime.ProcessInstance pi) {
        org.flowable.engine.repository.ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(pi.getProcessDefinitionId())
                .singleResult();
        
        if (pd == null) {
            return;
        }
        
        try {
            InputStream is = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
            String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String startEventConfig = extractStartEventConfig(xml);
            
            if (startEventConfig != null && !startEventConfig.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> config = mapper.readValue(startEventConfig, java.util.Map.class);
                
                String ccType = (String) config.get("ccType");
                
                ccService.saveCcRecords(pi.getId(), pi.getProcessDefinitionId(), 
                        pi.getProcessDefinitionKey(), pd.getName(), config, ccType);
            }
        } catch (Exception e) {
            log.error("保存抄送记录失败", e);
        }
    }
    
    private String extractStartEventConfig(String xml) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("flowable:startEventConfig=\"([^\"]*)\"");
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).replace("&#34;", "\"").replace("&quot;", "\"");
        }
        return null;
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
        Set<String> taskIds = new HashSet<>();
        
        TaskQuery assigneeQuery = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc();
        List<Task> assigneeTasks = assigneeQuery.list();
        for (Task t : assigneeTasks) {
            taskIds.add(t.getId());
            result.add(convertTask(t));
        }
        
        TaskQuery candidateQuery = taskService.createTaskQuery().taskCandidateUser(userId).orderByTaskCreateTime().desc();
        List<Task> candidateTasks = candidateQuery.list();
        for (Task t : candidateTasks) {
            if (!taskIds.contains(t.getId())) {
                result.add(convertTask(t));
            }
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
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return;
        }
        
        if (variables == null) {
            variables = new java.util.HashMap<>();
        }
        
        String comment = (String) variables.remove("comment");
        String action = (String) variables.remove("action");
        
        // 将评论信息保存到流程变量中，便于历史查询
        if (comment != null && !comment.isEmpty()) {
            variables.put("_COMMENT", comment);
        }
        if (action != null) {
            variables.put("_ACTION", action);
        }
        
        String processDefinitionKey = task.getProcessDefinitionId().split(":")[0];
        resolveNextNodeAssignees(task.getProcessDefinitionId(), task.getTaskDefinitionKey(), variables);
        
        if (!variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
    }

    private void resolveNextNodeAssignees(String processDefinitionId, String currentTaskDefinitionKey, Map<String, Object> variables) {
        try {
            org.flowable.engine.repository.ProcessDefinition pd = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();
            
            if (pd == null) {
                return;
            }
            
            InputStream is = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
            if (is == null) {
                return;
            }
            
            String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> currentVariables = variables;
            
            List<String> nextNodeIds = findNextNodeIds(xml, currentTaskDefinitionKey);
            
            for (String nodeId : nextNodeIds) {
                String nodeType = identifyNodeType(xml, nodeId);
                
                if ("exclusiveGateway".equals(nodeType) || "parallelGateway".equals(nodeType) || "inclusiveGateway".equals(nodeType)) {
                    List<String> gatewayNextNodes = findNextNodeIds(xml, nodeId);
                    for (String gatewayNextNodeId : gatewayNextNodes) {
                        String seqFlowId = findSequenceFlowId(xml, nodeId, gatewayNextNodeId);
                        String condition = extractConditionExpression(xml, seqFlowId);
                        boolean shouldTake = evaluateCondition(condition, currentVariables);
                        
                        if (shouldTake || "parallelGateway".equals(nodeType)) {
                            String nextNodeType = identifyNodeType(xml, gatewayNextNodeId);
                            if ("userTask".equals(nextNodeType)) {
                                resolveUserTaskAssignees(xml, gatewayNextNodeId, currentVariables);
                            }
                        }
                    }
                } else if ("userTask".equals(nodeType)) {
                    resolveUserTaskAssignees(xml, nodeId, currentVariables);
                }
            }
        } catch (Exception e) {
            log.error("解析下一节点审批人失败", e);
        }
    }

    private void resolveUserTaskAssignees(String xml, String nodeId, Map<String, Object> variables) {
        Map<String, String> taskInfo = extractUserTaskById(xml, nodeId);
        if (taskInfo == null) {
            return;
        }
        
        String config = taskInfo.get("config");
        if (config == null || config.isEmpty()) {
            return;
        }
        
        try {
            String decodedConfig = config.replace("&#34;", "\"").replace("&quot;", "\"");
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> configMap = mapper.readValue(decodedConfig, Map.class);
            
            String assigneeType = (String) configMap.get("assigneeType");
            List<String> assigneeList = new ArrayList<>();
            
            if ("user".equals(assigneeType)) {
                List<Object> assigneeUsers = (List<Object>) configMap.get("assigneeUsers");
                if (assigneeUsers != null) {
                    for (Object userId : assigneeUsers) {
                        if (userId != null) {
                            assigneeList.add(userId.toString());
                        }
                    }
                }
            } else if ("dept".equals(assigneeType)) {
                List<Object> assigneeDepts = (List<Object>) configMap.get("assigneeDepts");
                if (assigneeDepts != null && !assigneeDepts.isEmpty()) {
                    Set<String> userIds = new HashSet<>();
                    StringBuilder sql = new StringBuilder(
                        "select distinct u.user_id from sys_user u " +
                        "inner join sys_dept d on u.dept_id = d.dept_id " +
                        "where u.del_flag = '0' and (");
                    List<Long> deptIds = new ArrayList<>();
                    for (int i = 0; i < assigneeDepts.size(); i++) {
                        if (i > 0) sql.append(" or ");
                        sql.append("d.ancestors like concat(?, '%') or u.dept_id = ?");
                        Long did = Long.valueOf(assigneeDepts.get(i).toString());
                        deptIds.add(did);
                        deptIds.add(did);
                    }
                    sql.append(")");
                    List<Long> userIdList = jdbcTemplate.queryForList(sql.toString(), Long.class, deptIds.toArray());
                    for (Long uid : userIdList) {
                        userIds.add(uid.toString());
                    }
                    assigneeList.addAll(userIds);
                }
            } else if ("role".equals(assigneeType)) {
                List<Object> assigneeRoles = (List<Object>) configMap.get("assigneeRoles");
                if (assigneeRoles != null && !assigneeRoles.isEmpty()) {
                    Set<String> userIds = new HashSet<>();
                    StringBuilder sql = new StringBuilder(
                        "select distinct u.user_id from sys_user u " +
                        "inner join sys_user_role ur on u.user_id = ur.user_id " +
                        "where u.del_flag = '0' and ur.role_id in (");
                    List<Long> roleIds = new ArrayList<>();
                    for (int i = 0; i < assigneeRoles.size(); i++) {
                        if (i > 0) sql.append(",");
                        sql.append("?");
                        roleIds.add(Long.valueOf(assigneeRoles.get(i).toString()));
                    }
                    sql.append(")");
                    List<Long> userIdList = jdbcTemplate.queryForList(sql.toString(), Long.class, roleIds.toArray());
                    for (Long uid : userIdList) {
                        userIds.add(uid.toString());
                    }
                    assigneeList.addAll(userIds);
                }
            } else if ("formComponent".equals(assigneeType)) {
                String formField = (String) configMap.get("assigneeFormComponent");
                if (formField != null && variables.containsKey(formField)) {
                    Object fieldValue = variables.get(formField);
                    if (fieldValue instanceof String) {
                        String[] ids = ((String) fieldValue).split(",");
                        for (String id : ids) {
                            if (id != null && !id.trim().isEmpty()) {
                                assigneeList.add(id.trim());
                            }
                        }
                    } else if (fieldValue instanceof List) {
                        for (Object item : (List<?>) fieldValue) {
                            if (item != null) {
                                assigneeList.add(item.toString());
                            }
                        }
                    }
                }
            }
            
            if (!assigneeList.isEmpty()) {
                String innerContent = taskInfo.get("content");
                if (innerContent != null && innerContent.contains("multiInstanceLoopCharacteristics")) {
                    variables.put("assigneeList", assigneeList);
                    log.info("已设置会签assigneeList={} for node {}", assigneeList, nodeId);
                } else {
                    String assignee = assigneeList.get(0);
                    variables.put("assignee", assignee);
                    log.info("已设置审批人assignee={} for node {}", assignee, nodeId);
                }
            }
        } catch (Exception e) {
            log.warn("解析用户任务审批人配置失败, nodeId={}", nodeId, e);
        }
    }

    @Override
    public void rejectTask(String taskId, String reason) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return;
        }
        
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        if (reason != null && !reason.isEmpty()) {
            variables.put("_COMMENT", reason);
        }
        variables.put("_ACTION", "reject");
        
        runtimeService.setVariables(task.getExecutionId(), variables);
        
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
        repositoryService.createDeployment()
                .name(name)
                .key(processDefinitionKey)
                .category(category)
                .addString(name + ".bpmn20.xml", bpmnXml)
                .deploy();
    }

    @Override
    public com.ruoyi.flowable.domain.TaskDetail getTaskDetail(String taskId) {
        com.ruoyi.flowable.domain.TaskDetail detail = new com.ruoyi.flowable.domain.TaskDetail();
        
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return null;
        }
        
        detail.setTaskId(task.getId());
        detail.setTaskName(task.getName());
        detail.setProcessDefinitionId(task.getProcessDefinitionId());
        detail.setProcessInstanceId(task.getProcessInstanceId());
        detail.setExecutionId(task.getExecutionId());
        detail.setAssignee(task.getAssignee());
        detail.setCreateTime(task.getCreateTime());
        
        org.flowable.engine.runtime.ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        if (pi != null) {
            detail.setProcessDefinitionKey(pi.getProcessDefinitionKey());
            detail.setProcessDefinitionName(pi.getProcessDefinitionName());
            detail.setBusinessKey(pi.getBusinessKey());
            detail.setStartUserId(pi.getStartUserId());
            detail.setProcessVariables(runtimeService.getVariables(pi.getId()));
        } else {
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).singleResult();
            if (hpi != null) {
                detail.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
                detail.setProcessDefinitionName(hpi.getProcessDefinitionName());
                detail.setBusinessKey(hpi.getBusinessKey());
                detail.setStartUserId(hpi.getStartUserId());
            }
            Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).list()
                    .stream().collect(java.util.stream.Collectors.toMap(
                            var -> var.getVariableName(),
                            var -> var.getValue()
                    ));
            detail.setProcessVariables(variables);
        }
        
        detail.setHistoryComments(queryHistoryComments(task.getProcessInstanceId(), task.getTaskDefinitionKey()));
        
        detail.setNextNodes(calculateNextNodes(task));
        
        detail.setCurrentNodeConfig(getCurrentNodeConfig(task));
        
        return detail;
    }

    private List<com.ruoyi.flowable.domain.TaskDetail.HistoryComment> queryHistoryComments(String processInstanceId, String currentTaskDefinitionKey) {
        List<com.ruoyi.flowable.domain.TaskDetail.HistoryComment> comments = new ArrayList<>();
        
        List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricTaskInstanceEndTime().asc()
                .list();
        
        for (HistoricTaskInstance ht : historyTasks) {
            com.ruoyi.flowable.domain.TaskDetail.HistoryComment comment = new com.ruoyi.flowable.domain.TaskDetail.HistoryComment();
            comment.setTaskId(ht.getId());
            comment.setTaskName(ht.getName());
            comment.setAssignee(ht.getAssignee());
            comment.setAssigneeName(getUserName(ht.getAssignee()));
            comment.setCreateTime(ht.getCreateTime());
            comment.setEndTime(ht.getEndTime());
            
            // 尝试从历史变量中获取评论信息
            try {
                java.util.List<org.flowable.variable.api.history.HistoricVariableInstance> vars = 
                    historyService.createHistoricVariableInstanceQuery()
                        .taskId(ht.getId())
                        .list();
                for (org.flowable.variable.api.history.HistoricVariableInstance var : vars) {
                    if ("_COMMENT".equals(var.getVariableName())) {
                        comment.setComment(String.valueOf(var.getValue()));
                    } else if ("_ACTION".equals(var.getVariableName())) {
                        comment.setAction(String.valueOf(var.getValue()));
                    }
                }
            } catch (Exception e) {
                log.debug("获取历史评论失败", e);
            }
            
            // 如果没有设置action，根据任务名称判断
            if (comment.getAction() == null) {
                String taskName = ht.getName();
                if (taskName != null && taskName.contains("驳回")) {
                    comment.setAction("reject");
                } else {
                    comment.setAction("complete");
                }
            }
            
            comments.add(comment);
        }
        
        return comments;
    }

    private String getUserName(String userId) {
        if (userId == null || userId.isEmpty()) {
            return "";
        }
        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "select user_name from sys_user where user_id = ?", userId);
            if (!users.isEmpty()) {
                return (String) users.get(0).get("user_name");
            }
        } catch (Exception e) {
            log.warn("查询用户名失败, userId={}", userId, e);
        }
        return userId;
    }

    private List<com.ruoyi.flowable.domain.TaskDetail.NextNode> calculateNextNodes(Task task) {
        List<com.ruoyi.flowable.domain.TaskDetail.NextNode> nextNodes = new ArrayList<>();
        
        try {
            org.flowable.engine.repository.ProcessDefinition pd = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            
            if (pd == null) {
                return nextNodes;
            }
            
            InputStream is = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
            if (is == null) {
                return nextNodes;
            }
            
            String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
            
            String currentTaskDefinitionKey = task.getTaskDefinitionKey();
            
            List<String> nextNodeIds = findNextNodeIds(xml, currentTaskDefinitionKey);
            
            for (String nodeId : nextNodeIds) {
                String nodeType = identifyNodeType(xml, nodeId);
                
                if ("sequenceFlow".equals(nodeType)) {
                    continue;
                }
                
                if ("exclusiveGateway".equals(nodeType) || "parallelGateway".equals(nodeType) || "inclusiveGateway".equals(nodeType)) {
                    List<String> gatewayNextNodes = findNextNodeIds(xml, nodeId);
                    for (String gatewayNextNodeId : gatewayNextNodes) {
                        String seqFlowId = findSequenceFlowId(xml, nodeId, gatewayNextNodeId);
                        String condition = extractConditionExpression(xml, seqFlowId);
                        boolean shouldTake = evaluateCondition(condition, variables);
                        
                        if (shouldTake || "parallelGateway".equals(nodeType)) {
                            String nextNodeType = identifyNodeType(xml, gatewayNextNodeId);
                            if ("userTask".equals(nextNodeType)) {
                                com.ruoyi.flowable.domain.TaskDetail.NextNode nextNode = buildNextNode(xml, gatewayNextNodeId, variables);
                                if (nextNode != null) {
                                    nextNodes.add(nextNode);
                                }
                            } else if ("endEvent".equals(nextNodeType)) {
                                com.ruoyi.flowable.domain.TaskDetail.NextNode endNode = new com.ruoyi.flowable.domain.TaskDetail.NextNode();
                                endNode.setNodeId(gatewayNextNodeId);
                                endNode.setNodeName("结束");
                                endNode.setNodeType("endEvent");
                                nextNodes.add(endNode);
                            }
                        }
                    }
                } else if ("userTask".equals(nodeType)) {
                    com.ruoyi.flowable.domain.TaskDetail.NextNode nextNode = buildNextNode(xml, nodeId, variables);
                    if (nextNode != null) {
                        nextNodes.add(nextNode);
                    }
                } else if ("endEvent".equals(nodeType)) {
                    com.ruoyi.flowable.domain.TaskDetail.NextNode endNode = new com.ruoyi.flowable.domain.TaskDetail.NextNode();
                    endNode.setNodeId(nodeId);
                    endNode.setNodeName("结束");
                    endNode.setNodeType("endEvent");
                    nextNodes.add(endNode);
                }
            }
        } catch (Exception e) {
            log.error("计算下一节点失败, taskId={}", task.getId(), e);
        }
        
        return nextNodes;
    }

    private String identifyNodeType(String xml, String nodeId) {
        if (xml.contains("<bpmn:userTask id=\"" + nodeId + "\"")) {
            return "userTask";
        }
        if (xml.contains("<bpmn:exclusiveGateway id=\"" + nodeId + "\"")) {
            return "exclusiveGateway";
        }
        if (xml.contains("<bpmn:parallelGateway id=\"" + nodeId + "\"")) {
            return "parallelGateway";
        }
        if (xml.contains("<bpmn:inclusiveGateway id=\"" + nodeId + "\"")) {
            return "inclusiveGateway";
        }
        if (xml.contains("<bpmn:endEvent id=\"" + nodeId + "\"")) {
            return "endEvent";
        }
        if (xml.contains("<bpmn:sequenceFlow id=\"" + nodeId + "\"")) {
            return "sequenceFlow";
        }
        return "unknown";
    }

    private String findSequenceFlowId(String xml, String sourceRef, String targetRef) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<bpmn:sequenceFlow\\s+[^>]*id=\"([^\"]*)\"[^>]*sourceRef=\"" + sourceRef + "\"[^>]*targetRef=\"" + targetRef + "\"");
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractConditionExpression(String xml, String seqFlowId) {
        if (seqFlowId == null) {
            return null;
        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<bpmn:sequenceFlow\\s+[^>]*id=\"" + seqFlowId + "\"[^>]*>([\\s\\S]*?)</bpmn:sequenceFlow>");
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            String content = matcher.group(1);
            java.util.regex.Pattern conditionPattern = java.util.regex.Pattern.compile(
                "<bpmn:conditionExpression[^>]*>([\\s\\S]*?)</bpmn:conditionExpression>");
            java.util.regex.Matcher conditionMatcher = conditionPattern.matcher(content);
            if (conditionMatcher.find()) {
                return conditionMatcher.group(1).trim();
            }
        }
        return null;
    }

    private boolean evaluateCondition(String condition, Map<String, Object> variables) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        
        String expr = condition.trim();
        // 替换 ${variableName} 为实际值
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(expr);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            Object value = variables.get(varName);
            String replacement;
            if (value == null) {
                replacement = "null";
            } else if (value instanceof String) {
                replacement = "'" + value.toString().replace("'", "\\'") + "'";
            } else {
                replacement = value.toString();
            }
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        expr = sb.toString();
        
        try {
            org.springframework.expression.ExpressionParser parser = new org.springframework.expression.spel.standard.SpelExpressionParser();
            org.springframework.expression.EvaluationContext context = new org.springframework.expression.spel.support.StandardEvaluationContext();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
            org.springframework.expression.Expression exp = parser.parseExpression(expr);
            Boolean result = exp.getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("条件表达式计算失败, expr={}", expr, e);
            return false;
        }
    }

    private com.ruoyi.flowable.domain.TaskDetail.NextNode buildNextNode(String xml, String nodeId, Map<String, Object> variables) {
        Map<String, String> taskInfo = extractUserTaskById(xml, nodeId);
        if (taskInfo == null) {
            return null;
        }
        
        com.ruoyi.flowable.domain.TaskDetail.NextNode nextNode = new com.ruoyi.flowable.domain.TaskDetail.NextNode();
        nextNode.setNodeId(nodeId);
        
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(
            "<bpmn:userTask\\s+[^>]*id=\"" + nodeId + "\"[^>]*name=\"([^\"]*)\"");
        java.util.regex.Matcher nameMatcher = namePattern.matcher(xml);
        if (nameMatcher.find()) {
            nextNode.setNodeName(nameMatcher.group(1));
        } else {
            nextNode.setNodeName("用户任务");
        }
        nextNode.setNodeType("userTask");
        
        String config = taskInfo.get("config");
        nextNode.setConfig(config);
        
        List<String> assigneeNames = new ArrayList<>();
        if (config != null && !config.isEmpty()) {
            try {
                String decodedConfig = config.replace("&#34;", "\"").replace("&quot;", "\"");
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> configMap = mapper.readValue(decodedConfig, Map.class);
                
                String assigneeType = (String) configMap.get("assigneeType");
                if ("user".equals(assigneeType)) {
                    List<Object> assigneeUsers = (List<Object>) configMap.get("assigneeUsers");
                    if (assigneeUsers != null) {
                        for (Object userId : assigneeUsers) {
                            assigneeNames.add(getUserName(userId.toString()));
                        }
                    }
                } else if ("dept".equals(assigneeType)) {
                    List<Object> assigneeDepts = (List<Object>) configMap.get("assigneeDepts");
                    if (assigneeDepts != null && !assigneeDepts.isEmpty()) {
                        Set<String> userIds = new HashSet<>();
                        StringBuilder sql = new StringBuilder(
                            "select distinct u.user_id, u.user_name from sys_user u " +
                            "inner join sys_dept d on u.dept_id = d.dept_id " +
                            "where u.del_flag = '0' and (");
                        List<Long> deptIds = new ArrayList<>();
                        for (int i = 0; i < assigneeDepts.size(); i++) {
                            if (i > 0) sql.append(" or ");
                            sql.append("d.ancestors like concat(?, '%') or u.dept_id = ?");
                            Long did = Long.valueOf(assigneeDepts.get(i).toString());
                            deptIds.add(did);
                            deptIds.add(did);
                        }
                        sql.append(")");
                        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql.toString(), deptIds.toArray());
                        for (Map<String, Object> user : users) {
                            assigneeNames.add((String) user.get("user_name"));
                        }
                    }
                } else if ("role".equals(assigneeType)) {
                    List<Object> assigneeRoles = (List<Object>) configMap.get("assigneeRoles");
                    if (assigneeRoles != null && !assigneeRoles.isEmpty()) {
                        StringBuilder sql = new StringBuilder(
                            "select distinct u.user_name from sys_user u " +
                            "inner join sys_user_role ur on u.user_id = ur.user_id " +
                            "where u.del_flag = '0' and ur.role_id in (");
                        List<Long> roleIds = new ArrayList<>();
                        for (int i = 0; i < assigneeRoles.size(); i++) {
                            if (i > 0) sql.append(",");
                            sql.append("?");
                            roleIds.add(Long.valueOf(assigneeRoles.get(i).toString()));
                        }
                        sql.append(")");
                        List<String> names = jdbcTemplate.queryForList(sql.toString(), String.class, roleIds.toArray());
                        assigneeNames.addAll(names);
                    }
                } else if ("formComponent".equals(assigneeType)) {
                    String formField = (String) configMap.get("assigneeFormComponent");
                    if (formField != null && variables.containsKey(formField)) {
                        Object fieldValue = variables.get(formField);
                        if (fieldValue instanceof String) {
                            String[] ids = ((String) fieldValue).split(",");
                            for (String id : ids) {
                                if (id != null && !id.trim().isEmpty()) {
                                    assigneeNames.add(getUserName(id.trim()));
                                }
                            }
                        } else if (fieldValue instanceof List) {
                            for (Object item : (List<?>) fieldValue) {
                                if (item != null) {
                                    assigneeNames.add(getUserName(item.toString()));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析用户任务配置失败, nodeId={}", nodeId, e);
            }
        }
        
        nextNode.setAssigneeNames(assigneeNames);
        
        return nextNode;
    }

    private String getCurrentNodeConfig(Task task) {
        try {
            org.flowable.engine.repository.ProcessDefinition pd = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            
            if (pd == null) {
                return null;
            }
            
            InputStream is = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
            if (is == null) {
                return null;
            }
            
            String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> taskInfo = extractUserTaskById(xml, task.getTaskDefinitionKey());
            
            if (taskInfo != null) {
                String config = taskInfo.get("config");
                if (config != null) {
                    return config.replace("&#34;", "\"").replace("&quot;", "\"");
                }
            }
        } catch (Exception e) {
            log.warn("获取当前节点配置失败, taskId={}", task.getId(), e);
        }
        return null;
    }
}
