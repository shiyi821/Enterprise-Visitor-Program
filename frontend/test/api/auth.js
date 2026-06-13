// api/auth.js
import { request } from '../utils/request.js';

/**
 * 用户登录接口
 * @param {Object} data { username, password }
 */
export const loginApi = (data) => {
  return request({
    url: '/api/v1/auth/login',
    method: 'POST',
    data
  });
};

/**
 * 用户注册接口
 * @param {Object} data { username, password, mobile }
 */
export const registerApi = (data) => {
  return request({
    url: '/api/v1/auth/register',
    method: 'POST',
    data
  });
};