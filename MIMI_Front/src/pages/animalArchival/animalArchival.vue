<template>
  <view class="animal-list-container">
    <!-- 页面标题 -->
	<view class="page-header">
      <view class="back-btn" @click="onBack">
        <text class="back-icon">←</text>
      </view>
      
      <view class="title-group">
        <text class="page-title">动物档案</text>
        <text class="page-subtitle">展示所有动物的基本信息</text>
      </view>
    </view>
    <!-- <view class="page-header">
      <text class="page-title">动物档案</text>
      <text class="page-subtitle">展示所有动物的基本信息</text>
    </view> -->
    
    <!-- 搜索和筛选区 -->
    <!-- <view class="filter-bar">
      <view class="search-box">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input type="text" placeholder="输入动物名称或种类..." class="search-input" />
      </view>
	  <button class="comfirm-btn" size="mini" type="default">
		  确定
	  </button>
    </view> -->
	<view>
		<view class="filter-options">
		  <button class="filter-btn" size="mini" type="default">
		    <uni-icons type="filter" size="16" color="#666"></uni-icons>
		    筛选
		  </button>
		  <button class="filter-btn" size="mini" type="default" @click="animalsDocument_add()">
		    <uni-icons type="sort" size="16" color="#666"></uni-icons>
		    新增
		  </button>
		</view>
	</view>
    
    <!-- 动物列表 -->
    <view class="animal-grid">
      <!-- 动物卡片 - 循环展示 -->
      <view class="animal-card" v-for="(animal, index) in animals" :key="index">
        <!-- 动物照片 -->
        <view class="animal-photo">
          <image 
            :src="animal.albumPhoto" 
            mode="aspectFill"
            class="photo-img"
            :alt="`${animal.name}的照片`"
          ></image>
          <!-- 野生标识 -->
          <view class="wild-badge" v-if="animal.isWild === 1">
            <text class="badge-text">无家</text>
          </view>
          <!-- 展示状态标识 -->
          <view class="show-badge" v-if="animal.isShow === 1">
            <text class="badge-text">公开</text>
          </view>
        </view>
        
        <!-- 动物信息 -->
        <view class="animal-info">
          <view class="info-header">
            <text class="animal-name">{{ animal.name }}</text>
            <text class="animal-species">{{ animal.species }}</text>
          </view>
          
          <view class="info-details">
            <view class="detail-item">
              <uni-icons type="calendar" size="14" color="#888"></uni-icons>
              <text class="detail-text">出生日期: {{ animal.birthData }}</text>
            </view>
            
            <view class="detail-description">
              <text class="description-text">{{ animal.description }}</text>
            </view>
          </view>
          
          <!-- 操作按钮 -->
          <view class="action-buttons">
            <button class="btn view-btn" size="mini">
              <uni-icons type="eye" size="14" color="#fff"></uni-icons>
              查看
            </button>
            <button class="btn edit-btn" size="mini" @click="animalsDocumentUpdate(animal.stringId)">
              <uni-icons type="edit" size="14" color="#fff"></uni-icons>
              编辑
            </button>
			<button class="btn delete-btn" size="mini" @click="deleteAnimalArchival(animal.stringId)">
			  <uni-icons type="delete" size="14" color="#fff"></uni-icons>
			  删除
			</button>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 空状态提示 -->
    <view class="empty-state" v-if="animals.length === 0">
      <uni-icons type="empty" size="60" color="#ccc"></uni-icons>
      <text class="empty-text">暂无动物数据</text>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import {listGetAnimalArchival,deletePostAnimalArchivalById} from '@/services/animalArchival/animalArchival.js'
import { onShow, onHide, onUnload } from '@dcloudio/uni-app'; 

const loading = ref(false)
// 模拟动物数据
const animals = ref([
  {
	id:"",
    name: "森林之王",
    species: "狮子",
    isWild: 1,
    birthData: "2018-05-12",
    albumPhoto: "https://picsum.photos/seed/lion/300/200",
    description: "这是一只雄壮的雄狮，来自非洲草原，性格凶猛但对幼崽非常温柔。",
    isShow: 1,
    createUserId: 1001
  },
]);
// 返回“我的”页面 ( tabBar 页面，必须用 switchTab)
const onBack = () => {
  uni.switchTab({
    url: '/pages/my/my'
  });
};

onMounted(async ()=>{
	getAnimalList()
})

onShow(async ()=>{
	getAnimalList()
})

const deleteAnimalArchival = async (id) =>{
	try {
	  loading.value = true // 显示加载状态
	  // 传递分页参数
	  const response = await deletePostAnimalArchivalById(id)
	  // 假设后端返回格式为 { records: [], total: 0, ... }

	  // total.value = response.total
	} catch (error) {
	  console.error('处理数据失败:', error)
	} finally {
	  loading.value = false // 关闭加载状态
	  getAnimalList();
	}
}

