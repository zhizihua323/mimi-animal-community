<template>
  <view class="container">
    <view class="navbar">
      <view class="logo">
        <text class="logo-text">Mimi Shop</text>
        <view class="logo-icon">🛍️</view>
      </view>
      <view class="search-bar">
        <input type="text" placeholder="搜索可爱好物..." class="search-input" v-model="searchKeyword" @confirm="onSearch" />
        <view class="search-icon" @click="onSearch">🔍</view>
      </view>
      <view class="user-icon">👤</view>
    </view>

    <view class="category-container">
      <scroll-view scroll-x="true" class="category-scroll">
        <view class="category-item" v-for="(item, index) in categories" :key="index" 
              :class="{ 'active': activeCategory === index }"
              @click="activeCategory = index">
          <view class="category-color" :style="{ backgroundColor: item.color }"></view>
          <text class="category-name">{{ item.category }}</text>
        </view>
      </scroll-view>
    </view>

    <view class="goods-container">
      <view class="section-title">
        <text class="title-text">可爱好物推荐</text>
      </view>

      <view class="goods-grid">
        <view class="goods-card" v-for="(goods, index) in filteredGoods" :key="index" 
              @click="showGoodsDetail(goods)">
          <view class="goods-image-container">
            <image :src="goods.picUrl" mode="widthFix" class="goods-image"></image>
            <view class="collect-icon" :class="{ collected: goods.isCollected > 0 }" 
                  @click.stop="toggleCollect(goods, index)">
              {{ goods.isCollected > 0 ? '💖' : '🤍' }}
            </view>
          </view>
          <view class="goods-info">
            <text class="goods-material">{{ goods.goodsName }}</text>
            <view class="goods-price-weight">
              <text class="goods-price">¥{{ goods.price.toFixed(2) }}</text>
              <text class="goods-weight">{{ goods.weight }}kg</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="floating-cart" @click="gotoRealCart">
      <text class="cart-icon">🛒</text>
    </view>

    <view class="detail-modal" v-if="showDetail">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">商品详情</text>
          <view class="close-icon" @click="showDetail = false">✕</view>
        </view>
        <view class="detail-image-container">
          <image :src="currentGoods.picUrl" mode="widthFix" class="detail-image"></image>
        </view>
        <view class="detail-info">
          <text class="detail-material">{{ currentGoods.goodsName }}</text>
          <text class="detail-material">材质: {{ currentGoods.material }}</text>
          <text class="detail-category">分类: {{ getCategoryName(currentGoods.categoryId) }}</text>
          <text class="detail-price">价格: ¥{{ currentGoods.price.toFixed(2) }}</text>
        </view>
        
        <view class="modal-action-btns">
          <button class="action-btn add-cart-btn" @click="addToCart(currentGoods)">加入购物车</button>
          <button class="action-btn buy-now-btn" @click="buyNow(currentGoods)">立即购买</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { queryGetGoods, queryGetCategoryFamily} from "@/services/shopping/shopping.js"
import { addPostOrder } from "@/services/shopping/order.js"
import { addPostCart } from "@/services/shopping/cart.js" 
import { togglePostCollect, queryGetCollectList } from "@/services/my/collect.js";

const activeCategory = ref(0);
const showDetail = ref(false);
const currentGoods = ref(null);
const loading = ref(false);
const searchKeyword = ref(''); // 搜索词

const categories = ref([{ id: null, category: '全部商品', color: '#FF9EBB' }]);
const goodsList = ref([]);

onShow(() => {
  getGoods();
});

onMounted(() => {
  getCategoryFamily();
  getGoods();
})

// 1. 搜索功能：因为是实时计算过滤，这里点回车只需要隐藏键盘即可
const onSearch = () => {
  uni.hideKeyboard();
}

// 跳转到真正的购物车页面
const gotoRealCart = () => {
  uni.navigateTo({
    url: '/pages/shopping_cart/shopping_cart' 
  });
}

