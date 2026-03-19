<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="back-icon" @click="navigateBack">←</view>
      <text class="navbar-title">我的订单</text>
      <view class="empty-space"></view>
    </view>

    <!-- 订单状态筛选 -->
    <view class="status-tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'all' }" 
        @click="activeTab = 'all'"
      >
        全部
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'pending' }" 
        @click="activeTab = 'pending'"
      >
        待付款
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'paid' }" 
        @click="activeTab = 'paid'"
      >
        已付款
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'completed' }" 
        @click="activeTab = 'completed'"
      >
        已完成
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="order-container">
      <!-- 订单项 -->
      <view 
        class="order-item" 
        v-for="(order, index) in filteredOrders" 
        :key="order.id"
      >
        <!-- 订单头部信息 -->
        <view class="order-header">
          <text class="order-number">订单号: {{ order.stringId }}</text>
          <view class="order-status">
            <text :class="getStatusClass(order)">{{ getStatusText(order) }}</text>
            
            <!-- 倒计时显示：仅待付款且未过期的订单显示 -->
            <text 
              class="countdown" 
              v-if="order.status === 0 && !order.isExpired"
              :class="{ warning: order.remainingSeconds < 10 }"
            >
              剩余{{ formatTime(order.remainingSeconds) }}
            </text>
          </view>
        </view>

        <!-- 商品信息 -->
        <view class="order-goods" @click="gotoDetail(order)">
          <image 
            :src="order.goodsUrl" 
            mode="widthFix" 
            class="goods-image"
          ></image>
          <view class="goods-info">
            <text class="goods-name">{{ order.goodsName }}</text>
            <text class="goods-price">¥{{ order.price.toFixed(2) }}</text>
            <text class="goods-quantity">x{{ order.num }}</text>
          </view>
        </view>

        <!-- 订单金额 -->
        <view class="order-amount">
          <text class="amount-label">合计: </text>
          <text class="amount-value">¥{{ (order.price * order.num + order.freight).toFixed(2) }}</text>
        </view>

        <!-- 操作按钮 -->
        <view class="order-actions">
          <template v-if="order.status === 0">
            <!-- 待付款状态 -->
            <template v-if="!order.isExpired">
              <button 
                class="action-btn cancel-btn" 
                @click="cancelOrder(index)"
              >
                取消订单
              </button>
              <button 
                class="action-btn pay-btn" 
                @click="gotoPayment(order)"
              >
                立即支付
              </button>
            </template>
            <template v-else>
              <!-- 已过期状态 -->
              <button class="action-btn reorder-btn" @click="reorder(order)">
                重新下单
              </button>
            </template>
          </template>
          
          <template v-else-if="order.status === 1">
            <!-- 已付款状态 -->
            <button class="action-btn track-btn" @click="trackOrder(order)">
              查看物流
            </button>
          </template>
          
          <template v-else-if="order.status === 3">
            <!-- 已完成状态 -->
            <button class="action-btn review-btn" @click="reviewOrder(order)">
              评价
            </button>
          </template>
          
          <template v-else>
            <!-- 其他状态 -->
            <button class="action-btn detail-btn" @click="gotoDetail(order)">
              详情
            </button>
          </template>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredOrders.length === 0">
        <image 
          src="https://picsum.photos/seed/emptyorder/200/200" 
          mode="widthFix" 
          class="empty-image"
        ></image>
        <text class="empty-text">暂无订单</text>
        <button class="empty-btn" @click="gotoHome">去逛逛</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onUnmounted, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app'; 
import { listPostOrder } from '@/services/shopping/order.js'

// 订单列表数据
const orderList = ref([]);
// 选中的标签页
const activeTab = ref('all');
// 定时器集合（用于多订单分别计时）
const timers = ref({});
const loading = ref(false);

// 页面加载和每次显示时，都重新获取最新订单
onShow(() => {
  fetchOrders();
});

// 页面卸载时清除所有定时器
onUnmounted(() => {
  Object.values(timers.value).forEach(timer => {
    clearInterval(timer);
  });
});

