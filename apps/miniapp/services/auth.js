const request = require('../utils/request');

function loginWithWechat(payload) {
  return request.post('/auth/wechat/login', payload, { auth: false });
}

module.exports = {
  loginWithWechat,
};
