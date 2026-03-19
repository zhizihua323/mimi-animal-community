import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 用户信息
    userInfo: uni.getStorageSync('userInfo') || null,
    // 登录令牌
    token: uni.getStorageSync('token') || '',
    // 是否登录
    isLogin: !!uni.getStorageSync('token')
  }),
  
  actions: {
    // 保存用户信息
    saveUserInfo(info) {
      this.userInfo = info
      this.isLogin = true
      uni.setStorageSync('userInfo', info)
    },
    
    // 保存令牌
    saveToken(token) {
      this.token = token
      this.isLogin = true
      uni.setStorageSync('token', token)
    },
    
    // 退出登录
    logout() {
      this.userInfo = null
      this.token = ''
      this.isLogin = false
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('token')
      
      // 退出后跳转到登录页
      uni.reLaunch({
        url: '/pages/login/login'
      })
    },
    
    // 登录方法
    async login(loginInfo = {}) {
      try {
        // 显示加载中
        uni.showLoading({
          title: '登录中...'
        })
        
        // 调用 uni.login 获取code
        const [loginErr, loginRes] = await uni.login().catch(err => [err, null])
        if (loginErr || !loginRes.code) {
          throw new Error('获取登录凭证失败')
        }
        
        // 构建登录参数
        const params = {
          code: loginRes.code,
          ...loginInfo
        }
        
        // 调用后端登录接口（请替换为你的实际接口）
        const [err, res] = await uni.request({
          url: '/api/auth/login',
          method: 'POST',
          data: params
        }).catch(e => [e, null])
        
        if (err || res.statusCode !== 200 || !res.data.success) {
          throw new Error(res.data?.message || '登录失败，请重试')
        }
        
        // 保存登录信息
        this.saveToken(res.data.data.token)
        this.saveUserInfo(res.data.data.userInfo)
        
        // 登录成功提示
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
        
        // 返回首页或之前的页面
        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/index/index'
          })
        }, 1000)
        
        return true
      } catch (error) {
        // 显示错误信息
        uni.showToast({
          title: error.message,
          icon: 'none'
        })
        return false
      } finally {
        // 隐藏加载中
        uni.hideLoading()
      }
    }
  }
})
    