const BASE_URL = 'http://localhost:8080/api/app/v1';

const STORAGE_KEYS = {
  ACCESS_TOKEN: 'taskList.accessToken',
  USER_PROFILE: 'taskList.userProfile',
  PENDING_REDIRECT: 'taskList.pendingRedirect',
};

const TAB_PAGES = ['/pages/tasks/index', '/pages/stats/index', '/pages/me/index'];

const TASK_STATUS_OPTIONS = [
  { label: '全部', value: '' },
  { label: '待办', value: 'TODO' },
  { label: '已完成', value: 'DONE' },
  { label: '已过期', value: 'EXPIRED' },
];

const PRIORITY_OPTIONS = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' },
];

const REPEAT_OPTIONS = [
  { label: '不重复', value: 'NONE' },
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' },
];

const OVERVIEW_RANGE_OPTIONS = [
  { label: '今日', value: 'today' },
  { label: '近 7 天', value: 'week' },
  { label: '近 30 天', value: 'month' },
];

const TREND_RANGE_OPTIONS = [
  { label: '近 7 天', value: 'week' },
  { label: '近 30 天', value: 'month' },
];

const CATEGORY_COLOR_OPTIONS = ['#07c160', '#1677ff', '#faad14', '#ff7875', '#722ed1', '#13c2c2'];

module.exports = {
  BASE_URL,
  STORAGE_KEYS,
  TAB_PAGES,
  TASK_STATUS_OPTIONS,
  PRIORITY_OPTIONS,
  REPEAT_OPTIONS,
  OVERVIEW_RANGE_OPTIONS,
  TREND_RANGE_OPTIONS,
  CATEGORY_COLOR_OPTIONS,
};
