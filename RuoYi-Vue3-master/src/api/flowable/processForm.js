import request from '@/utils/request'

export function bindProcessForm(data) {
  return request({
    url: '/flowable/processForm',
    method: 'post',
    data: data
  })
}

export function updateProcessForm(data) {
  return request({
    url: '/flowable/processForm',
    method: 'put',
    data: data
  })
}

export function getProcessFormByKey(processKey) {
  return request({
    url: '/flowable/processForm/process/' + processKey,
    method: 'get'
  })
}