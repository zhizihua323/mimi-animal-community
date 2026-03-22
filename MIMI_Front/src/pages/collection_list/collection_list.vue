<template>
  <view class="container">
    <scroll-view scroll-x class="tabs-scroll">
      <view class="tabs">
        <view class="tab-item" :class="{ active: currentType === 0 }" @click="currentType = 0">全部</view>
        <view class="tab-item" :class="{ active: currentType === 1 }" @click="currentType = 1">🛍️ 商品</view>
        <view class="tab-item" :class="{ active: currentType === 2 }" @click="currentType = 2">🗂️ 档案</view>
      </view>
    </scroll-view>

    <view class="list-container" v-if="filteredList.length > 0">
      <view class="collect-card" v-for="item in filteredList" :key="item.id" @click="gotoDetail(item)">
        <image class="cover-img" :src="item.picUrl" mode="aspectFill"></image>
        
        <view class="info-box">
          <text class="title">{{ item.title }}</text>
          <text class="summary">{{ item.summary }}</text>
          
          <view class="bottom-row">
            <view class="tag-row">
              <text class="type-tag tag-goods" v-if="item.targetType === 1">商品</text>
              <text class="type-tag tag-archive" v-else-if="item.targetType === 2">档案</text>
              <text class="time-text">{{ formatDate(item.createTime) }}</text>
            </view>
            <view class="cancel-btn" @click.stop="cancelCollect(item)">取消收藏</view>
          </view>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <view class="empty-icon">📂</view>
      <text class="empty-text">还没有收藏任何内容哦~</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { queryGetCollectList, togglePostCollect } from "@/services/my/collect.js"; 

const currentType = ref(0); // 0-全部, 1-商品, 2-档案
const collectList = ref([]);

onShow(() => {
  fetchList();
});

const fetchList = async () => {
  try {
    const res = await queryGetCollectList();
    collectList.value = res.data || [];
  } catch (err) {
    console.error('获取收藏失败:', err);
  }
};

const filteredList = computed(() => {
  if (currentType.value === 0) return collectList.value;
  return collectList.value.filter(item => item.targetType === currentType.value);
});

const gotoDetail = (item) => {
  if (item.targetType === 1) {
    // 1是商品，既然没有商品详情页，直接跳回商城页面
    uni.switchTab({ 
      url: '/pages/cart/cart' 
    });
  } else if (item.targetType === 2) {
    // 2是档案，跳到动物循迹页面 (假设页面路径是 /pages/category/category)
    uni.switchTab({ 
      url: '/pages/category/category' 
    });
  }
};

const cancelCollect = (item) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消收藏吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await togglePostCollect({ targetId: item.targetId, targetType: item.targetType });
          uni.showToast({ title: '已取消', icon: 'success' });
          fetchList();
        } catch (error) {
          uni.showToast({ title: '取消失败', icon: 'none' });
        }
      }
    }
  });
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const safeDateStr = dateStr.replace('T', ' ').replace(/-/g, '/');
  const date = new Date(safeDateStr);
  return `${date.getMonth() + 1}-${date.getDate()}`;
};
</script>

<style scoped>
/* 样式部分几乎不变，只修改了标签颜色 */
.container { min-height: 100vh; background-color: #F5F7FA; display: flex; flex-direction: column; }
.tabs-scroll { background-color: white; border-bottom: 1px solid #F0F0F0; }
.tabs { display: flex; padding: 0 20rpx; }
.tab-item { padding: 24rpx 30rpx; font-size: 28rpx; color: #666; white-space: nowrap; transition: all 0.2s; position: relative; }
.tab-item.active { color: #FF85A2; font-weight: bold; }
.tab-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40rpx; height: 6rpx; background-color: #FF85A2; border-radius: 4rpx; }

.list-container { padding: 20rpx; }
.collect-card { display: flex; background-color: white; padding: 24rpx; border-radius: 20rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03); transition: transform 0.2s; }
.collect-card:active { transform: scale(0.98); }
.cover-img { width: 180rpx; height: 180rpx; border-radius: 12rpx; margin-right: 24rpx; background-color: #f5f5f5; flex-shrink: 0; }
.info-box { flex: 1; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden; }
.title { font-size: 30rpx; font-weight: bold; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.summary { font-size: 26rpx; color: #888; margin-top: 10rpx; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }

.bottom-row { display: flex; justify-content: space-between; align-items: center; margin-top: 20rpx; }
.tag-row { display: flex; align-items: center; }
.type-tag { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 8rpx; font-weight: bold; margin-right: 16rpx;}
.tag-goods { color: #FF4D6D; background-color: #FFF0F3; }
.tag-archive { color: #FF9800; background-color: #FFF3E0; } /* 档案使用橙色标签 */
.time-text { font-size: 24rpx; color: #999; }
.cancel-btn { font-size: 24rpx; color: #999; padding: 8rpx 20rpx; border: 1px solid #ddd; border-radius: 30rpx; }
.cancel-btn:active { background-color: #f0f0f0; }

.empty-state { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; margin-top: 200rpx; }
.empty-icon { font-size: 100rpx; opacity: 0.6; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #999; }
</style>