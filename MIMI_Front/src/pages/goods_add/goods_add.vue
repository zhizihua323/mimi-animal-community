<template>
  <view class="container">
    <view class="custom-header">
      <view class="back-section" @click="navigateBackHome">
        <text class="arrow">←</text> </view>
      <text class="navbar-title">MIMI寄卖</text>
      <view class="header-placeholder"></view> </view>

    <view class="content-wrapper">
      <view class="form-content">
        <view class="upload-section" @click="chooseImage">
          <image v-if="formData.picUrl" :src="formData.picUrl" mode="aspectFill" class="preview-img"></image>
          <view v-else class="upload-placeholder">
            <text class="plus">+</text>
            <text class="text">上传宝贝主图</text>
          </view>
        </view>

        <view class="input-item">
          <input class="input-title" v-model="formData.goodsName" placeholder="标题：品牌 型号 成色 (例如: 9新猫爬架)" maxlength="30" />
        </view>
        
        <view class="input-item">
          <textarea class="input-desc" v-model="formData.material" placeholder="描述一下宝贝的材质、使用情况、转让原因..." maxlength="200" />
        </view>
        
        <view class="cell-item">
          <text class="label">宝贝分类</text>
          <picker class="picker-box" :range="categoryList" range-key="name" :value="formData.categoryIndex" @change="onCategoryChange">
            <view class="picker-display">
              <text :class="{ 'placeholder-text': formData.categoryIndex === -1 }">
                {{ formData.categoryIndex === -1 ? '请选择分类' : categoryList[formData.categoryIndex].name }}
              </text>
              <text class="arrow">></text>
            </view>
          </picker>
        </view>

        <view class="cell-item border-none">
          <text class="label">价格 (¥)</text>
          <input type="digit" v-model="formData.price" placeholder="0.00" class="price-input" />
        </view>
      </view>
    </view>

    <view class="footer-btn">
      <button class="submit-btn" @click="submitGoods">确认发布</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { uploadGoodsImageToMinio, publishGoods } from '@/services/shopping/shopping.js';

// 这里对应你商城首页的真实分类
const categoryList = ref([
  { id: 1, name: '服饰穿搭' },
  { id: 2, name: '玩具零食' },
  { id: 3, name: '日常用品' },
  { id: 4, name: '出行装备' }
]);

const formData = ref({
  goodsName: '',
  material: '', 
  price: '',
  picUrl: '', 
  categoryIndex: -1 // 初始值为-1，代表未选择
});

const isUploading = ref(false); // 新增防抖变量

// 修改点 2: 真正的返回上一页
const navigateBackHome = () => {
  uni.navigateBack({
    delta: 1 // 返回上一页
  });
  // 如果你需要强制跳回首页，可以用 uni.reLaunch 或 uni.switchTab
  // uni.reLaunch({
  //   url: '/pages/index/index'
  // });
};

// 选择器改变事件
const onCategoryChange = (e) => {
  formData.value.categoryIndex = e.detail.value;
};

// 1. 替换 chooseImage 方法
const chooseImage = () => {
  if (isUploading.value) return; 
  uni.chooseImage({
    count: 1,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      try {
        isUploading.value = true;
        uni.showLoading({ title: '上传中...' });

        // 调用 shopping.js 里的标准上传方法
        const uploadResult = await uploadGoodsImageToMinio(res.tempFilePaths[0]);
        
        if (uploadResult.code === 200 && uploadResult.data) {
          formData.value.picUrl = uploadResult.data; 
          uni.showToast({ title: '图片上传成功', icon: 'success' });
        } else {
          throw new Error('图片上传失败');
        }
      } catch (error) {
        uni.showToast({ title: error.message || '图片上传失败', icon: 'none' });
      } finally {
        isUploading.value = false;
        uni.hideLoading(); 
      }
    },
    fail: (err) => {
      if (err.errMsg !== "chooseImage:fail cancel") {
         uni.showToast({ title: '取消选择', icon: 'none' });
      }
    }
  });
};

