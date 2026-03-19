<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="logo">
        <text class="logo-text">Mimi Shop</text>
        <view class="logo-icon">🛍️</view>
      </view>
      <view class="search-bar">
        <input type="text" placeholder="搜索可爱好物..." class="search-input" />
        <view class="search-icon">🔍</view>
      </view>
      <view class="user-icon">👤</view>
    </view>

    <!-- 分类导航 -->
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

    <!-- 商品展示区 -->
    <view class="goods-container">
      <view class="section-title">
        <text class="title-text">可爱好物推荐</text>
        <text class="view-more">查看全部 →</text>
      </view>

      <view class="goods-grid">
        <view class="goods-card" v-for="(goods, index) in filteredGoods" :key="index" 
              @click="showGoodsDetail(goods)">
          <view class="goods-image-container">
            <image :src="goods.picUrl" mode="widthFix" class="goods-image"></image>
            <view class="collect-icon" :class="{ collected: goods.collect > 0 }" 
                  @click.stop="toggleCollect(goods, index)">
              {{ goods.collect > 0 ? '💖' : '🤍' }}
            </view>
          </view>
          <view class="goods-info">
			<text class="goods-material">{{ goods.goodsName }}</text>
            <!-- <text class="goods-material">{{ goods.material }}</text> -->
            <view class="goods-price-weight">
              <text class="goods-price">¥{{ goods.price.toFixed(2) }}</text>
              <text class="goods-weight">{{ goods.weight }}kg</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部导航 -->
<!--    <view class="tabbar">
      <view class="tab-item active">
        <view class="tab-icon">🏠</view>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item">
        <view class="tab-icon">🔖</view>
        <text class="tab-text">分类</text>
      </view>
      <view class="tab-item">
        <view class="tab-icon">🛒</view>
        <text class="tab-text">购物车</text>
      </view>
      <view class="tab-item">
        <view class="tab-icon">❤️</view>
        <text class="tab-text">收藏</text>
      </view>
    </view> -->

    <!-- 商品详情弹窗 -->
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
          <!-- <text class="detail-weight">重量: {{ currentGoods.weight }}kg</text> -->
          <text class="detail-category">分类: {{ getCategoryName(currentGoods.categoryId) }}</text>
		  <text class="detail-price">价格: ¥{{ currentGoods.price.toFixed(2) }}</text>
        </view>
        <button class="add-to-cart" @click="addCart(currentGoods)">立即购买 🛒</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { queryGetGoods, queryGetCategoryFamily} from "@/services/shopping/shopping.js"
import { addPostOrder } from "@/services/shopping/order.js"

onMounted(async()=>{
	getCategoryFamily();
	getGoods();
})

const postOrder = async(order) =>{
	try{
		loading.value = true // 显示加载状态
		const response = await addPostOrder(order);
		return response.data
		
	}catch (error) {
		console.error('处理数据失败:', error)
	} finally {
		loading.value = false // 关闭加载状态
	}
}

const getCategoryFamily = async () => {
  try {
    loading.value = true // 显示加载状态
    // 传递分页参数
    const response = await queryGetCategoryFamily()

    // total.value = response.total
  } catch (error) {
    console.error('处理数据失败:', error)
  } finally {
    loading.value = false // 关闭加载状态
  }
}

const getGoods = async () => {
  try {
    loading.value = true // 显示加载状态
    // 传递分页参数
    const response = await queryGetGoods()
    
    // 假设后端返回格式为 { records: [], total: 0, ... }
    goodsList.value = response.data.records
	console.log("animals的值",animals.value)
    // total.value = response.total
  } catch (error) {
    console.error('处理数据失败:', error)
  } finally {
    loading.value = false // 关闭加载状态
  }
}


// 模拟分类数据
const categories = ref([
  { id: 1, category: '全部商品', color: '#FF9EBB' },
  { id: 2, category: '美妆护肤', color: '#A8DADC' },
  { id: 3, category: '家居用品', color: '#FFD6BA' },
  { id: 4, category: '数码配件', color: '#B8E1FF' },
  { id: 5, category: '服饰鞋帽', color: '#C8E6C9' },
  { id: 6, category: '零食饮料', color: '#FFB7B2' },
  { id: 7, category: '文具玩具', color: '#E1BEE7' }
]);

const loading = ref(false)

