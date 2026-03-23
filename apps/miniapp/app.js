const { getToken, getUserProfile, setUserProfile, clearSession, redirectToLogin } = require('./utils/auth');
const request = require('./utils/request');
const userService = require('./services/user');

App({
  globalData: {
    userProfile: getUserProfile() || null,
  },

  onLaunch() {
    request.setUnauthorizedHandler(() => {
      this.handleUnauthorized();
    });

    if (getToken()) {
      this.refreshUserProfile().catch(() => {
        // The request layer handles errors and auth redirects.
      });
    }
  },

  async refreshUserProfile() {
    if (!getToken()) {
      this.globalData.userProfile = null;
      return null;
    }

    const profile = await userService.getMe();
    this.globalData.userProfile = profile;
    setUserProfile(profile);
    return profile;
  },

  handleUnauthorized() {
    const pages = getCurrentPages();
    const currentRoute = pages.length ? `/${pages[pages.length - 1].route}` : '/pages/tasks/index';
    clearSession();
    this.globalData.userProfile = null;
    redirectToLogin(currentRoute);
  },
});