// 提交发布
const submitGoods = async () => {
  if (!formData.value.picUrl) return uni.showToast({ title: '请上传宝贝图片', icon: 'none' });
  if (!formData.value.goodsName.trim()) return uni.showToast({ title: '请填写宝贝标题', icon: 'none' });
  if (formData.value.categoryIndex === -1) return uni.showToast({ title: '请选择宝贝分类', icon: 'none' });
  if (!formData.value.price) return uni.showToast({ title: '请填写宝贝价格', icon: 'none' });

  const priceNum = parseFloat(formData.value.price);
  if (isNaN(priceNum) || priceNum <= 0) return uni.showToast({ title: '价格格式不正确', icon: 'none' });

  const payload = {
      goodsName: formData.value.goodsName,
      material: formData.value.material,
      price: parseFloat(formData.value.price),
      picUrl: formData.value.picUrl,
      categoryId: categoryList.value[formData.value.categoryIndex].id 
    };
  
    try {
      uni.showLoading({ title: '发布中...' });
      // 调用 shopping.js 里的标准发布方法
      const res = await publishGoods(payload); 
      uni.hideLoading();
      
      // 这里根据你的 http 封装工具的返回格式判断
      if (res.code === 200) { 
        uni.showToast({ title: '发布成功！', icon: 'success' });
        setTimeout(() => {
          uni.navigateBack({ delta: 1 });
        }, 1500);
      } else {
        uni.showToast({ title: res.message || '发布失败', icon: 'none' });
      }
    } catch (error) {
      uni.hideLoading();
      uni.showToast({ title: '网络失败，请重试', icon: 'none' });
    }
};
</script>

<style scoped>
/* 全局容器 */
.container { 
  width: 100%; 
  min-height: 100vh; 
  background-color: #f5f5f5; 
  overflow: hidden;
  padding-bottom: 120rpx;
}

/* 修改点 3: 自定义导航栏样式 (固定在顶部) */
.custom-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 88rpx;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03);
  z-index: 1000;
  margin-top: 50rpx;
}

/* 返回箭头样式 */
.back-arrow {
  width: 50rpx;
  font-size: 36rpx;
  color: #FF85A2;
  font-weight: bold;
}

/* 导航栏标题样式 */
.navbar-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

/* 右侧占位 */
.header-placeholder {
  width: 50rpx;
}

/* 调整：内容区增加了 top 边距，把头部撑开 */
.content-wrapper {
  padding-top: 108rpx; /* 88rpx 的头部高度 + 20rpx 的额外边距 */
  padding-left: 30rpx;
  padding-right: 30rpx;
  margin-top: 50rpx;
}

.form-content { background: white; border-radius: 20rpx; padding: 20rpx;}

/* 上传区域 */
.upload-section { width: 100%; height: 300rpx; background: #f9f9f9; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; margin-bottom: 30rpx; border: 2rpx dashed #e0e0e0;}
.preview-img { width: 100%; height: 100%; border-radius: 16rpx; object-fit: cover;}
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #999;}
.plus { font-size: 80rpx; font-weight: 300; margin-bottom: 10rpx;}
.text { font-size: 28rpx;}

/* 输入框 */
.input-item { margin-bottom: 20rpx; border-bottom: 1px solid #f0f0f0; }
.input-title { width: 100%; height: 90rpx; font-size: 32rpx; font-weight: bold;}
.input-desc { width: 100%; height: 160rpx; font-size: 28rpx; color: #666; padding: 20rpx 0; line-height: 1.5;}

/* 通用列表行 (分类和价格) */
.cell-item { display: flex; justify-content: space-between; align-items: center; padding: 30rpx 0; border-bottom: 1px solid #f0f0f0; }
.border-none { border-bottom: none; }
.label { font-size: 30rpx; color: #333; font-weight: bold;}

/* 分类选择器样式 */
.picker-box { flex: 1; text-align: right; }
.picker-display { display: flex; justify-content: flex-end; align-items: center; font-size: 30rpx; color: #333; }
.placeholder-text { color: #999; }
.arrow { margin-left: 10rpx; color: #ccc; font-weight: bold; }

/* 价格输入框样式 */
.price-input { text-align: right; font-size: 40rpx; color: #FF85A2; font-weight: bold; width: 250rpx;}

/* 底部按钮 */
.footer-btn { position: fixed; bottom: 0; left: 0; width: 100%; padding: 20rpx 30rpx; background: white; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05); padding-bottom: env(safe-area-inset-bottom);}
.submit-btn { background-color: #FF85A2; color: white; height: 88rpx; line-height: 88rpx; border-radius: 44rpx; font-size: 32rpx; font-weight: bold; border: none;}
.submit-btn:active { opacity: 0.8; }
</style>