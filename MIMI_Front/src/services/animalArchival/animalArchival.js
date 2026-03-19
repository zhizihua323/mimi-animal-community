// 引入项目封装的 request 工具（注意变量名正确）
import { http } from "@/utils/request.js";  // 修复：这里应该用http而不是request

/**
 * 上传文件到 MinIO 服务器（适配后端 /community/minio/fileUpload 接口）
 * @param {String} filePath - UniApp选择的文件本地路径（如res.tempFilePaths[0]）
 * @returns {Promise<Object>} - 后端返回的响应结果（Result格式）
 */
export const uploadFileToMinio = async (filePath) => {
  try {
    // 1. 校验文件路径是否存在
    if (!filePath) {
      throw new Error("请选择要上传的文件");
    }

    console.log("准备上传文件，路径：", filePath);

    // 2. 针对UniApp环境，使用uni.uploadFile原生API更可靠
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: "http://127.0.0.1:8080/community/minio/fileUpload",  // 接口路径
        filePath: filePath,                  // 本地文件路径（UniApp要求）
        name: "file",                        // 与后端MultipartFile参数名一致
        header: {
          "token": uni.getStorageSync("token") || "",  // 携带token
          // 注意：不要手动设置Content-Type，UniApp会自动处理multipart/form-data
        },
        success: (uploadRes) => {
          console.log("上传响应状态码：", uploadRes.statusCode);
          console.log("上传原始响应数据：", uploadRes.data);

          // 解析响应数据（uni.uploadFile返回的data是字符串）
          let res;
          try {
            res = JSON.parse(uploadRes.data);
          } catch (e) {
            reject(new Error("后端返回格式错误，无法解析JSON"));
            return;
          }

          // 处理HTTP状态码
          if (uploadRes.statusCode === 200) {
            if (res.code === 200) {
              console.log("文件上传成功，返回数据：", res);
              resolve(res);
            } else {
              reject(new Error(`上传失败：${res.message || "未知错误"}`));
            }
          } else {
            reject(new Error(`请求失败，状态码：${uploadRes.statusCode}`));
          }
        },
        fail: (err) => {
          console.error("上传请求失败（网络层面）：", err);
          reject(new Error(`网络错误：${err.errMsg || "上传失败"}`));
        }
      });
    });

  } catch (error) {
    // 统一错误处理
    const errorMsg = error.message || "文件上传失败，请重试";
    console.error("文件上传到MinIO失败:", errorMsg);
    throw new Error(errorMsg);
  }
};


export const addPostanimalArchival = async (animalArchival) => {
  try {
    const res = await http({
      method: "POST",
      url: "/community/animalArchival",
      data: animalArchival,
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

export const listGetAnimalArchival = async ()=>{
	try{
		const res = await http({
			method: "GET",
			url: "/community/animalArchival",
		});
		
		// 关键：强制处理 records 中的 id，确保是字符串（双重保险）
		const handledRecords = res.data.records.map(item => ({
			...item,
			id: String(item.id) // 无论原类型是什么，强制转为字符串
		}));
		
		// 替换原 records 为处理后的版本
		res.data.records = handledRecords;
		
		// 验证：打印处理后的 ID 和类型
		console.log('处理后第一个ID:', handledRecords[0].id); // 如 "1962123288106307585"
		console.log('处理后ID类型:', typeof handledRecords[0].id); // 必须是 "string"
		
		console.log('数据获取成功', res);
		return res;
	} catch (error){
		console.error('数据获取失败:', error);
		uni.showToast({ title: '获取数据失败，请稍后重试', icon: 'none', duration: 2000 });
		throw error;
	}
}

export const deletePostAnimalArchivalById = async (id)=>{
	try{
		if (!id) {
		      throw new Error('删除失败：缺少档案ID')
		}
		
		const res = await http({
			method: "POST",
			url: `/community/animalArchival/${id}`,
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

export const queryGetAnimalArchivalById = async(id) => {
	try {
	  const res = await http({
	    method: "GET",
	    url: "/community/animalArchival/getOne",
		params: {
		    id: id // 传递ID参数，与后端接口参数名保持一致
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


// 更新动物档案接口（对应后端的/updated接口）
export const updateAnimalArchival = async (animalArchival) => {
  // 前置验证：确保传递了必要的ID信息
  if (!animalArchival || (!animalArchival.id && !animalArchival.stringId)) {
    throw new Error('更新失败：动物档案ID不能为空');
  }

  try {
    const res = await http({
      method: "POST",
      // 匹配后端的/update接口路径
      url: "/community/animalArchival/updated",
      // 传递完整的动物档案对象（后端@RequestBody接收）
      data: animalArchival,
    });

    console.log('动物档案更新成功:', res);
    return res;
  } catch (error) {
    // 增强错误信息，方便调试
    const errorMsg = `更新动物档案失败：${error.message || '网络异常或后端处理错误'}`;
    console.error(errorMsg, error);
    throw new Error(errorMsg);
  }
};
