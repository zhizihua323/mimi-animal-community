<template>
  <view class="category-container">
    <view class="navbar">
      <view class="back-btn" @click="navigateBack">←</view>
      <text class="title">动物循迹</text>
      <view class="empty"></view>
    </view>

    <view class="search-box">
      <view class="search-icon">🔍</view>
      <input type="text" class="search-input" placeholder="搜索动物名称或种类..." v-model="searchQuery" @input="handleSearch" />
    </view>

    <view class="content">
      <view class="left-menu">
        <view 
          class="menu-item" 
          :class="{ active: activeSpecies === 'all' }" 
          @click="handleSpeciesClick('all')"
        >
          全部动物
        </view>
        <view 
          class="menu-item" 
          :class="{ active: activeSpecies === 'cat' }" 
          @click="handleSpeciesClick('cat')"
        >
          猫咪
        </view>
        <view 
          class="menu-item" 
          :class="{ active: activeSpecies === 'dog' }" 
          @click="handleSpeciesClick('dog')"
        >
          狗狗
        </view>
        <view 
          class="menu-item" 
          :class="{ active: activeSpecies === 'bird' }" 
          @click="handleSpeciesClick('bird')"
        >
          鸟类
        </view>
        <view 
          class="menu-item" 
          :class="{ active: activeSpecies === 'rabbit' }" 
          @click="handleSpeciesClick('rabbit')"
        >
          兔子
        </view>
        <view 
          class="menu-item" 
          :class="{ active: activeSpecies === 'other' }" 
          @click="handleSpeciesClick('other')"
        >
          其他
        </view>
      </view>

      <view class="right-content">
        <view class="map-container">
          <map
            id="category-map"
            class="map-view"
            :style="{ height: mapHeight + 'px' }"
            :latitude="mapLatitude"
            :longitude="mapLongitude"
            :markers="filteredMarkers"
            :scale="14"
            @markertap="onMarkerTap"
            :subkey="tencentMapKey"
            show-location
          ></map>
          <view class="locate-btn" @click="locateToCurrent">📍</view>
        </view>

        <view class="marker-list">
          <view class="list-header">
            <text class="list-title">动物档案列表</text>
            <text class="count">{{ filteredMarkers.length }}个档案</text>
          </view>
          
          <view class="marker-item" v-for="marker in filteredMarkers" :key="marker.id" @click="navigateToDetail(marker)">
            <view class="marker-icon"  @click="navigateToOtherPage(marker)">
              <image :src="marker.albumPhoto || getDefaultSpeciesIcon(marker.species)" mode="widthFix"></image>
            </view>
            <view class="marker-info">
              <text class="marker-title">{{ marker.name }}（{{ marker.species }}）</text>
              <text class="marker-address">
                {{ marker.latitude && marker.longitude ? 
                  `经纬度：${marker.latitude.toFixed(6)}, ${marker.longitude.toFixed(6)}` : 
                  '未设置位置' 
                }}
              </text>
              <view class="marker-tags">
                <text class="tag">{{ marker.isWild === 1 ? '野生动物' : '家养动物' }}</text>
                <text class="tag">{{ marker.isShow === 1 ? '公开可见' : '仅自己可见' }}</text>
              </view>
            </view>
            <view class="arrow-icon">→</view>
          </view>
        </view>
      </view>
    </view>

   <view class="add-marker-btn" @click="openAddMarker">
      <!-- <text class="plus-icon">+</text>
      <text class="btn-text">添加动物档案</text> -->
    </view>

    <view class="marker-detail-popup" v-if="showMarkerDetail">
      <view class="detail-header">
        <text class="detail-title">{{ currentMarker.name }}的档案</text>
        <view class="close-detail" @click="closeMarkerDetail">✕</view>
      </view>
      
      <view class="detail-body">
        <image 
          :src="currentMarker.albumPhoto || getDefaultSpeciesIcon(currentMarker.species)" 
          class="detail-img" 
          mode="widthFix"
        ></image>
        
        <view class="detail-info">
          <view class="info-row">
            <text class="info-label">物种：</text>
            <text class="info-value">{{ currentMarker.species || '未填写' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">类型：</text>
            <text class="info-value">{{ currentMarker.isWild === 1 ? '野生动物' : '家养动物' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">出生日期：</text>
            <text class="info-value">{{ currentMarker.birthData ? formatDate(currentMarker.birthData) : '未填写' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">位置：</text>
            <text class="info-value">
              {{ currentMarker.latitude && currentMarker.longitude ? 
                `纬度：${currentMarker.latitude.toFixed(6)}，经度：${currentMarker.longitude.toFixed(6)}` : 
                '未设置' 
              }}
            </text>
          </view>
          <view class="info-row">
            <text class="info-label">档案描述：</text>
            <text class="info-value">{{ currentMarker.description || '无' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">可见性：</text>
            <text class="info-value">{{ currentMarker.isShow === 1 ? '公开可见' : '仅自己可见' }}</text>
          </view>
        </view>
      </view>
      
      <view class="detail-actions">
        <button class="navigate-btn" @click="navigateToLocation(currentMarker)">宠物日常</button>
        <button class="collect-btn" @click="toggleShowStatus">
          {{ currentMarker.isShow === 1 ? '设为仅自己可见' : '设为公开可见' }}
        </button>
      </view>
    </view>

    <view class="overlay" v-if="showMarkerDetail" @click="closeMarkerDetail"></view>

    <view class="add-marker-popup" v-if="showAddMarkerPopup">
        <view class="popup-header">
          <text class="popup-title">添加动物档案</text>
          <view class="close-btn" @click="closeAddMarkerPopup">✕</view>
        </view>
    
        <view class="location-info">
          <text class="info-label">当前位置（可选）：</text>
          <text class="info-value">
            纬度：{{ currentLocation.latitude.toFixed(6) }}，
            经度：{{ currentLocation.longitude.toFixed(6) }}
            <button class="use-location-btn" @click="useCurrentLocation">使用此位置</button>
          </text>
        </view>
    
        <view class="form-content">
          <view class="form-item">
            <text class="form-label">动物名称 <text class="required">*</text></text>
            <input 
              class="form-input" 
              v-model="animalForm.name" 
              placeholder="例如：小白、咪宝"
            />
          </view>
    
          <view class="form-item">
            <text class="form-label">动物种类 <text class="required">*</text></text>
            <picker 
              class="form-picker" 
              :value="animalForm.speciesIndex" 
              :range="animalSpecies"
              @change="onSpeciesChange"
            >
              <view class="picker-display">
                {{ animalSpecies[animalForm.speciesIndex] || '请选择种类' }}
              </view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">动物类型 <text class="required">*</text></text>
            <picker 
              class="form-picker" 
              :value="animalForm.isWild" 
              :range="wildOptions"
              @change="onWildChange"
            >
              <view class="picker-display">
                {{ wildOptions[animalForm.isWild] }}
              </view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">出生日期</text>
            <picker 
              mode="date" 
              class="form-picker"
              :value="animalForm.birthData"
              start="1900-01-01"
              end="2025-12-31"
              @change="onBirthDateChange"
            >
              <view class="picker-display">
                {{ animalForm.birthData || '请选择日期' }}
              </view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">相册封面图</text>
            <button class="upload-btn" @click="chooseAlbumPhoto">选择图片</button>
            <image :src="animalForm.albumPhoto" class="preview-img" mode="widthFix" v-if="animalForm.albumPhoto"></image>
          </view>

          <view class="form-item">
            <text class="form-label">档案描述</text>
            <textarea 
              class="form-textarea" 
              v-model="animalForm.description" 
              placeholder="描述动物特征、习性等（可选）"
              rows="3"
            ></textarea>
          </view>

          <view class="form-item">
            <text class="form-label">可见性</text>
            <picker 
              class="form-picker" 
              :value="animalForm.isShow" 
              :range="showOptions"
              @change="onShowChange"
            >
              <view class="picker-display">
                {{ showOptions[animalForm.isShow] }}
              </view>
            </picker>
          </view>
        </view>
    
        <view class="popup-actions">
          <button class="cancel-btn" @click="closeAddMarkerPopup">取消</button>
          <button class="submit-btn" @click="submitAnimal">提交档案</button>
        </view>
      </view>
    <view class="overlay" v-if="showAddMarkerPopup" @click="closeAddMarkerPopup"></view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { listGetAnimalArchival } from '@/services/animalArchival/animalArchival.js'

// 动物种类列表（替代原markerTypes）
const animalSpecies = ref(['猫咪', '狗狗', '鸟类', '兔子', '其他']);
// 新增：5 只可爱的假小动物数据 (用于丰富测试场景)
// ID 用负数，防止和数据库真实雪花ID冲突，为了 find 备份用的
const fakeAnimalList = [
  { id: -1, name: '流浪狗阿黄(分类测试)', species: '狗狗', description: '经常在新乡龙之光广场附近出没，很亲人，喜欢摇尾巴。', latitude: 35.318000, longitude: 113.915000, albumPhoto: 'https://picsum.photos/seed/fake-dog1/100/100' },
  { id: -2, name: '咪咪 (分类测试)', species: '猫咪', description: '河师大附属中学附近的常客，有点高冷，喜欢晒太阳。', latitude: 35.311000, longitude: 113.900000, albumPhoto: 'https://picsum.photos/seed/fake-cat1/100/100' },
  { id: -3, name: '玉兔 (分类测试)', species: '兔子', description: '可能是谁家跑出来的，在新乡国际饭店附近的小树林看到过。', latitude: 35.325000, longitude: 113.905000, albumPhoto: 'https://picsum.photos/seed/fake-rabbit1/100/100' },
  { id: -4, name: '小百灵(分类测试)', species: '鸟类', description: '公园里的歌唱家。', latitude: 35.316000, longitude: 113.920000, albumPhoto: 'https://picsum.photos/seed/fake-bird1/100/100' },
  { id: -5, name: '小仓鼠 (分类测试)', species: '其他', description: '也是个测试的小可爱。', latitude: 35.312000, longitude: 113.918000, albumPhoto: 'https://picsum.photos/seed/fake-other1/100/100' }
];

// 腾讯地图密钥
const tencentMapKey = ref('TO7BZ-2GR67-XFZX4-PK5VA-3IEH7-ANBFS');
// 当前选中的物种筛选条件
const activeSpecies = ref('all');
// 搜索查询词
const searchQuery = ref('');
// 详情弹窗显示状态
const showMarkerDetail = ref(false);
// 当前选中的动物档案
const currentMarker = ref(null);
// 地图中心纬度
const mapLatitude = ref(39.90882);
// 地图中心经度
const mapLongitude = ref(116.39748);
// 添加动物档案弹窗显示状态
const showAddMarkerPopup = ref(false);
// 地图高度
const mapHeight = ref(300);
// 系统信息
const systemInfo = ref(uni.getSystemInfoSync());

// 表单选项
const wildOptions = ref(['家养动物', '野生动物']);
const showOptions = ref(['仅自己可见', '公开可见']);

// 动物档案表单数据
const animalForm = ref({
  name: '',
  speciesIndex: 0,
  isWild: 0,
  birthData: '',
  albumPhoto: '',
  description: '',
  isShow: 0,
  latitude: null,
  longitude: null
});

// 当前位置信息
const currentLocation = ref({
  latitude: 0,
  longitude: 0
});
const loading = ref(false)

// 动物档案列表（模拟数据）
const animalArchives = ref([
  {
    id: 1,
    name: '小白',
    species: '猫咪',
    isWild: 0,
    birthData: '2022-03-15',
    albumPhoto: 'https://picsum.photos/seed/cat1/36/36',
    description: '白色英短，性格温顺，喜欢玩逗猫棒',
    isShow: 1,
    createUserId: 1001,
    latitude: 39.90882,
    longitude: 116.39748,
    width: 36,
    height: 36
  },
  {
    id: 2,
    name: '大黄',
    species: '狗狗',
    isWild: 1,
    birthData: '2021-10-20',
    albumPhoto: 'https://picsum.photos/seed/dog1/36/36',
    description: '流浪狗救助，已绝育，警惕性较高',
    isShow: 0,
    createUserId: 1001,
    latitude: 39.91882,
    longitude: 116.40748,
    width: 36,
    height: 36
  },
  {
    id: 3,
    name: '灰灰',
    species: '兔子',
    isWild: 0,
    birthData: '2023-01-05',
    albumPhoto: 'https://picsum.photos/seed/rabbit1/36/36',
    description: '荷兰侏儒兔，体型小巧，很活泼',
    isShow: 1,
    createUserId: 1002,
    latitude: 39.92882,
    longitude: 116.41748,
    width: 36,
    height: 36
  }
]);

// 筛选动物档案（转换为地图标记点格式）
const filteredMarkers = computed(() => {
  let result = [...animalArchives.value];
  
  // 按物种筛选
  if (activeSpecies.value !== 'all') {
    const speciesMap = {
      'cat': '猫咪',
      'dog': '狗狗',
      'bird': '鸟类',
      'rabbit': '兔子',
      'other': '其他'
    };
    const targetSpecies = speciesMap[activeSpecies.value];
    result = result.filter(archive => archive.species === targetSpecies);
  }
  
  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(archive => 
      archive.name.toLowerCase().includes(query) || 
      (archive.species && archive.species.toLowerCase().includes(query)) || 
      (archive.description && archive.description.toLowerCase().includes(query))
    );
  }
  
  // 在这里重新分配安全的数字下标给地图组件！
  return result.map((archive, index) => ({
    ...archive,
    id: index, // 强制给地图用从 0 开始的安全数字下标，覆盖掉原来的长 ID
    width: archive.width || 36,
    height: archive.height || 36,
    iconPath: archive.albumPhoto || getDefaultSpeciesIcon(archive.species)
  }));
});

// 初始化
onMounted(async() => {
  getCurrentLocation();
  setMapHeight();
  getAnimalList();
  
  // 监听窗口尺寸变化
  uni.onWindowResize((res) => {
    systemInfo.value.windowHeight = res.windowHeight;
    setMapHeight();
  });
});

// 获取动物列表
const getAnimalList = async () => {
  try {
    loading.value = true // 显示加载状态
    const response = await listGetAnimalArchival()
    
    let records = [];
    if (response.code === 200) {
      // 拿到真实数据，并过滤掉后端万一没有经纬度的脏数据
      records = (response.data.records || []).filter(item => item.latitude && item.longitude);
    }

    // 1. 魔法时间：把 5 条假数据和后端真数据合并成一个大数组！
    const mergedList = [...fakeAnimalList, ...records];

    // 2. 核心修复：重新遍历大数组，确保每一个元素的 id 属性都强制等于它的下标 index ！
    animalArchives.value = mergedList.map((animal, index) => ({
        ...animal,
        
        // ================= 终极修复报错的关键点 =================
        id: index, // 必须写这行！强制让它等于数字 0, 1, 2, 3... 满足微信地图的变态要求
        // ======================================================
        
        realId: animal.id, // 把如果是负数的假ID或者长长雪花的真ID备份下来，跳转用
        width: 36,
        height: 36,
        iconPath: animal.albumPhoto || getDefaultSpeciesIcon(animal.species)
      }))
      
    // 修复打印，打印真实的合并后数据
    console.log("animalArchives 合并后的真实值：", animalArchives.value)
    
  } catch (error) {
    console.error('处理数据失败:', error)
    // 保险起见，fail 里也要把假数据显示出来
    animalArchives.value = fakeAnimalList.map((animal, index) => ({ ...animal, id: index, realId: animal.id, width: 36, height: 36 }));
  } finally {
    loading.value = false // 关闭加载状态
  }
}

// 设置地图高度
const setMapHeight = () => {
  const maxAllowedHeight = systemInfo.value.windowHeight * 0.6;
  const minHeight = 300;
  const calculatedHeight = systemInfo.value.windowHeight - 400;
  mapHeight.value = Math.max(minHeight, Math.min(calculatedHeight, maxAllowedHeight));
};

// 获取当前位置
const getCurrentLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      mapLatitude.value = res.latitude;
      mapLongitude.value = res.longitude;
      currentLocation.value.latitude = res.latitude;
      currentLocation.value.longitude = res.longitude;
    },
    fail: (err) => {
      console.error('获取位置失败:', err);
      currentLocation.value.latitude = 39.90882;
      currentLocation.value.longitude = 116.39748;
    }
  });
};

// 处理物种筛选点击事件（自动初始化相关值）
const handleSpeciesClick = (speciesType) => {
  activeSpecies.value = speciesType;
  
  // 初始化与该物种相关的表单值映射
  const speciesIndexMap = {
    'all': 0,
    'cat': 0,
    'dog': 1,
    'bird': 2,
    'rabbit': 3,
    'other': 4
  };
  
  // 记录当前弹窗状态
  const wasPopupOpen = showAddMarkerPopup.value;
  
  // 如果弹窗是打开的，更新表单中的物种选择
  if (wasPopupOpen) {
    animalForm.value.speciesIndex = speciesIndexMap[speciesType];
  }
};

// 标记点点击事件
const onMarkerTap = (e) => {
    // 1. 获取地图返回的数字下标
    const clickedIndex = e.detail.markerId !== undefined ? e.detail.markerId : e.markerId;
    console.log("用户点击了标记，地图返回的下标是：", clickedIndex);

    // 2. 核心修改：必须从 filteredMarkers 里取数据！因为地图上展示的就是这个过滤后的数组
    const clickedAnimal = filteredMarkers.value[clickedIndex];
    
    if (!clickedAnimal) {
      uni.showToast({ title: '未找到该标记数据', icon: 'none' });
      return;
    }
  
    console.log("成功找到小动物，准备弹窗：", clickedAnimal.name);

    // 3. 完美弹出弹窗
    currentMarker.value = clickedAnimal;
    showMarkerDetail.value = true;
};

// 返回上一页
const navigateBack = () => {
  uni.navigateBack();
};

// 定位到当前位置
const locateToCurrent = () => {
  getCurrentLocation();
};

// 导航到详情
const navigateToDetail = (marker) => {
  currentMarker.value = marker;
  showMarkerDetail.value = true;
};

// 关闭详情弹窗
const closeMarkerDetail = () => {
  showMarkerDetail.value = false;
};

// 切换显示状态
const toggleShowStatus = () => {
  if (currentMarker.value) {
    currentMarker.value.isShow = currentMarker.value.isShow === 1 ? 0 : 1;
    const index = animalArchives.value.findIndex(m => m.id === currentMarker.value.id);
    if (index !== -1) {
      animalArchives.value[index].isShow = currentMarker.value.isShow;
    }
    
    uni.showToast({
      title: `已${currentMarker.value.isShow === 1 ? '设为公开可见' : '设为仅自己可见'}`,
      icon: 'none'
    });
  }
};

// 点击图片跳转到其他页面
const navigateToOtherPage = (marker) => {
  // 关闭可能打开的弹窗（如果需要）
  showMarkerDetail.value = false;
  
  // 使用uni.navigateTo跳转，url为目标页面路径，可通过query传递参数
  uni.navigateTo({
    url: `/pages/animalDetail/animalDetail?id=${marker.id}&name=${marker.name}`,
    // 跳转失败的处理
    fail: (err) => {
      console.error('页面跳转失败:', err);
      uni.showToast({ title: '跳转失败', icon: 'none' });
    }
  });
};

// 导航到位置
const navigateToLocation = (currentMarker) => {
  // if (!currentMarker.value.latitude || !currentMarker.value.longitude) {
  //   uni.showToast({
  //     title: '该动物未设置位置信息',
  //     icon: 'none'
  //   });
  //   return;
  // }
  
 //  // 模拟导航功能
 //  uni.showToast({
	
 //    title: '导航功能开发中',
 //    icon: 'none'
 //  });
 
 uni.navigateTo({
 	url:`/pages/animal_daily/animal_daily?id=${currentMarker.stringId}`
 })
};

// 打开添加动物档案弹窗（自动初始化表单）
const openAddMarker = () => {
  // 初始化表单值
  initAnimalForm();
  // 刷新当前位置
  getCurrentLocation();
  // 显示弹窗
  showAddMarkerPopup.value = true;
};

// 初始化动物表单
const initAnimalForm = () => {
  // 根据当前选中的物种自动设置表单默认值
  const speciesIndexMap = {
    'all': 0,
    'cat': 0,
    'dog': 1,
    'bird': 2,
    'rabbit': 3,
    'other': 4
  };
  
  animalForm.value = {
    name: '',
    speciesIndex: speciesIndexMap[activeSpecies.value],
    isWild: 0, // 默认家养
    birthData: '',
    albumPhoto: '',
    description: '',
    isShow: 0, // 默认仅自己可见
    latitude: null,
    longitude: null
  };
};

// 关闭添加弹窗
const closeAddMarkerPopup = () => {
  showAddMarkerPopup.value = false;
};

// 物种选择变化
const onSpeciesChange = (e) => {
  animalForm.value.speciesIndex = e.detail.value;
};

// 动物类型变化
const onWildChange = (e) => {
  animalForm.value.isWild = e.detail.value;
};

// 出生日期变化
const onBirthDateChange = (e) => {
  animalForm.value.birthData = e.detail.value;
};

// 可见性变化
const onShowChange = (e) => {
  animalForm.value.isShow = e.detail.value;
};

// 选择相册图片
const chooseAlbumPhoto = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0];
      animalForm.value.albumPhoto = tempFilePath;
      // 实际项目中这里应该上传图片到服务器并获取URL
    }
  });
};

