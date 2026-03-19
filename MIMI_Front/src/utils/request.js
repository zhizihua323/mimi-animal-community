// 定义基础URL
const baseURL = 'http://127.0.0.1:8080';
// 本地虚拟机地址
// const baseURL = 'http://192.168.119.128:8080';

// HTTP拦截器
const httpInterceptor = {
  invoke(options) {
    // 拼接基础URL
    if (!options.url.startsWith('http')) {
      options.url = baseURL + options.url;
    }
    
    // 获取token
    const token = uni.getStorageSync('token');
    
    // 处理请求头
    options.header = {
      ...options.header,
      'Content-Type': 'application/json',
      ...(token ? { token: token } : {}) // 有token才添加
    };
    
    // 设置超时时间
    options.timeout = 10000; // 10秒超时
    
    console.log('请求配置:', options);
  }
};

// 添加拦截器
uni.addInterceptor('request', httpInterceptor);

// 处理大数字ID的函数
const handleLargeNumberIds = (data) => {
  if (typeof data !== 'object' || data === null) return data;
  
  // 递归处理对象和数组
  const process = (obj) => {
    if (Array.isArray(obj)) {
      return obj.map(item => process(item));
    } else if (obj !== null && typeof obj === 'object') {
      const newObj = { ...obj };
      // 处理id字段（如果是大数字则转为字符串）
      if (newObj.id && typeof newObj.id === 'number' && newObj.id.toString().length > 16) {
        newObj.id = newObj.id.toString();
      }
      // 处理嵌套对象
      Object.keys(newObj).forEach(key => {
        if (typeof newObj[key] === 'object' && newObj[key] !== null) {
          newObj[key] = process(newObj[key]);
        }
      });
      return newObj;
    }
    return obj;
  };
  
  return process(data);
};

// 手动拼接URL参数的工具函数（替代URLSearchParams）
const encodeParams = (params) => {
  if (!params || typeof params !== 'object') return '';
  
  const parts = [];
  for (const key in params) {
    if (params.hasOwnProperty(key)) {
      const value = params[key];
      // 处理数组和特殊值
      if (Array.isArray(value)) {
        value.forEach(v => {
          parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(v)}`);
        });
      } else if (value !== null && value !== undefined) {
        parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
      }
    }
  }
  return parts.join('&');
};

// 最终的HTTP请求函数
export const http = (options) => {
  return new Promise((resolve, reject) => {
    // 处理GET请求参数（使用兼容方式）
    if (options.method?.toUpperCase() === 'GET' && options.params) {
      const paramsStr = encodeParams(options.params);
      if (paramsStr) {
        options.url += (options.url.includes('?') ? '&' : '?') + paramsStr;
      }
    }
    
    uni.request({
      ...options,
      success(res) {
        // 处理HTTP状态码
        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            // 解析JSON
            const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
            
            // 处理大数字ID
            const handledData = handleLargeNumberIds(data);
            
            resolve(handledData);
          } catch (err) {
            console.error('JSON解析失败:', err);
            resolve(res.data); // 解析失败时返回原始数据
          }
        } else {
          uni.showToast({
            icon: 'none',
            title: `请求错误: ${res.statusCode}`
          });
          reject(res);
        }
      },
      fail(err) {
        console.error('请求失败:', err);
        uni.showToast({
          icon: 'none',
          title: `网络错误: ${err.errMsg || '请稍后重试'}`
        });
        reject(err);
      }
    });
  });
};
