<template>
  <view class="container">
    <view class="navbar">
      <view class="back-icon" @click="navigateBack">←</view>
      <text class="navbar-title">我的交易</text> <view class="empty-space"></view>
    </view>

    <view class="main-tabs">
      <view class="main-tab-item" :class="{ active: mainTab === 0 }" @click="switchMainTab(0)">
        <text>我买到的</text>
        <view class="line" v-if="mainTab === 0"></view>
      </view>
      <view class="main-tab-item" :class="{ active: mainTab === 1 }" @click="switchMainTab(1)">
        <text>我发布的</text>
        <view class="line" v-if="mainTab === 1"></view>
      </view>
    </view>

    <view v-if="mainTab === 0" class="tab-content-area">
      <view class="status-tabs">
        <view class="tab-item" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">全部</view>
        <view class="tab-item" :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'">待付款</view>
        <view class="tab-item" :class="{ active: activeTab === 'paid' }" @click="activeTab = 'paid'">已付款</view>
        <view class="tab-item" :class="{ active: activeTab === 'completed' }" @click="activeTab = 'completed'">已完成</view>
      </view>

      <scroll-view scroll-y class="order-container">
        <view class="order-item" v-for="(order, index) in filteredOrders" :key="order.id">
          <view class="order-header">
            <text class="order-number">订单号: {{ order.stringId }}</text>
            <view class="order-status">
              <text :class="getStatusClass(order)">{{ getStatusText(order) }}</text>
              <text class="countdown" v-if="order.status === 0 && !order.isExpired" :class="{ warning: order.remainingSeconds < 10 }">
                剩余{{ formatTime(order.remainingSeconds) }}
              </text>
            </view>
          </view>

          <view class="order-goods" @click="gotoDetail(order)">
            <image :src="order.goodsUrl" mode="widthFix" class="goods-image"></image>
            <view class="goods-info">
              <text class="goods-name">{{ order.goodsName }}</text>
              <text class="goods-price">¥{{ order.price.toFixed(2) }}</text>
              <text class="goods-quantity">x{{ order.num }}</text>
            </view>
          </view>

          <view class="order-amount">
            <text class="amount-label">合计: </text>
            <text class="amount-value">¥{{ (order.price * order.num + order.freight).toFixed(2) }}</text>
          </view>

          <view class="order-actions">
            <template v-if="order.status === 0">
              <template v-if="!order.isExpired">
                <button class="action-btn cancel-btn" @click="cancelOrder(index)">取消订单</button>
                <button class="action-btn pay-btn" @click="gotoPayment(order)">立即支付</button>
              </template>
              <template v-else>
                <button class="action-btn reorder-btn" @click="reorder(order)">重新下单</button>
              </template>
            </template>
            <template v-else-if="order.status === 1">
              <button class="action-btn track-btn" @click="trackOrder(order)">查看物流</button>
            </template>
            <template v-else-if="order.status === 3">
              <button class="action-btn review-btn" @click="reviewOrder(order)">评价</button>
            </template>
            <template v-else>
              <button class="action-btn detail-btn" @click="gotoDetail(order)">详情</button>
            </template>
          </view>
        </view>

        <view class="empty-state" v-if="filteredOrders.length === 0">
          <image src="https://picsum.photos/seed/emptyorder/200/200" mode="widthFix" class="empty-image"></image>
          <text class="empty-text">暂无订单</text>
          <button class="empty-btn" @click="gotoHome">去逛逛</button>
        </view>
      </scroll-view>
    </view>

    <view v-if="mainTab === 1" class="tab-content-area publish-area">
      <scroll-view scroll-y class="order-container">
        <view v-if="myPublishList.length > 0">
          <view class="order-item publish-item" v-for="item in myPublishList" :key="item.id">
            <view class="order-goods no-border">
              <image :src="item.picUrl" mode="aspectFill" class="goods-image"></image>
              <view class="goods-info publish-info">
                <text class="goods-name">{{ item.goodsName }}</text>
                <text class="publish-desc">{{ item.material || '无描述' }}</text>
                <view class="publish-bottom">
                  <text class="goods-price">¥{{ item.price }}</text>
                  <button class="action-btn cancel-btn small-btn" @click="deletePublish(item.id)">下架</button>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <view class="empty-state" v-else>
          <text class="empty-icon" style="font-size: 100rpx; margin-bottom: 20rpx;">📦</text>
          <text class="empty-text">你还没有发布过闲置哦~</text>
          <button class="empty-btn" @click="goToPublish">去发布</button>
        </view>
      </scroll-view>
    </view>

  </view>
