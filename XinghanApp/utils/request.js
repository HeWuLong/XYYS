// utils/request.js
// 真机调试请改成你电脑局域网IP，例如：http://192.168.1.10:8080
const BASE_URL = 'http://localhost:8080';

export const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    const header = options.header || {};

    if (token) {
      header['Authorization'] = `Bearer ${token}`;
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        const data = res.data;
        if (res.statusCode === 200 && data?.code === 200) {
          resolve(data.data);
          return;
        }

        if (data?.code === 401) {
          uni.hideLoading();
          uni.removeStorageSync('token');
          uni.removeStorageSync('userInfo');
          uni.showToast({ title: data.msg || '登录已过期，请重新登录', icon: 'none' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' });
          }, 1200);
          reject(data.msg || '未授权');
          return;
        }

        uni.hideLoading();
        uni.showToast({ title: data?.msg || '请求失败', icon: 'none' });
        reject(data?.msg || `HTTP ${res.statusCode}`);
      },
      fail: (err) => {
        uni.hideLoading();
        uni.showToast({ title: '网络异常，请稍后再试', icon: 'none' });
        reject(err);
      }
    });
  });
};

export default request;
