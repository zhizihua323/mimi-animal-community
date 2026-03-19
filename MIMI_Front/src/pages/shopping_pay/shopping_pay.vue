<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="back-icon" @click="navigateBack">←</view>
      <text class="navbar-title">订单确认</text>
      <view class="empty-space"></view>
    </view>

    <!-- 订单信息卡片 -->
    <view class="order-card">
      <view class="card-title">
        <text class="title-text">商品信息</text>
        <text class="order-status">待付款</text>
      </view>
      
      <view class="goods-info">
        <image :src="goodsImage" mode="widthFix" class="goods-image"></image>
        <view class="goods-details">
          <text class="goods-name">{{ goodsName }}</text>
          <text class="goods-price">¥{{ price.toFixed(2) }}</text>
          <text class="goods-quantity">数量: {{ num }}件</text>
        </view>
      </view>
    </view>

    <!-- 收货地址卡片 -->
    <view class="address-card" @click="chooseAddress">
      <view class="card-title">
        <text class="title-text">收货地址</text>
        <text class="edit-icon">✎</text>
      </view>
      
      <view class="address-info" v-if="address">
        <text class="recipient">{{ address.name }} {{ address.tel }}</text>
        <text class="address-detail">{{ address.address }}</text>
      </view>
      
      <view class="no-address" v-else>
        <text class="add-address-text">+ 添加收货地址</text>
      </view>
    </view>

    <!-- 支付信息卡片 -->
    <view class="payment-card">
      <view class="card-title">
        <text class="title-text">支付信息</text>
      </view>
      
      <view class="payment-detail">
        <view class="payment-item">
          <text class="label">商品总价</text>
          <text class="value">¥{{ (price * num).toFixed(2) }}</text>
        </view>
        <view class="payment-item">
          <text class="label">运费</text>
          <text class="value">¥{{ freight.toFixed(2) }}</text>
        </view>
        <view class="payment-item total">
          <text class="label">实付款</text>
          <text class="value">¥{{ (price * num + freight).toFixed(2) }}</text>
        </view>
      </view>
    </view>

    <!-- 支付方式选择 -->
    <view class="payment-method-card">
      <view class="card-title">
        <text class="title-text">支付方式</text>
      </view>
      
      <view class="method-options">
        <view class="method-item" :class="{ selected: selectedMethod === 'wechat' }" @click="selectedMethod = 'wechat'">
          <text class="method-icon">💬</text>
          <text class="method-name">微信支付</text>
          <text class="check-icon" v-if="selectedMethod === 'wechat'">✓</text>
        </view>
        <view class="method-item" :class="{ selected: selectedMethod === 'alipay' }" @click="selectedMethod = 'alipay'">
          <text class="method-icon">支</text>
          <text class="method-name">支付宝</text>
          <text class="check-icon" v-if="selectedMethod === 'alipay'">✓</text>
        </view>
      </view>
    </view>

    <!-- 底部支付按钮 -->
    <view class="footer">
      <view class="total-amount">
        <text class="amount-label">合计:</text>
        <text class="amount-value">¥{{ (price * num + freight).toFixed(2) }}</text>
      </view>
      <button class="pay-button" @click="submitPayment">
        <text class="pay-text">立即支付</text>
        <text class="pay-icon">→</text>
      </button>
    </view>

    <!-- 支付成功弹窗 -->
    <view class="success-modal" v-if="showSuccess">
      <view class="modal-content">
        <view class="success-icon">✓</view>
        <text class="success-text">支付成功！</text>
        <text class="order-number">订单号: {{ orderId }}</text>
        <button class="confirm-button" @click="gotoOrderList">查看我的订单</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad , onUnload} from '@dcloudio/uni-app'; 
import { queryGetOrder } from "@/services/shopping/order.js"
import { queryGetAddress } from "@/services/shopping/address.js"

// 订单和商品信息
const orderId = ref('');
const goodsName = ref('');
const price = ref(0);
const num = ref(1);
const freight = ref(0);
const goodsImage = ref('');
const loading = ref(false);