</template>

<script setup>
import { ref, onUnmounted, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app'; 
import { listPostOrder } from '@/services/shopping/order.js';

// 🌟 新增：顶级 Tab 状态
const mainTab = ref(0); // 0:我买到的, 1:我发布的
const myPublishList = ref([]); // 存放我发布的商品数据

// 原有订单变量
const orderList = ref([]);
const activeTab = ref('all');
const timers = ref({});
const loading = ref(false);

// 每次显示时获取数据
onShow(() => {
  if (mainTab.value === 0) {
    fetchOrders();
  } else {
    fetchMyPublish();
  }
});

// 页面卸载时清除所有定时器
onUnmounted(() => {
  Object.values(timers.value).forEach(timer => {
    clearInterval(timer);
  });
});

// 🌟 切换顶级 Tab
const switchMainTab = (index) => {
  mainTab.value = index;
  if (index === 0) {
    fetchOrders();
  } else {
    fetchMyPublish();
  }
};

// ==================== 我发布的 相关逻辑 ====================
const fetchMyPublish = () => {
  uni.request({
    url: 'http://127.0.0.1:8080/shopping/goods/myPublish', // 确认你的后端地址
    method: 'GET',
    success: (res) => {
      myPublishList.value = res.data.data || res.data || []; 
    },
    fail: (err) => {
      console.error('获取发布列表失败', err);
    }
  });
};

const goToPublish = () => {
  uni.navigateTo({ url: '/pages/goods_add/goods_add' });
};

// ==================== 订单相关原有逻辑 ====================
const fetchOrders = async() => {
  try {
    loading.value = true;
    const response = await listPostOrder();
    const rawOrderList = response.data?.records || response.data || []; 
    
    orderList.value = rawOrderList.map(order => {
      const orderDate = parseBackendTime(order.orderTime || order.createTime);
      if (!orderDate) {
        return { ...order, isExpired: true, remainingSeconds: 0 };
      }
      const expiryTime = new Date(orderDate.getTime() + 30 * 60 * 1000);
      const now = new Date();
      const remainingSeconds = Math.floor((expiryTime - now) / 1000);
      return {
        ...order,
        isExpired: order.status === 4 || (order.status === 0 && remainingSeconds <= 0),
        remainingSeconds: remainingSeconds > 0 ? remainingSeconds : 0
      };
    });
    
    orderList.value.sort((a, b) => {
       const timeA = parseBackendTime(a.orderTime || a.createTime)?.getTime() || 0;
       const timeB = parseBackendTime(b.orderTime || b.createTime)?.getTime() || 0;
       return timeB - timeA;
    });
    
    startCountdowns();
  } catch (error) {
    console.error('获取订单列表失败:', error);
    orderList.value = [];
  } finally {
    loading.value = false;
  }
};

const parseBackendTime = (timeStr) => {
  if (!timeStr) return null;
  const isoTimeStr = timeStr.replace(' ', 'T');
  const date = new Date(isoTimeStr);
  return isNaN(date.getTime()) ? null : date;
};

const startCountdowns = () => {
  orderList.value.forEach((order, index) => {
    if (order.status === 0 && !order.isExpired) {
      if (timers.value[index]) clearInterval(timers.value[index]);
      timers.value[index] = setInterval(() => {
        orderList.value[index].remainingSeconds--;
        if (orderList.value[index].remainingSeconds <= 0) {
          orderList.value[index].remainingSeconds = 0;
          orderList.value[index].isExpired = true;
          clearInterval(timers.value[index]);
        }
      }, 1000);
    }
  });
};

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orderList.value;
  return orderList.value.filter(order => {
    if (activeTab.value === 'pending') return order.status === 0 && !order.isExpired;
    if (activeTab.value === 'paid') return order.status === 1 || order.status === 2;
    if (activeTab.value === 'completed') return order.status === 3;
    return false;
  });
});

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

