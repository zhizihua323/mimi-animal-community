/**
 * 显示提示信息
 * @param {string} title - 提示内容
 * @param {string} icon - 图标类型 success/error/loading/none
 * @param {number} duration - 显示时长
 */
export const showToast = (title, icon = 'none', duration = 2000) => {
  return new Promise((resolve) => {
    uni.showToast({
      title,
      icon,
      duration,
      success: resolve
    });
  });
};

/**
 * 检查登录状态
 * @returns {boolean} 是否登录
 */
export const checkLogin = () => {
  const token = uni.getStorageSync('token');
  return !!token;
};

/**
 * 登录拦截，如果未登录则跳转到登录页
 * @returns {boolean} 是否已登录
 */
export const loginInterceptor = () => {
  if (!checkLogin()) {
    uni.navigateTo({
      url: '/pages/login/login'
    });
    return false;
  }
  return true;
};
