<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <text class="title">动物日常</text>
      <view class="right-icon">
        <!-- <text class="icon">🔍</text> -->
        <text class="icon" @click="navToAdd()">➕</text>
      </view>
    </view>

    <!-- 内容列表 - 采用左右两列阶梯式布局 -->
    <view class="feed-container">
      <!-- 左列 - 第一排先展示 -->
      <view class="left-column">
        <view 
          class="post-item" 
          v-for="(item, index) in leftColumnItems" 
          :key="item.id"
          :style="{ marginTop: index === 0 ? '0' : '30rpx' }"
        >
          <view class="post-image" >
            <image :src="item.picUrl" mode="cover" @click="niviageToDetail(item.stringId)"></image>
            <!-- 位置信息 -->
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

      <!-- 右列 - 比左列滞后一个位置，形成阶梯感 -->
      <view class="right-column">
        <view 
          class="post-item" 
          v-for="(item, index) in rightColumnItems" 
          :key="item.id"
          :style="{ marginTop: index === 0 ? '60rpx' : '30rpx' }"
        >
          <view class="post-image">
            <image :src="item.picUrl" mode="cover" @click="niviageToDetail(item.stringId)"></image>
            <!-- 位置信息 -->
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
import { onLoad , onShow, onUnload} from '@dcloudio/uni-app'; 
import {listGetAnimalDaily} from '@/services/animalArchival/animalDaily.js'

const loading = ref(false)
const id = ref();
// 模拟动物日常数据
const animalDailyList = ref([
  {
    id: 1,
    picUrl: 'https://picsum.photos/seed/animal1/600/800',
    text: '今天天气真好，带着小白去公园散步，它看起来很开心，到处探索新环境。',
    latitude: 39.90882,
    longitude: 116.39748,
    createTime: '2023-10-15T09:30:00',
    createUser: 1001,
    archivalId: 1
  },
  {
    id: 2,
    picUrl: 'https://picsum.photos/seed/animal2/600/800',
    text: '大黄今天学会了新技能，真是只聪明的狗狗！奖励了它最喜欢的零食。',
    latitude: 39.91882,
    longitude: 116.40748,
    createTime: '2023-10-15T14:20:00',
    createUser: 1001,
    archivalId: 2
  },
  {
    id: 3,
    picUrl: 'https://picsum.photos/seed/animal3/600/800',
    text: '灰灰今天特别活跃，一直在院子里蹦蹦跳跳，看起来精力充沛。',
    latitude: 39.92882,
    longitude: 116.41748,
    createTime: '2023-10-16T10:15:00',
    createUser: 1002,
    archivalId: 3
  },
  {
    id: 4,
    picUrl: 'https://picsum.photos/seed/animal4/600/800',
    text: '发现一只可爱的小鸟在窗台筑巢，真是个惊喜！希望能见证小鸟宝宝的诞生。',
    latitude: 39.93882,
    longitude: 116.42748,
    createTime: '2023-10-16T16:40:00',
    createUser: 1003,
    archivalId: 4
  },
  {
    id: 5,
    picUrl: 'https://picsum.photos/seed/animal5/600/800',
    text: '小白今天有点调皮，把毛线球弄得到处都是，但看它那无辜的眼神又不忍心责怪。',
    latitude: 39.94882,
    longitude: 116.43748,
    createTime: '2023-10-17T08:50:00',
    createUser: 1001,
    archivalId: 1
  }
]);

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

// 分割数据到左右两列，形成阶梯效果
const leftColumnItems = computed(() => {
  // 左列取偶数索引项（0, 2, 4...）
  return animalDailyList.value.filter((_, index) => index % 2 === 0);
});

const rightColumnItems = computed(() => {
  // 右列取奇数索引项（1, 3, 5...）
  return animalDailyList.value.filter((_, index) => index % 2 === 1);
});

// 格式化经纬度显示
const formatLocation = (latitude, longitude) => {
  if (!latitude || !longitude) return '未知位置';
  // 保留6位小数，显示简洁的位置信息
  return `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
};

// 页面加载时可以调用接口获取数据
onMounted(() => {
  // 实际项目中这里会调用后端接口获取animalDaily数据
  // fetchAnimalDailyData();;
});

onShow(()=>{
	getAnimalList(id.value);
})

onLoad((options)=>{
    // options 中包含 URL 传递的所有参数
    console.log('接收的参数：', options);
    // 赋值到 data 中（如果参数经过 encodeURIComponent 编码，需要用 decodeURIComponent 解码）
	id.value = options.id;
    
}) 

const getAnimalList = async (id) => {
  try {
    loading.value = true // 显示加载状态
    const response = await listGetAnimalDaily(id)
    
    // 把后端返回的数据赋值给日常列表
    animalDailyList.value = response.data || [];
    
    console.log("获取到的动物日常数据：", animalDailyList.value)
    
  } catch (error) {
    console.error('处理数据失败:', error)
  } finally {
    loading.value = false // 关闭加载状态
  }
}

// 模拟获取数据的方法
const fetchAnimalDailyData = async () => {
  try {
    // 实际项目中替换为真实接口
    // const response = await getAnimalDailyList();
    // animalDailyList.value = response.data;
  } catch (error) {
    console.error('获取动物日常数据失败:', error);
  }
};
</script>

<style scoped>
.container {
  width: 100%;
  min-height: 100vh;
  background-color: #fafafa;
}

/* 导航栏样式 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: white;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.right-icon {
  display: flex;
  gap: 30rpx;
}

.icon {
  font-size: 32rpx;
  color: #333;
}

/* 内容布局样式 */
.feed-container {
  display: flex;
  padding: 0 15rpx;
  gap: 15rpx;
}

.left-column, .right-column {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 帖子项样式 */
.post-item {
  background-color: white;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.post-image {
  position: relative;
  width: 100%;
  height: 400rpx;
  overflow: hidden;
}

.post-image image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 位置标签样式 */
.location-tag {
  position: absolute;
  bottom: 15rpx;
  left: 15rpx;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 5rpx 10rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  font-size: 22rpx;
}

.location-icon {
  margin-right: 5rpx;
  font-size: 20rpx;
}

.location-text {
  max-width: 280rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 帖子内容样式 */
.post-content {
  padding: 15rpx;
}

.post-text {
  font-size: 26rpx;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 操作按钮样式 */
.post-actions {
  display: flex;
  padding: 10rpx 15rpx;
  gap: 20rpx;
}

.action-icon {
  font-size: 30rpx;
  color: #666;
}

/* 适配不同屏幕尺寸 */
@media (max-width: 375px) {
  .post-image {
    height: 350rpx;
  }
}

@media (min-width: 414px) {
  .post-image {
    height: 450rpx;
  }
}
</style>