const getStatusText = (order) => {
  if (order.status === 0) return order.isExpired ? '已取消/过期' : '待付款';
  if (order.status === 1) return '已付款/待发货';
  if (order.status === 2) return '已发货';
  if (order.status === 3) return '已完成';
  if (order.status === 4) return '已取消';
  return '未知状态';
};

const getStatusClass = (order) => {
  if (order.status === 0) return order.isExpired ? 'status-expired' : 'status-pending';
  if (order.status === 1) return 'status-paid';
  if (order.status === 2) return 'status-shipped';
  if (order.status === 3) return 'status-completed';
  if (order.status === 4) return 'status-cancelled';
  return '';
};

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

const reorder = (order) => { uni.switchTab({ url: '/pages/cart/cart' }); };
const gotoPayment = (order) => { uni.navigateTo({ url: `/pages/shopping_pay/shopping_pay?id=${order.stringId || order.id}&goodsName=${encodeURIComponent(order.goodsName)}` }); };
const gotoDetail = () => { uni.showToast({ title: '详情开发中', icon: 'none' }) };
const trackOrder = () => { uni.showToast({ title: '物流查询开发中', icon: 'none' }) };
const reviewOrder = () => { uni.showToast({ title: '评价功能开发中', icon: 'none' }) };
const navigateBack = () => { uni.navigateBack({ delta: 1 }); };
const gotoHome = () => { uni.switchTab({ url: '/pages/cart/cart' }); };
// 🌟 新增：删除/下架我发布的闲置
const deletePublish = (goodsId) => {
  uni.showModal({
    title: '温馨提示',
    content: '确定要下架并删除该宝贝吗？',
    confirmColor: '#FF4D6D',
    success: (res) => {
      if (res.confirm) {
        uni.request({
          // 确认你的后端地址和端口
          url: `http://127.0.0.1:8080/shopping/goods/delete/${goodsId}`, 
          method: 'DELETE',
          success: (apiRes) => {
            uni.showToast({ title: '下架成功', icon: 'success' });
            // 重新拉取列表刷新页面
            fetchMyPublish(); 
          },
          fail: () => {
            uni.showToast({ title: '操作失败', icon: 'none' });
          }
        });
      }
    }
  });
};
</script>

