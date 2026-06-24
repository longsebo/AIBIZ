import request from '@/utils/request'

// 获取所有指令映射
export function listAll() {
  return request({
    url: '/system/aiCommand/listAll',
    method: 'get'
  })
}

// 根据指令查询
export function getByCommand(command) {
  return request({
    url: '/system/aiCommand/' + command,
    method: 'get'
  })
}

// 查询指令列表
export function list(query) {
  return request({
    url: '/system/aiCommand/list',
    method: 'get',
    params: query
  })
}

// 新增指令
export function add(data) {
  return request({
    url: '/system/aiCommand',
    method: 'post',
    data: data
  })
}

// 修改指令
export function edit(data) {
  return request({
    url: '/system/aiCommand',
    method: 'put',
    data: data
  })
}

// 删除指令
export function del(id) {
  return request({
    url: '/system/aiCommand/' + id,
    method: 'delete'
  })
}
