const userService = require('../../services/user');
const {
  getUserProfile,
  setUserProfile,
  clearSession,
  ensureAuthenticated,
} = require('../../utils/auth');

Page({
  data: {
    profile: null,
    showLogoutDialog: false,
  },

  onShow() {
    if (!ensureAuthenticated('/pages/me/index')) {
      return;
    }

    const cachedProfile = getUserProfile();
    if (cachedProfile) {
      this.setData({ profile: cachedProfile });
    }
    this.refreshProfile();
  },

  async refreshProfile() {
    try {
      const profile = await userService.getMe();
      this.setData({ profile });
      getApp().globalData.userProfile = profile;
      setUserProfile(profile);
    } catch (error) {
      if (!this.data.profile) {
        wx.showToast({
          title: error.message || '获取资料失败',
          icon: 'none',
        });
      }
    }
  },

  goCategories() {
    wx.navigateTo({ url: '/pages/categories/index' });
  },

  goRecycleBin() {
    wx.navigateTo({ url: '/pages/recycle-bin/index' });
  },

  openLogoutDialog() {
    this.setData({ showLogoutDialog: true });
  },

  closeLogoutDialog() {
    this.setData({ showLogoutDialog: false });
  },

  confirmLogout() {
    clearSession();
    this.setData({ showLogoutDialog: false });
    wx.reLaunch({ url: '/pages/login/index' });
  },
});
