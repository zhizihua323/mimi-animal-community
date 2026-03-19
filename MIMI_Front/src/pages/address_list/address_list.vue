<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="back-icon" @click="navigateBack">←</view>
      <text class="navbar-title">收货地址管理</text>
      <view class="empty-space"></view>
    </view>

    <!-- 地址列表 -->
    <view class="address-list">
      <!-- 地址项 -->
      <view class="address-item" v-for="(item, index) in addressList" :key="item.id">
        <view class="address-info">
          <view class="name-tel">
            <text class="name">{{ item.name }}</text>
            <text class="tel">{{ item.tel }}</text>
          </view>
          <text class="address-detail">{{ item.address }}</text>
          <view class="default-tag" v-if="item.isDefault">默认地址</view>
        </view>
        
        <view class="address-actions">
          <view class="action-btn edit-btn" @click="editAddress(item)">
            <text class="icon">✎</text>
            <text class="text">编辑</text>
          </view>
          <view class="action-btn delete-btn" @click="deleteAddress(index, item.stringId)">
            <text class="icon">🗑</text>
            <text class="text">删除</text>
          </view>
         <!-- <view class="action-btn default-btn" @click="setDefault(item.id)" v-if="!item.isDefault">
            <text class="icon">★</text>
            <text class="text">默认</text>
          </view> -->
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="addressList.length === 0">
        <view class="empty-icon">📦</view>
        <text class="empty-text">暂无收货地址</text>
        <text class="empty-desc">添加你的第一个收货地址吧~</text>
      </view>
    </view>

    <!-- 添加地址按钮 -->
    <button class="add-address-btn" @click="navigateToAddAddress">
      <text class="add-icon">+</text>
      <text class="add-text">添加新地址</text>
    </button>

    <!-- 删除确认弹窗 -->
    <view class="confirm-modal" v-if="showDeleteConfirm">
      <view class="modal-content">
        <text class="modal-title">确认删除</text>
        <text class="modal-message">确定要删除这个地址吗？</text>
        <view class="modal-buttons">
          <button class="cancel-btn" @click="showDeleteConfirm = false">取消</button>
          <button class="confirm-btn" @click="confirmDelete()">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import {  onShow } from '@dcloudio/uni-app'; 
import { queryGetAddress,deletePostAddressById } from '@/services/shopping/address.js'

// 地址列表数据
const addressList = ref([]);
// 删除确认弹窗状态
const showDeleteConfirm = ref(false);
// 当前要删除的地址索引和ID
const currentDeleteIndex = ref(-1);
const currentDeleteId = ref('');
const loading = ref(false)

// 页面加载时获取地址列表
onShow(async()=>{
	fetchAddressList();
})
// 模拟获取地址列表数据
const fetchAddressList = async() => {
  // 实际项目中这里会调用接口
  // 模拟数据
  addressList.value = [
    {
      id: 1,
      stringId: '1',
      userId: 123,
      name: '小可爱',
      tel: '138****5678',
      address: '北京市朝阳区某某小区1号楼2单元301室',
      isDefault: true
    },
    {
      id: 2,
      stringId: '2',
      userId: 123,
      name: '小萌宠',
      tel: '139****1234',
      address: '上海市浦东新区某某街道5号602室',
      isDefault: false
    }
  ];
  try {
    loading.value = true // 显示加载状态
    // 传递分页参数
    const response = await queryGetAddress();
	addressList.value = response.data;
	
  } catch (error) {
    console.error('处理数据失败:', error)
  } finally {
    loading.value = false // 关闭加载状态
  }
  
};

// 返回上一页
const navigateBack = () => {
  uni.navigateBack({
    delta: 1
  });
};

// 跳转到添加地址页面
const navigateToAddAddress = () => {
  uni.navigateTo({
    url: '/pages/address_add/address_add'
  });
};

// 编辑地址
const editAddress = (item) => {
  uni.navigateTo({
    url: `/pages/address_edit/address_edit?id=${item.stringId}&name=${encodeURIComponent(item.name)}&tel=${item.tel}&address=${encodeURIComponent(item.address)}&isDefault=${item.isDefault}`
  });
};

// 显示删除确认
const deleteAddress = (index, id) => {
  currentDeleteIndex.value = index;
  currentDeleteId.value = id;
  showDeleteConfirm.value = true;
};

// 确认删除
const confirmDelete = async() => {
  // 实际项目中这里会调用删除接口
  addressList.value.splice(currentDeleteIndex.value, 1);
  
  const result = await deletePostAddressById(currentDeleteId.value);
  
  // 提示删除成功
  uni.showToast({
    title: '地址已删除',
    icon: 'none'
  });
  
  // 关闭弹窗
  showDeleteConfirm.value = false;
};

// 设置默认地址
const setDefault = (id) => {
  // 实际项目中这里会调用接口
  addressList.value.forEach(item => {
    item.isDefault = item.id === id;
  });
  
  uni.showToast({
    title: '已设为默认地址',
    icon: 'none'
  });
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #FFF5F7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

/* 顶部导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background-color: #FF85A2;
  color: white;
  position: relative;
}

.back-icon {
  font-size: 36rpx;
  width: 40rpx;
}

.navbar-title {
  font-size: 34rpx;
  font-weight: bold;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.empty-space {
  width: 40rpx;
}

/* 地址列表 */
.address-list {
  flex: 1;
  padding: 20rpx;
}

.address-item {
  background-color: white;
  border-radius: 24rpx;
  padding: 50rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05);
  position: relative;
}

.address-info {
  padding-right: 200rpx;
}

.name-tel {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.tel {
  font-size: 28rpx;
  color: #666;
}

.address-detail {
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 10rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.default-tag {
  display: inline-block;
  font-size: 24rpx;
  color: #FF85A2;
  background-color: #FFF0F3;
  padding: 2rpx 16rpx;
  border-radius: 14rpx;
}

.address-actions {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80rpx;
}

.action-btn .icon {
  font-size: 30rpx;
  margin-bottom: 5rpx;
}

.action-btn .text {
  font-size: 24rpx;
}

.edit-btn .icon, .edit-btn .text {
  color: #4CD964;
}

.delete-btn .icon, .delete-btn .text {
  color: #FF4D6D;
}

.default-btn .icon, .default-btn .text {
  color: #FFC107;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  color: #999;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 30rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 32rpx;
  margin-bottom: 15rpx;
}

.empty-desc {
  font-size: 26rpx;
  opacity: 0.8;
}

/* 添加地址按钮 */
.add-address-btn {
  position: fixed;
  bottom: 30rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  background-color: #FF85A2;
  color: white;
  border: none;
  border-radius: 40rpx;
  padding: 22rpx 0;
  font-size: 30rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 16rpx rgba(255, 133, 162, 0.3);
}

.add-icon {
  font-size: 34rpx;
  margin-right: 10rpx;
}

/* 删除确认弹窗 */
.confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  width: 80%;
  max-width: 500rpx;
  background-color: white;
  border-radius: 30rpx;
  padding: 40rpx;
  display: flex;
  flex-direction: column;
}

.modal-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  text-align: center;
}

.modal-message {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 30rpx;
  text-align: center;
  line-height: 1.5;
}

.modal-buttons {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
}

.cancel-btn, .confirm-btn {
  flex: 1;
  padding: 20rpx 0;
  border-radius: 25rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.cancel-btn {
  background-color: #F5F5F5;
  color: #666;
  border: none;
}

.confirm-btn {
  background-color: #FF4D6D;
  color: white;
  border: none;
}
</style>
    