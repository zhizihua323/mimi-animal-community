<template>
  <view class="detail-container" v-if="animal">
    <view class="cover-photo">
      <image :src="animal.albumPhoto || defaultImg" mode="aspectFill" class="img"></image>
      <view class="cover-overlay"></view>
    </view>

    <view class="info-card main-card">
      <view class="name-row">
        <text class="name">{{ animal.name }}</text>
        <text class="species">{{ animal.species }}</text>
      </view>
      <view class="tags-row">
        <text class="tag wild-tag">{{ animal.isWild === 1 ? '野生/无家' : '家养宠物' }}</text>
        <text class="tag show-tag">{{ animal.isShow === 1 ? '公开可见' : '仅自己可见' }}</text>
      </view>
    </view>

    <view class="info-card detail-card">
      <view class="section-title">
        <text class="icon">🐾</text> 详细资料
      </view>

      <view class="info-list">
        <view class="info-row">
          <text class="label">出生日期</text>
          <text class="value">{{ animal.birthData || '暂无记录' }}</text>
        </view>

        <view class="info-row">
          <text class="label">常出没地</text>
          <text class="value location" v-if="animal.latitude && animal.longitude">
            纬度: {{ animal.latitude.toFixed(6) }}<br/>
            经度: {{ animal.longitude.toFixed(6) }}
          </text>
          <text class="value" v-else>未记录位置</text>
        </view>

        <view class="info-row description-box">
          <text class="label">档案描述</text>
          <text class="value desc">{{ animal.description || '主人很懒，什么都没写~' }}</text>
        </view>
      </view>
    </view>

    <view class="bottom-bar">
      <button class="btn back-btn" @click="goBack">返回列表</button>
      <button class="btn daily-btn" @click="goToDaily">宠物日常</button>
    </view>
  </view>

  <view class="loading-state" v-else>
    <uni-icons type="spinner-cycle" size="30" color="#FF85A2" class="spin-icon"></uni-icons>
    <text>正在加载档案...</text>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { queryGetAnimalArchivalById } from '@/services/animalArchival/animalArchival.js';

const animal = ref(null);
const defaultImg = 'https://picsum.photos/seed/default/400/300'; // 默认底图防空

// 页面加载时获取传过来的 id
onLoad(async (options) => {
  if (options.id) {
    // 动态设置导航栏标题
    if (options.name) {
      uni.setNavigationBarTitle({ title: `${options.name} 的档案` });
    }
    await fetchAnimalDetail(options.id);
  } else {
    uni.showToast({ title: '参数错误，缺少动物ID', icon: 'none' });
    setTimeout(() => uni.navigateBack(), 1500);
  }
});

// 请求后端获取动物详细信息
const fetchAnimalDetail = async (id) => {
  try {
    uni.showLoading({ title: '加载中...' });
    const res = await queryGetAnimalArchivalById(id);
    if (res && res.data) {
      animal.value = res.data;
    } else {
      uni.showToast({ title: '未找到该档案', icon: 'none' });
    }
  } catch (error) {
    console.error('获取档案详情失败:', error);
    uni.showToast({ title: '网络错误，请重试', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
};

// 返回上一页
const goBack = () => {
  uni.navigateBack();
};

// 跳转到该动物的日常记录页
const goToDaily = () => {
  const animalId = animal.value.stringId || animal.value.id;
  uni.navigateTo({
    url: `/pages/animal_daily/animal_daily?id=${animalId}&name=${animal.value.name}`,
    fail: () => {
      uni.showToast({ title: '日常功能开发中', icon: 'none' });
    }
  });
};
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 140rpx; /* 给底部悬浮按钮留出空间 */
}

/* 顶部封面图 */
.cover-photo {
  width: 100%;
  height: 500rpx;
  position: relative;
}
.img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
/* 给图片底部加一个黑色渐变，让悬浮卡片过渡更自然 */
.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100rpx;
  background: linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,0.3));
}

/* 卡片通用样式 */
.info-card {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin: 0 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

/* 核心信息卡片 (往上浮动盖住一点图片) */
.main-card {
  margin-top: -60rpx;
  position: relative;
  z-index: 10;
  margin-bottom: 30rpx;
}

.name-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 15rpx;
}
.name {
  font-size: 44rpx;
  font-weight: bold;
  color: #333;
  margin-right: 20rpx;
}
.species {
  font-size: 26rpx;
  color: #666;
  background-color: #f0f2f5;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.tags-row {
  display: flex;
  gap: 15rpx;
}
.tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 30rpx;
  font-weight: 500;
}
.wild-tag {
  background-color: #FFF0F3;
  color: #FF4D6D;
}
.show-tag {
  background-color: #E8F5E9;
  color: #4CAF50;
}

/* 详细资料卡片 */
.detail-card {
  margin-bottom: 30rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  border-bottom: 1px solid #eee;
  padding-bottom: 20rpx;
  display: flex;
  align-items: center;
}
.section-title .icon {
  margin-right: 10rpx;
  font-size: 34rpx;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 25rpx;
}
.info-row {
  display: flex;
  align-items: flex-start;
}
.label {
  width: 140rpx;
  font-size: 28rpx;
  color: #888;
  flex-shrink: 0;
}
.value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}
.location {
  color: #409eff;
  font-family: monospace;
}
.description-box {
  flex-direction: column;
  background-color: #f9f9f9;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-top: 10rpx;
}
.description-box .label {
  margin-bottom: 10rpx;
  color: #333;
  font-weight: 500;
}
.description-box .desc {
  color: #666;
  font-size: 26rpx;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 120rpx;
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}
.btn {
  width: 46%;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
  margin: 0;
}
.back-btn {
  background-color: #f5f5f5;
  color: #666;
}
.daily-btn {
  background-color: #FF85A2;
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(255, 133, 162, 0.4);
}

/* 加载状态 */
.loading-state {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #999;
  font-size: 28rpx;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.spin-icon {
  animation: spin 1s linear infinite;
  margin-bottom: 20rpx;
}
</style>