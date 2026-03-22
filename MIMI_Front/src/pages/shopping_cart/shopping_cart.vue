<template>
  <view class="container">
    <view class="header-bar" v-if="cartList.length > 0">
      <text class="item-count">共 {{ totalNum }} 件宝贝</text>
      <text class="manage-text" @click="isManageMode = !isManageMode">
        {{ isManageMode ? '完成' : '管理' }}
      </text>
    </view>

    <scroll-view scroll-y class="cart-scroll" v-if="cartList.length > 0">
      <view class="cart-item" v-for="(item, index) in cartList" :key="item.id">
        <view class="checkbox-area" @click="toggleCheck(index)">
          <text class="checkbox-icon" :class="{ 'checked': item.isChecked === 1 }">
            {{ item.isChecked === 1 ? '✓' : '' }}
          </text>
        </view>
        
        <image class="goods-img" :src="item.picUrl" mode="aspectFill" @click="gotoDetail(item)"></image>
        
        <view class="goods-info">
          <text class="goods-name" @click="gotoDetail(item)">{{ item.goodsName }}</text>
          
          <view class="price-num-box">
            <text class="price">¥{{ item.price.toFixed(2) }}</text>
            
            <view class="num-controller">
              <view class="btn minus" @click="changeNum(index, -1)" :class="{ 'disabled': item.num <= 1 }">-</view>
              <input class="num-input" type="number" v-model.number="item.num" @blur="onNumBlur(index)" />
              <view class="btn plus" @click="changeNum(index, 1)">+</view>
            </view>
          </view>
        </view>

		<view class="delete-mask" v-if="isManageMode" @click="deleteItem(index, item.stringId)">
			<text class="delete-text">删除</text>
		</view>
      </view>
    </scroll-view>

    <view class="empty-cart" v-else>
      <view class="empty-icon">🛒</view>
      <text class="empty-text">购物车空空如也~</text>
      <button class="go-shop-btn" @click="gotoShop">去逛逛</button>
    </view>

    <view class="bottom-bar" v-if="cartList.length > 0">
      <view class="check-all-area" @click="toggleCheckAll">
        <text class="checkbox-icon" :class="{ 'checked': isAllChecked }">
          {{ isAllChecked ? '✓' : '' }}
        </text>
        <text class="check-all-text">全选</text>
      </view>

      <view class="action-area" v-if="!isManageMode">
        <view class="total-price-box">
          <text class="total-label">合计:</text>
          <text class="total-price">¥{{ totalPrice.toFixed(2) }}</text>
        </view>
        <button class="checkout-btn" :class="{ 'disabled': checkedCount === 0 }" @click="checkout">
          结算({{ checkedCount }})
        </button>
      </view>

      <view class="action-area" v-else>
        <button class="delete-all-btn" :class="{ 'disabled': checkedCount === 0 }" @click="deleteSelected">
          删除选中项
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { queryGetCart, updatePutCart, deletePostCart } from "@/services/shopping/cart.js"

const cartList = ref([]);
const isManageMode = ref(false); 

onShow(() => {
  fetchCartData();
});

const fetchCartData = async () => {
  try {
    const response = await queryGetCart();
    const resData = response.data?.records || response.data || [];
    cartList.value = resData;
  } catch (error) {
    console.error('获取购物车失败:', error);
  }
};

const isAllChecked = computed(() => {
  return cartList.value.length > 0 && cartList.value.every(item => item.isChecked === 1);
});

const checkedCount = computed(() => {
  return cartList.value.filter(item => item.isChecked === 1).reduce((sum, item) => sum + item.num, 0);
});

const totalNum = computed(() => {
  return cartList.value.reduce((sum, item) => sum + item.num, 0);
});

const totalPrice = computed(() => {
  return cartList.value
    .filter(item => item.isChecked === 1)
    .reduce((sum, item) => sum + (item.price * item.num), 0);
});

// 🌟 这里改用 stringId 同步后端
const toggleCheck = async (index) => {
  const item = cartList.value[index];
  const newStatus = item.isChecked === 1 ? 0 : 1;
  item.isChecked = newStatus; 
  
  try {
    await updatePutCart({ id: item.stringId, isChecked: newStatus });
  } catch (error) {
    item.isChecked = newStatus === 1 ? 0 : 1; 
  }
};

const toggleCheckAll = () => {
  const targetStatus = isAllChecked.value ? 0 : 1;
  cartList.value.forEach(item => {
    if (item.isChecked !== targetStatus) {
      item.isChecked = targetStatus;
      // 🌟 改用 stringId
      updatePutCart({ id: item.stringId, isChecked: targetStatus }).catch(() => {});
    }
  });
};

const changeNum = async (index, step) => {
  const item = cartList.value[index];
  const newNum = item.num + step;
  if (newNum < 1) return; 
  
  item.num = newNum; 
  try {
    // 🌟 改用 stringId
    await updatePutCart({ id: item.stringId, num: newNum });
  } catch (error) {
    item.num -= step; 
  }
};

const onNumBlur = async (index) => {
  const item = cartList.value[index];
  let num = parseInt(item.num);
  if (isNaN(num) || num <= 0) {
    num = 1;
  }
  item.num = num;
  try {
    // 🌟 改用 stringId
    await updatePutCart({ id: item.stringId, num: num });
  } catch (error) {}
};

