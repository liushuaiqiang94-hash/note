const taskService = require('../../services/tasks');
const { ensureAuthenticated } = require('../../utils/auth');
const { formatDateTime } = require('../../utils/date');
const { getPriorityMeta, getStatusMeta } = require('../../utils/enums');

Page({
  data: {
    loading: false,
    loadingMore: false,
    tasks: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
  },

  onShow() {
    if (!ensureAuthenticated('/pages/recycle-bin/index')) {
      return;
    }
    this.loadTasks(true);
  },

  onPullDownRefresh() {
    this.loadTasks(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (!this.data.loading && !this.data.loadingMore && this.data.hasMore) {
      this.loadTasks(false);
    }
  },

  decorateTask(task) {
    const priorityMeta = getPriorityMeta(task.priority);
    const statusMeta = getStatusMeta(task.status);

    return {
      ...task,
      dueText: formatDateTime(task.dueAt),
      deletedText: formatDateTime(task.deletedAt),
      priorityText: priorityMeta.text,
      priorityClass: priorityMeta.className,
      statusText: statusMeta.text,
      statusClass: statusMeta.className,
    };
  },

  async loadTasks(reset) {
    const nextPage = reset ? 1 : this.data.pageNum;
    this.setData(reset ? { loading: true } : { loadingMore: true });

    try {
      const result = await taskService.getRecycleBin({
        pageNum: nextPage,
        pageSize: this.data.pageSize,
      });

      const list = (result.list || []).map((item) => this.decorateTask(item));
      const tasks = reset ? list : this.data.tasks.concat(list);
      const total = Number(result.total || 0);

      this.setData({
        tasks,
        pageNum: nextPage + 1,
        hasMore: tasks.length < total,
      });
    } catch (error) {
      wx.showToast({
        title: error.message || '加载回收站失败',
        icon: 'none',
      });
    } finally {
      this.setData({
        loading: false,
        loadingMore: false,
      });
    }
  },

  async restoreTask(event) {
    const taskId = event.currentTarget.dataset.id;
    try {
      await taskService.restoreTask(taskId);
      wx.showToast({
        title: '任务已恢复',
        icon: 'success',
      });
      await this.loadTasks(true);
    } catch (error) {
      wx.showToast({
        title: error.message || '恢复失败',
        icon: 'none',
      });
    }
  },
});
