const request = require('../utils/request');

function getMe() {
  return request.get('/me');
}

module.exports = {
  getMe,
};
