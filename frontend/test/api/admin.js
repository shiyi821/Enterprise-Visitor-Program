import {request} from '@/utils/request'

// 获取管理台看板动态统计数据
export function getDashboardStats() {
  return request({
    url: '/api/v1/visitor-applications/dashboard/stats',
    method: 'GET'
  })
}