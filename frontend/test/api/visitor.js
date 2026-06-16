import { request } from '@/utils/request.js';

/**
 * 💡 终极辅助函数：手动把对象转成 URL 上的查询参数
 * 例如：{ pageNum: 1, startDate: '2026-06-16' } => "?pageNum=1&startDate=2026-06-16"
 * 这样可以完美绕过 request.js 对 params/data 的兼容性 bug
 */
function buildQueryString(params) {
  if (!params) return '';
  const query = Object.keys(params)
    // 过滤掉 undefined、null 和空字符串，只传有用的参数
    .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&');
  return query ? `?${query}` : '';
}

/**
 * 1. 申请人视角：我的申请列表
 */
export function getVisitorPage(queryParams) {
  return request({
    url: '/api/v1/visitor-applications' + buildQueryString(queryParams),
    method: 'GET'
  });
}

/**
 * 2. 员工/被访人视角：我的被访列表
 */
export function getHostVisitorPage(queryParams) {
  return request({
    url: '/api/v1/visitor-applications/audit' + buildQueryString(queryParams),
    method: 'GET'
  });
}

/**
 * 3. 管理员视角：全量申请记录
 */
export function getAdminVisitorPage(queryParams) {
  return request({
    url: '/api/v1/visitor-applications/admin-approval' + buildQueryString(queryParams),
    method: 'GET'
  });
}

/**
 * 4. 门卫视角：全量记录（默认今日）
 */
export function getGuardVisitorPage(queryParams) {
  return request({
    url: '/api/v1/visitor-applications/guard-page' + buildQueryString(queryParams),
    method: 'GET'
  });
}