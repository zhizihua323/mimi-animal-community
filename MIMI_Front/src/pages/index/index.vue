<template>
  <view class="home-container">
    <!-- 1. 顶部Banner区 -->
    <view class="banner">
      <image 
        class="banner-img" 
        src="https://picsum.photos/seed/animal-banner/750/300" 
        mode="widthFix"
      ></image>
      <view class="banner-overlay">
        <text class="banner-title">MIMI 动物社区</text>
        <text class="banner-desc">记录每一个毛孩子的美好时光</text>
      </view>
    </view>

    <!-- 2. 核心功能入口（网格布局） -->
    <view class="function-grid">
      <view class="function-item" @click="navTo('animal-archival')">
        <view class="item-icon">🐾</view>
        <text class="item-text">动物档案</text>
        <text class="item-desc">管理宠物基本信息</text>
      </view>
      <view class="function-item" @click="navTo('animal-daily')">
        <view class="item-icon">📝</view>
        <text class="item-text">日常记录</text>
        <text class="item-desc">分享宠物生活点滴</text>
      </view>
      <view class="function-item" @click="navTo('animal-map')">
        <view class="item-icon">🗺️</view>
        <text class="item-text">地图定位</text>
        <text class="item-desc">标记宠物活动位置</text>
      </view>
      <view class="function-item" @click="navTo('collect')">
        <view class="item-icon">❤️</view>
        <text class="item-text">我的收藏</text>
        <text class="item-desc">保存重要内容</text>
      </view>
    </view>

    <!-- 3. 功能详情展览区（滚动渐显） -->
    <view class="feature-exhibition">
      <!-- 3.1 动物档案模块 -->
      <view class="feature-card" :class="{ 'fade-in': isShowCard1 }" @scroll="handleScroll">
        <view class="card-left">
          <text class="card-tag">核心功能</text>
          <text class="card-title">动物档案管理</text>
          <text class="card-desc">
            建立宠物专属档案，记录物种、出生日期、疫苗情况等信息，
            支持上传相册封面，永久保存宠物成长轨迹。
          </text>
          <button class="card-btn" @click="navTo('animal-archival')">查看档案</button>
        </view>
        <view class="card-right">
          <image 
            src="https://picsum.photos/seed/animal-archival/400/300" 
            mode="widthFix"
            class="card-img"
          ></image>
        </view>
      </view>

      <!-- 3.2 日常记录模块 -->
      <view class="feature-card reverse" :class="{ 'fade-in': isShowCard2 }" @scroll="handleScroll">
        <view class="card-left">
          <image 
            src="https://picsum.photos/seed/animal-daily/400/300" 
            mode="widthFix"
            class="card-img"
          ></image>
        </view>
        <view class="card-right">
          <text class="card-tag">生活分享</text>
          <text class="card-title">宠物日常记录</text>
          <text class="card-desc">
            上传宠物日常照片，添加文字描述和地理位置，
            按时间轴展示宠物生活，支持点赞收藏互动。
          </text>
          <button class="card-btn" @click="navTo('animal-daily')">发布记录</button>
        </view>
      </view>

      <!-- 3.3 地图定位模块 -->
      <view class="feature-card" :class="{ 'fade-in': isShowCard3 }" @scroll="handleScroll">
        <view class="card-left">
          <text class="card-tag">位置服务</text>
          <text class="card-title">地图定位标记</text>
          <text class="card-desc">
            基于腾讯地图，标记宠物常去地点（如宠物医院、公园），
            支持导航功能，轻松找到宠物相关服务位置。
          </text>
          <button class="card-btn" @click="navTo('animal-map')">打开地图</button>
        </view>
        <view class="card-right">
          <image 
            src="https://picsum.photos/seed/animal-map/400/300" 
            mode="widthFix"
            class="card-img"
          ></image>
        </view>
      </view>
    </view>

    <!-- 4. 底部导航栏 -->
    <view class="home-footer">
      <view class="footer-links">
        <text class="link-item">关于我们</text>
        <text class="link-item">使用帮助</text>
        <text class="link-item">隐私政策</text>
      </view>
      <text class="copyright">© 2026 MIMI 动物社区 版权所有</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { onPageScroll } from '@dcloudio/uni-app'; // 🌟 引入小程序专属的页面滚动生命周期

// 获取当前组件实例，用来替代 this
const instance = getCurrentInstance();

// 滚动渐显状态
const isShowCard1 = ref(false);
const isShowCard2 = ref(false);
const isShowCard3 = ref(false);

// 检查滚动位置，控制卡片渐显
const checkScrollPosition = () => {
  uni.createSelectorQuery()
    .in(instance.proxy) // ✅ 修复报错：用 instance.proxy 替代 this
    .selectAll('.feature-card')
    .boundingClientRect((rects) => {
      if (!rects || rects.length === 0) return; // 防止节点还未渲染时报错
      const windowHeight = uni.getSystemInfoSync().windowHeight;
      rects.forEach((rect, index) => {
        // 当卡片顶部进入视口 70% 时，触发渐显
        if (rect.top < windowHeight * 0.7) {
          if (index === 0) isShowCard1.value = true;
          if (index === 1) isShowCard2.value = true;
          if (index === 2) isShowCard3.value = true;
        }
      });
    })
    .exec();
};

