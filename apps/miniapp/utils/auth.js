const { STORAGE_KEYS, TAB_PAGES } = require('../constants/index');
const { getStorage, setStorage, removeStorage } = require('./storage');

function getToken() {
  return getStorage(STORAGE_KEYS.ACCESS_TOKEN, '');
}

function setToken(token) {
  setStorage(STORAGE_KEYS.ACCESS_TOKEN, token);
}

function getUserProfile() {
  return getStorage(STORAGE_KEYS.USER_PROFILE, null);
}

function setUserProfile(profile) {
  setStorage(STORAGE_KEYS.USER_PROFILE, profile);
}

function clearSession() {
  removeStorage(STORAGE_KEYS.ACCESS_TOKEN);
  removeStorage(STORAGE_KEYS.USER_PROFILE);
  removeStorage(STORAGE_KEYS.PENDING_REDIRECT);
}

function rememberRedirect(path) {
  setStorage(STORAGE_KEYS.PENDING_REDIRECT, path || '/pages/tasks/index');
}

function consumeRedirect() {
  const path = getStorage(STORAGE_KEYS.PENDING_REDIRECT, '/pages/tasks/index');
  removeStorage(STORAGE_KEYS.PENDING_REDIRECT);
  return path;
}

function isTabPage(path) {
  return TAB_PAGES.includes(path);
}

function redirectToLogin(path) {
  rememberRedirect(path);
  wx.reLaunch({
    url: '/pages/login/index',
  });
}

function ensureAuthenticated(path) {
  if (getToken()) {
    return true;
  }
  redirectToLogin(path);
  return false;
}

function redirectAfterLogin() {
  const path = consumeRedirect();
  if (isTabPage(path)) {
    wx.switchTab({ url: path });
    return;
  }
  wx.redirectTo({ url: path });
}

module.exports = {
  getToken,
  setToken,
  getUserProfile,
  setUserProfile,
  clearSession,
  redirectToLogin,
  ensureAuthenticated,
  redirectAfterLogin,
};
