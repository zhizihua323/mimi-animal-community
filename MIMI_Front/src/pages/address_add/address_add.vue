<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="back-icon" @click="navigateBack">←</view>
      <text class="navbar-title">添加收货地址</text>
      <view class="empty-space"></view>
    </view>

    <!-- 表单内容 -->
    <view class="form-container">
      <view class="form-group">
        <text class="form-label">收件人</text>
        <input 
          class="form-input" 
          placeholder="请输入收件人姓名" 
          v-model="formData.name"
          maxlength="20"
        />
      </view>
      
      <view class="form-group">
        <text class="form-label">手机号码</text>
        <input 
          class="form-input" 
          placeholder="请输入手机号码" 
          v-model="formData.tel"
          type="number"
          maxlength="11"
        />
      </view>
      
      <view class="form-group address-group">
        <text class="form-label">详细地址</text>
        <textarea 
          class="form-textarea" 
          placeholder="请输入详细地址" 
          v-model="formData.address"
          maxlength="200"
          auto-height
        ></textarea>
      </view>
      
     <view class="form-group switch-group">
        <text class="form-label">设为默认地址</text>
        <switch :checked="formData.isDefault === 1" color="#FF85A2" @change="onDefaultChange" />
     </view>
    </view>

    <!-- 保存按钮 -->
    <button class="save-btn" @click="saveAddress">保存地址</button>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import {addPostAddress} from "@/services/shopping/address.js"

const loading = ref(false);
// 表单数据
const formData = ref({
  name: '',
  tel: '',
  address: '',
  isDefault: 0 // 默认为 0 (非默认)
});

// 切换开关触发的方法
const onDefaultChange = (e) => {
  formData.value.isDefault = e.detail.value ? 1 : 0;
};

// 返回上一页
const navigateBack = () => {
  uni.navigateBack({
    delta: 1
  });
};

// 保存地址
const saveAddress = async() => {
  // 表单验证
  if (!formData.value.name.trim()) {
    uni.showToast({
      title: '请输入收件人姓名',
      icon: 'none'
    });
    return;
  }
  
  if (!formData.value.tel.trim() || formData.value.tel.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号码',
      icon: 'none'
    });
    return;
  }
  
  if (!formData.value.address.trim()) {
    uni.showToast({
      title: '请输入详细地址',
      icon: 'none'
    });
    return;
  }
  
  // 实际项目中这里会调用保存接口
  try {
    loading.value = true // 显示加载状态
    // 传递分页参数
    const response = await addPostAddress(formData.value);
  	
  } catch (error) {
    console.error('处理数据失败:', error)
  } finally {
    loading.value = false // 关闭加载状态
  }
  
  // 模拟保存成功
  uni.showToast({
    title: '地址添加成功',
    icon: 'none'
  });
  
  // 延迟返回列表页
  setTimeout(() => {
    navigateBack();
  }, 1500);
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

/* 表单内容 */
.form-container {
  flex: 1;
  padding: 30rpx 24rpx;
}

.form-group {
  display: flex;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1px solid #F5F5F5;
  background-color: white;
  padding-left: 24rpx;
  padding-right: 24rpx;
  margin-bottom: 10rpx;
  border-radius: 16rpx;
}

.form-label {
  width: 140rpx;
  font-size: 28rpx;
  color: #333;
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  padding: 10rpx 0;
}

.address-group {
  align-items: flex-start;
  padding-top: 25rpx;
}

.form-textarea {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  min-height: 120rpx;
  line-height: 1.5;
}

.checkbox-group {
  background-color: white;
  padding: 20rpx 24rpx;
}

.form-checkbox {
  transform: scale(1.2);
  margin-right: 15rpx;
}

.checkbox-text {
  font-size: 28rpx;
  color: #333;
}

/* 保存按钮 */
.save-btn {
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
  box-shadow: 0 6rpx 16rpx rgba(255, 133, 162, 0.3);
}
</style>
    