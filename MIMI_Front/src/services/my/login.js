import { http } from '@/utils/http.js'

// 正确的导出语法
export const login = async (code) => {
	console.log("这个是code" + code)
  try {
    const res = await http({
      method: "GET",
      url: `/user/wxLogin/wxLogin/${code}`, 
	  
    })
    return res
  } catch (error) {
    // 可以在这里统一处理错误
    throw error // 继续向上抛出错误，让调用方处理
  }
}