// 获取真实的订单列表数据
const fetchOrders = async() => {
  try {
    loading.value = true;
    
    // 1. 调用接口获取真实数据
    const response = await listPostOrder();
    
    // 兼容后端返回格式 (可能是 records 数组，也可能是直接的数组)
    const rawOrderList = response.data?.records || response.data || []; 
    
    // 2. 数据清洗与倒计时计算
    orderList.value = rawOrderList.map(order => {
      const orderDate = parseBackendTime(order.orderTime || order.createTime); // 兼容时间字段名
      
      if (!orderDate) {
        return { 
          ...order, 
          isExpired: true,    
          remainingSeconds: 0 
        };
      }
      
      // 假设订单 30 分钟未支付即过期 (30 * 60 * 1000)
      const expiryTime = new Date(orderDate.getTime() + 30 * 60 * 1000);
      const now = new Date();
      const remainingSeconds = Math.floor((expiryTime - now) / 1000);
      
      return {
        ...order,
        // 如果后端状态本身就是取消(4)，或者时间到了且还未付款(0)，则标记为过期
        isExpired: order.status === 4 || (order.status === 0 && remainingSeconds <= 0),
        remainingSeconds: remainingSeconds > 0 ? remainingSeconds : 0
      };
    });
    
    // 3. 按照下单时间倒序排列（最新的订单在最上面）
    orderList.value.sort((a, b) => {
       const timeA = parseBackendTime(a.orderTime || a.createTime)?.getTime() || 0;
       const timeB = parseBackendTime(b.orderTime || b.createTime)?.getTime() || 0;
       return timeB - timeA;
    });
    
    // 4. 启动待付款订单的倒计时
    startCountdowns();
    
  } catch (error) {
    console.error('获取订单列表失败:', error);
    orderList.value = [];
  } finally {
    loading.value = false;
  }
};

// 解析后端时间字符串为 Date 对象
const parseBackendTime = (timeStr) => {
  if (!timeStr) return null;
  // 兼容带有 'T' 或者 ' ' 的日期格式
  const isoTimeStr = timeStr.replace(' ', 'T');
  const date = new Date(isoTimeStr);
  return isNaN(date.getTime()) ? null : date;
};

// 启动待付款订单的倒计时
const startCountdowns = () => {
  orderList.value.forEach((order, index) => {
    if (order.status === 0 && !order.isExpired) {
      if (timers.value[index]) {
        clearInterval(timers.value[index]);
      }

      timers.value[index] = setInterval(() => {
        orderList.value[index].remainingSeconds--;
        
        if (orderList.value[index].remainingSeconds <= 0) {
          orderList.value[index].remainingSeconds = 0;
          orderList.value[index].isExpired = true;
          // 可选：这里可以悄悄发个请求告诉后端订单过期了
          clearInterval(timers.value[index]);
        }
      }, 1000);
    }
  });
};

// 根据标签筛选订单 (核心状态映射)
const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orderList.value;
  
  return orderList.value.filter(order => {
    if (activeTab.value === 'pending') {
      return order.status === 0 && !order.isExpired; // 待付款（未过期）
    }
    if (activeTab.value === 'paid') {
      return order.status === 1 || order.status === 2; // 已付款/待发货 和 已发货
    }
    if (activeTab.value === 'completed') {
      return order.status === 3; // 已完成
    }
    return false;
  });
});

// 格式化倒计时时间（秒 -> MM:SS）
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 获取订单状态文本
const getStatusText = (order) => {
  if (order.status === 0) return order.isExpired ? '已取消/过期' : '待付款';
  if (order.status === 1) return '已付款/待发货';
  if (order.status === 2) return '已发货';
  if (order.status === 3) return '已完成';
  if (order.status === 4) return '已取消';
  return '未知状态';
};

// 获取订单状态样式
const getStatusClass = (order) => {
  if (order.status === 0) return order.isExpired ? 'status-expired' : 'status-pending';
  if (order.status === 1) return 'status-paid';
  if (order.status === 2) return 'status-shipped';
  if (order.status === 3) return 'status-completed';
  if (order.status === 4) return 'status-cancelled';
  return '';
};

// 取消订单 (仅前端隐藏，实际应该请求后端修改状态)
const cancelOrder = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消此订单吗？',
    success: (res) => {
      if (res.confirm) {
        if (timers.value[index]) clearInterval(timers.value[index]);
        orderList.value[index].status = 4;
        orderList.value[index].isExpired = true;
        uni.showToast({ title: '订单已取消', icon: 'none' });
      }
    }
  });
};

