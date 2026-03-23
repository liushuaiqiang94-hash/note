const categoryService = require('../../services/categories');
const { CATEGORY_COLOR_OPTIONS } = require('../../constants/index');
const { ensureAuthenticated } = require('../../utils/auth');

Page({
  data: {
    loading: false,
    saving: false,
    categories: [],
    colorOptions: CATEGORY_COLOR_OPTIONS,
    editingId: '',
    showDeleteDialog: false,
    pendingDeleteId: '',
    form: {
      name: '',
      color: CATEGORY_COLOR_OPTIONS[0],
      sortNo: '0',
    },
  },

  onShow() {
    if (!ensureAuthenticated('/pages/categories/index')) {
      return;
    }
    this.loadCategories();
  },

  async loadCategories() {
    this.setData({ loading: true });
    try {
      const categories = await categoryService.listCategories();
      this.setData({ categories: categories || [] });
    } catch (error) {
      wx.showToast({
        title: error.message || '加载分类失败',
        icon: 'none',
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  handleNameInput(event) {
    this.setData({
      'form.name': event.detail.value || '',
    });
  },

  handleSortNoInput(event) {
    this.setData({
      'form.sortNo': event.detail.value || '0',
    });
  },

  selectColor(event) {
    this.setData({
      'form.color': event.currentTarget.dataset.color,
    });
  },

  editCategory(event) {
    const categoryId = event.currentTarget.dataset.id;
    const category = this.data.categories.find((item) => String(item.id) === String(categoryId));
    if (!category) {
      return;
    }

    this.setData({
      editingId: category.id,
      form: {
        name: category.name || '',
        color: category.color || this.data.colorOptions[0],
        sortNo: String(category.sortNo || 0),
      },
    });
  },

  resetForm() {
    this.setData({
      editingId: '',
      form: {
        name: '',
        color: this.data.colorOptions[0],
        sortNo: '0',
      },
    });
  },

  async handleSave() {
    if (!this.data.form.name.trim()) {
      wx.showToast({
        title: '请先填写分类名称',
        icon: 'none',
      });
      return;
    }

    this.setData({ saving: true });
    const payload = {
      name: this.data.form.name.trim(),
      color: this.data.form.color,
      sortNo: Number(this.data.form.sortNo || 0),
    };

    try {
      if (this.data.editingId) {
        await categoryService.updateCategory(this.data.editingId, payload);
      } else {
        await categoryService.createCategory(payload);
      }

      wx.showToast({
        title: this.data.editingId ? '已更新分类' : '已新增分类',
        icon: 'success',
      });
      this.resetForm();
      await this.loadCategories();
    } catch (error) {
      wx.showToast({
        title: error.message || '保存分类失败',
        icon: 'none',
      });
    } finally {
      this.setData({ saving: false });
    }
  },

  openDeleteDialog(event) {
    this.setData({
      pendingDeleteId: event.currentTarget.dataset.id,
      showDeleteDialog: true,
    });
  },

  closeDeleteDialog() {
    this.setData({
      pendingDeleteId: '',
      showDeleteDialog: false,
    });
  },

  async confirmDelete() {
    try {
      await categoryService.deleteCategory(this.data.pendingDeleteId);
      this.closeDeleteDialog();
      if (String(this.data.editingId) === String(this.data.pendingDeleteId)) {
        this.resetForm();
      }
      wx.showToast({
        title: '分类已删除',
        icon: 'success',
      });
      await this.loadCategories();
    } catch (error) {
      wx.showToast({
        title: error.message || '删除分类失败',
        icon: 'none',
      });
    }
  },
});
