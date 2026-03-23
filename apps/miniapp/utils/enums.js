const PRIORITY_META = {
  LOW: { text: '低优先级', className: 'chip--low' },
  MEDIUM: { text: '中优先级', className: 'chip--medium' },
  HIGH: { text: '高优先级', className: 'chip--high' },
};

const STATUS_META = {
  TODO: { text: '待办', className: 'chip--todo' },
  DONE: { text: '已完成', className: 'chip--done' },
  EXPIRED: { text: '已过期', className: 'chip--expired' },
};

function getPriorityMeta(priority) {
  return PRIORITY_META[priority] || PRIORITY_META.MEDIUM;
}

function getStatusMeta(status) {
  return STATUS_META[status] || STATUS_META.TODO;
}

module.exports = {
  getPriorityMeta,
  getStatusMeta,
};
