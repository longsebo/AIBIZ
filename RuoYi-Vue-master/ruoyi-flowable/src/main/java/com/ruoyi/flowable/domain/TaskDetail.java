package com.ruoyi.flowable.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class TaskDetail {

    private String taskId;
    private String taskName;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private String processInstanceId;
    private String executionId;
    private String businessKey;
    private String assignee;
    private Date createTime;
    private String startUserId;

    private Map<String, Object> processVariables;

    private List<HistoryComment> historyComments;

    private List<NextNode> nextNodes;

    private String currentNodeConfig;

    @Data
    public static class HistoryComment {
        private String taskId;
        private String taskName;
        private String assignee;
        private String assigneeName;
        private Date createTime;
        private Date endTime;
        private String comment;
        private String action;
    }

    @Data
    public static class NextNode {
        private String nodeId;
        private String nodeName;
        private String nodeType;
        private String config;
        private List<String> assigneeNames;
    }
}