// 🌟 单个删除
const deleteItem = (index, stringId) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这个宝贝吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' });
          await deletePostCart([stringId]); // 传数组 [ID]
          cartList.value.splice(index, 1);
          uni.hideLoading();
          uni.showToast({ title: '已删除', icon: 'success' });
        } catch (error) {
          uni.hideLoading();
        }
      }
    }
  });
};

// 🌟 批量删除
const deleteSelected = () => {
  if (checkedCount.value === 0) return;
  
  // 🌟 提取 stringId
  const selectedIds = cartList.value.filter(item => item.isChecked === 1).map(item => item.stringId);
  
  uni.showModal({
    title: '提示',
    content: `确定要删除这 ${checkedCount.value} 件宝贝吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' });
          await deletePostCart(selectedIds); // 直接传数组给后端
          fetchCartData();
          isManageMode.value = false;
          uni.hideLoading();
          uni.showToast({ title: '已删除', icon: 'success' });
        } catch (error) {
          uni.hideLoading();
        }
      }
    }
  });
};

const gotoShop = () => {
  uni.navigateBack({ delta: 1 });
};

const checkout = () => {
  if (checkedCount.value === 0) {
    return uni.showToast({ title: '请先选择要购买的商品', icon: 'none' });
  }
  
  const selectedItems = cartList.value.filter(item => item.isChecked === 1);
  uni.showLoading({ title: '正在生成订单...' });
  
  setTimeout(() => {
    uni.hideLoading();
    if (selectedItems.length > 1) {
      return uni.showModal({ title: '提示', content: '亲，结算页目前仅支持单商品结算哦~', showCancel: false });
    }
    
    const goodsToBuy = selectedItems[0];
    const fakeOrderId = 'CART' + new Date().getTime();
    uni.navigateTo({
      url: `/pages/shopping_pay/shopping_pay?id=${fakeOrderId}&goodsName=${encodeURIComponent(goodsToBuy.goodsName)}`
    });
  }, 500);
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #F5F7FA;
}

/* 顶部管理栏 */
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: white;
  border-bottom: 1px solid #F0F0F0;
}
.item-count { font-size: 28rpx; color: #666; }
.manage-text { font-size: 28rpx; color: #FF85A2; font-weight: bold; }

/* 购物车列表区 */
.cart-scroll {
  flex: 1;
  padding: 20rpx;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  align-items: center;
  background-color: white;
  padding: 24rpx;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  position: relative;
  overflow: hidden;
}

/* 勾选框样式 */
.checkbox-area { padding: 10rpx 20rpx 10rpx 0; }
.checkbox-icon {
  display: inline-block;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #CCC;
  text-align: center;
  line-height: 40rpx;
  color: white;
  font-size: 28rpx;
  transition: all 0.2s;
}
.checkbox-icon.checked {
  background-color: #FF85A2;
  border-color: #FF85A2;
}

.goods-img { width: 160rpx; height: 160rpx; border-radius: 12rpx; margin-right: 20rpx; background-color: #f9f9f9; }

.goods-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; height: 160rpx; }
.goods-name { font-size: 28rpx; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.price-num-box { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 32rpx; color: #FF4D6D; font-weight: bold; }

/* 数量控制器 */
.num-controller { display: flex; align-items: center; border: 1px solid #EEEEEE; border-radius: 8rpx; }
.btn { width: 44rpx; height: 44rpx; text-align: center; line-height: 40rpx; font-size: 32rpx; color: #666; background-color: #F8F8F8; }
.btn.disabled { color: #CCC; }
.num-input { width: 60rpx; height: 44rpx; text-align: center; font-size: 26rpx; border-left: 1px solid #EEEEEE; border-right: 1px solid #EEEEEE; }

/* 删除遮罩层 */
.delete-mask {
  position: absolute;
  top: 0; right: 0; bottom: 0;
  width: 120rpx;
  background-color: #FF4D6D;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}
.delete-text { color: white; font-size: 28rpx; font-weight: bold; }

/* 空购物车 */
.empty-cart { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; }
.empty-icon { font-size: 100rpx; margin-bottom: 30rpx; opacity: 0.5; }
.empty-text { font-size: 30rpx; color: #999; margin-bottom: 40rpx; }
.go-shop-btn { background-color: #FF85A2; color: white; border-radius: 40rpx; padding: 10rpx 60rpx; font-size: 30rpx; }

/* 底部结算栏 */
.bottom-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: white;
  border-top: 1px solid #EEE;
  padding-bottom: env(safe-area-inset-bottom); /* 适配iPhone底部黑条 */
}

.check-all-area { display: flex; align-items: center; }
.check-all-text { font-size: 28rpx; color: #333; margin-left: 10rpx; }

.action-area { display: flex; align-items: center; }
.total-price-box { display: flex; align-items: center; margin-right: 20rpx; }
.total-label { font-size: 26rpx; color: #333; margin-right: 10rpx; }
.total-price { font-size: 34rpx; color: #FF4D6D; font-weight: bold; }

.checkout-btn { background-color: #FF85A2; color: white; border-radius: 40rpx; padding: 0 40rpx; height: 72rpx; line-height: 72rpx; font-size: 28rpx; font-weight: bold; margin: 0; }
.checkout-btn.disabled { background-color: #FFC0CB; }

.delete-all-btn { background-color: transparent; color: #FF4D6D; border: 2rpx solid #FF4D6D; border-radius: 40rpx; padding: 0 40rpx; height: 68rpx; line-height: 64rpx; font-size: 28rpx; margin: 0; }
.delete-all-btn.disabled { color: #CCC; border-color: #CCC; }
</style>