// 使用当前位置
const useCurrentLocation = () => {
  animalForm.value.latitude = currentLocation.value.latitude;
  animalForm.value.longitude = currentLocation.value.longitude;
  
  uni.showToast({
    title: '已使用当前位置',
    icon: 'none'
  });
};

// 提交动物档案
const submitAnimal = () => {
  if (!animalForm.value.name.trim()) {
    uni.showToast({
      title: '请输入动物名称',
      icon: 'none'
    });
    return;
  }
  
  const newAnimal = {
    id: animalArchives.value.length + 1,
    name: animalForm.value.name,
    species: animalSpecies.value[animalForm.value.speciesIndex],
    isWild: animalForm.value.isWild,
    birthData: animalForm.value.birthData,
    albumPhoto: animalForm.value.albumPhoto,
    description: animalForm.value.description,
    isShow: animalForm.value.isShow,
    createUserId: 1001, // 实际应从登录信息获取
    latitude: animalForm.value.latitude,
    longitude: animalForm.value.longitude,
    width: 36,
    height: 36
  };
  
  animalArchives.value.push(newAnimal);
  closeAddMarkerPopup();
  
  uni.showToast({
    title: '动物档案添加成功',
    icon: 'success'
  });
};

// 获取默认物种图标
const getDefaultSpeciesIcon = (species) => {
  const iconMap = {
    '猫咪': 'https://picsum.photos/seed/cat/36/36',
    '狗狗': 'https://picsum.photos/seed/dog/36/36',
    '鸟类': 'https://picsum.photos/seed/bird/36/36',
    '兔子': 'https://picsum.photos/seed/rabbit/36/36',
    '其他': 'https://picsum.photos/seed/animal/36/36'
  };
  return iconMap[species] || iconMap['其他'];
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  // 简单的日期格式化，实际项目中可使用更完善的日期库
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑由computed自动处理
};
</script>

