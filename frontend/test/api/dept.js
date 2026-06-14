import { request } from '@/utils/request.js';

/**
 * 获取部门列表 (通常返回树形结构数据)
 * 后端接口：GET /api/v1/depts
 */
export function getDeptList(queryParams) {
  return request({
    url: '/api/v1/depts',
    method: 'GET',
    data: queryParams
  });
}

/**
 * 获取部门下拉选项
 * 后端接口：GET /api/v1/depts/options
 */
export function getDeptOptions() {
  return request({
    url: '/api/v1/depts/options',
    method: 'GET'
  });
}

/**
 * 新增部门
 * 后端接口：POST /api/v1/depts
 */
export function addDept(data) {
  return request({
    url: '/api/v1/depts',
    method: 'POST',
    data
  });
}

/**
 * 修改部门
 * 后端接口：PUT /api/v1/depts/{deptId}
 */
export function updateDept(deptId, data) {
  return request({
    url: `/api/v1/depts/${deptId}`,
    method: 'PUT',
    data
  });
}

/**
 * 删除部门
 * 后端接口：DELETE /api/v1/depts/{ids}
 */
export function deleteDept(ids) {
  return request({
    url: `/api/v1/depts/${ids}`,
    method: 'DELETE'
  });
}