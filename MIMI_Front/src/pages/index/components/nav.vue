<template>
  <view class="nav-container" :style="{paddingTop : safeAreaInsets}">
    <view class="nav-content">
      <text class="nav-title">我的应用</text>
      <view class="nav-menu">
        <text>首页</text>
        <text @click="goToLogin">登入</text>
        <text>我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
    const{safeAreaInsets} = uni.getSystemInfo()
	
	const goToLogin = () => {
	  // 1. 获取当前页面路径（可选：用于登录成功后返回原页面）
	  const currentPages = getCurrentPages();
	  const currentPagePath = currentPages[currentPages.length - 1]?.route;
	  if (currentPagePath && !currentPagePath.includes('login')) {
	    // 存储当前页路径，登录成功后可返回（与之前登录页逻辑呼应）
	    uni.setStorageSync('redirectUrl', `/${currentPagePath}`);
	  }
	  	
	  // 2. 跳转到登录页（用 redirectTo 关闭当前页，避免返回导航页）
	  uni.redirectTo({
	    url: '/pages/login/login',
	    // 跳转失败处理（如路径错误）
	    fail: (err) => {
	      console.error('跳转到登录页失败：', err);
	      uni.showToast({ title: '跳转失败，请重试', icon: 'none' });
	    }
	  });
	};
</script>

<style scoped>
.nav-container {
  background-color: #007aff;
  padding: 20rpx;
}

.nav-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.nav-title {
  color: white;
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 15rpx;
}

.nav-menu {
  display: flex;
  justify-content: space-around;
  width: 100%;
}

.nav-menu text {
  color: white;
  font-size: 28rpx;
  margin: 0 20rpx;
}
</style>
