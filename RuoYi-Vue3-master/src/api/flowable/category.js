import request from '@/utils/request'

// 查询流程分类列表
export function listCategory(query) {
  return request({
    url: '/flowable/category/list',
    method: 'get',
    params: query
  })
}

// 查询流程分类详细
export function getCategory(categoryId) {
  return request({
    url: '/flowable/category/' + categoryId,
    method: 'get'
  })
}

// 查询所有流程分类（用于下拉选择）
export function listCategoryAll() {
  return request({
    url: '/flowable/category/listAll',
    method: 'get'
  })
}

// 新增流程分类
export function addCategory(data) {
  return request({
    url: '/flowable/category',
    method: 'post',
    data: data
  })
}

// 修改流程分类
export function updateCategory(data) {
  return request({
    url: '/flowable/category',
    method: 'put',
    data: data
  })
}

// 删除流程分类
export function delCategory(categoryId) {
  return request({
    url: '/flowable/category/' + categoryId,
    method: 'delete'
  })
}

// 导出流程分类
export function exportCategory(query) {
  return request({
    url: '/flowable/category/export',
    method: 'post',
    params: query
  })
}