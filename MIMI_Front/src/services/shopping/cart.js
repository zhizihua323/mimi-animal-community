import { http } from "@/utils/request.js"; 

// 加入购物车
export const addPostCart = async (cartData) => {
  return await http({
    method: "POST",
    url: "/shopping/cart",
    data: cartData
  });
}

// 获取购物车列表
export const queryGetCart = async () => {
  return await http({
    method: "GET",
    url: "/shopping/cart"
  });
}

// 更新购物车 (数量、选中状态)
export const updatePutCart = async (cartData) => {
  return await http({
    method: "PUT",
    url: "/shopping/cart",
    data: cartData
  });
}

// 删除购物车商品
export const deletePostCart = async (ids) => {
  return await http({
    method: "POST",
    url: `/shopping/cart/delete`,
    data: ids // 直接把 ID 数组塞进 body 里
  });
}