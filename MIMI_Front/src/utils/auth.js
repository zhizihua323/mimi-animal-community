// 全局登录验证工具
export function setupAuthGuard() {
  // 不需要登录的页面白名单
  const whiteList = [
    '/pages/login/login',
    '/pages/register/register',
    // 可以添加其他不需要登录的页面
  ];
  
  // 监听页面路由跳转
  uni.addInterceptor('navigateTo', {
    invoke(to) {
      return checkAuth(to.url);
    }
  });
  
  uni.addInterceptor('redirectTo', {
    invoke(to) {
      return checkAuth(to.url);
    }
  });
  
  uni.addInterceptor('reLaunch', {
    invoke(to) {
      return checkAuth(to.url);
    }
  });
  
  // 检查登录状态
  function checkAuth(url) {
    // 解析页面路径
    const pagePath = url.split('?')[0];
    
    // 白名单页面直接放行
    if (whiteList.includes(pagePath)) {
      return true;
    }
    
    // 检查是否有token
    const token = uni.getStorageSync('token');
    
    if (!token) {
      // 没有token，记录当前要访问的页面
      uni.setStorageSync('redirectUrl', url);
      
      // 跳转到登录页
      uni.redirectTo({
        url: '/pages/login/login'
      });
      
      // 阻止原跳转
      return false;
    }
    
    // 有token，允许跳转
    return true;
  }
  
  // 初始化时检查当前页面
  checkCurrentPage();
  
  function checkCurrentPage() {
    const pages = getCurrentPages();
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1];
      const currentPath = `/${currentPage.route}`;
      
      // 如果当前页面不在白名单且没有token，跳转到登录页
      if (!whiteList.includes(currentPath) && !uni.getStorageSync('token')) {
        uni.setStorageSync('redirectUrl', currentPath);
        uni.redirectTo({
          url: '/pages/login/login'
        });
      }
    }
  }
}