// 重新下单 -> 跳转回商城
const reorder = (order) => {
  uni.switchTab({
    url: '/pages/cart/cart' // 假设商城页是 tabBar
  });
};

// 待付款订单 -> 继续去支付
const gotoPayment = (order) => {
  uni.navigateTo({
    url: `/pages/shopping_pay/shopping_pay?id=${order.stringId || order.id}&goodsName=${encodeURIComponent(order.goodsName)}`
  });
};

// 空白占位点击
const gotoDetail = () => { uni.showToast({ title: '详情开发中', icon: 'none' }) };
const trackOrder = () => { uni.showToast({ title: '物流查询开发中', icon: 'none' }) };
const reviewOrder = () => { uni.showToast({ title: '评价功能开发中', icon: 'none' }) };

// 返回上一页
const navigateBack = () => {
  uni.navigateBack({ delta: 1 });
};

// 去逛逛
const gotoHome = () => {
  uni.switchTab({ url: '/pages/cart/cart' });
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #FFF5F7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

/* 顶部导航 */
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

/* 状态标签页 */
.status-tabs {
  display: flex;
  background-color: white;
  border-bottom: 1px solid #F5F5F5;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: #FF85A2;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 4rpx;
  background-color: #FF85A2;
}

/* 订单容器 */
.order-container {
  padding-bottom: 120rpx;
}

/* 订单项 */
.order-item {
  background-color: white;
  border-radius: 24rpx;
  margin: 20rpx;
  padding: 24rpx;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.order-number {
  font-size: 26rpx;
  color: #999;
}

.order-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.status-pending {
  color: #FF4D6D;
  background-color: #FFF0F3;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
}

.status-expired {
  color: #9E9E9E;
  background-color: #F5F5F5;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
}

.status-paid {
  color: #4CAF50;
  background-color: #E8F5E9;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
}

.status-shipped {
  color: #2196F3;
  background-color: #E3F2FD;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
}

.status-completed {
  color: #9C27B0;
  background-color: #F3E5F5;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
}

.status-cancelled {
  color: #607D8B;
  background-color: #ECEFF1;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
}

/* 倒计时样式 */
.countdown {
  font-size: 24rpx;
  color: #FF4D6D;
  font-weight: bold;
}

.countdown.warning {
  color: #FF9800;
  animation: flash 1s infinite alternate;
}

@keyframes flash {
  from { opacity: 1; }
  to { opacity: 0.6; }
}

/* 商品信息 */
.order-goods {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20rpx;
  cursor: pointer;
}

.goods-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  object-fit: cover;
  margin-right: 20rpx;
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.goods-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-price {
  font-size: 28rpx;
  color: #FF4D6D;
  font-weight: bold;
  margin-bottom: 5rpx;
}

.goods-quantity {
  font-size: 26rpx;
  color: #999;
}

/* 订单金额 */
.order-amount {
  display: flex;
  justify-content: flex-end;
  padding: 15rpx 0;
  border-top: 1px dashed #F5F5F5;
  margin-bottom: 20rpx;
}

.amount-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 10rpx;
}

.amount-value {
  font-size: 30rpx;
  color: #FF4D6D;
  font-weight: bold;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15rpx;
}

.action-btn {
  padding: 12rpx 30rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  border: none;
}

.cancel-btn {
  background-color: #F5F5F5;
  color: #666;
}

.pay-btn {
  background-color: #FF85A2;
  color: white;
}

.reorder-btn {
  background-color: #FFF0F3;
  color: #FF85A2;
}

.track-btn, .review-btn, .detail-btn {
  background-color: #F0F7FF;
  color: #2196F3;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 150rpx;
  padding: 0 20rpx;
}

.empty-image {
  width: 240rpx;
  height: 240rpx;
  margin-bottom: 40rpx;
  opacity: 0.7;
}

.empty-text {
  font-size: 30rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.empty-btn {
  background-color: #FF85A2;
  color: white;
  border: none;
  border-radius: 40rpx;
  padding: 18rpx 40rpx;
  font-size: 28rpx;
}
</style>
    