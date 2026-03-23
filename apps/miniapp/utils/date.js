function normalizeDateTimeInput(value) {
  return value ? value.replace(' ', 'T') : '';
}

function pad(num) {
  return String(num).padStart(2, '0');
}

function toDate(value) {
  if (!value) {
    return null;
  }
  const date = new Date(normalizeDateTimeInput(value));
  if (Number.isNaN(date.getTime())) {
    return null;
  }
  return date;
}

function formatDateTime(value) {
  const date = toDate(value);
  if (!date) {
    return '未设置';
  }
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function formatShortDate(value) {
  const date = toDate(value);
  if (!date) {
    return '--';
  }
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function getTodayDateString() {
  const date = new Date();
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function splitDateTime(value) {
  const date = toDate(value);
  if (!date) {
    return {
      date: '',
      time: '',
    };
  }
  return {
    date: `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`,
    time: `${pad(date.getHours())}:${pad(date.getMinutes())}`,
  };
}

function combineDateTime(date, time) {
  if (!date && !time) {
    return null;
  }
  if (!date || !time) {
    return '__INVALID__';
  }
  return `${date}T${time}:00`;
}

function formatPercent(value) {
  const percent = Number(value || 0) * 100;
  return `${Math.round(percent)}%`;
}

module.exports = {
  formatDateTime,
  formatShortDate,
  getTodayDateString,
  splitDateTime,
  combineDateTime,
  formatPercent,
};