// 获取动物列表数据的方法
const getAnimalList = async () => {
  try {
    loading.value = true // 显示加载状态
    // 传递分页参数
    const response = await listGetAnimalArchival()
    
    // 假设后端返回格式为 { records: [], total: 0, ... }
    animals.value = response.data.records
	console.log("animals的值",animals.value)
    // total.value = response.total
  } catch (error) {
    console.error('处理数据失败:', error)
  } finally {
    loading.value = false // 关闭加载状态
  }
}

const animalsDocumentUpdate = (id) =>{
	uni.setStorageSync("animalArchivalUpdated",id)
	uni.navigateTo({
	   url: '/pages/animalArchival_add/animalArchival_add', 
	   fail: (err) => {
	     console.error('跳转到动物档案添加失败：', err);
	     uni.showToast({ title: '跳转失败，请重试', icon: 'none' });
	   }
	 });
}



const animalsDocument_add = () =>{
	uni.removeStorageSync("animalArchivalUpdated");
	 uni.navigateTo({
	    url: '/pages/animalArchival_add/animalArchival_add', 
	    fail: (err) => {
	      console.error('跳转到动物档案添加失败：', err);
	      uni.showToast({ title: '跳转失败，请重试', icon: 'none' });
	    }
	  });
}
</script>

<style scoped>
.animal-list-container {
  padding: 16px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 页面标题样式 */
.page-header {
  text-align: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
}

/* 筛选栏样式 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 20px;
  padding: 8px 16px;
  width: 100%;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.search-input {
  border: none;
  outline: none;
  margin-left: 8px;
  width: 100%;
  font-size: 14px;
}

.filter-options {
  display: flex;
  gap: 10px;
}

.filter-btn {
  margin-bottom: 1rem;
  width: 50%;
  border-radius: 20px !important;
  background-color: #fff !important;
  color: #666 !important;
  border: 1px solid #eee !important;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.filter-btn:hover{
	margin-bottom: 1rem;
	width: 50%;
	border-radius: 20px !important;
	background-color: #fff !important;
	color: skyblue;
	border: 1px solid #eee !important;
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

/* 动物列表网格 */
.animal-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* 动物卡片样式 */
.animal-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.animal-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

/* 动物照片样式 */
.animal-photo {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.photo-img {
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
}

.animal-card:hover .photo-img {
  transform: scale(1.05);
}

/* 标识徽章 */
.wild-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background-color: #ff4d4f;
  color: white;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.show-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #52c41a;
  color: white;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.badge-text {
  color: #fff;
  font-size: 12px;
}

/* 动物信息样式 */
.animal-info {
  padding: 12px;
}

.info-header {
  margin-bottom: 10px;
}

.animal-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.animal-species {
  font-size: 12px;
  color: #666;
  margin-left: 8px;
  background-color: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.info-details {
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #888;
}

.detail-text {
  margin-left: 4px;
}

.detail-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.btn {
  width: 32%;
  border-radius: 6px !important;
  font-size: 13px !important;
  padding: 6px 0 !important;
}

.comfirm-btn{
	width: 25%;
	border-radius: 12px !important;
	background-color: #42A5F5 !important;
	color: #fff !important;
	padding: 3px 0 !important;
}

.view-btn {
  /* background-color: #4096ff !important; */
  background-image: linear-gradient(135deg, #4096ff, #1890ff80) !important;
  color: #fff !important;
}

.edit-btn {
  /* background-color: #faad14 !important; */
   background-image: linear-gradient(135deg, #ffb44f, #faad1480) !important;
  
  color: #fff !important;
}

.delete-btn {
  /* background-color: #FF4444 !important; */
  background-image: linear-gradient(135deg, #ff5252, #FF444480) !important;	
  color: #fff !important;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #ccc;
}

.empty-text {
  margin-top: 16px;
  font-size: 16px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .animal-grid {
    grid-template-columns: 1fr;
  }
  
  .search-box {
    width: 60%;
  }
}

.page-header {
  position: relative; /* 关键：加上相对定位，让返回按钮以它为基准 */
  display: flex;
  flex-direction: column;
  align-items: center;  /* 保证你的原标题继续居中 */
  justify-content: center;
  padding: 20rpx 0;
}

.title-group {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* --- 新增的返回按钮样式 --- */
.back-btn {
  position: absolute; /* 绝对定位，直接钉在左边，不挤占标题的空间 */
  left: 30rpx; 
  top: 50%;
  transform: translateY(-50%); /* 确保按钮垂直居中 */
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #f5f7fa;
}

.back-icon {
  font-size: 36rpx;
  color: #333;
}
/* ----------------------- */
</style>
