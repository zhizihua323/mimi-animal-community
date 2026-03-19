<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="back-btn" @click="navigateBack">←</view>
      <text class="title">添加动物日常</text>
      <view class="save-btn" @click="submitForm">发布</view>
    </view>

    <!-- 表单内容 -->
    <view class="form-container">
      <!-- 图片上传区域 -->
      <view class="form-item">
        <text class="form-label">上传图片</text>
        <view class="upload-area" @click="chooseImage" v-if="!formData.picUrl">
          <text class="upload-icon">+</text>
          <text class="upload-text">点击添加图片</text>
        </view>
        <view class="image-preview" v-else>
          <image :src="formData.picUrl" mode="aspectFill"></image>
          <view class="remove-img" @click="removeImage">✕</view>
        </view>
      </view>

      <!-- 文本描述 -->
      <view class="form-item">
        <text class="form-label">描述内容</text>
        <textarea 
          class="text-input" 
          v-model="formData.text" 
          placeholder="分享一下动物的日常吧..."
          maxlength="200"
          auto-height
        ></textarea>
        <text class="word-count">{{ formData.text.length }}/200</text>
      </view>

      <!-- 经纬度信息 -->
      <view class="form-item">
        <text class="form-label">位置信息</text>
        <view class="location-info">
          <view class="location-item">
            <text class="location-label">纬度：</text>
            <input 
              type="number" 
              class="location-input" 
              v-model="formData.latitude" 
              placeholder="点击获取或手动输入"
              step="0.000001"
            />
          </view>
          <view class="location-item">
            <text class="location-label">经度：</text>
            <input 
              type="number" 
              class="location-input" 
              v-model="formData.longitude" 
              placeholder="点击获取或手动输入"
              step="0.000001"
            />
          </view>
          <button class="get-location-btn" @click="getLocation">
            {{ isGettingLocation ? '获取中...' : '获取当前位置' }}
          </button>
        </view>
      </view>

      <!-- 关联的动物档案 -->
      <!-- <view class="form-item">
        <text class="form-label">关联档案</text>
        <picker 
          class="picker-select" 
          :value="selectedArchivalIndex" 
          :range="archivalOptions"
          @change="onArchivalChange"
        >
          <view class="picker-display">
            {{ selectedArchivalIndex !== -1 ? archivalOptions[selectedArchivalIndex] : '选择关联的动物档案' }}
          </view>
        </picker>
      </view> -->
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { onLoad } from '@dcloudio/uni-app'; 
import { uploadFileToMinio } from '@/services/animalArchival/animalArchival.js'
import { addPostanimalDaily } from '@/services/animalArchival/animalDaily.js'

// 表单数据
const formData = reactive({
  picUrl: '',
  text: '',
  latitude: null,
  longitude: null,
  archivalId: null
});

// 状态变量
const isGettingLocation = ref(false);
const isSubmitting = ref(false);

// 页面加载时，直接接收上一个页面传过来的动物 ID
onLoad((options) => {
  console.log('接收的参数：', options);
  if (options.id) {
    formData.archivalId = options.id;
  }
});

// 返回上一页 (使用 uni-app 原生方法，抛弃 vue-router)
const navigateBack = () => {
  uni.navigateBack();
};

// 选择图片并上传到 MinIO
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      // 先显示本地临时图片，提升用户体验
      formData.picUrl = res.tempFilePaths[0];
      uni.showLoading({ title: '图片上传中...' });
      
      try {
        const uploadResult = await uploadFileToMinio(res.tempFilePaths[0]);
        if (uploadResult.code === 200 && uploadResult.data) {
          // 替换为 MinIO 返回的真实线上 URL
          formData.picUrl = uploadResult.data;
          uni.hideLoading();
          uni.showToast({ title: '图片上传成功', icon: 'success' });
        } else {
          throw new Error('上传失败');
        }
      } catch (error) {
        console.error('图片上传出错:', error);
        uni.hideLoading();
        uni.showToast({ title: '上传失败，请重试', icon: 'none' });
        formData.picUrl = ''; // 上传失败则清空图片
      }
    }
  });
};

// 移除图片
const removeImage = () => {
  formData.picUrl = '';
};

