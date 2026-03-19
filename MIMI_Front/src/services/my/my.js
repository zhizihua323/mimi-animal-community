import { http } from '@/utils/http.js'

// 正确的导出语法
export const getData = async () => {
  try {
    const res = await http({
      method: "GET",
      url: "/random/image",
    })
    // 可以在这里处理返回数据
    console.log('获取图片数据成功:', res)
    return res
  } catch (error) {
    console.error('获取图片数据失败:', error)
    // 可以在这里统一处理错误
    throw error // 继续向上抛出错误，让调用方处理
  }
}
    