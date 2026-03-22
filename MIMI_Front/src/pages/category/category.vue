<template>
  <view class="category-container">
    <view class="navbar">
		<text class="title">动物循迹</text>
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
        <view class="header-actions">
          <text class="collect-icon" 
                :style="{ color: currentMarker.isCollected ? '#FF9800' : '#ccc' }"
                @click="toggleArchiveCollect(currentMarker)">
            {{ currentMarker.isCollected ? '💖' : '🤍' }}
          </text>
          <view class="close-detail" @click="closeMarkerDetail">×</view>
        </view>
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
import { onShow } from '@dcloudio/uni-app'; 
import { listGetAnimalArchival } from '@/services/animalArchival/animalArchival.js'
import { togglePostCollect, queryGetCollectList } from "@/services/my/collect.js"; 

// 修复一：正确获取图片字段 + 传递真实数据库 ID
const toggleArchiveCollect = async (archive) => {
  const originalStatus = archive.isCollected;
  archive.isCollected = !originalStatus; // 乐观锁先变色

  // 极其关键：必须使用真实的雪花 ID (realId 或 stringId)
  const realTargetId = archive.stringId || archive.realId || archive.id;

  const payload = {
    targetId: realTargetId, 
    targetType: 2, // 2代表档案
    title: `${archive.name}的档案`,
    picUrl: archive.albumPhoto || '', // 修正：从 albumPhoto 取图，去掉下划线
    summary: `物种: ${archive.species || '未知'}` 
  };

  try {
    await togglePostCollect(payload);
    uni.showToast({ title: archive.isCollected ? '收藏成功' : '已取消收藏', icon: 'none' });
  } catch (error) {
    archive.isCollected = originalStatus; // 回滚
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

// 动物种类列表
const animalSpecies = ref(['猫咪', '狗狗', '鸟类', '兔子', '其他']);
const fakeAnimalList = [
  { id: -1, name: '流浪狗阿黄', species: '狗狗', description: '亲人，喜欢摇尾巴。', latitude: 35.318000, longitude: 113.915000, albumPhoto: 'https://picsum.photos/seed/fake-dog1/100/100' },
  { id: -2, name: '咪咪', species: '猫咪', description: '有点高冷，喜欢晒太阳。', latitude: 35.311000, longitude: 113.900000, albumPhoto: 'https://picsum.photos/seed/fake-cat1/100/100' }
];

const tencentMapKey = ref('TO7BZ-2GR67-XFZX4-PK5VA-3IEH7-ANBFS');
const activeSpecies = ref('all');
const searchQuery = ref('');
const showMarkerDetail = ref(false);
const currentMarker = ref(null);
const mapLatitude = ref(39.90882);
const mapLongitude = ref(116.39748);
const showAddMarkerPopup = ref(false);
const mapHeight = ref(300);
const systemInfo = ref(uni.getSystemInfoSync());
const wildOptions = ref(['家养动物', '野生动物']);
const showOptions = ref(['仅自己可见', '公开可见']);
const animalForm = ref({ name: '', speciesIndex: 0, isWild: 0, birthData: '', albumPhoto: '', description: '', isShow: 0, latitude: null, longitude: null });
const currentLocation = ref({ latitude: 0, longitude: 0 });
const loading = ref(false)
const animalArchives = ref([]); 

// 修复二：每次切回页面，自动刷新收藏状态
onShow(() => {
  getAnimalList();
});

onMounted(() => {
  getCurrentLocation();
  setMapHeight();
  getAnimalList();
  uni.onWindowResize((res) => {
    systemInfo.value.windowHeight = res.windowHeight;
    setMapHeight();
  });
});

// 获取数据时，拉取收藏夹进行交叉比对
const getAnimalList = async () => {
  try {
    loading.value = true;
    const response = await listGetAnimalArchival();
    
    let records = [];
    if (response.code === 200) {
      records = (response.data.records || []).filter(item => item.latitude && item.longitude);
    }
    const mergedList = [...fakeAnimalList, ...records];

    let myCollectIds = [];
    try {
      const collectRes = await queryGetCollectList();
      myCollectIds = (collectRes.data || [])
        .filter(item => item.targetType === 2) // 只看档案
        .map(item => String(item.targetId));
    } catch (e) {
      console.error('获取收藏列表失败', e);
    }

    // 处理数据映射
    animalArchives.value = mergedList.map((animal, index) => {
      const realBackendId = String(animal.stringId || animal.id);
      return {
        ...animal,
        id: index, // 给地图用的假下标
        realId: animal.id, // 备份真ID
        stringId: animal.stringId, // 备份真StringID
        width: 36,
        height: 36,
        iconPath: animal.albumPhoto || getDefaultSpeciesIcon(animal.species),
        isCollected: myCollectIds.includes(realBackendId) 
      }
    });
    
  } catch (error) {
    console.error('处理数据失败:', error);
  } finally {
    loading.value = false;
  }
}

// 筛选逻辑
const filteredMarkers = computed(() => {
  let result = [...animalArchives.value];
  if (activeSpecies.value !== 'all') {
    const speciesMap = { 'cat': '猫咪', 'dog': '狗狗', 'bird': '鸟类', 'rabbit': '兔子', 'other': '其他' };
    result = result.filter(archive => archive.species === speciesMap[activeSpecies.value]);
  }
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(archive => 
      archive.name.toLowerCase().includes(query) || 
      (archive.species && archive.species.toLowerCase().includes(query)) || 
      (archive.description && archive.description.toLowerCase().includes(query))
    );
  }
  return result.map((archive, index) => ({ ...archive, id: index }));
});

// ================== 原有普通方法恢复 ==================
const setMapHeight = () => {
  const maxAllowedHeight = systemInfo.value.windowHeight * 0.6;
  mapHeight.value = Math.max(300, Math.min(systemInfo.value.windowHeight - 400, maxAllowedHeight));
};

const getCurrentLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => { mapLatitude.value = res.latitude; mapLongitude.value = res.longitude; currentLocation.value = { ...res }; },
    fail: () => { currentLocation.value = { latitude: 39.90882, longitude: 116.39748 }; }
  });
};

