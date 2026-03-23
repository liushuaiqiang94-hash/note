const statsService = require('../../services/stats');
const {
  OVERVIEW_RANGE_OPTIONS,
  TREND_RANGE_OPTIONS,
} = require('../../constants/index');
const { ensureAuthenticated } = require('../../utils/auth');
const { formatPercent, formatShortDate } = require('../../utils/date');

Page({
  data: {
    loading: false,
    overviewRangeOptions: OVERVIEW_RANGE_OPTIONS,
    trendRangeOptions: TREND_RANGE_OPTIONS,
    currentOverviewRange: 'today',
    currentTrendRange: 'week',
    overview: {
      completedCount: 0,
      totalCount: 0,
      completionRateText: '0%',
      consecutiveCompletedDays: 0,
    },
    trend: [],
  },

  onShow() {
    if (!ensureAuthenticated('/pages/stats/index')) {
      return;
    }
    this.loadStats();
  },

  async loadStats() {
    this.setData({ loading: true });
    try {
      const [overview, trend] = await Promise.all([
        statsService.getOverview(this.data.currentOverviewRange),
        statsService.getTrend(this.data.currentTrendRange),
      ]);

      this.setData({
        overview: {
          completedCount: overview.completedCount || 0,
          totalCount: overview.totalCount || 0,
          completionRateText: formatPercent(overview.completionRate || 0),
          consecutiveCompletedDays: overview.consecutiveCompletedDays || 0,
        },
        trend: (trend || []).map((item) => ({
          ...item,
          displayDate: formatShortDate(item.date),
        })),
      });
    } catch (error) {
      wx.showToast({
        title: error.message || '加载统计失败',
        icon: 'none',
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  changeOverviewRange(event) {
    const value = event.currentTarget.dataset.value;
    if (value === this.data.currentOverviewRange) {
      return;
    }
    this.setData({ currentOverviewRange: value });
    this.loadStats();
  },

  changeTrendRange(event) {
    const value = event.currentTarget.dataset.value;
    if (value === this.data.currentTrendRange) {
      return;
    }
    this.setData({ currentTrendRange: value });
    this.loadStats();
  },
});