// 地址信息
const address = ref(null);

// 新增：用于存储选中的地址
const selectedAddress = ref(null);

// 支付方式
const selectedMethod = ref('wechat');

// 支付成功弹窗状态
const showSuccess = ref(false);

; // 显式分号，解决解析歧义

// 页面加载时获取参数
onLoad((options)=>{
	// 解析URL参数（增加容错处理）
	if (options && options.id) { // 先判断options是否存在
	  orderId.value = options.id;
	} else {
	  console.warn('未接收到订单ID');
	  // 可选：跳转回上一页或显示错误提示
	  // uni.navigateBack();
	}
	console.log("options",options)
	if (options && options.goodsName) {
	
	  try {
	    goodsName.value = decodeURIComponent(options.goodsName);
	  } catch (e) {
	    console.error('商品名称解码失败：', e);
	    goodsName.value = '未知商品'; // 默认值
	  }
	} else {
	  goodsName.value = '未知商品'; // 默认值
	}
	// 只有当orderId存在时，才请求订单详情
	if (orderId.value) {
	  fetchOrderDetails(orderId.value);
	}
	fetchUserAddress();
	
	
	uni.$on('addressSelected', (selectedAddr) => {
	    address.value = selectedAddr; // 通过 .value 赋值，触发响应式
	  });
	
}) 

onUnload(() =>{
	// 2. 页面卸载时移除监听，避免内存泄漏
	uni.$off('addressSelected');
})

// 模拟获取订单详情
const fetchOrderDetails = async(id) => {
  // 实际项目中这里会调用接口
  price.value = 89.99;
  num.value = 1;
  freight.value = 10;
  // 根据商品名称设置对应的图片
  setGoodsImage(goodsName.value);
  
  try {
    loading.value = true // 显示加载状态
    // 传递分页参数
    const response = await queryGetOrder(id);
    // 假设后端返回格式为 { records: [], total: 0, ... }
	console.log("响应结果",response.data)
	price.value = response.data.price;
	num.value = response.data.num;
	freight.value = response.data.num;
	setGoodsImage(response.data.goodsUrl);
    // total.value = response.total
  } catch (error) {
    console.error('处理数据失败:', error)
  } finally {
    loading.value = false // 关闭加载状态
  }
  
  
};

// 根据商品名称设置图片
const setGoodsImage = (url) => {
  // if (name.includes('猫粮')) {
  //   goodsImage.value = 'https://picsum.photos/seed/catfood/300/300';
  // } else if (name.includes('狗粮')) {
  //   goodsImage.value = 'https://picsum.photos/seed/dogfood/300/300';
  // } else if (name.includes('玩具')) {
  //   goodsImage.value = 'https://picsum.photos/seed/pettoy/300/300';
  // } else {
  //   goodsImage.value = 'https://picsum.photos/seed/petproduct/300/300';
  // }
  goodsImage.value = url
};

