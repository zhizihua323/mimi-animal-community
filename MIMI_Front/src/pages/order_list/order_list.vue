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
import { onLoad } from '@dcloudio/uni-app'; 
import { listPostOrder } from '@/services/shopping/order.js'

// 订单列表数据
const orderList = ref([]);
// 选中的标签页
const activeTab = ref('all');
// 定时器集合（用于多订单分别计时）
const timers = ref({});
const loading = ref(false)

// 页面加载时获取订单数据
onLoad(() => {
  fetchOrders();
});

// 页面卸载时清除所有定时器
onUnmounted(() => {
  Object.values(timers.value).forEach(timer => {
    clearInterval(timer);
  });
});

// 获取订单列表数据
const fetchOrders = async() => {
  // 模拟从接口获取数据
  orderList.value = [
    {
      id: 1962863412305817600,
      stringId: '1962863412305817600',
      userId: 1961677606954274800,
      userOpenid: 'o6_bmjrPTlm6_2sgVt7hMZOPfL2M',
      userAddress: '北京市朝阳区某某小区',
      status: 0, // 0-待付款, 1-已付款, 2-已发货, 3-已完成, 4-已取消
      orderTime: new Date(Date.now() - 30 * 1000).toISOString(), // 30秒前下单
      payTime: '',
      deliverTime: '',
      receivedTime: '',
      goodsName: '天然无谷狗粮 2kg',
      price: 129.99,
      num: 1,
      freight: 8,
      tel: '138****5678',
      goodsId: 'g1001',
      goodsUrl: 'https://picsum.photos/seed/dogfood/300/300',
      isExpired: false,
      remainingSeconds: 30 // 剩余30秒
    },
    {
      id: 1962863412305817601,
      stringId: '1962863412305817601',
      userId: 1961677606954274800,
      userOpenid: 'o6_bmjrPTlm6_2sgVt7hMZOPfL2M',
      userAddress: '上海市浦东新区某某街道',
      status: 0,
      orderTime: new Date(Date.now() - 55 * 1000).toISOString(), // 55秒前下单
      payTime: '',
      deliverTime: '',
      receivedTime: '',
      goodsName: '猫咪益智玩具套装',
      price: 45.50,
      num: 2,
      freight: 6,
      tel: '139****8765',
      goodsId: 'g1002',
      goodsUrl: 'https://picsum.photos/seed/pettoy/300/300',
      isExpired: false,
      remainingSeconds: 5 // 剩余5秒（即将过期）
    },
    {
      id: 1962863412305817602,
      stringId: '1962863412305817602',
      userId: 1961677606954274800,
      userOpenid: 'o6_bmjrPTlm6_2sgVt7hMZOPfL2M',
      userAddress: '广州市天河区某某路',
      status: 0,
      orderTime: new Date(Date.now() - 70 * 1000).toISOString(), // 70秒前下单
      payTime: '',
      deliverTime: '',
      receivedTime: '',
      goodsName: '宠物自动饮水机',
      price: 89.00,
      num: 1,
      freight: 10,
      tel: '137****4321',
      goodsId: 'g1003',
      goodsUrl: 'https://picsum.photos/seed/petproduct/300/300',
      isExpired: true,
      remainingSeconds: 0 // 已过期
    },
    {
      id: 1962863412305817603,
      stringId: '1962863412305817603',
      userId: 1961677606954274800,
      userOpenid: 'o6_bmjrPTlm6_2sgVt7hMZOPfL2M',
      userAddress: '深圳市南山区某某大道',
      status: 1,
      orderTime: new Date(Date.now() - 3600 * 1000).toISOString(), // 1小时前下单
      payTime: new Date(Date.now() - 3540 * 1000).toISOString(),
      deliverTime: '',
      receivedTime: '',
      goodsName: '宠物指甲剪套装',
      price: 29.90,
      num: 1,
      freight: 5,
      tel: '136****9876',
      goodsId: 'g1004',
      goodsUrl: 'https://picsum.photos/seed/pettoy/300/300',
      isExpired: false,
      remainingSeconds: 0
    }
  ];
  
  
  try {
      loading.value = true; // 显示加载状态
      // 1. 调用接口获取真实数据（删除模拟数据覆盖逻辑）
      const response = await listPostOrder();
      // 2. 拿到接口返回的原始数据（response.data 是你的订单数组）
      const rawOrderList = response.data; 
      
      // 3. 关键：对每个订单进行时间解析和倒计时计算（核心修复点）
      orderList.value = rawOrderList.map(order => {
        // 3.1 解析后端返回的 orderTime（格式："2025-09-04 13:44:22"）
        const orderDate = parseBackendTime(order.orderTime);
        
        // 3.2 时间解析失败的降级处理（避免 NaN）
        if (!orderDate) {
          return { 
            ...order, 
            isExpired: true,    // 标记为已过期
            remainingSeconds: 0 // 剩余时间设为 0
          };
        }
        
        // 3.3 计算过期时间（下单时间 + 1分钟，根据业务需求调整）
        const expiryTime = new Date(orderDate.getTime() + 60 * 1000);
        // 3.4 计算当前剩余秒数（当前时间 - 过期时间）
        const now = new Date();
        const remainingSeconds = Math.floor((expiryTime - now) / 1000);
        
        // 3.5 返回处理后的订单数据（新增 isExpired 和 remainingSeconds 字段）
        return {
          ...order,
          isExpired: remainingSeconds <= 0, // 剩余时间<=0 标记为已过期
          remainingSeconds: remainingSeconds > 0 ? remainingSeconds : 0 // 确保非负
        };
      });
      
      // 4. 启动所有待付款订单的倒计时
      startCountdowns();
      
    } catch (error) {
      console.error('获取订单列表失败:', error);
      // 异常处理：显示空状态或错误提示
      orderList.value = [];
      uni.showToast({ title: '获取订单失败', icon: 'none' });
    } finally {
      loading.value = false; // 关闭加载状态
    }


  // 为每个待付款订单启动倒计时
  startCountdowns();
};