<style scoped>
.category-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background-color: #FF85A2;
  color: white;
}

.back-btn {
  font-size: 36rpx;
  width: 40rpx;
}

.title {
  font-size: 34rpx;
  font-weight: bold;
}

.empty {
  width: 40rpx;
}

.search-box {
  display: flex;
  align-items: center;
  padding: 15rpx 30rpx;
  background-color: white;
}

.search-icon {
  font-size: 28rpx;
  color: #999;
  margin-right: 15rpx;
}

.search-input {
  flex: 1;
  height: 60rpx;
  background-color: #f5f5f5;
  border-radius: 30rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
}

.content {
  display: flex;
  height: calc(100vh - 240rpx);
}

.left-menu {
  width: 200rpx;
  background-color: white;
  overflow-y: auto;
}

.menu-item {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  border-left: 4rpx solid transparent;
}

.menu-item.active {
  color: #FF85A2;
  border-left-color: #FF85A2;
  background-color: rgba(255, 133, 162, 0.05);
}

.right-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.map-container {
  position: relative;
  width: 100%;
  overflow: hidden;
}

.map-view {
  width: 100%;
}

.locate-btn {
  position: absolute;
  right: 30rpx;
  bottom: 30rpx;
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.2);
}

.marker-list {
  height: 300rpx;
  background-color: white;
  overflow-y: auto;
  border-top-left-radius: 30rpx;
  border-top-right-radius: 30rpx;
  padding: 20rpx;
}