// 模拟获取用户地址
const fetchUserAddress = async() => {
  // 实际项目中这里会调用接口获取用户地址列表
  address.value = {
    name: '小可爱',
    tel: '138****5678',
    address: '北京市朝阳区某某小区1号楼2单元301室'
  };
  
  try {
    loading.value = true // 显示加载状态
    // 传递分页参数
    const response = await queryGetAddress();
    address.value = response.data[1]
  	
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

// 选择地址
const chooseAddress = () => {
  // 实际项目中这里会跳转到地址选择页面
  uni.showToast({
    title: '跳转到地址选择',
    icon: 'none'
  });
  
  uni.navigateTo({
  	url: "/pages/address_selected/address_selected"
  })
};

// 提交支付
const submitPayment = () => {
  if (!address.value) {
    uni.showToast({
      title: '请添加收货地址',
      icon: 'none'
    });
    return;
  }
  
  // 模拟支付过程
  uni.showLoading({
    title: '支付中...'
  });
  
  setTimeout(() => {
    uni.hideLoading();
    // 显示支付成功弹窗
    showSuccess.value = true;
  }, 1500);
};

// 前往订单列表
const gotoOrderList = () => {
  showSuccess.value = false;
  // 跳转到订单列表页面
  uni.navigateTo({
    url: '/pages/order_list/order_list'
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

/* 通用卡片样式 */
.order-card, .address-card, .payment-card, .payment-method-card {
  background-color: white;
  border-radius: 24rpx;
  padding: 24rpx;
  margin: 20rpx;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05);
}
	
.payment-method-card {
	margin-bottom: 180rpx;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1px solid #F5F5F5;
}

.title-text {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

/* 订单卡片 */
.order-status {
  font-size: 26rpx;
  color: #FF4D6D;
  background-color: #FFF0F3;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.goods-info {
  display: flex;
  align-items: flex-start;
}

.goods-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  object-fit: cover;
  margin-right: 20rpx;
}

.goods-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.goods-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-price {
  font-size: 30rpx;
  color: #FF4D6D;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.goods-quantity {
  font-size: 26rpx;
  color: #999;
}

/* 地址卡片 */
.edit-icon {
  font-size: 28rpx;
  color: #FF85A2;
}

.address-info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.recipient {
  font-size: 28rpx;
  color: #333;
}

.address-detail {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.no-address {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 30rpx 0;
}

.add-address-text {
  font-size: 28rpx;
  color: #FF85A2;
  border: 2rpx dashed #FF85A2;
  padding: 10rpx 30rpx;
  border-radius: 30rpx;
}

/* 支付信息卡片 */
.payment-detail {
  display: flex;
  flex-direction: column;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 0;
  font-size: 28rpx;
}

.payment-item.total {
  margin-top: 10rpx;
  padding-top: 16rpx;
  border-top: 1px dashed #F5F5F5;
}

.label {
  color: #666;
}

.value {
  color: #333;
  font-weight: 500;
}

.payment-item.total .value {
  color: #FF4D6D;
  font-weight: bold;
  font-size: 30rpx;
}

/* 支付方式卡片 */
.method-options {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  border-radius: 16rpx;
  border: 2rpx solid #F5F5F5;
  transition: all 0.3s ease;
}

.method-item.selected {
  border-color: #FF85A2;
  background-color: #FFF0F3;
}

.method-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
  width: 40rpx;
  text-align: center;
}

.method-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.check-icon {
  font-size: 30rpx;
  color: #FF85A2;
  font-weight: bold;
}

/* 底部支付按钮 */
.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background-color: white;
  border-top: 1px solid #F5F5F5;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
}

.total-amount {
  display: flex;
  align-items: center;
}

.amount-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 10rpx;
}

.amount-value {
  font-size: 32rpx;
  color: #FF4D6D;
  font-weight: bold;
}

.pay-button {
  background-color: #FF85A2;
  color: white;
  border: none;
  border-radius: 40rpx;
  padding: 18rpx 40rpx;
  font-size: 30rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 10rpx rgba(255, 133, 162, 0.3);
  transition: all 0.3s ease;
}

.pay-button:active {
  background-color: #FF6B8B;
  transform: scale(0.98);
}

.pay-text {
  margin-right: 8rpx;
}

.pay-icon {
  font-size: 26rpx;
}

/* 支付成功弹窗 */
.success-modal {
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
  align-items: center;
  animation: popIn 0.3s ease;
}

@keyframes popIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-icon {
  width: 120rpx;
  height: 120rpx;
  background-color: #4CD964;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 70rpx;
  margin-bottom: 30rpx;
}

.success-text {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.order-number {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 40rpx;
  word-break: break-all;
  text-align: center;
}

.confirm-button {
  width: 100%;
  background-color: #FF85A2;
  color: white;
  border: none;
  border-radius: 30rpx;
  padding: 20rpx 0;
  font-size: 30rpx;
  font-weight: bold;
}
</style>
    