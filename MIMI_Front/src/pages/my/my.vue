<template>
  <view class="mine-page">
    

    <!-- 用户信息区域 -->
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
      <!-- <view class="edit-btn" @click="editUserInfo" v-if="hasToken">
        <uni-icons type="edit" size="18" color="#666"></uni-icons>
        <text>编辑资料</text>
      </view> -->
    </view>

    <!-- 数据概览 - 仅登录状态显示 -->
    <view class="data-overview" v-if="hasToken">
      <view class="data-item" @click="viewMyPosts">
        <!-- <view class="data-value">24</view> -->
        <view class="data-label">动物档案</view>
      </view>
     <!-- <view class="data-item" @click="viewCollections">
        <view class="data-value">156</view>
        <view class="data-label">收藏</view>
      </view>
      <view class="data-item" @click="viewFollowers">
        <view class="data-value">328</view>
        <view class="data-label">粉丝</view>
      </view>
      <view class="data-item" @click="viewFollowing">
        <view class="data-value">56</view>
        <view class="data-label">关注</view>
      </view> -->
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-group">
        <view class="menu-item" @click="goToMyOrders" v-if="hasToken">
          <uni-icons type="list" size="20" color="#666" class="menu-icon"></uni-icons>
          <text class="menu-text">我的订单</text>
          <uni-icons type="right" size="16" color="#ccc" class="menu-arrow"></uni-icons>
        </view>
        <!-- <view class="menu-item" @click="goToCoupons" v-if="hasToken">
          <uni-icons type="ticket" size="20" color="#666" class="menu-icon"></uni-icons>
          <text class="menu-text">优惠券</text>
          <view class="badge">5</view>
          <uni-icons type="right" size="16" color="#ccc" class="menu-arrow"></uni-icons>
        </view> -->
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

    <!-- 登录/退出登录按钮 - 动态显示 -->
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
  hasToken.value = !!token; // 转换为布尔值，有值则为true，无值则为false
};

// 跳转到登录页面
const goToLogin = () => {
  console.log("ing跳转")
  uni.redirectTo({
    url: '/pages/login/login',
    // 跳转失败处理（如路径错误）
    fail: (err) => {
      console.error('跳转到登录页失败：', err);
      uni.showToast({ title: '跳转失败，请重试', icon: 'none' });
    }
  });
};

// 跳转到设置页面
const goToSettings = () => {
  // if (hasToken.value) {
  //   uni.navigateTo({
  //     url: '/pages/settings/settings'
  //   });
  // } else {
  //   uni.showToast({
  //     title: '请先登录',
  //     icon: 'none'
  //   });
  // }
};

// 编辑用户信息
const editUserInfo = () => {
  // uni.navigateTo({
  //   url: '/pages/edit-info/edit-info'
  // });
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

// 查看收藏
const viewCollections = () => {
  // uni.navigateTo({
  //   url: '/pages/collections/collections'
  // });
};

// 查看粉丝
const viewFollowers = () => {
  // uni.navigateTo({
  //   url: '/pages/followers/followers'
  // });
};

// 查看关注
const viewFollowing = () => {
  // uni.navigateTo({
  //   url: '/pages/following/following'
  // });
};

// 跳转到我的订单
const goToMyOrders = () => {
  uni.navigateTo({
    url: '/pages/order_list/order_list'
  });
};

// 跳转到优惠券
const goToCoupons = () => {
  // uni.navigateTo({
  //   url: '/pages/coupons/coupons'
  // });
};

// 跳转到收货地址
const goToAddress = () => {
  uni.navigateTo({
    url: '/pages/address_list/address_list'
  });
};

// 跳转到帮助中心
const goToHelpCenter = () => {
  // uni.navigateTo({
  //   url: '/pages/help/help'
  // });
};

// 跳转到意见反馈
const goToFeedback = () => {
  // uni.navigateTo({
  //   url: '/pages/feedback/feedback'
  // });
};

// 跳转到关于我们
const goToAbout = () => {
  // uni.navigateTo({
  //   url: '/pages/about/about'
  // });
};

// 显示退出登录确认框
const showLogoutConfirm = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // 清除token
        uni.removeStorageSync('token');
        // 更新登录状态
        hasToken.value = false;
        // 提示退出成功
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

/* 导航栏样式 */
.navbar {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background-color: #ffffff;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.right-icon {
  position: absolute;
  right: 16px;
}

/* 用户信息区域 */
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

.edit-btn {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.edit-btn text {
  margin-left: 4px;
}

/* 数据概览 */
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

.data-value {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.data-label {
  font-size: 14px;
  color: #666;
}

/* 功能菜单 */
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

.badge {
  width: 20px;
  height: 20px;
  background-color: #ff4d4f;
  color: #ffffff;
  border-radius: 50%;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

/* 登录/退出登录按钮 */
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