<style scoped>
/* =========== 原有样式保留并补充 =========== */
.container { min-height: 100vh; background-color: #FFF5F7; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; display: flex; flex-direction: column; }
.navbar { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 24rpx; background-color: #FF85A2; color: white; position: relative; }
.back-icon { font-size: 36rpx; width: 40rpx; }
.navbar-title { font-size: 34rpx; font-weight: bold; position: absolute; left: 50%; transform: translateX(-50%); }
.empty-space { width: 40rpx; }

/* 🌟 新增：顶级 Tab 样式 */
.main-tabs { display: flex; background-color: white; height: 100rpx; align-items: center; box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.02); z-index: 10; }
.main-tab-item { flex: 1; text-align: center; font-size: 30rpx; color: #666; position: relative; height: 100%; line-height: 100rpx; transition: all 0.3s;}
.main-tab-item.active { color: #FF85A2; font-weight: bold; font-size: 32rpx;}
.line { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40rpx; height: 6rpx; background-color: #FF85A2; border-radius: 4rpx; }

.tab-content-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

/* 状态标签页 (二级Tab) */
.status-tabs { display: flex; background-color: white; border-bottom: 1px solid #F5F5F5; }
.tab-item { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #666; position: relative; }
.tab-item.active { color: #FF85A2; font-weight: bold; }
.tab-item.active::after { content: ''; position: absolute; bottom: 0; left: 0; width: 100%; height: 4rpx; background-color: #FF85A2; }

/* 订单容器 */
.order-container { flex: 1; height: 0; }

.order-item { background-color: white; border-radius: 24rpx; margin: 20rpx; padding: 24rpx; box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05); }
.order-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20rpx; }
.order-number { font-size: 26rpx; color: #999; }
.order-status { display: flex; flex-direction: column; align-items: flex-end; gap: 8rpx; }
.status-pending { color: #FF4D6D; background-color: #FFF0F3; padding: 4rpx 12rpx; border-radius: 16rpx; font-size: 26rpx; }
.status-expired { color: #9E9E9E; background-color: #F5F5F5; padding: 4rpx 12rpx; border-radius: 16rpx; font-size: 26rpx; }
.status-paid { color: #4CAF50; background-color: #E8F5E9; padding: 4rpx 12rpx; border-radius: 16rpx; font-size: 26rpx; }
.status-shipped { color: #2196F3; background-color: #E3F2FD; padding: 4rpx 12rpx; border-radius: 16rpx; font-size: 26rpx; }
.status-completed { color: #9C27B0; background-color: #F3E5F5; padding: 4rpx 12rpx; border-radius: 16rpx; font-size: 26rpx; }
.status-cancelled { color: #607D8B; background-color: #ECEFF1; padding: 4rpx 12rpx; border-radius: 16rpx; font-size: 26rpx; }
.countdown { font-size: 24rpx; color: #FF4D6D; font-weight: bold; }
.countdown.warning { color: #FF9800; animation: flash 1s infinite alternate; }
@keyframes flash { from { opacity: 1; } to { opacity: 0.6; } }

.order-goods { display: flex; align-items: flex-start; margin-bottom: 20rpx; cursor: pointer; }
.goods-image { width: 160rpx; height: 160rpx; border-radius: 16rpx; object-fit: cover; margin-right: 20rpx; background-color: #f5f5f5;}
.goods-info { flex: 1; display: flex; flex-direction: column; }
.goods-name { font-size: 28rpx; color: #333; margin-bottom: 10rpx; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.goods-price { font-size: 28rpx; color: #FF4D6D; font-weight: bold; margin-bottom: 5rpx; }
.goods-quantity { font-size: 26rpx; color: #999; }
.order-amount { display: flex; justify-content: flex-end; padding: 15rpx 0; border-top: 1px dashed #F5F5F5; margin-bottom: 20rpx; }
.amount-label { font-size: 28rpx; color: #666; margin-right: 10rpx; }
.amount-value { font-size: 30rpx; color: #FF4D6D; font-weight: bold; }
.order-actions { display: flex; justify-content: flex-end; gap: 15rpx; }
.action-btn { padding: 12rpx 30rpx; border-radius: 24rpx; font-size: 26rpx; border: none; margin: 0;}
.cancel-btn { background-color: #F5F5F5; color: #666; }
.pay-btn { background-color: #FF85A2; color: white; }
.reorder-btn { background-color: #FFF0F3; color: #FF85A2; }
.track-btn, .review-btn, .detail-btn { background-color: #F0F7FF; color: #2196F3; }

.empty-state { display: flex; flex-direction: column; align-items: center; margin-top: 150rpx; padding: 0 20rpx; }
.empty-image { width: 240rpx; height: 240rpx; margin-bottom: 40rpx; opacity: 0.7; }
.empty-text { font-size: 30rpx; color: #999; margin-bottom: 40rpx; }
.empty-btn { background-color: #FF85A2; color: white; border: none; border-radius: 40rpx; padding: 18rpx 40rpx; font-size: 28rpx; }

/* 🌟 我发布的页面专属微调 */
.publish-area { padding-top: 10rpx; }
.publish-item { padding-bottom: 10rpx; }
.no-border { margin-bottom: 0; }
.publish-info { justify-content: space-between; }
.publish-desc { font-size: 24rpx; color: #999; margin-top: 10rpx; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.publish-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: auto;}
.small-btn { padding: 6rpx 24rpx; font-size: 24rpx; }
</style>