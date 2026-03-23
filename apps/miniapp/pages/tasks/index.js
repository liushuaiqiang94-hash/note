const categoryService = require('../../services/categories');
const taskService = require('../../services/tasks');
const { TASK_STATUS_OPTIONS } = require('../../constants/index');
const { ensureAuthenticated, getUserProfile } = require('../../utils/auth');
const { formatDateTime, formatPercent, getTodayDateString } = require('../../utils/date');
const { getPriorityMeta, getStatusMeta } = require('../../utils/enums');

Page({
  data: {
    loading: false,
    loadingMore: false,
    tasks: [],
    categories: [],
    categoryPickerOptions: ['全部分类'],
    selectedCategoryIndex: 0,
    selectedCategoryId: '',
    statusOptions: TASK_STATUS_OPTIONS,
    selectedStatus: '',
    keyword: '',
    searchFocused: false,
    todayOnly: false,
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    todaySummary: {
      total: 0,
      done: 0,
      rateText: '0%',
    },
    userProfile: null,
    showDeleteDialog: false,
    pendingDeleteId: null,
  },

  onShow() {
    if (!ensureAuthenticated('/pages/tasks/index')) {
      return;
    }

    this.setData({
      userProfile: getUserProfile(),
    });
    this.loadBootstrap();
  },

  onPullDownRefresh() {
    this.loadBootstrap().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (!this.data.loading && !this.data.loadingMore && this.data.hasMore) {
      this.loadTasks(false);
    }
  },

  async loadBootstrap() {
    this.setData({
      loading: true,
      pageNum: 1,
      hasMore: true,
    });

    try {
      const [categories, todayTasks] = await Promise.all([
        categoryService.listCategories(),
        taskService.getTodayTasks(),
      ]);

      const categoryPickerOptions = ['全部分类'].concat((categories || []).map((item) => item.name));
      const doneCount = (todayTasks || []).filter((item) => item.status === 'DONE').length;

      this.setData({
        categories: categories || [],
        categoryPickerOptions,
        todaySummary: {
          total: (todayTasks || []).length,
          done: doneCount,
          rateText: formatPercent((todayTasks || []).length ? doneCount / (todayTasks || []).length : 0),
        },
      });

      await this.loadTasks(true);
    } catch (error) {
      if (!this.data.tasks.length) {
        wx.showToast({
          title: error.message || '加载任务失败',
          icon: 'none',
        });
      }
    } finally {
      this.setData({ loading: false });
    }
  },

  decorateTask(task) {
    const category = this.data.categories.find((item) => String(item.id) === String(task.categoryId));
    const priorityMeta = getPriorityMeta(task.priority);
    const statusMeta = getStatusMeta(task.status);

    return {
      ...task,
      categoryName: category ? category.name : '未分类',
      dueText: formatDateTime(task.dueAt),
      priorityText: priorityMeta.text,
      priorityClass: priorityMeta.className,
      statusText: statusMeta.text,
      statusClass: statusMeta.className,
      isDone: task.status === 'DONE',
      isExpired: task.status === 'EXPIRED',
      descriptionText: task.description || '暂无备注',
    };
  },

  async loadTasks(reset) {
    const nextPage = reset ? 1 : this.data.pageNum;
    this.setData(reset ? { loading: true } : { loadingMore: true });

    try {
      const result = await taskService.listTasks({
        status: this.data.selectedStatus || undefined,
        categoryId: this.data.selectedCategoryId || undefined,
        keyword: this.data.keyword || undefined,
        date: this.data.todayOnly ? getTodayDateString() : undefined,
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
      if (reset) {
        this.setData({ tasks: [] });
      }
    } finally {
      this.setData({
        loading: false,
        loadingMore: false,
      });
    }
  },

  triggerSearchReload() {
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => {
      this.loadTasks(true);
    }, 250);
  },

  handleSearchFocus() {
    this.setData({ searchFocused: true });
  },

  handleSearchBlur() {
    this.setData({ searchFocused: false });
  },

  handleKeywordInput(event) {
    this.setData({ keyword: event.detail.value || '' });
    this.triggerSearchReload();
  },

  handleKeywordClear() {
    this.setData({
      keyword: '',
      searchFocused: false,
    });
    this.loadTasks(true);
  },

  selectStatus(event) {
    const value = event.currentTarget.dataset.value || '';
    this.setData({ selectedStatus: value });
    this.loadTasks(true);
  },

  handleCategoryChange(event) {
    const index = Number(event.detail.value || 0);
    const category = index > 0 ? this.data.categories[index - 1] : null;
    this.setData({
      selectedCategoryIndex: index,
      selectedCategoryId: category ? category.id : '',
    });
    this.loadTasks(true);
  },

  toggleTodayOnly() {
    this.setData({ todayOnly: !this.data.todayOnly });
    this.loadTasks(true);
  },

  goTaskForm(event) {
    const taskId = event && event.currentTarget ? event.currentTarget.dataset.id : '';
    const url = taskId ? `/pages/task-form/index?id=${taskId}` : '/pages/task-form/index';
    wx.navigateTo({ url });
  },

  async toggleTaskStatus(event) {
    const taskId = event.currentTarget.dataset.id;
    const currentStatus = event.currentTarget.dataset.status;
    const nextStatus = currentStatus === 'DONE' ? 'TODO' : 'DONE';

    try {
      await taskService.updateTaskStatus(taskId, nextStatus);
      await this.loadBootstrap();
    } catch (error) {
      wx.showToast({
        title: error.message || '更新状态失败',
        icon: 'none',
      });
    }
  },

  openDeleteDialog(event) {
    this.setData({
      showDeleteDialog: true,
      pendingDeleteId: event.currentTarget.dataset.id,
    });
  },

  closeDeleteDialog() {
    this.setData({
      showDeleteDialog: false,
      pendingDeleteId: null,
    });
  },

  async confirmDelete() {
    if (!this.data.pendingDeleteId) {
      return;
    }

    try {
      await taskService.deleteTask(this.data.pendingDeleteId);
      this.closeDeleteDialog();
      await this.loadBootstrap();
    } catch (error) {
      wx.showToast({
        title: error.message || '删除失败',
        icon: 'none',
      });
    }
  },

  onUnload() {
    clearTimeout(this.searchTimer);
  },
});