// 模拟商品数据
const goodsList = ref([
  {
    id: 1,
    categoryId: 2,
    price: 89.99,
    material: '天然植物精华',
    weight: 0.35,
    picUrl: 'https://picsum.photos/seed/cosmetic1/300/300',
    collect: 0,
    stringId: 'g001'
  },
  {
    id: 2,
    categoryId: 3,
    price: 45.50,
    material: '环保陶瓷',
    weight: 1.2,
    picUrl: 'https://picsum.photos/seed/home1/300/300',
    collect: 1,
    stringId: 'g002'
  },
  {
    id: 3,
    categoryId: 4,
    price: 129.00,
    material: '硅胶材质',
    weight: 0.2,
    picUrl: 'https://picsum.photos/seed/digital1/300/300',
    collect: 0,
    stringId: 'g003'
  },
  {
    id: 4,
    categoryId: 5,
    price: 199.99,
    material: '纯棉面料',
    weight: 0.5,
    picUrl: 'https://picsum.photos/seed/clothes1/300/300',
    collect: 0,
    stringId: 'g004'
  },
  {
    id: 5,
    categoryId: 6,
    price: 25.80,
    material: '进口原料',
    weight: 0.15,
    picUrl: 'https://picsum.photos/seed/food1/300/300',
    collect: 1,
    stringId: 'g005'
  },
  {
    id: 6,
    categoryId: 7,
    price: 36.00,
    material: '安全塑料',
    weight: 0.4,
    picUrl: 'https://picsum.photos/seed/stationery1/300/300',
    collect: 0,
    stringId: 'g006'
  },
  {
    id: 7,
    categoryId: 2,
    price: 68.50,
    material: '有机成分',
    weight: 0.25,
    picUrl: 'https://picsum.photos/seed/cosmetic2/300/300',
    collect: 0,
    stringId: 'g007'
  },
  {
    id: 8,
    categoryId: 3,
    price: 89.00,
    material: '优质木材',
    weight: 1.8,
    picUrl: 'https://picsum.photos/seed/home2/300/300',
    collect: 1,
    stringId: 'g008'
  }
]);

// 状态管理
const activeCategory = ref(0);
const showDetail = ref(false);
const currentGoods = ref(null);

// 过滤商品列表
const filteredGoods = computed(() => {
  if (activeCategory.value === 0) {
    return goodsList.value;
  }
  const categoryId = categories.value[activeCategory.value].id;
  return goodsList.value.filter(goods => goods.categoryId === categoryId);
});

// 方法
const toggleCollect = (goods, index) => {
  goods.collect = goods.collect > 0 ? 0 : 1;
  // 添加收藏动画效果
  const element = document.querySelector(`.goods-card:nth-child(${index + 1}) .collect-icon`);
  if (element) {
    element.classList.add('bounce');
    setTimeout(() => {
      element.classList.remove('bounce');
    }, 500);
  }
};

const showGoodsDetail = (goods) => {
  currentGoods.value = { ...goods };
  showDetail.value = true;
};

const getCategoryName = (categoryId) => {
  const category = categories.value.find(cat => cat.id === categoryId);
  return category ? category.category : '未知分类';
};

const addCart = async(currentGoods) => {
  // 模拟加入购物车动画
  // const button = document.querySelector('.add-to-cart');
  // button.classList.add('added');
  // setTimeout(() => {
  //   button.classList.remove('added');
  //   showDetail.value = false;
  //   // 这里可以添加实际加入购物车的逻辑
  // }, 1000);
  const order = await postOrder({
	  goodsId:currentGoods.id,
	  goodsName:currentGoods.goodsName
  });
  
  uni.navigateTo({
    // 正确格式：?key1=value1&key2=value2（无空格，用&连接）
    url: `/pages/shopping_pay/shopping_pay?id=${order.stringId}&goodsName=${encodeURIComponent(order.goodsName)}`,
    fail: (err) => {
      console.error('跳转到订单支付页失败：', err);
      uni.showToast({ title: '跳转失败，请重试', icon: 'none' });
    }
  });
  
};
</script>

<style scoped>
/* 全局样式 */
.container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #FFF5F7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

/* 顶部导航栏 */
.navbar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background-color: #FF85A2;
  color: white;
}

.logo {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 36rpx;
  margin-right: 24rpx;
}

.logo-text {
  margin-right: 12rpx;
}

