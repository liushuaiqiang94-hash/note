const categoryService = require('../../services/categories');
const taskService = require('../../services/tasks');
const {
  PRIORITY_OPTIONS,
  REPEAT_OPTIONS,
} = require('../../constants/index');
const { ensureAuthenticated } = require('../../utils/auth');
const { splitDateTime, combineDateTime } = require('../../utils/date');

Page({
  data: {
    loading: false,
    saving: false,
    isEdit: false,
    taskId: '',
    taskStatus: 'TODO',
    categories: [],
    categoryPickerOptions: ['未分类'],
    selectedCategoryIndex: 0,
    priorityOptions: PRIORITY_OPTIONS,
    repeatOptions: REPEAT_OPTIONS,
    selectedPriorityIndex: 1,
    selectedRepeatIndex: 0,
    showDeleteDialog: false,
    form: {
      title: '',
      description: '',
      dueDate: '',
      dueTime: '',
      remindDate: '',
      remindTime: '',
    },
  },

  onLoad(options) {
    const taskId = options && options.id ? options.id : '';
    const isEdit = Boolean(taskId);

    this.setData({
      taskId,
      isEdit,
    });

    wx.setNavigationBarTitle({
      title: isEdit ? '编辑任务' : '新增任务',
    });
  },

  onShow() {
    const redirect = this.data.isEdit && this.data.taskId
      ? `/pages/task-form/index?id=${this.data.taskId}`
      : '/pages/task-form/index';

    if (!ensureAuthenticated(redirect)) {
      return;
    }

    this.loadPage();
  },

  async loadPage() {
    this.setData({ loading: true });
    try {
      const categories = await categoryService.listCategories();
      const categoryPickerOptions = ['未分类'].concat((categories || []).map((item) => item.name));

      this.setData({
        categories: categories || [],
        categoryPickerOptions,
      });

      if (this.data.isEdit) {
        await this.loadTaskDetail();
      }
    } catch (error) {
      wx.showToast({
        title: error.message || '页面加载失败',
        icon: 'none',
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadTaskDetail() {
    const task = await taskService.getTask(this.data.taskId);
    const due = splitDateTime(task.dueAt);
    const remind = splitDateTime(task.remindAt);
    const categoryIndex = this.data.categories.findIndex((item) => String(item.id) === String(task.categoryId));
    const priorityIndex = this.data.priorityOptions.findIndex((item) => item.value === task.priority);
    const repeatIndex = this.data.repeatOptions.findIndex((item) => item.value === task.repeatType);

    this.setData({
      taskStatus: task.status || 'TODO',
      selectedCategoryIndex: categoryIndex >= 0 ? categoryIndex + 1 : 0,
      selectedPriorityIndex: priorityIndex >= 0 ? priorityIndex : 1,
      selectedRepeatIndex: repeatIndex >= 0 ? repeatIndex : 0,
      form: {
        title: task.title || '',
        description: task.description || '',
        dueDate: due.date,
        dueTime: due.time,
        remindDate: remind.date,
        remindTime: remind.time,
      },
    });
  },

  updateFormField(field, value) {
    this.setData({
      [`form.${field}`]: value,
    });
  },

  handleTitleInput(event) {
    this.updateFormField('title', event.detail.value || '');
  },

  handleDescriptionInput(event) {
    this.updateFormField('description', event.detail.value || '');
  },

  handleCategoryChange(event) {
    this.setData({
      selectedCategoryIndex: Number(event.detail.value || 0),
    });
  },

  handlePriorityChange(event) {
    this.setData({
      selectedPriorityIndex: Number(event.detail.value || 0),
    });
  },

  handleRepeatChange(event) {
    this.setData({
      selectedRepeatIndex: Number(event.detail.value || 0),
    });
  },

  handleDueDateChange(event) {
    this.updateFormField('dueDate', event.detail.value || '');
  },

  handleDueTimeChange(event) {
    this.updateFormField('dueTime', event.detail.value || '');
  },

  handleRemindDateChange(event) {
    this.updateFormField('remindDate', event.detail.value || '');
  },

  handleRemindTimeChange(event) {
    this.updateFormField('remindTime', event.detail.value || '');
  },

  validateForm() {
    if (!this.data.form.title.trim()) {
      return '请先填写任务标题';
    }

    const dueAt = combineDateTime(this.data.form.dueDate, this.data.form.dueTime);
    if (dueAt === '__INVALID__') {
      return '截止时间需要同时选择日期和时间';
    }

    const remindAt = combineDateTime(this.data.form.remindDate, this.data.form.remindTime);
    if (remindAt === '__INVALID__') {
      return '提醒时间需要同时选择日期和时间';
    }

    return '';
  },

  buildPayload() {
    const category = this.data.selectedCategoryIndex > 0
      ? this.data.categories[this.data.selectedCategoryIndex - 1]
      : null;
    const dueAt = combineDateTime(this.data.form.dueDate, this.data.form.dueTime);
    const remindAt = combineDateTime(this.data.form.remindDate, this.data.form.remindTime);
    const description = this.data.form.description.trim();
    const payload = {
      title: this.data.form.title.trim(),
      priority: this.data.priorityOptions[this.data.selectedPriorityIndex].value,
      repeatType: this.data.repeatOptions[this.data.selectedRepeatIndex].value,
    };

    if (category) {
      payload.categoryId = category.id;
    }
    if (description) {
      payload.description = description;
    }
    if (dueAt) {
      payload.dueAt = dueAt;
    }
    if (remindAt) {
      payload.remindAt = remindAt;
    }

    return payload;
  },

  backToTasks() {
    if (getCurrentPages().length > 1) {
      wx.navigateBack();
      return;
    }
    wx.switchTab({ url: '/pages/tasks/index' });
  },

  async handleSave() {
    const message = this.validateForm();
    if (message) {
      wx.showToast({
        title: message,
        icon: 'none',
      });
      return;
    }

    this.setData({ saving: true });
    const payload = this.buildPayload();

    try {
      if (this.data.isEdit) {
        await taskService.updateTask(this.data.taskId, payload);
      } else {
        await taskService.createTask(payload);
      }

      wx.showToast({
        title: '保存成功',
        icon: 'success',
      });
      setTimeout(() => {
        this.backToTasks();
      }, 300);
    } catch (error) {
      wx.showToast({
        title: error.message || '保存失败',
        icon: 'none',
      });
    } finally {
      this.setData({ saving: false });
    }
  },

  async handleToggleStatus() {
    if (!this.data.isEdit) {
      return;
    }

    const nextStatus = this.data.taskStatus === 'DONE' ? 'TODO' : 'DONE';

    try {
      const updatedTask = await taskService.updateTaskStatus(this.data.taskId, nextStatus);
      wx.showToast({
        title: updatedTask.status === 'DONE' ? '已标记完成' : '任务已恢复',
        icon: 'success',
      });
      this.setData({ taskStatus: updatedTask.status || nextStatus });
      setTimeout(() => {
        this.backToTasks();
      }, 300);
    } catch (error) {
      wx.showToast({
        title: error.message || '状态更新失败',
        icon: 'none',
      });
    }
  },

  openDeleteDialog() {
    this.setData({ showDeleteDialog: true });
  },

  closeDeleteDialog() {
    this.setData({ showDeleteDialog: false });
  },

  async confirmDelete() {
    try {
      await taskService.deleteTask(this.data.taskId);
      this.setData({ showDeleteDialog: false });
      wx.showToast({
        title: '已移入回收站',
        icon: 'success',
      });
      setTimeout(() => {
        this.backToTasks();
      }, 300);
    } catch (error) {
      wx.showToast({
        title: error.message || '删除失败',
        icon: 'none',
      });
    }
  },
});
