<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="back-icon" @click="navigateBack">←</view>
      <text class="navbar-title">选择收货地址</text>
      <view class="add-btn" @click="gotoAddAddress">+ 添加</view>
    </view>

    <!-- 地址列表 -->
    <view class="address-list">
      <view 
        class="address-item" 
        v-for="(item, index) in addressList" 
        :key="item.id"
        @click="selectAddress(item)"
      >
        <view class="address-header">
          <text class="recipient">{{ item.name }} {{ item.tel }}</text>
          <text class="default-tag" v-if="item.isDefault">默认</text>
        </view>
        <text class="address-detail">{{ item.address }}</text>
        <view class="address-actions">
          <text class="edit-btn" @click.stop="gotoEditAddress(item)">编辑</text>
          <text class="delete-btn" @click.stop="deleteAddress(index)">删除</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="addressList.length === 0">
        <text class="empty-text">暂无收货地址</text>
        <button class="add-first-btn" @click="gotoAddAddress">立即添加</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app'; 
import { queryGetAddress } from '@/services/shopping/address.js'

const loading = ref(false)

// 地址列表数据
const addressList = ref([
  {
    id: 1,
    name: '小可爱',
    tel: '138****5678',
    address: '北京市朝阳区某某小区1号楼2单元301室',
    isDefault: true
  },
  {
    id: 2,
    name: '小宝贝',
    tel: '139****9012',
    address: '上海市浦东新区某某街道45号602室',
    isDefault: false
  }
]);

// 页面加载时获取地址列表
onLoad(()=>{
	// 实际项目中从接口获取
	fetchAddressList();
	
})

const fetchAddressList = async() =>{
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
	
}

// 选择地址（核心：回传数据给上一页）
const selectAddress = (address) => {
  // 获取当前页面栈
  const pages = getCurrentPages();
  // 获取上一页实例
  const prevPage = pages[pages.length - 2];
  
  // 给上一页设置选中的地址
  prevPage.$vm.address = address;
  
  uni.$emit('addressSelected', address);
  console.log(prevPage.$vm.address)
  // 返回上一页
  uni.navigateBack({
    delta: 1
  });
};

// 前往添加地址页
const gotoAddAddress = () => {
  uni.navigateTo({
    url: '/pages/address_add/address_add'
  });
};

// 前往编辑地址页
const gotoEditAddress = (item) => {
  uni.navigateTo({
    url: `/pages/address_edit/address_edit?id=${item.id}&name=${encodeURIComponent(item.name)}&tel=${item.tel}&address=${encodeURIComponent(item.address)}&isDefault=${item.isDefault}`
  });
};

// 删除地址
const deleteAddress = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这个地址吗？',
    success: (res) => {
      if (res.confirm) {
        addressList.value.splice(index, 1);
        uni.showToast({
          title: '删除成功',
          icon: 'none'
        });
      }
    }
  });
};

// 返回上一页
const navigateBack = () => {
  uni.navigateBack({
    delta: 1
  });
};
</script>

<style scoped>
/* 样式保持与之前一致的可爱风格 */
.container {
  min-height: 100vh;
  background-color: #FFF5F7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background-color: #FF85A2;
  color: white;
}

.back-icon {
  font-size: 36rpx;
}

.navbar-title {
  font-size: 34rpx;
  font-weight: bold;
}

.add-btn {
  font-size: 30rpx;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.address-list {
  padding: 20rpx;
}

.address-item {
  background-color: white;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05);
  position: relative;
}

.address-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.recipient {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.default-tag {
  font-size: 24rpx;
  color: white;
  background-color: #FF85A2;
  padding: 2rpx 12rpx;
  border-radius: 12rpx;
}

.address-detail {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.address-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
}

.edit-btn, .delete-btn {
  font-size: 26rpx;
  padding: 4rpx 12rpx;
  border-radius: 14rpx;
}

.edit-btn {
  color: #FF85A2;
  background-color: #FFF0F3;
}

.delete-btn {
  color: #FF6B6B;
  background-color: #FFEEEE;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 150rpx;
  gap: 30rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.add-first-btn {
  background-color: #FF85A2;
  color: white;
  border: none;
  border-radius: 30rpx;
  padding: 16rpx 40rpx;
  font-size: 28rpx;
}
</style>
    