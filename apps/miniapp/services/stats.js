const request = require('../utils/request');

function getOverview(range) {
  return request.get('/stats/overview', { range });
}

function getTrend(range) {
  return request.get('/stats/trend', { range });
}

module.exports = {
  getOverview,
  getTrend,
};
