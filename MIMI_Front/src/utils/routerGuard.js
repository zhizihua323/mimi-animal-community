// 全局路由守卫，检查登录状态
export function setupRouterGuard(router) {
  // 全局前置守卫
  router.beforeEach((to, from, next) => {
    // 不需要登录的页面白名单
    const whiteList = [
      '/pages/login/login', 
      '/pages/register/register',
      '/pages/index/index' // 可以根据实际需求添加首页等公开页面
    ];
    
    // 检查是否在白名单中
    if (whiteList.includes(to.path)) {
      return next();
    }
    
    // 检查是否有token
    const token = uni.getStorageSync('token');
    
    if (token) {
      // 有token，允许访问
      next();
    } else {
      // 没有token，记录当前路径用于登录后返回
      uni.setStorageSync('redirectUrl', to.path);
      // 跳转到登录页
      next('/pages/login/login');
    }
  });
  
  // 全局后置守卫，可用于页面加载完成后的操作
  router.afterEach((to, from) => {
    // 可以在这里添加页面统计等功能
    console.log(`页面从 ${from.path} 跳转到 ${to.path}`);
  });
}
