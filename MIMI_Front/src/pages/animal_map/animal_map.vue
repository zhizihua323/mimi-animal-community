<template>
  <view class="map-container">
    <view class="floating-header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="header-title">🐾 附近的小动物</text>
      <view class="empty-placeholder"></view> </view>

    <map
      id="myMap"
      class="map"
      :latitude="latitude"
      :longitude="longitude"
      :markers="markers"
      scale="14"
      show-location
      @markertap="onMarkerTap"
      @tap="closeCard"
    ></map>

    <view class="locate-btn" @click="moveToCurrentLocation">
      <text class="locate-icon">🎯</text>
    </view>

    <view class="bottom-deco">
      <text class="deco-text">用爱心点亮每一个角落 💖</text>
    </view>

    <view class="info-card" v-if="currentAnimal">
      <view class="card-header">
        <text class="animal-name">{{ currentAnimal.name }}</text>
        <text class="animal-species">{{ currentAnimal.species }}</text>
      </view>
      <view class="card-content">
        <text class="animal-desc">{{ currentAnimal.description || '暂无详细描述...' }}</text>
      </view>
      <button class="nav-btn" @click="goToDetail">查看档案详情</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// ========== 1. 状态定义 ==========
// 地图中心点经纬度 (先设个默认值，稍后用定位覆盖)
const latitude = ref(35.314930);
const longitude = ref(113.908660);

// 标记点数组 (传给 map 组件的)
const markers = ref([]);

// 原始数据列表 (从数据库查出来的)
const animalList = ref([]);

// 当前被点击选中的小动物
const currentAnimal = ref(null);

// ========== 2. 生命周期 ==========
onMounted(() => {
  // 1. 获取手机当前位置
  getUserLocation();
  // 2. 去后端拉取动物数据
  fetchAnimalMarkers();
});

// ========== 3. 方法定义 ==========
// 获取用户真实位置
const getUserLocation = () => {
  uni.getLocation({
    type: 'gcj02', // 微信小程序地图专用的坐标系
    success: (res) => {
      latitude.value = res.latitude;
      longitude.value = res.longitude;
    },
    fail: (err) => {
      console.log('获取位置失败，使用默认坐标', err);
    }
  });
};

// 💡 提取出一个通用的小工具函数：专门把数组转换成地图认识的标记格式
const generateMarkers = (list) => {
  return list.map((item) => {
    return {
      id: item.id,               
      latitude: item.latitude,   
      longitude: item.longitude, 
      title: item.name,
      width: 32,
      height: 32,
      iconPath: '/static/images/paw.png', // 依然是你的小猫爪
      
      callout: {
        content: ` ${item.name} `, 
        color: '#ffffff',
        fontSize: 13,
        borderRadius: 8,
        bgColor: '#FF85A2',
        padding: 6,
        display: 'ALWAYS'
      }
    };
  });
};

// 获取动物数据并生成地图标记 (真假数据合并版)
const fetchAnimalMarkers = () => {
  // 1. 先把两个尽职尽责的“假动物”准备好 (ID 用负数，防止和数据库真实数据冲突)
  const fakeAnimalList = [
    { id: -1, name: '流浪猫小橘', species: '猫咪', latitude: latitude.value + 0.002, longitude: longitude.value + 0.003, description: '性格温顺，喜欢吃冻干，经常在小树林附近晒太阳。' },
    { id: -2, name: '看门狗大黄', species: '狗狗', latitude: latitude.value - 0.003, longitude: longitude.value - 0.001, description: '保安亭的编外人员，很尽职。' }
  ];

  // 2. 发起请求去拿后端真实的动物数据
  uni.request({
    url: 'http://127.0.0.1:8080/community/animalArchival/mapList', // 你的接口
    method: 'GET',
    // header: { 'Authorization': '如果需要token就写这里' }, 
    success: (res) => {
      let realAnimalList = [];
      
      // 如果接口通了，且返回成功
      if (res.data && res.data.code === 200) {
        // 拿到真实数据，并过滤掉没有经纬度的脏数据
        realAnimalList = (res.data.data || []).filter(item => item.latitude && item.longitude);
      }
      
      // 3. 🌟核心魔法：用扩展运算符 [...] 把假数据和真数据合并成一个大数组！
      animalList.value = [...fakeAnimalList, ...realAnimalList];
      
      // 4. 调用工具函数，渲染到地图上
      markers.value = generateMarkers(animalList.value);
    },
    fail: (err) => {
      console.error('后端接口请求失败，将仅展示假数据', err);
      // 就算后端没启动/报错，假数据也依然会坚强地显示在地图上！
      animalList.value = [...fakeAnimalList];
      markers.value = generateMarkers(animalList.value);
    }
  });
};