// 工具函数：解析后端时间字符串为 Date 对象（保持不变，但确保调用）
const parseBackendTime = (timeStr) => {
  if (!timeStr) return null;
  // 关键：将 "2025-09-04 13:44:22" 转为 Date 可识别的 "2025-09-04T13:44:22"
  const isoTimeStr = timeStr.replace(' ', 'T');
  const date = new Date(isoTimeStr);
  // 验证解析结果：如果是 Invalid Date，返回 null
  return isNaN(date.getTime()) ? null : date;
};

// 格式化倒计时时间（增加异常处理，避免 NaN）
// const formatTime = (seconds) => {
//   // 关键：处理 undefined、null 或非数字的情况
//   if (isNaN(seconds) || seconds < 0) {
//     return "00:00";
//   }
//   const mins = Math.floor(seconds / 60);
//   const secs = seconds % 60;
//   // 确保数字补零（如 5 → "05"）
//   return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
// };

// 启动所有待付款订单的倒计时
const startCountdowns = () => {
  orderList.value.forEach((order, index) => {
    if (order.status === 0 && !order.isExpired) {
      // 清除已有定时器
      if (timers.value[index]) {
        clearInterval(timers.value[index]);
      }

      // 创建新定时器
      timers.value[index] = setInterval(() => {
        orderList.value[index].remainingSeconds--;
        
        // 倒计时结束
        if (orderList.value[index].remainingSeconds <= 0) {
          orderList.value[index].remainingSeconds = 0;
          orderList.value[index].isExpired = true;
          clearInterval(timers.value[index]);
          // 可以在这里调用接口更新订单状态
        }
      }, 1000);
    }
  });
};

// 根据标签筛选订单
const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orderList.value;
  
  return orderList.value.filter(order => {
    if (activeTab.value === 'pending') {
      return order.status === 0; // 待付款（包括已过期）
    }
    if (activeTab.value === 'paid') {
      return order.status === 1 || order.status === 2; // 已付款和已发货
    }
    if (activeTab.value === 'completed') {
      return order.status === 3; // 已完成
    }
    return true;
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
  if (order.status === 0) {
    return order.isExpired ? '已过期' : '待付款';
  }
  if (order.status === 1) return '已付款';
  if (order.status === 2) return '已发货';
  if (order.status === 3) return '已完成';
  if (order.status === 4) return '已取消';
  return '未知状态';
};

// 获取订单状态样式
const getStatusClass = (order) => {
  if (order.status === 0) {
    return order.isExpired ? 'status-expired' : 'status-pending';
  }
  if (order.status === 1) return 'status-paid';
  if (order.status === 2) return 'status-shipped';
  if (order.status === 3) return 'status-completed';
  if (order.status === 4) return 'status-cancelled';
  return '';
};

// 取消订单
const cancelOrder = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: (res) => {
      if (res.confirm) {
        // 取消定时器
        if (timers.value[index]) {
          clearInterval(timers.value[index]);
        }
        // 更新订单状态
        orderList.value[index].status = 4;
        uni.showToast({
          title: '订单已取消',
          icon: 'none'
        });
      }
    }
  });
};

// 重新下单
const reorder = (order) => {
  // 跳转到商品详情页
  uni.navigateTo({
    url: `/pages/goods_detail/goods_detail?id=${order.goodsId}`
  });
};

// 前往支付页面
const gotoPayment = (order) => {
  uni.navigateTo({
    url: `/pages/shopping_pay/shopping_pay?id=${order.stringId}&goodsName=${encodeURIComponent(order.goodsName)}`
  });
};

// 前往订单详情
const gotoDetail = (order) => {
  uni.navigateTo({
    url: `/pages/order_detail/order_detail?id=${order.stringId}`
  });
};

// 查看物流
const trackOrder = (order) => {
  uni.navigateTo({
    url: `/pages/logistics/logistics?id=${order.stringId}`
  });
};

// 评价订单
const reviewOrder = (order) => {
  uni.navigateTo({
    url: `/pages/review/review?id=${order.stringId}`
  });
};

// 返回上一页
const navigateBack = () => {
  uni.navigateBack({
    delta: 1
  });
};

// 去首页
const gotoHome = () => {
  uni.switchTab({
    url: '/pages/index/index'
  });
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
    