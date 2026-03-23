function getStorage(key, fallback = '') {
  const value = wx.getStorageSync(key);
  return value === '' || value === undefined ? fallback : value;
}

function setStorage(key, value) {
  wx.setStorageSync(key, value);
}

function removeStorage(key) {
  wx.removeStorageSync(key);
}

module.exports = {
  getStorage,
  setStorage,
  removeStorage,
};
