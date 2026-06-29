import request from '@/utils/request'

// 查询流程定义列表
export function listProcessDefinition() {
  return request({
    url: '/flowable/process/definitionList',
    method: 'get'
  })
}

// 部署流程
export function deployProcess(data) {
  return request({
    url: '/flowable/process/deploy',
    method: 'post',
    data: data
  })
}

// 删除流程定义
export function deleteProcessDefinition(deploymentId) {
  return request({
    url: '/flowable/process/definition/' + deploymentId,
    method: 'delete'
  })
}

// 启动流程
export function startProcess(data) {
  return request({
    url: '/flowable/process/start',
    method: 'post',
    data: data
  })
}

// 获取流程实例详情
export function getProcessInstance(id) {
  return request({
    url: '/flowable/process/instance/' + id,
    method: 'get'
  })
}

// 我发起的流程
export function myStarted() {
  return request({
    url: '/flowable/process/myStarted',
    method: 'get'
  })
}

// 我的待办
export function myTodo() {
  return request({
    url: '/flowable/process/myTodo',
    method: 'get'
  })
}

// 我的已办
export function myDone() {
  return request({
    url: '/flowable/process/myDone',
    method: 'get'
  })
}

// 完成任务
export function completeTask(taskId, data) {
  return request({
    url: '/flowable/process/complete/' + taskId,
    method: 'post',
    data: data || {}
  })
}

// 驳回任务
export function rejectTask(taskId, reason) {
  return request({
    url: '/flowable/process/reject/' + taskId,
    method: 'post',
    data: { reason: reason }
  })
}

// 委派任务
export function delegateTask(taskId, userId) {
  return request({
    url: '/flowable/process/delegate/' + taskId,
    method: 'post',
    data: { userId: userId }
  })
}

// 认领任务
export function claimTask(taskId) {
  return request({
    url: '/flowable/process/claim/' + taskId,
    method: 'post'
  })
}

// 获取流程图
export function getProcessDiagram(processDefinitionId) {
  return request({
    url: '/flowable/process/diagram/' + processDefinitionId,
    method: 'get'
  })
}

// 更新并重新部署流程
export function updateProcess(data) {
  return request({
    url: '/flowable/process/update',
    method: 'post',
    data: data
  })
}

// 根据流程Key获取流程XML
export function getProcessXmlByKey(processDefinitionKey) {
  return request({
    url: '/flowable/process/xml/' + processDefinitionKey,
    method: 'get'
  })
}

// 获取我的抄送列表
export function listCc() {
  return request({
    url: '/flowable/cc/list',
    method: 'get'
  })
}

// 标记已读
export function readCc(id) {
  return request({
    url: '/flowable/cc/read',
    method: 'post',
    params: { id: id }
  })
}