// 2. 获取分类（修复 .map is not a function 的数据解析问题）
const getCategoryFamily = async () => {
  try {
    const response = await queryGetCategoryFamily();
    
    // 安全地提取数组：不论后端返回的是直接的数组，还是包在 records 里的数组
    let resData = [];
    if (Array.isArray(response.data)) {
      resData = response.data;
    } else if (response.data && Array.isArray(response.data.records)) {
      resData = response.data.records;
    }

    // 如果数据库里没配置分类 (提取出来的数组是空的)，用默认分类兜底
    if (resData.length === 0) {
      resData = [
        { id: 1, name: '服饰穿搭' },
        { id: 2, name: '玩具零食' },
        { id: 3, name: '日常用品' },
        { id: 4, name: '出行装备' }
      ];
    }

    // 分配好看的颜色池
    const colors = ['#FF9EBB', '#A8DADC', '#FFD6BA', '#B8E1FF', '#C8E6C9', '#FFB7B2', '#E1BEE7'];
    
    // 映射数据
    const backendCategories = resData.map((item, index) => ({
      id: item.id,
      category: item.category || item.name || '未命名分类',
      color: colors[index % colors.length]
    }));
    
    // 拼接“全部商品”到最前面
    categories.value = [{ id: null, category: '全部商品', color: '#FF9EBB' }, ...backendCategories];
    
  } catch (error) {
    console.error('获取分类失败:', error);
  }
}

// 3. 核心：终极商品过滤器 (分类过滤 + 关键词搜索 完美结合)
const filteredGoods = computed(() => {
  let result = goodsList.value;

  // 第一步：按照分类过滤
  if (activeCategory.value !== 0 && categories.value[activeCategory.value]?.id) {
    const categoryId = categories.value[activeCategory.value].id;
    result = result.filter(goods => goods.categoryId === categoryId);
  }

  // 第二步：按照搜索词过滤 (实时模糊搜索)
  if (searchKeyword.value.trim() !== '') {
    const keyword = searchKeyword.value.trim().toLowerCase();
    result = result.filter(goods => 
      (goods.goodsName && goods.goodsName.toLowerCase().includes(keyword)) ||
      (goods.material && goods.material.toLowerCase().includes(keyword))
    );
  }

  return result;
});

// 获取商品数据 (加入交叉比对逻辑)
const getGoods = async () => {
  try {
    loading.value = true;
    // 1. 获取所有商品
    const response = await queryGetGoods();
    const records = response.data?.records || response.data || [];
    
    // 2. 获取当前用户的真实收藏列表
    let myCollectIds = [];
    try {
      const collectRes = await queryGetCollectList();
      const myCollects = collectRes.data || [];
      // 提取我收藏的所有 targetId
      myCollectIds = myCollects.map(item => String(item.targetId)); 
    } catch (e) {
      console.error('获取收藏列表失败，跳过比对', e);
    }

    // 3. 交叉比对，给商品打上个人专属标签 isCollected
    goodsList.value = records.map(goods => {
      // 提取安全的 ID 字符串
      const safeId = String(goods.stringId || goods.id);
      return {
        ...goods,
        // 如果我的收藏列表里有这个商品ID，isCollected 就是 1，否则是 0
        isCollected: myCollectIds.includes(safeId) ? 1 : 0
      }
    });
  } catch (error) {
    console.error('获取商品失败:', error);
  } finally {
    loading.value = false;
  }
}