.list-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.list-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.count {
  font-size: 26rpx;
  color: #999;
}

.marker-item {
  display: flex;
  align-items: center;
  padding: 15rpx 0;
  border-bottom: 1px solid #f5f5f5;
}

.marker-icon {
  width: 60rpx;
  height: 60rpx;
  margin-right: 20rpx;
}

.marker-icon image {
  width: 100%;
  height: 100%;
}

.marker-info {
  flex: 1;
}

.marker-title {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 5rpx;
}

.marker-address {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 5rpx;
}

.marker-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.tag {
  font-size: 20rpx;
  background-color: #f5f5f5;
  color: #666;
  padding: 3rpx 10rpx;
  border-radius: 10rpx;
}

.arrow-icon {
  font-size: 28rpx;
  color: #ccc;
}

.add-marker-btn {
/*  position: fixed;
  right: 40rpx;
  bottom: 120rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #FF85A2;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 5rpx 15rpx rgba(255, 133, 162, 0.5); */
}

.plus-icon {
  font-size: 40rpx;
  line-height: 40rpx;
}

.btn-text {
  font-size: 20rpx;
  line-height: 20rpx;
  margin-top: 5rpx;
}

.marker-detail-popup {
  position: fixed;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 100%;
  background-color: white;
  border-top-left-radius: 30rpx;
  border-top-right-radius: 30rpx;
  padding: 30rpx;
  z-index: 100;
  max-height: 70vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px solid #f5f5f5;
}

