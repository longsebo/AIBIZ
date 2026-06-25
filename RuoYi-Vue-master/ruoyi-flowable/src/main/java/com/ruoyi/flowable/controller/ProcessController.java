package com.ruoyi.flowable.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flowable.domain.ProcessDefinition;
import com.ruoyi.flowable.domain.ProcessInstance;
import com.ruoyi.flowable.domain.TaskInfo;
import com.ruoyi.flowable.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程管理 Controller
 *
 * @author aibiz
 */
@RestController
@RequestMapping("/flowable/process")
@RequiredArgsConstructor
public class ProcessController extends BaseController {

    private final ProcessService processService;

    /**
     * 查询流程定义列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:list')")
    @GetMapping("/definitionList")
    public AjaxResult definitionList() {
        List<ProcessDefinition> list = processService.listProcessDefinition();
        return success(list);
    }

    /**
     * 部署流程
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:deploy')")
    @PostMapping("/deploy")
    public AjaxResult deploy(@RequestBody Map<String, String> params) {
        processService.deployProcessDefinition(
                params.get("name"),
                params.get("category"),
                params.get("bpmnXml")
        );
        return success();
    }

    /**
     * 删除流程定义
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:remove')")
    @DeleteMapping("/definition/{deploymentId}")
    public AjaxResult deleteDefinition(@PathVariable String deploymentId) {
        processService.deleteProcessDefinition(deploymentId);
        return success();
    }

    /**
     * 启动流程
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:start')")
    @PostMapping("/start")
    public AjaxResult start(@RequestBody Map<String, Object> params) {
        String key = (String) params.get("processDefinitionKey");
        String businessKey = (String) params.get("businessKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) params.get("variables");
        ProcessInstance pi = processService.startProcess(key, businessKey, variables);
        return success(pi);
    }

    /**
     * 获取流程实例详情
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:query')")
    @GetMapping("/instance/{id}")
    public AjaxResult getInstance(@PathVariable String id) {
        ProcessInstance pi = processService.getProcessInstance(id);
        return success(pi);
    }

    /**
     * 我发起的流程
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:query')")
    @GetMapping("/myStarted")
    public AjaxResult myStarted() {
        List<ProcessInstance> list = processService.listMyStartedProcess(getUsername());
        return success(list);
    }

    /**
     * 我的待办
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:list')")
    @GetMapping("/myTodo")
    public AjaxResult myTodo() {
        List<TaskInfo> list = processService.listMyTodoTask(getUsername());
        return success(list);
    }

    /**
     * 我的已办
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:list')")
    @GetMapping("/myDone")
    public AjaxResult myDone() {
        List<TaskInfo> list = processService.listMyDoneTask(getUsername());
        return success(list);
    }

    /**
     * 完成任务
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @PostMapping("/complete/{taskId}")
    public AjaxResult complete(@PathVariable String taskId, @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> variables = params == null ? null : (Map<String, Object>) params.get("variables");
        processService.completeTask(taskId, variables);
        return success();
    }

    /**
     * 驳回任务
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:reject')")
    @PostMapping("/reject/{taskId}")
    public AjaxResult reject(@PathVariable String taskId, @RequestBody Map<String, String> params) {
        processService.rejectTask(taskId, params.get("reason"));
        return success();
    }

    /**
     * 委派任务
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:delegate')")
    @PostMapping("/delegate/{taskId}")
    public AjaxResult delegate(@PathVariable String taskId, @RequestBody Map<String, String> params) {
        processService.delegateTask(taskId, params.get("userId"));
        return success();
    }

    /**
     * 认领任务
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:claim')")
    @PostMapping("/claim/{taskId}")
    public AjaxResult claim(@PathVariable String taskId) {
        processService.claimTask(taskId, getUsername());
        return success();
    }

    /**
     * 获取流程图
     */
    @PreAuthorize("@ss.hasPermi('flowable:process:query')")
    @GetMapping("/diagram/{processDefinitionId}")
    public AjaxResult diagram(@PathVariable String processDefinitionId) {
        String xml = processService.getProcessDiagram(processDefinitionId);
        Map<String, Object> data = new HashMap<>();
        data.put("xml", xml);
        return success(data);
    }
}
