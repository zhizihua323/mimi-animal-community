<!-- pages/login/login.vue 完整结构 -->
<template>
  <!-- 必须有模板内容（哪怕是一个空容器），否则报错 -->
  <view class="login-page">
    <view class="login-title">微信登录</view>
    <!-- 微信登录按钮 -->
    <button 
      class="wechat-login-btn" 
      :loading="isLoading" 
      :disabled="isLoading"
      @click="handleWechatLogin"
    >
      微信快捷登录
    </button>
  </view>
</template>

<script setup>
// 必须有脚本标签（哪怕是空的，也可避免报错）
import { ref } from 'vue';
import { isLogin, toLogin } from '@/utils/auth';
import { login } from '@/services/my/login.js';

// 登录按钮加载状态
const isLoading = ref(false);

// 微信登录核心逻辑
const handleWechatLogin = async () => {
 uni.login({
  
       // 调用接口获取登录凭证（code）
  
       success: (Result) => {
		   console.log(Result)
  
         // 向后台发起request.login请求,用code换取用户登录态信息openid,存储为token;
  
         login( Result.code
		).then((token) => {
  
           // 存储用户登录态信息token
  
           uni.setStorageSync('token', token.data.token)
		   uni.switchTab({
		   	url:'/pages/index/index'
		   })
  
         }) .catch(error => {
  
           console.log("换取登录态token失败：",error)
  
         });
  
       },
  
       fail:(res)=> { console.log("获取登录凭证code失败！",res) }
  
     })

  }
;
</script>

<style scoped lang="scss">
/* 可选：添加样式美化 */
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  padding: 20rpx;
}
.login-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 60rpx;
}
.wechat-login-btn {
  width: 100%;
  height: 90rpx;
  background-color: #07c160;
  color: #fff;
  font-size: 32rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wechat-icon {
  width: 44rpx;
  height: 44rpx;
  margin-right: 16rpx;
}
</style>