.detail-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.close-detail {
  font-size: 36rpx;
  color: #999;
}

.detail-body {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.detail-img {
  width: 100rpx;
  height: 100rpx;
}

.detail-info {
  flex: 1;
}

.info-row {
  margin-bottom: 15rpx;
}

.info-label {
  font-size: 26rpx;
  color: #666;
  display: inline-block;
  width: 140rpx;
}

.info-value {
  font-size: 26rpx;
  color: #333;
}

.detail-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.navigate-btn, .collect-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
}

.navigate-btn {
  background-color: #FF85A2;
  color: white;
  border: none;
}

.collect-btn {
  background-color: #f5f5f5;
  color: #333;
  border: none;
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

.add-marker-popup {
  position: fixed;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 100%;
  background-color: white;
  border-top-left-radius: 30rpx;
  border-top-right-radius: 30rpx;
  padding: 30rpx;
  z-index: 100;
  max-height: 80vh;
  overflow-y: auto;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.popup-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 36rpx;
  color: #999;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
}

.required {
  color: #ff4d4f;
}

.form-input {
  width: 100%;
  height: 80rpx;
  border: 1px solid #eee;
  border-radius: 16rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
}

.form-textarea {
  width: 100%;
  border: 1px solid #eee;
  border-radius: 16rpx;
  padding: 20rpx;
  font-size: 28rpx;
  min-height: 120rpx;
}

.form-picker {
  width: 100%;
  height: 80rpx;
  border: 1px solid #eee;
  border-radius: 16rpx;
  overflow: hidden;
}

.picker-display {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
}

.popup-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.cancel-btn, .submit-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
  border: none;
}

.submit-btn {
  background-color: #FF85A2;
  color: white;
  border: none;
}

.location-info {
  padding: 15rpx 0;
  margin-bottom: 20rpx;
  background-color: #f5f7fa;
  border-radius: 10rpx;
  padding: 20rpx;
}

.info-label {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

.info-value {
  font-size: 26rpx;
  color: #333;
  word-break: break-all;
}

.use-location-btn {
  font-size: 24rpx;
  color: #FF85A2;
  background: transparent;
  padding: 0 10rpx;
  margin-left: 10rpx;
  border: none;
}

.upload-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  border: 1px dashed #eee;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #666;
  background-color: #fafafa;
  text-align: center;
}

.preview-img {
  width: 120rpx;
  height: 120rpx;
  margin-top: 10rpx;
  border-radius: 8rpx;
}
</style>
