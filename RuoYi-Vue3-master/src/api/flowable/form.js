import request from '@/utils/request'

// 保存表单设计（新增）
export function saveForm(data) {
  return request({
    url: '/flowable/form',
    method: 'post',
    data: data
  })
}

// 获取表单列表
export function listForm(query) {
  return request({
    url: '/flowable/form/list',
    method: 'get',
    params: query
  })
}

// 获取表单详情
export function getForm(formId) {
  return request({
    url: '/flowable/form/' + formId,
    method: 'get'
  })
}

// 删除表单
export function deleteForm(formIds) {
  return request({
    url: '/flowable/form/' + formIds,
    method: 'delete'
  })
}

// 更新表单
export function updateForm(data) {
  return request({
    url: '/flowable/form',
    method: 'put',
    data: data
  })
}