<template>
  <view class="container">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="back-btn" @click="onBack">
        <text class="back-icon">←</text>
      </view>
      <view class="title">添加动物信息</view>
      <view class="empty-btn"></view> <!-- 占位，使标题居中 -->
    </view>

    <!-- 表单卡片 -->
    <view class="form-card">
      <!-- 名称输入 -->
      <view class="form-item">
        <view class="label">名称</view>
        <view class="input-container">
          <input 
            type="text" 
            v-model="formData.name" 
            placeholder="请输入动物名称" 
            class="form-input"
          />
        </view>
      </view>

      <!-- 物种输入 -->
      <view class="form-item">
        <view class="label">物种</view>
        <view class="input-container">
          <input 
            type="text" 
            v-model="formData.species" 
            placeholder="请输入物种类型" 
            class="form-input"
          />
        </view>
      </view>

      <!-- 是否野生 -->
      <view class="form-item">
        <view class="label">是否野生</view>
        <view class="radio-group">
          <label class="radio-label" :class="{ active: formData.isWild === 1 }" @click="formData.isWild = 1">
            <view class="radio-icon"></view>
            <text>是</text>
          </label>
          <label class="radio-label" :class="{ active: formData.isWild === 0 }" @click="formData.isWild = 0">
            <view class="radio-icon"></view>
            <text>否</text>
          </label>
        </view>
      </view>

      <!-- 出生日期 -->
      <view class="form-item">
        <view class="label">出生日期</view>
        <view class="input-container">
          <input 
            type="date" 
            v-model="formData.birthData" 
            class="form-input date-input"
          />
        </view>
      </view>

      <!-- 相册照片 -->
      <view class="form-item">
        <view class="label">相册照片</view>
        <view class="upload-container">
          <view class="upload-area" @click="chooseImage">
            <text class="camera-icon">📷</text>
            <text class="upload-text">点击上传照片</text>
            <text class="upload-desc">支持JPG、PNG格式</text>
          </view>
          
          <!-- 已上传图片预览 -->
          <view class="preview-list" v-if="formData.albumPhoto">
            <view class="preview-item">
              <image :src="formData.albumPhoto" mode="cover" class="preview-img"></image>
              <view class="delete-img" @click="deleteImage">
                <text class="close-icon">×</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 描述信息 -->
      <view class="form-item">
        <view class="label">描述</view>
        <view class="textarea-container">
          <textarea 
            v-model="formData.description" 
            placeholder="请输入详细描述信息" 
            class="form-textarea"
            rows="4"
          ></textarea>
          <view class="word-count">{{ formData.description.length }}/200</view>
        </view>
      </view>

      <!-- 是否展示 -->
      <view class="form-item">
        <view class="label">是否展示</view>
        <view class="radio-group">
          <label class="radio-label" :class="{ active: formData.isShow === 1 }" @click="formData.isShow = 1">
            <view class="radio-icon"></view>
            <text>展示</text>
          </label>
          <label class="radio-label" :class="{ active: formData.isShow === 0 }" @click="formData.isShow = 0">
            <view class="radio-icon"></view>
            <text>不展示</text>
          </label>
        </view>
      </view>

      <!-- 创建者ID -->
      <!-- <view class="form-item">
        <view class="label">创建者ID</view>
        <view class="input-container">
          <input 
            type="number" 
            v-model="formData.createUserId" 
            placeholder="请输入创建者ID" 
            class="form-input"
          />
        </view>
      </view> -->
    </view>

    <!-- 提交按钮 -->
    <view class="submit-btn" @click="onSubmit">
      保存信息
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import {uploadFileToMinio,addPostanimalArchival} from '@/services/animalArchival/animalArchival.js'

// 状态管理
const isUploading = ref(false); // 图片上传状态
const isSubmitting = ref(false); // 表单提交状态
// 表单数据
const formData = ref({
  name: '',
  species: '',
  isWild: 0, // 0-否 1-是
  birthData: '',
  albumPhoto: '',
  description: '',
  isShow: 1, // 0-不展示 1-展示
  createUserId: ''
});

// 返回事件
const onBack = () => {
  uni.navigateBack({
    delta: 1
  });
};

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: async(res) => {
	  try {
	          isUploading.value = true;
	          // 调用上传接口，获取图片URL
	          const uploadResult = await uploadFileToMinio(res.tempFilePaths[0]);
	          // 假设后端返回格式：{ code: 200, data: { url: '图片URL' } }
	          if (uploadResult.code === 200 && uploadResult.data) {
	            formData.value.albumPhoto = uploadResult.data;
	            uni.showToast({
	              title: '图片上传成功',
	              icon: 'success',
	              duration: 1500
	            });
	          } else {
	            throw new Error('图片上传失败');
	          }
	        } catch (error) {
	          uni.showToast({
	            title: error.message || '图片上传失败',
	            icon: 'none',
	            duration: 1500
	          });
	          console.error('图片上传错误:', error);
	        } finally {
	          isUploading.value = false;
	        }
    },
    fail: (err) => {
      uni.showToast({
        title: '图片选择失败',
        icon: 'none',
        duration: 1500
      });
      console.error('选择图片错误:', err);
    }
  });
};

