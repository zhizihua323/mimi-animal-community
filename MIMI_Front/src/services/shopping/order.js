import { http } from "@/utils/request.js"; 

export const listPostOrder = async () => {
  try {
    const res = await http({
      method: "GET",
      url: "/shopping/order",
    })
    // 可以在这里处理返回数据
    console.log('数据上传成功:', res)
    return res
  } catch (error) {
    console.error('数据上传失败:', error)
    // 可以在这里统一处理错误
    throw error // 继续向上抛出错误，让调用方处理
  }
}



export const addPostOrder = async (order) => {
  try {
    const res = await http({
      method: "POST",
      url: "/shopping/order",
      data: order,
    })
    // 可以在这里处理返回数据
    console.log('数据上传成功:', res)
    return res
  } catch (error) {
    console.error('数据上传失败:', error)
    // 可以在这里统一处理错误
    throw error // 继续向上抛出错误，让调用方处理
  }
}

export const queryGetOrder = async(id) => {
	try {
	  const res = await http({
	    method: "GET",
	    url: "/shopping/order/getOne",
		params:{
			id:id
		}
	  })
	  // 可以在这里处理返回数据
	  console.log('数据上传成功:', res)
	  return res
	} catch (error) {
	  console.error('数据上传失败:', error)
	  // 可以在这里统一处理错误
	  throw error // 继续向上抛出错误，让调用方处理
	}
}