// 页面挂载时检查一次（确保首屏内容显示）
onMounted(() => {
  // 稍微延时一下，确保节点渲染完成再查询
  setTimeout(() => {
    checkScrollPosition();
  }, 100);
});

// 🌟 修复监听：使用小程序专用的页面滚动钩子，替代原先的 addEventListener
onPageScroll(() => {
  checkScrollPosition();
});

// ⚠️ 修复模板报错：你在 template 里写了 @scroll="handleScroll"，但没定义这个方法
const handleScroll = () => {
  // 目前不需要具体逻辑，保留空函数防止 Vue 报错即可
  // 其实更好的做法是去 template 里把 @scroll="handleScroll" 删掉
};

// 导航到对应功能页面
const navTo = (pageName) => {
  const pageMap = {
    // ✅ 1. 动物档案 (路径已精准匹配)
    'animal-archival': '/pages/animalArchival/animalArchival', 
    
    // ✅ 2. 日常记录 (路径已精准匹配)
    'animal-daily': '/pages/animal_daily/animal_daily', 
    
    // ⚠️ 3. 地图定位 ( pages.json 里还没建这个页面，暂时留空或写个占位)
    'animal-map': '/pages/animal_map/animal_map', 
    
    // ⚠️ 4. 我的收藏 ( pages.json 里还没建这个页面，暂时留空或写个占位)
    'collect': ''
  };
  
  const targetUrl = pageMap[pageName];

  // 如果还没建页面（路径为空），给个弹窗提示，防止报错
  if (!targetUrl) {
    uni.showToast({
      title: '该功能正在开发中',
      icon: 'none'
    });
    return;
  }
  
  // 执行跳转
  uni.navigateTo({
    url: targetUrl,
    fail: (err) => {
      console.log('跳转失败:', err);
      // 万一以后把这些页面改成了底部 TabBar，走这里兜底
      uni.switchTab({
        url: targetUrl
      });
    }
  });
};
</script>

<style scoped>
/* 全局容器 */
.home-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
  overflow-x: hidden;
}

/* 1. 顶部Banner */
.banner {
  position: relative;
  width: 100%;
  height: 300rpx;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
}

.banner-title {
  font-size: 40rpx;
  font-weight: bold;
  margin-bottom: 15rpx;
}

.banner-desc {
  font-size: 26rpx;
  opacity: 0.9;
}

/* 2. 功能网格入口 */
.function-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 30rpx;
  background-color: white;
  margin: 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25rpx 10rpx;
  background-color: #fafafa;
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.function-item:hover {
  transform: translateY(-5rpx);
  background-color: rgba(255, 133, 162, 0.08);
}

.item-icon {
  font-size: 50rpx;
  margin-bottom: 15rpx;
  color: #FF85A2;
}

.item-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.item-desc {
  font-size: 22rpx;
  color: #999;
  text-align: center;
}

/* 3. 功能详情展览区 */
.feature-exhibition {
  padding: 0 20rpx 50rpx;
  overflow-y: auto;
  max-height: calc(100vh - 500rpx);
}

.feature-card {
  display: flex;
  gap: 30rpx;
  background-color: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  opacity: 0;
  transform: translateY(50rpx);
  transition: all 0.6s ease;
}

/* 反向布局（图片在左，文字在右） */
.feature-card.reverse {
  flex-direction: row-reverse;
}

/* 渐显动画类 */
.feature-card.fade-in {
  opacity: 1;
  transform: translateY(0);
}

.card-left, .card-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.card-tag {
  display: inline-block;
  font-size: 22rpx;
  color: #FF85A2;
  background-color: rgba(255, 133, 162, 0.1);
  padding: 5rpx 15rpx;
  border-radius: 12rpx;
  margin-bottom: 15rpx;
}

.card-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.card-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 30rpx;
}

.card-btn {
  width: 200rpx;
  height: 70rpx;
  line-height: 70rpx;
  background-color: #FF85A2;
  color: white;
  border: none;
  border-radius: 12rpx;
  font-size: 26rpx;
  transition: all 0.3s ease;
}

.card-btn:hover {
  background-color: #ff6b8f;
}

.card-img {
  width: 100%;
  height: 250rpx;
  object-fit: cover;
  border-radius: 12rpx;
}

/* 4. 底部导航 */
.home-footer {
  background-color: white;
  padding: 30rpx 20rpx;
  border-top: 1px solid #eee;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  margin-bottom: 20rpx;
}

.link-item {
  font-size: 24rpx;
  color: #666;
}

.copyright {
  display: block;
  text-align: center;
  font-size: 22rpx;
  color: #999;
}

/* 适配小屏幕 */
@media (max-width: 375px) {
  .feature-card {
    flex-direction: column;
  }
  .feature-card.reverse {
    flex-direction: column;
  }
  .card-img {
    height: 200rpx;
  }
}
</style>