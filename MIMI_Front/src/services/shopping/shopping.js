// 引入项目封装的 request 工具（注意变量名正确）
import { http } from "@/utils/request.js"; 

export const queryGetGoods = async() => {
	try {
	  const res = await http({
	    method: "GET",
	    url: "/shopping/goods",
		
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

export const queryGetCategoryFamily = async(id) => {
	try {
	  const res = await http({
	    method: "GET",
	    url: "/shopping/categoryFamily",
			
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

// ==================== 追加以下代码 ====================

// 上传商品图片到 MinIO
export const uploadGoodsImageToMinio = async (filePath) => {
  try {
    if (!filePath) {
      throw new Error("请选择要上传的文件");
    }

    console.log("准备上传商品图片，路径：", filePath);

    return new Promise((resolve, reject) => {
      uni.uploadFile({
        // 指向我们在第一步用 Java 写的商品专属 MinIO 接口
        // 注意：如果你的基础 URL 有统一个配置，可以拼接上。这里先写死用于测试
        url: "http://127.0.0.1:8080/shopping/minio/fileUpload", 
        filePath: filePath,
        name: "file",
        header: {
          "token": uni.getStorageSync("token") || ""
        },
        success: (uploadRes) => {
          let res;
          try {
            res = JSON.parse(uploadRes.data);
          } catch (e) {
            reject(new Error("后端返回格式错误，无法解析JSON"));
            return;
          }

          if (uploadRes.statusCode === 200) {
            if (res.code === 200) {
              console.log("商品图片上传成功：", res);
              resolve(res);
            } else {
              reject(new Error(`上传失败：${res.message || "未知错误"}`));
            }
          } else {
            reject(new Error(`请求失败，状态码：${uploadRes.statusCode}`));
          }
        },
        fail: (err) => {
          console.error("上传请求失败：", err);
          reject(new Error(`网络错误：${err.errMsg || "上传失败"}`));
        }
      });
    });

  } catch (error) {
    const errorMsg = error.message || "文件上传失败，请重试";
    console.error("商品图片上传到MinIO失败:", errorMsg);
    throw new Error(errorMsg);
  }
};

// 发布商品的接口，保持接口管理的整洁
export const publishGoods = async(data) => {
  try {
    const res = await http({
      method: "POST",
      url: "/shopping/goods/publish",
      data: data
    })
    return res
  } catch (error) {
    console.error('发布商品失败:', error)
    throw error 
  }
}