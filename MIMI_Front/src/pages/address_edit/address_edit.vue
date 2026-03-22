<template>
  <view class="container">
    <view class="form-group">
      <view class="form-item">
        <text class="label">收货人</text>
        <input class="input" type="text" v-model="form.name" placeholder="请填写收货人姓名" />
      </view>
      <view class="form-item">
        <text class="label">手机号码</text>
        <input class="input" type="number" v-model="form.tel" placeholder="请填写收货人手机号" maxlength="11" />
      </view>
      <view class="form-item">
        <text class="label">详细地址</text>
        <textarea class="textarea" v-model="form.address" placeholder="街道、楼牌号等" :auto-height="true"></textarea>
      </view>
      <view class="form-item switch-item">
        <text class="label">设为默认地址</text>
        <switch :checked="form.isDefault === 1" color="#FF85A2" @change="onDefaultChange" />
      </view>
    </view>

    <view class="btn-group">
      <button class="save-btn" @click="updateAddress">保存修改</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { updatePostAddress } from '@/services/shopping/address.js';

// 表单数据
const form = ref({
  id: '',
  name: '',
  tel: '',
  address: '',
  isDefault: 0
});

// 页面加载时，接收列表页传过来的参数进行回显
onLoad((options) => {
  if (options && options.id) {
    // 完美解析你列表页传过来的 URI 编码数据
    form.value.id = options.id;
    form.value.name = decodeURIComponent(options.name || '');
    form.value.tel = options.tel || '';
    form.value.address = decodeURIComponent(options.address || '');
    // 将字符串 'true'/'false' 或 '1'/'0' 转换为数字 1 或 0
    form.value.isDefault = (options.isDefault === 'true' || options.isDefault == 1) ? 1 : 0;
  }
});

// 切换默认地址开关
const onDefaultChange = (e) => {
  form.value.isDefault = e.detail.value ? 1 : 0;
};


const updateAddress = async () => {
  if (!form.value.name.trim()) return uni.showToast({ title: '请填写姓名', icon: 'none' });
  if (!form.value.tel.trim() || form.value.tel.length !== 11) return uni.showToast({ title: '请填写正确的手机号', icon: 'none' });
  if (!form.value.address.trim()) return uni.showToast({ title: '请填写详细地址', icon: 'none' });

  try {
    uni.showLoading({ title: '保存修改中...' });
    
    // 真正调用后端的修改接口
    await updatePostAddress(form.value); 
    
    uni.hideLoading();
    uni.showToast({ title: '修改成功', icon: 'success' });
    
    setTimeout(() => {
      uni.navigateBack({ delta: 1 });
    }, 1000);
  } catch (error) {
    uni.hideLoading();
    uni.showToast({ title: '修改失败', icon: 'none' });
  }
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #F5F7FA;
  padding: 20rpx;
}

.form-group {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  padding: 0 20rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 10rpx;
  border-bottom: 1px solid #F0F0F0;
}

.form-item:last-child {
  border-bottom: none;
}

.switch-item {
  justify-content: space-between;
}

.label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
}

.input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.textarea {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  min-height: 80rpx;
  padding-top: 6rpx;
}

.btn-group {
  margin-top: 60rpx;
  padding: 0 20rpx;
}

.save-btn {
  background-color: #FF85A2;
  color: #FFFFFF;
  border-radius: 40rpx;
  font-size: 32rpx;
  font-weight: bold;
  padding: 10rpx 0;
}

.save-btn::after {
  border: none;
}
</style>