<template>
  <view class="container">
    <view class="navbar">
      <text class="title">动物日常</text>
      <view class="right-icon">
        <text class="icon" @click="navToAdd()">➕</text>
      </view>
    </view>

    <view class="feed-container">
      <view class="left-column">
        <view class="post-item" v-for="(item, index) in leftColumnItems" :key="item.id" :style="{ marginTop: index === 0 ? '0' : '30rpx' }">
          <view class="post-image" >
            <image :src="item.picUrl" mode="cover" @click="niviageToDetail(item.stringId)"></image>
            <view class="location-tag">
              <text class="location-icon">📍</text>
              <text class="location-text">
                {{ formatLocation(item.latitude, item.longitude) }}
              </text>
            </view>
          </view>
          <view class="post-content">
            <text class="post-text">{{ item.text }}</text>
          </view>
          <view class="post-actions">
            <text class="action-icon">❤️</text>
            <text class="action-icon">💬</text>
            <text class="action-icon">🔖</text>
            <text class="action-icon">↗️</text>
          </view>
        </view>
      </view>

      <view class="right-column">
        <view class="post-item" v-for="(item, index) in rightColumnItems" :key="item.id" :style="{ marginTop: index === 0 ? '60rpx' : '30rpx' }">
          <view class="post-image">
            <image :src="item.picUrl" mode="cover" @click="niviageToDetail(item.stringId)"></image>
            <view class="location-tag">
              <text class="location-icon">📍</text>
              <text class="location-text">
                {{ formatLocation(item.latitude, item.longitude) }}
              </text>
            </view>
          </view>
          <view class="post-content">
            <text class="post-text">{{ item.text }}</text>
          </view>
          <view class="post-actions">
            <text class="action-icon">❤️</text>
            <text class="action-icon">💬</text>
            <text class="action-icon">🔖</text>
            <text class="action-icon">↗️</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onLoad , onShow} from '@dcloudio/uni-app'; 
import {listGetAnimalDaily} from '@/services/animalArchival/animalDaily.js'

const loading = ref(false)
const id = ref();

const animalDailyList = ref([]);

const niviageToDetail = (id)=>{
	uni.navigateTo({
		url: `/pages/animal_daily_detail/animal_daily_detail?id=${id}`
	})
}

const navToAdd = () =>{
	uni.navigateTo({
		url:`/pages/animal_daily_add/animal_daily_add?id=${id.value}`
	})
}

const leftColumnItems = computed(() => {
  return animalDailyList.value.filter((_, index) => index % 2 === 0);
});

const rightColumnItems = computed(() => {
  return animalDailyList.value.filter((_, index) => index % 2 === 1);
});

const formatLocation = (latitude, longitude) => {
  if (!latitude || !longitude) return '未知位置';
  return `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
};

onShow(()=>{
	getAnimalList(id.value);
})

onLoad((options)=>{
	id.value = options.id;
}) 

// 恢复成单纯获取数据的逻辑，移除交叉比对
const getAnimalList = async (id) => {
  try {
    loading.value = true;
    const response = await listGetAnimalDaily(id);
    animalDailyList.value = response.data || [];
  } catch (error) {
    console.error('处理数据失败:', error);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
/* 样式保留原样 */
.container { width: 100%; min-height: 100vh; background-color: #fafafa; }
.navbar { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 30rpx; background-color: white; border-bottom: 1px solid #eee; }
.title { font-size: 34rpx; font-weight: bold; color: #333; }
.right-icon { display: flex; gap: 30rpx; }
.icon { font-size: 32rpx; color: #333; }
.feed-container { display: flex; padding: 0 15rpx; gap: 15rpx; }
.left-column, .right-column { flex: 1; display: flex; flex-direction: column; }
.post-item { background-color: white; border-radius: 16rpx; overflow: hidden; margin-bottom: 20rpx; box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05); }
.post-image { position: relative; width: 100%; height: 400rpx; overflow: hidden; }
.post-image image { width: 100%; height: 100%; object-fit: cover; }
.location-tag { position: absolute; bottom: 15rpx; left: 15rpx; background-color: rgba(0, 0, 0, 0.5); color: white; padding: 5rpx 10rpx; border-radius: 12rpx; display: flex; align-items: center; font-size: 22rpx; }
.location-icon { margin-right: 5rpx; font-size: 20rpx; }
.location-text { max-width: 280rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.post-content { padding: 15rpx; }
.post-text { font-size: 26rpx; color: #333; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.post-actions { display: flex; padding: 10rpx 15rpx; gap: 20rpx; }
.action-icon { font-size: 30rpx; color: #666; }
@media (max-width: 375px) { .post-image { height: 350rpx; } }
@media (min-width: 414px) { .post-image { height: 450rpx; } }
</style>