.logo-icon {
  font-size: 40rpx;
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 30rpx;
  padding: 12rpx 20rpx;
  color: #ccc;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  border: none;
  outline: none;
  color: #333;
}

.search-icon {
  font-size: 30rpx;
  color: #FF85A2;
}

.user-icon {
  font-size: 40rpx;
  margin-left: 24rpx;
}

/* 分类导航 */
.category-container {
  padding: 20rpx;
  background-color: white;
}

.category-scroll {
  white-space: nowrap;
  padding: 10rpx 0;
}

.category-item {
  display: inline-flex;
  align-items: center;
  margin-right: 30rpx;
  padding: 10rpx 16rpx;
  border-radius: 30rpx;
  background-color: #FFF0F3;
  transition: all 0.3s ease;
}

.category-item.active {
  background-color: #FFE0E9;
  box-shadow: 0 4rpx 8rpx rgba(255, 133, 162, 0.3);
}

.category-color {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.category-name {
  font-size: 28rpx;
  color: #333;
}

/* 商品展示区 */
.goods-container {
  flex: 1;
  padding: 20rpx;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.title-text {
  font-size: 34rpx;
  font-weight: bold;
  color: #FF6B8B;
}

.view-more {
  font-size: 26rpx;
  color: #FF85A2;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.goods-card {
  background-color: white;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.goods-card:hover {
  transform: translateY(-6rpx);
  box-shadow: 0 12rpx 24rpx rgba(0, 0, 0, 0.1);
}

.goods-image-container {
  position: relative;
  width: 100%;
  height: 320rpx;
  overflow: hidden;
}

.goods-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.goods-card:hover .goods-image {
  transform: scale(1.05);
}

.collect-icon {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  font-size: 40rpx;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.collect-icon.collected {
  color: #FF4D6D;
}

.collect-icon.bounce {
  animation: bounce 0.5s ease;
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

.goods-info {
  padding: 16rpx;
}

.goods-material {
  font-size: 28rpx;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-price-weight {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8rpx;
}

.goods-price {
  font-size: 30rpx;
  font-weight: bold;
  color: #FF4D6D;
}

.goods-weight {
  font-size: 24rpx;
  color: #999;
}

/* 底部导航 */
.tabbar {
  display: flex;
  justify-content: space-around;
  padding: 16rpx 0;
  background-color: white;
  border-top: 1px solid #F5F5F5;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
  transition: color 0.3s ease;
}

.tab-item.active {
  color: #FF6B8B;
}

.tab-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.tab-text {
  font-size: 24rpx;
}

/* 商品详情弹窗 */
.detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
  padding: 40rpx;
}

.modal-content {
  width: 100%;
  max-width: 600rpx;
  background-color: white;
  border-radius: 30rpx;
  overflow: hidden;
  animation: modalIn 0.3s ease;
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: translateY(50rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background-color: #FF85A2;
  color: white;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
}

.close-icon {
  font-size: 36rpx;
}

.detail-image-container {
  width: 100%;
  height: 400rpx;
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.detail-material, .detail-price, .detail-weight, .detail-category {
  font-size: 30rpx; /* 略微增大字体提升可读性 */
  color: #ffffff; /* 白色文字 */
  text-shadow: 1rpx 1rpx 0 #000000, /* 右下阴影 */
               -1rpx -1rpx 0 #000000, /* 左上阴影 */
               1rpx -1rpx 0 #000000, /* 右上阴影 */
               -1rpx 1rpx 0 #000000; /* 左下阴影 */
  font-weight: 500; /* 适当加粗增强质感 */
  padding: 6rpx 0; /* 增加垂直间距 */
}

/* 价格可以特殊处理，更突出 */
.detail-price {
  color: #ff4d6d; /* 保持原有粉色调 */
  font-weight: bold;
  font-size: 32rpx;
  text-shadow: 1rpx 1rpx 0 rgba(0,0,0,0.3); /* 更柔和的阴影 */
}

.add-to-cart {
  width: 90%;
  margin: 0 auto 24rpx;
  padding: 20rpx 0;
  background-color: #FF85A2;
  color: white;
  border: none;
  border-radius: 30rpx;
  font-size: 30rpx;
  font-weight: bold;
  transition: all 0.3s ease;
}

.add-to-cart.added {
  background-color: #4CAF50;
}

.add-to-cart.added::after {
  content: " ✓ 已加入";
}
</style>