const handleSpeciesClick = (speciesType) => { activeSpecies.value = speciesType; };

const onMarkerTap = (e) => {
  const clickedIndex = e.detail.markerId !== undefined ? e.detail.markerId : e.markerId;
  const clickedAnimal = filteredMarkers.value[clickedIndex];
  if (clickedAnimal) {
    currentMarker.value = clickedAnimal;
    showMarkerDetail.value = true;
  }
};

const navigateBack = () => uni.navigateBack();
const locateToCurrent = () => getCurrentLocation();
const navigateToDetail = (marker) => { currentMarker.value = marker; showMarkerDetail.value = true; };
const closeMarkerDetail = () => showMarkerDetail.value = false;

// 恢复：切换可见性状态
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

const navigateToOtherPage = (marker) => {
  showMarkerDetail.value = false;
  uni.navigateTo({ url: `/pages/animalDetail/animalDetail?id=${marker.realId}&name=${marker.name}` });
};
const navigateToLocation = (currentMarker) => {
  uni.navigateTo({ url:`/pages/animal_daily/animal_daily?id=${currentMarker.stringId}` });
};

const openAddMarker = () => { initAnimalForm(); getCurrentLocation(); showAddMarkerPopup.value = true; };
const initAnimalForm = () => { animalForm.value = { name: '', speciesIndex: 0, isWild: 0, birthData: '', albumPhoto: '', description: '', isShow: 0, latitude: null, longitude: null }; };
const closeAddMarkerPopup = () => showAddMarkerPopup.value = false;
const onSpeciesChange = (e) => animalForm.value.speciesIndex = e.detail.value;
const onWildChange = (e) => animalForm.value.isWild = e.detail.value;
const onBirthDateChange = (e) => animalForm.value.birthData = e.detail.value;
const onShowChange = (e) => animalForm.value.isShow = e.detail.value;

// 恢复：选择图片
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

const useCurrentLocation = () => { animalForm.value.latitude = currentLocation.value.latitude; animalForm.value.longitude = currentLocation.value.longitude; };

// 恢复：提交动物表单
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

const getDefaultSpeciesIcon = (species) => {
  const iconMap = { '猫咪': 'https://picsum.photos/seed/cat/36/36', '狗狗': 'https://picsum.photos/seed/dog/36/36', '鸟类': 'https://picsum.photos/seed/bird/36/36', '兔子': 'https://picsum.photos/seed/rabbit/36/36', '其他': 'https://picsum.photos/seed/animal/36/36' };
  return iconMap[species] || iconMap['其他'];
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

// 恢复：搜索处理（这里原本也是空的，但我给你放回来了）
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
  align-items: center;       /* 垂直居中 */
  justify-content: center;    /* 水平居中 */
  padding: 20rpx 30rpx;
  background-color: #FF85A2;
  color: white;
  height: 88rpx;             /* 建议给个固定高度 */
}

.back-btn {
  font-size: 36rpx;
  width: 40rpx;
}

.title {
  font-size: 34rpx;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 20rpx; /* 图标之间的间距 */
}

.collect-icon {
  font-size: 40rpx; /* 根据实际需要调整大小 */
  padding: 0 10rpx;
}
</style>
