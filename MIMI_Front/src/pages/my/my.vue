<template>
  <view class="mine-page">
    
    <view class="user-info-section">
      <view class="user-avatar">
        <image 
          src="https://picsum.photos/id/64/200/200" 
          mode="widthFix"
          alt="用户头像"
        ></image>
      </view>
      <view class="user-info">
        <view class="user-name">{{ hasToken ? 'MIMI' : '请登录' }}</view>
        <view class="user-signature">{{ hasToken ? '热爱生活，享受编程的乐趣' : '登录后可查看个人信息' }}</view>
      </view>
    </view>

    <view class="data-overview" v-if="hasToken">
      <view class="data-item" @click="viewMyPosts">
        <view class="data-label">动物档案</view>
      </view>
    </view>

    <view class="menu-section">
      <view class="menu-group">
        
        <view class="menu-item" @click="goToMyTrade" v-if="hasToken">
          <uni-icons type="list" size="20" color="#666" class="menu-icon"></uni-icons>
          <text class="menu-text">我的交易</text>
          <uni-icons type="right" size="16" color="#ccc" class="menu-arrow"></uni-icons>
        </view>
        
        <view class="menu-item" @click="goToAddress" v-if="hasToken">
          <uni-icons type="location" size="20" color="#666" class="menu-icon"></uni-icons>
          <text class="menu-text">收货地址</text>
          <uni-icons type="right" size="16" color="#ccc" class="menu-arrow"></uni-icons>
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item" @click="goToHelpCenter">
          <uni-icons type="help" size="20" color="#666" class="menu-icon"></uni-icons>
          <text class="menu-text">帮助中心</text>
          <uni-icons type="right" size="16" color="#ccc" class="menu-arrow"></uni-icons>
        </view>
        <view class="menu-item" @click="goToFeedback">
          <uni-icons type="chat" size="20" color="#666" class="menu-icon"></uni-icons>
          <text class="menu-text">意见反馈</text>
          <uni-icons type="right" size="16" color="#ccc" class="menu-arrow"></uni-icons>
        </view>
        <view class="menu-item" @click="goToAbout">
          <uni-icons type="info" size="20" color="#666" class="menu-icon"></uni-icons>
          <text class="menu-text">关于我们</text>
          <uni-icons type="right" size="16" color="#ccc" class="menu-arrow"></uni-icons>
        </view>
      </view>
    </view>

    <button 
      class="logout-btn" 
      @click="hasToken ? showLogoutConfirm() : goToLogin()"
    >
      {{ hasToken ? '退出登录' : ' 登录 ' }}
    </button>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue';

// 响应式变量存储登录状态
const hasToken = ref(false);

// 页面加载时检查token
onMounted(() => {
  checkLoginStatus();
});

// 检查登录状态
const checkLoginStatus = () => {
  const token = uni.getStorageSync('token');
  hasToken.value = !!token; 
};

// 跳转到登录页面
const goToLogin = () => {
  console.log("ing跳转")
  uni.redirectTo({
    url: '/pages/login/login',
    fail: (err) => {
      console.error('跳转到登录页失败：', err);
      uni.showToast({ title: '跳转失败，请重试', icon: 'none' });
    }
  });
};

// 查看我的动物档案
const viewMyPosts = () => {
  uni.navigateTo({
    url: '/pages/animalArchival/animalArchival',
    fail: (err) => {
      console.error('跳转到动物档案失败：', err);
      uni.showToast({ title: '跳转失败，请重试', icon: 'none' });
    }
  });
};

// 修改点：新的跳转到我的交易页面的方法
const goToMyTrade = () => {
  uni.navigateTo({
    url: '/pages/order_list/order_list',
    fail: (err) => {
      console.error('跳转到我的交易失败：', err);
      uni.showToast({ title: '跳转失败，请检查页面路径', icon: 'none' });
    }
  });
};

// 跳转到收货地址
const goToAddress = () => {
  uni.navigateTo({
    url: '/pages/address_list/address_list'
  });
};

// 跳转到帮助中心
const goToHelpCenter = () => {};

// 跳转到意见反馈
const goToFeedback = () => {};

// 跳转到关于我们
const goToAbout = () => {};

// 显示退出登录确认框
const showLogoutConfirm = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token');
        hasToken.value = false;
        uni.showToast({
          title: '退出登录成功',
          icon: 'none'
        });
      }
    }
  });
};
</script>

<style scoped>
/* 样式保持不变 */
.mine-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.user-info-section {
  background-color: #ffffff;
  padding: 20px 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
}

.user-avatar image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.user-signature {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.data-overview {
  background-color: #ffffff;
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.data-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.data-label {
  font-size: 14px;
  color: #666;
}

.menu-section {
  margin-top: 10px;
  flex: 1;
}

.menu-group {
  background-color: #ffffff;
  margin-bottom: 10px;
}

.menu-item {
  display: flex;
  align-items: center;
  height: 50px;
  padding: 0 16px;
  border-bottom: 1px solid #eee;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  margin-right: 16px;
}

.menu-text {
  flex: 1;
  font-size: 16px;
  color: #333;
}

.logout-btn {
  width: 90%;
  height: 45px;
  line-height: 45px;
  margin: 20px auto;
  background-color: #ffffff;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  border-radius: 8px;
  font-size: 16px;
}
</style>