// utils/request.js

// [核心配置] 后端服务的基础本地绝对路径 (当前后端端口为 8000)
// 注意：如果只在电脑浏览器(H5)调试，写 localhost 没问题。
// 如果用手机真机或微信小程序模拟器调试，必须改成你电脑的局域网 IP (例如 http://192.168.1.100:8000)
const BASE_URL = 'http://127.0.0.1:8000'; //127.0.0.1

export const request = (options) => {
  return new Promise((resolve, reject) => {
    // 自动从本地缓存中读取用户登录成功后存下的 Token
    const token = uni.getStorageSync('token');
    
    // 组装请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    };
    
    // 如果 Token 存在，按照 youlai-boot (Spring Security) 的标准安全规范，在请求头带上 Bearer 令牌
    if (token) {
      header['Authorization'] = 'Bearer ' + token;
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        // HTTP 状态码 200 表示服务器响应成功
        if (res.statusCode === 200) {
          const data = res.data;

          // ========== 令牌过期全局拦截 ==========
          if (data.code === 'A0230') {
            // 先清除所有登录缓存
            uni.removeStorageSync('token');
            uni.removeStorageSync('userInfo');
            uni.removeStorageSync('userRole');
            uni.removeStorageSync('is_new_user_flag');

            // 全局标记，防止多接口并发时重复弹窗跳转
            const app = getApp();
            app.globalData = app.globalData || {};
            if (!app.globalData.isTokenExpired) {
              app.globalData.isTokenExpired = true;

              uni.showToast({
                title: '登录已过期，请重新登录',
                icon: 'none',
                duration: 1500
              });

              setTimeout(() => {
                uni.reLaunch({
                  url: '/pages/login/login',
                  complete: () => {
                    app.globalData.isTokenExpired = false;
                  }
                });
              }, 1500);
            }

            reject(data);
            return;
          }

          resolve(data); // 将后端返回的自定义全局结果（如 code, data, msg）返回给上一层
        } else {
          uni.showToast({
            title: res.data?.msg || '服务器响应异常',
            icon: 'none'
          });
          reject(res.data);
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络连接失败，请检查后端服务是否正常启动',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};