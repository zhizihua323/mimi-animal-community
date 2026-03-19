// 定义基础URL
const baseURL = 'http://127.0.0.1:8080';

// 修正了变量名拼写错误：htttp -> http
const httpInterceptor = {
  // invoke 是拦截前触发
  invoke(options) {
    // 修复括号不匹配的语法错误
    if (!options.url.startsWith('http')) {
      options.url = baseURL + options.url
      // 添加请求头
      options.header = {
        ...options.header,
        // 'source-client': 'miniapp',
        // // 合并原有header，避免覆盖
        // Cookie: 'acw_tc=0a47329d17550655503927839e724a464cdce35850b394d024ff91db0b6d26',
        // Authorization: '0a47329a17550565118754554e5a41f05ed74723f1d2fdf62a786cb2a54f5d'
      };
    }
    options.timeout = 10000; // 10秒超时
    console.log('请求配置:', options);
  }
}

// 只需要添加一次拦截器
uni.addInterceptor('request', httpInterceptor)

export const http = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      success(res) {
        // 可以在这里统一处理状态码，比如401未授权等
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data); // 直接返回数据部分
        } else {
          uni.showToast({
            icon: 'none',
            title: `请求错误: ${res.statusCode}`
          })
          reject(res)
        }
      },
      fail(err) {
        // 打印详细错误信息，方便排查
        console.error('请求失败:', err)
        uni.showToast({
          icon: 'none',
          title: `网络错误: ${err.errMsg || '超时'}`
        })
        reject(err)
      }
    })
  })
}
