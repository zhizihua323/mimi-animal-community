import { http } from "@/utils/request.js"; 

export const queryGetAddress = async() => {
	try {
	  const res = await http({
	    method: "GET",
	    url: "/shopping/address",
	  })
	  // 可以在这里处理返回数据
	  console.log('数据获取成功:', res)
	  return res
	} catch (error) {
	  console.error('数据获取失败:', error)
	  // 可以在这里统一处理错误
	  throw error // 继续向上抛出错误，让调用方处理
	}
}

export const addPostAddress = async (address) => {
  try {
    const res = await http({
      method: "POST",
      url: "/shopping/address",
      data: address,
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

export const deletePostAddressById = async (id)=>{
	try{
		if (!id) {
		      throw new Error('删除失败：缺少ID')
		}
		
		const res = await http({
			method: "POST",
			url: `/shopping/address/${id}`,
		})
		console.log('数据获取成功', res)
		return res
	} catch (error){
		console.error('数据获取失败:', error)
		uni.showToast({
		      title: '获取数据失败，请稍后重试',
		      icon: 'none',
		      duration: 2000
		    })
		// 可以在这里统一处理错误
		throw error // 继续向上抛出错误，让调用方处理
	}
}

// 修改地址接口 (对应后端的 @PutMapping)
export const updatePostAddress = async (address) => {
  try {
    const res = await http({
      method: "PUT", 
      url: "/shopping/address",
      data: address,
    })
    console.log('数据更新成功:', res)
    return res
  } catch (error) {
    console.error('数据更新失败:', error)
    throw error 
  }
}