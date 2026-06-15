import {request} from '@/utils/request.js';

/**
 * 获取部门下拉选项 (动态获取真实的部门数据)
 * 后端接口：GET /api/v1/depts/options
 */
export function getDeptOptions() {
  return request({
    url: '/api/v1/depts/options',
    method: 'GET'
  });
}

/**
 * 分页查询员工列表
 * 后端接口：GET /api/v1/users
 */
export function getEmployeePage(queryParams) {
  return request({
    url: '/api/v1/users',
    method: 'GET',
    data: queryParams
  });
}

/**
 * 获取当前登录用户的详细信息
 */
export function getCurrentUserInfo() {
  return request({
    url: '/api/v1/users/me',
    method: 'GET'
  });
}

/**
 * 新增员工
 */
export function addEmployee(payload) {
  return request({
    url: '/api/v1/users',
    method: 'POST',
    data: payload
  });
}

/**
 * 修改员工信息
 */
export function updateEmployee(userId, payload) {
  return request({
    url: `/api/v1/users/${userId}`,
    method: 'PUT',
    data: payload
  });
}

/**
 * 删除员工
 */
export function deleteEmployees(ids) {
  return request({
    url: `/api/v1/users/${ids}`,
    method: 'DELETE'
  });
}

/**
 * 重置员工密码为 123456
 * 后端接口：PUT /api/v1/users/{userId}/password/reset
 * ⚠️ 注意：后端使用的是 @RequestParam 接收，所以必须将 password 拼接到 URL 后面
 */
export function resetEmployeePassword(userId, password) {
  return request({
    url: `/api/v1/users/${userId}/password/reset?password=${password}`,
    method: 'PUT'
  });
}
/**
 * 获取角色下拉选项
 * 后端接口：GET /api/v1/roles/options
 */
export function getRoleOptions() {
  return request({
    url: '/api/v1/roles/options',
    method: 'GET'
  });
}

/**
 * 修改员工状态
 * 后端接口：PATCH /api/v1/users/{userId}/status
 */
export function updateEmployeeStatus(userId, status) {
  return request({
    url: `/api/v1/users/${userId}/status?status=${status}`,
    method: 'PATCH'
  });
}