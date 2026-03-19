import { http } from "@/utils/request.js";

export const addPostanimalDaily = async (animalDaily) => {
  try {
    const res = await http({
      method: "POST",
      url: "/community/animalDaily",
      data: animalDaily,
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



export const listGetAnimalDaily = async (id)=>{
	try{
		if (!id) {
		      throw new Error('删除失败：缺少档案ID')
		}
		
		const res = await http({
			method: "GET",
			url: `/community/animalDaily/${id}`,
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

export const GetAnimalDailyDetail = async (id)=>{
	try{
		if (!id) {
		      throw new Error('删除失败：缺少档案ID')
		}
		
		const res = await http({
			method: "GET",
			url: `/community/animalDaily/detail/${id}`,
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

// ====== 新增：评论相关接口 ======

// 发表评论
export const addDailyComment = async (commentData) => {
  try {
    const res = await http({
      method: "POST",
      url: "/community/animalDailyComment",
      data: commentData,
    })
    console.log('评论发送成功:', res)
    return res
  } catch (error) {
    console.error('评论发送失败:', error)
    throw error 
  }
}

// 获取某个日常的评论列表
export const getDailyCommentsList = async (dailyId) => {
  try {
    if (!dailyId) throw new Error('缺少动态ID')
    
    const res = await http({
      method: "GET",
      url: `/community/animalDailyComment/list/${dailyId}`,
    })
    console.log('获取评论列表成功', res)
    return res
  } catch (error) {
    console.error('获取评论列表失败:', error)
    throw error 
  }
}