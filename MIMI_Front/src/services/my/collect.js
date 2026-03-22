import { http } from "@/utils/request.js"; 

// 切换收藏状态 (加入/取消)
export const togglePostCollect = async (data) => {
  return await http({
    method: "POST",
    url: "/user/collect/toggle",
    data: data
  });
}

// 获取收藏列表
export const queryGetCollectList = async () => {
  return await http({
    method: "GET",
    url: "/user/collect/list"
  });
}