// 点击某个地图标记时触发
const onMarkerTap = (e) => {
  const markerId = e.detail.markerId;
  // 从列表里揪出被点击的那个小动物
  const animal = animalList.value.find(item => item.id === markerId);
  if (animal) {
    currentAnimal.value = animal; // 赋值，底部卡片就会弹出来
  }
};

// 点击地图空白处，关闭底部卡片
const closeCard = () => {
  currentAnimal.value = null;
};

// 点击卡片上的按钮跳转详情
const goToDetail = () => {
  if (!currentAnimal.value) return;
  // 携带 ID 跳转到你的详情页或修改页
  uni.navigateTo({
    url: `/pages/animalArchival_updated/animalArchival_updated?id=${currentAnimal.value.id}`
  });
};

// 新增：返回首页 (首页是 TabBar，必须用 switchTab)
const goBack = () => {
  uni.switchTab({
    url: '/pages/index/index'
  });
};

// 新增：一键将地图中心移回我的位置
const moveToCurrentLocation = () => {
  // 根据地图组件的 id ('myMap') 创建地图上下文
  const mapCtx = uni.createMapContext('myMap');
  mapCtx.moveToLocation(); // 调用小程序原生的回滚当前位置方法
};
</script>

<style scoped>
/* 让地图铺满整个屏幕 */
.map-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.map {
  width: 100%;
  height: 100%;
}

/* 底部悬浮卡片样式 */
.info-card {
  position: absolute;
  bottom: 60rpx;
  left: 30rpx;
  right: 30rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 35rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.15);
  z-index: 999;
  /* 加个简单的弹出动画 */
  animation: slideUp 0.3s ease-out forwards; 
}

@keyframes slideUp {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.card-header {
  display: flex;
  align-items: flex-end;
  margin-bottom: 20rpx;
}

.animal-name {
  font-size: 38rpx;
  font-weight: bold;
  color: #333;
  margin-right: 20rpx;
}

.animal-species {
  font-size: 24rpx;
  color: #FF85A2;
  background-color: rgba(255, 133, 162, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
}

.card-content {
  margin-bottom: 30rpx;
}

.animal-desc {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.nav-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #FF85A2;
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 40rpx;
  border: none;
}

.nav-btn:active {
  background-color: #ff6b8f;
  transform: scale(0.98);
}

/* ================= 新增装饰与按钮样式 ================= */

/* 1. 顶部悬浮栏 (胶囊形状) */
.floating-header {
  position: absolute;
  top: 100rpx; /* 避开手机顶部状态栏 */
  left: 30rpx;
  right: 30rpx;
  height: 90rpx;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 45rpx; /* 圆角胶囊 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10rpx 0 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.back-btn {
  width: 70rpx;
  height: 70rpx;
  background-color: #f5f7fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 36rpx;
  color: #333;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.empty-placeholder {
  width: 70rpx;
}

/* 2. 定位当前位置按钮 */
.locate-btn {
  position: absolute;
  right: 30rpx;
  bottom: 400rpx; /* 保证它悬浮在卡片上方不被遮挡 */
  width: 80rpx;
  height: 80rpx;
  background-color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.15);
  z-index: 90;
}

.locate-icon {
  font-size: 40rpx;
}

/* 3. 底部温馨装饰条 */
.bottom-deco {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 80rpx;
  background-color: #FF85A2; /* 主题粉色 */
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 80;
  box-shadow: 0 -4rpx 20rpx rgba(255, 133, 162, 0.3);
}

.deco-text {
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 500;
  letter-spacing: 2rpx;
}

/* 注意：如果卡片弹出来后遮住了底部粉色条，把上面原有 .info-card 的 bottom 改为 120rpx */
</style>