const authService = require('../../services/auth');
const userService = require('../../services/user');
const {
  getToken,
  setToken,
  setUserProfile,
  redirectAfterLogin,
} = require('../../utils/auth');

Page({
  data: {
    loading: false,
    showTips: false,
    tipMessage: '',
  },

  loginByWechat() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: resolve,
        fail: reject,
      });
    });
  },

  getWechatProfile() {
    return new Promise((resolve, reject) => {
      if (!wx.getUserProfile) {
        resolve({
          userInfo: {
            nickName: '微信用户',
            avatarUrl: '',
          },
        });
        return;
      }

      wx.getUserProfile({
        desc: '用于展示昵称和头像',
        success: resolve,
        fail: reject,
      });
    });
  },

  onShow() {
    if (getToken()) {
      redirectAfterLogin();
    }
  },

  showTopTips(message) {
    this.setData({
      showTips: true,
      tipMessage: message,
    });
    clearTimeout(this.tipTimer);
    this.tipTimer = setTimeout(() => {
      this.setData({ showTips: false, tipMessage: '' });
    }, 2000);
  },

  async handleWechatLogin() {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true, showTips: false, tipMessage: '' });

    try {
      const loginRes = await this.loginByWechat();
      if (!loginRes.code) {
        throw new Error('未获取到微信登录凭证');
      }

      let profile = {
        nickName: '微信用户',
        avatarUrl: '',
      };

      try {
        const userProfile = await this.getWechatProfile();
        if (userProfile && userProfile.userInfo) {
          profile = {
            nickName: userProfile.userInfo.nickName || '微信用户',
            avatarUrl: userProfile.userInfo.avatarUrl || '',
          };
        }
      } catch (error) {
        profile = {
          nickName: '微信用户',
          avatarUrl: '',
        };
      }

      const authPayload = await authService.loginWithWechat({
        code: loginRes.code,
        nickName: profile.nickName,
        avatarUrl: profile.avatarUrl,
      });

      setToken(authPayload.accessToken);

      let me = null;
      try {
        me = await userService.getMe();
      } catch (error) {
        me = {
          userId: authPayload.userId,
          nickName: authPayload.nickName || profile.nickName,
          avatarUrl: authPayload.avatarUrl || profile.avatarUrl,
        };
      }

      getApp().globalData.userProfile = me;
      setUserProfile(me);
      redirectAfterLogin();
    } catch (error) {
      this.showTopTips(error.message || '登录失败，请稍后重试');
    } finally {
      this.setData({ loading: false });
    }
  },

  onUnload() {
    clearTimeout(this.tipTimer);
  },
});