// 删除图片
const deleteImage = () => {
  formData.value.albumPhoto = '';
};

// 提交表单
const onSubmit = async() => {
  // 表单验证
  if (!formData.value.name.trim()) {
    return uni.showToast({
      title: '请输入动物名称',
      icon: 'none',
      duration: 1500
    });
	
	
	
  }
  
  if (!formData.value.species.trim()) {
    return uni.showToast({
      title: '请输入物种类型',
      icon: 'none',
      duration: 1500
    });
  }
  
  // if (!formData.value.createUserId.toString().trim()) {
  //   return uni.showToast({
  //     title: '请输入创建者ID',
  //     icon: 'none',
  //     duration: 1500
  //   });
  // }
  
  if (formData.value.description.length > 200) {
    return uni.showToast({
      title: '描述不能超过200字',
      icon: 'none',
      duration: 1500
    });
  }
try {
  // 调用异步函数时使用 await，并等待结果
  const result = await addPostanimalArchival(formData.value);
  // 提交成功后的操作（如提示用户、刷新页面等）
  console.log('提交成功，返回结果:', result);
  // 例如：ElMessage.success('提交成功！'); // 若使用 Element Plus 等 UI 库
} catch (error) {
  // 捕获异步函数抛出的错误，并处理
  console.error('提交失败:', error);
  // 例如：ElMessage.error('提交失败，请稍后重试！');
}
  uni.showToast({
    title: '保存成功',
    icon: 'success',
    duration: 1500,
    success: () => {
      setTimeout(() => {
        onBack();
      }, 1500);
    }
  });
};
</script>

<style scoped>
/* 移除字体文件引用，避免找不到文件的错误 */

.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 120rpx;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 120rpx;
  padding: 0 30rpx;
  background-color: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #f5f7fa;
}

.back-icon {
  font-size: 36rpx;
  color: #333;
}

.title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
}

.empty-btn {
  width: 60rpx;
  height: 60rpx;
}

/* 表单卡片 */
.form-card {
  margin: 30rpx;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);
}

/* 表单项目 */
.form-item {
  display: flex;
  padding: 25rpx 0;
  border-bottom: 1px solid #f5f7fa;
  align-items: flex-start;
}

.form-item:last-child {
  border-bottom: none;
}

.label {
  width: 200rpx;
  font-size: 32rpx;
  color: #333;
  display: flex;
  align-items: center;
  margin-top: 15rpx;
}

/* 输入框容器 */
.input-container {
  flex: 1;
}

.form-input {
  width: 100%;
  height: 70rpx;
  font-size: 32rpx;
  color: #666;
  padding: 0 10rpx;
  box-sizing: border-box;
  border: none;
  background: transparent;
}

.form-input::placeholder {
  color: #ccc;
}

/* 日期输入 */
.date-input {
  color: #666;
}

/* 单选按钮组 */
.radio-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 40rpx;
  margin-top: 15rpx;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 15rpx;
  font-size: 32rpx;
  color: #666;
  cursor: pointer;
}

.radio-icon {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2px solid #ddd;
  position: relative;
}

.radio-label.active .radio-icon {
  border-color: #409eff;
}

.radio-label.active .radio-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background-color: #409eff;
}

.radio-label.active text {
  color: #409eff;
  font-weight: 500;
}

/* 上传区域 */
.upload-container {
  flex: 1;
}

.upload-area {
  width: 100%;
  height: 200rpx;
  border: 2rpx dashed #ddd;
  border-radius: 10rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15rpx;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.camera-icon {
  font-size: 60rpx;
  color: #ccc;
  transition: color 0.3s;
}

.upload-area:hover .camera-icon {
  color: #409eff;
}

.upload-text {
  font-size: 30rpx;
  color: #999;
}

.upload-desc {
  font-size: 24rpx;
  color: #ccc;
}

/* 预览图片 */
.preview-list {
  margin-top: 20rpx;
}

.preview-item {
  width: 160rpx;
  height: 160rpx;
  border-radius: 10rpx;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-img {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background-color: rgba(255, 77, 77, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.close-icon {
  font-size: 24rpx;
  font-weight: bold;
}

/* 文本域 */
.textarea-container {
  flex: 1;
  position: relative;
}

.form-textarea {
  width: 100%;
  font-size: 32rpx;
  color: #666;
  padding: 15rpx;
  box-sizing: border-box;
  border: 1px solid #eee;
  border-radius: 10rpx;
  min-height: 200rpx;
  resize: none;
  background: transparent;
}

.form-textarea:focus {
  outline: none;
  border-color: #409eff;
}

.form-textarea::placeholder {
  color: #ccc;
}

.word-count {
  position: absolute;
  right: 20rpx;
  bottom: 15rpx;
  font-size: 24rpx;
  color: #999;
}

/* 提交按钮 */
.submit-btn {
  position: fixed;
  bottom: 40rpx;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 60rpx);
  height: 100rpx;
  background-color: #409eff;
  color: #fff;
  font-size: 34rpx;
  font-weight: 500;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 5rpx 15rpx rgba(64, 158, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:active {
  background-color: #3a8ee6;
  transform: translateX(-50%) scale(0.98);
}
</style>