// 获取当前位置经纬度
const getLocation = () => {
  if (isGettingLocation.value) return;
  
  isGettingLocation.value = true;
  uni.showLoading({ title: '获取位置中...' });
  
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      formData.latitude = res.latitude.toFixed(6);
      formData.longitude = res.longitude.toFixed(6);
      uni.showToast({ title: '位置获取成功', icon: 'success' });
    },
    fail: (err) => {
      console.error('获取位置失败:', err);
      uni.showToast({ title: '获取位置失败，请手动输入', icon: 'none' });
    },
    complete: () => {
      isGettingLocation.value = false;
      uni.hideLoading();
    }
  });
};

// 提交整个日常表单到后端
const submitForm = async () => {
  // 1. 表单判空拦截
  if (!formData.picUrl) return uni.showToast({ title: '请上传图片', icon: 'none' });
  if (!formData.text.trim()) return uni.showToast({ title: '请输入描述内容', icon: 'none' });
  if (!formData.latitude || !formData.longitude) return uni.showToast({ title: '请获取或输入位置信息', icon: 'none' });
  if (!formData.archivalId) return uni.showToast({ title: '数据异常：未关联动物档案', icon: 'none' });
  
  // 防止重复点击提交
  if (isSubmitting.value) return;
  isSubmitting.value = true;
  uni.showLoading({ title: '发布中...' });
  
  try {
    // 2. 真实调用后端接口
    const result = await addPostanimalDaily(formData);
    uni.hideLoading();
    
    if (result.code === 200) {
      uni.showToast({ title: '发布成功', icon: 'success' });
      
      // 3. 发布成功后，延迟 1.5 秒自动退回上一页
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
      
    } else {
      uni.showToast({ title: result.message || '发布失败', icon: 'none' });
    }
  } catch (error) {
    console.error('发布异常:', error);
    uni.hideLoading();
    uni.showToast({ title: '网络异常，发布失败', icon: 'none' });
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.container {
  width: 100%;
  min-height: 100vh;
  background-color: #fafafa;
}

/* 导航栏样式 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: white;
  border-bottom: 1px solid #eee;
}

.back-btn {
  font-size: 36rpx;
  color: #333;
  width: 40rpx;
}

.title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.save-btn {
  font-size: 30rpx;
  color: #FF85A2;
  font-weight: 500;
}

/* 表单容器 */
.form-container {
  padding: 20rpx 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
  background-color: white;
  padding: 25rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 15rpx;
  font-weight: 500;
}

/* 图片上传区域 */
.upload-area {
  width: 100%;
  height: 300rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f9f9f9;
}

.upload-icon {
  font-size: 60rpx;
  color: #ccc;
  margin-bottom: 15rpx;
}

.upload-text {
  font-size: 26rpx;
  color: #999;
}

.image-preview {
  width: 100%;
  height: 300rpx;
  border-radius: 12rpx;
  overflow: hidden;
  position: relative;
}

.image-preview image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-img {
  position: absolute;
  top: 15rpx;
  right: 15rpx;
  width: 40rpx;
  height: 40rpx;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}

/* 文本输入区域 */
.text-input {
  width: 100%;
  min-height: 120rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  padding: 0;
  border: none;
}

.text-input:focus {
  outline: none;
}

.word-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

/* 位置信息区域 */
.location-info {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.location-item {
  display: flex;
  align-items: center;
}

.location-label {
  font-size: 26rpx;
  color: #666;
  width: 100rpx;
}

.location-input {
  flex: 1;
  height: 70rpx;
  border: 1px solid #eee;
  border-radius: 8rpx;
  padding: 0 15rpx;
  font-size: 26rpx;
}

.get-location-btn {
  width: 100%;
  height: 70rpx;
  line-height: 70rpx;
  background-color: #f5f5f5;
  color: #333;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
  margin-top: 10rpx;
}

/* 档案选择器 */
.picker-select {
  width: 100%;
  height: 70rpx;
  border: 1px solid #eee;
  border-radius: 8rpx;
  overflow: hidden;
}

.picker-display {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 15rpx;
  font-size: 26rpx;
  color: #333;
}
</style>
    