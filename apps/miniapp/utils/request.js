const { BASE_URL } = require('../constants/index');
const { getToken } = require('./auth');

let unauthorizedHandler = null;

function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

function showErrorToast(message) {
  wx.showToast({
    title: message || '请求失败，请稍后重试',
    icon: 'none',
    duration: 2000,
  });
}

function normalizeError(statusCode, payload, fallbackMessage) {
  return {
    statusCode: statusCode || 0,
    code: payload && typeof payload.code === 'number' ? payload.code : -1,
    message: (payload && payload.message) || fallbackMessage || '请求失败，请稍后重试',
    payload: payload || null,
  };
}

function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    auth = true,
    header = {},
    showError = true,
  } = options;

  if (auth && !getToken()) {
    const error = normalizeError(401, { code: 40100, message: 'Unauthorized' }, 'Unauthorized');
    if (typeof unauthorizedHandler === 'function') {
      unauthorizedHandler(error);
    }
    return Promise.reject(error);
  }

  return new Promise((resolve, reject) => {
    const requestHeader = { ...header };
    if (auth) {
      requestHeader.Authorization = `Bearer ${getToken()}`;
    }

    wx.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: requestHeader,
      success: (response) => {
        const payload = response.data || {};
        if (response.statusCode >= 200 && response.statusCode < 300 && payload.code === 0) {
          resolve(payload.data);
          return;
        }

        const error = normalizeError(response.statusCode, payload, '服务开小差了，请稍后重试');
        if (error.code === 40100 && typeof unauthorizedHandler === 'function') {
          unauthorizedHandler(error);
        } else if (showError) {
          showErrorToast(error.message);
        }
        reject(error);
      },
      fail: () => {
        const error = normalizeError(0, null, '网络请求失败，请检查服务是否启动');
        if (showError) {
          showErrorToast(error.message);
        }
        reject(error);
      },
    });
  });
}

function get(url, data, options = {}) {
  return request({ url, method: 'GET', data, ...options });
}

function post(url, data, options = {}) {
  return request({ url, method: 'POST', data, ...options });
}

function put(url, data, options = {}) {
  return request({ url, method: 'PUT', data, ...options });
}

function patch(url, data, options = {}) {
  return request({ url, method: 'PATCH', data, ...options });
}

function del(url, data, options = {}) {
  return request({ url, method: 'DELETE', data, ...options });
}

module.exports = {
  request,
  get,
  post,
  put,
  patch,
  del,
  setUnauthorizedHandler,
};