// 真实触发后端收藏逻辑 (使用 isCollected 替代原来的 collect)
const toggleCollect = async (goods, index) => {
  // 乐观锁：用 isCollected 记录个人状态
  const originalStatus = goods.isCollected;
  goods.isCollected = originalStatus > 0 ? 0 : 1; 
  
  const payload = {
    targetId: goods.stringId || goods.id, 
    targetType: 1,
    title: goods.goodsName || '未知商品',
    picUrl: goods.picUrl || '',
    summary: `¥${(goods.price || 0).toFixed(2)}` 
  };

  try {
    await togglePostCollect(payload);
    uni.showToast({ 
      title: goods.isCollected > 0 ? '收藏成功' : '已取消收藏', 
      icon: 'none' 
    });
  } catch (error) {
    goods.isCollected = originalStatus; // 失败回滚
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const showGoodsDetail = (goods) => {
  currentGoods.value = { ...goods };
  showDetail.value = true;
};

const getCategoryName = (categoryId) => {
  const category = categories.value.find(cat => cat.id === categoryId);
  return category ? category.category : '未分类';
};

// 加入购物车逻辑
const addToCart = async (goods) => {
  uni.showLoading({ title: '加入中...' });
  try {
    await addPostCart({
      goodsId: goods.id,
      goodsName: goods.goodsName,
      picUrl: goods.picUrl,
      price: goods.price,
      num: 1
    });
    uni.hideLoading();
    uni.showToast({ title: '已加入购物车', icon: 'success' });
    showDetail.value = false; 
  } catch (err) {
    uni.hideLoading();
    uni.showToast({ title: '加入失败', icon: 'none' });
  }
}

// 立即购买逻辑
const buyNow = async (currentGoods) => {
  try {
    loading.value = true;
    const response = await addPostOrder({
      goodsId: currentGoods.id,
      goodsName: currentGoods.goodsName
    });
    const order = response.data || response;
    
    if (order) {
      uni.navigateTo({
        url: `/pages/shopping_pay/shopping_pay?id=${order.stringId || order.id}&goodsName=${encodeURIComponent(currentGoods.goodsName)}`
      });
      showDetail.value = false;
    }
  } catch (error) {
    console.error('立即购买失败:', error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.container { display: flex; flex-direction: column; min-height: 100vh; background-color: #FFF5F7; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
.navbar { display: flex; align-items: center; padding: 16rpx 24rpx; background-color: #FF85A2; color: white; }
.logo { display: flex; align-items: center; font-weight: bold; font-size: 36rpx; margin-right: 24rpx; }
.logo-text { margin-right: 12rpx; }
.logo-icon { font-size: 40rpx; }
.search-bar { flex: 1; display: flex; align-items: center; background-color: white; border-radius: 30rpx; padding: 12rpx 20rpx; color: #ccc; }
.search-input { flex: 1; font-size: 28rpx; border: none; outline: none; color: #333; }
.search-icon { font-size: 30rpx; color: #FF85A2; }
.user-icon { font-size: 40rpx; margin-left: 24rpx; }
.category-container { padding: 20rpx; background-color: white; }
.category-scroll { white-space: nowrap; padding: 10rpx 0; }
.category-item { display: inline-flex; align-items: center; margin-right: 30rpx; padding: 10rpx 16rpx; border-radius: 30rpx; background-color: #FFF0F3; transition: all 0.3s ease; }
.category-item.active { background-color: #FFE0E9; box-shadow: 0 4rpx 8rpx rgba(255, 133, 162, 0.3); }
.category-color { width: 16rpx; height: 16rpx; border-radius: 50%; margin-right: 12rpx; }
.category-name { font-size: 28rpx; color: #333; }
.goods-container { flex: 1; padding: 20rpx; }
.section-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.title-text { font-size: 34rpx; font-weight: bold; color: #FF6B8B; }
.view-more { font-size: 26rpx; color: #FF85A2; }
.goods-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24rpx; }
.goods-card { background-color: white; border-radius: 24rpx; overflow: hidden; box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05); }
.goods-image-container { position: relative; width: 100%; height: 320rpx; overflow: hidden; }
.goods-image { width: 100%; height: 100%; object-fit: cover; }
.collect-icon { position: absolute; top: 16rpx; right: 16rpx; font-size: 40rpx; background-color: rgba(255, 255, 255, 0.8); border-radius: 50%; width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; }
.collect-icon.collected { color: #FF4D6D; }
.goods-info { padding: 16rpx; }
.goods-material { font-size: 28rpx; color: #333; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.goods-price-weight { display: flex; justify-content: space-between; align-items: center; margin-top: 8rpx; }
.goods-price { font-size: 30rpx; font-weight: bold; color: #FF4D6D; }
.goods-weight { font-size: 24rpx; color: #999; }
.detail-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0, 0, 0, 0.5); display: flex; justify-content: center; align-items: center; z-index: 999; padding: 40rpx; }
.modal-content { width: 100%; max-width: 600rpx; background-color: white; border-radius: 30rpx; overflow: hidden; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 24rpx; background-color: #FF85A2; color: white; }
.modal-title { font-size: 32rpx; font-weight: bold; }
.close-icon { font-size: 36rpx; }
.detail-image-container { width: 100%; height: 400rpx; }
.detail-image { width: 100%; height: 100%; object-fit: cover; }
.detail-info { padding: 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.detail-material, .detail-price, .detail-category { font-size: 30rpx; color: #ffffff; text-shadow: 1px 1px 0 #000; font-weight: 500; }
.detail-price { color: #ff4d6d; font-weight: bold; font-size: 32rpx; }

/* 新增：底部操作按钮区 */
.modal-action-btns {
  display: flex;
  justify-content: space-between;
  padding: 0 24rpx 24rpx;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  padding: 16rpx 0;
}

.add-cart-btn {
  background-color: #FFF0F3;
  color: #FF85A2;
  border: 2rpx solid #FF85A2;
}

.buy-now-btn {
  background-color: #FF85A2;
  color: white;
}

/* 新增：悬浮购物车图标 */
.floating-cart {
  position: fixed;
  right: 40rpx;
  bottom: 150rpx;
  width: 100rpx;
  height: 100rpx;
  background-color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 8rpx 20rpx rgba(255, 133, 162, 0.4);
  z-index: 100;
  transition: transform 0.2s;
}

.floating-cart:active {
  transform: scale(0.9);
}

.cart-icon {
  font-size: 50rpx;
}
</style>