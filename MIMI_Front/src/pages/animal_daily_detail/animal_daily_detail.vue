<template>
  <view class="animal-detail-container">
    
    <!-- 动物照片展示区 -->
    <view class="photo-gallery">
      <image 
        :src="animal.picUrl" 
        mode="widthFix"
        class="main-photo"
        :alt="`${animal.name}的照片`"
      ></image>
      <!-- 照片指示器 -->
      <!-- <view class="photo-indicator">
        <view class="indicator-dot active"></view>
        <view class="indicator-dot"></view>
        <view class="indicator-dot"></view>
      </view> -->
    </view>
    
    <!-- 基本信息区 -->
    <view class="basic-info">
     <!-- <view class="title-section">
        <text class="animal-name">{{ animal.name }}</text>
        <view class="badges">
          <view class="badge wild-badge" v-if="animal.isWild === 1">
            <text class="badge-text">野生</text>
          </view>
          <view class="badge show-badge" v-if="animal.isShow === 1">
            <text class="badge-text">可展示</text>
          </view>
        </view>
      </view> -->
      
      
      <view class="stats-grid">
        <view class="stat-item">
          <uni-icons type="calendar" size="16" color="#888"></uni-icons>
          <text class="stat-text">发表日期</text>
          <text class="stat-value">{{ animal.createTime }}</text>
        </view>
      </view>
    </view>
    
    <!-- 详细描述区 -->
    <view class="description-section">
      <view class="description-content">
        <text class="description-text">{{ animal.text }}</text>
      </view>
    </view>
    
    <!-- 评论区 -->
    <view class="comments-section">
      <view class="comments-header">
        <text class="section-title">评论 ({{ comments.length }})</text>
        <text class="sort-text">最新</text>
      </view>
      
      <!-- 评论输入框 -->
      <view class="comment-input-box">
        <image src="https://picsum.photos/seed/user/40/40" class="user-avatar" mode="widthFix" alt="用户头像"></image>
        <input 
          type="text" 
          placeholder="写下你的评论..." 
          class="comment-input"
          v-model="newComment"
          @confirm="addComment"
          :focus="isFocus" 
          @blur="isFocus = false"
        />
		
        <button class="send-btn" @click="addComment">
          <uni-icons type="send" size="18" color="#1890ff"></uni-icons>
        </button>
      </view>
      
      <!-- 评论列表 -->
      <view class="comments-list">
        <view class="comment-item" v-for="(comment, index) in comments" :key="index">
          <image :src="comment.avatar" class="comment-avatar" mode="widthFix" :alt="`${comment.username}的头像`"></image>
          <view class="comment-content">
            <view class="comment-header">
              <text class="comment-username">{{ comment.username }}</text>
              <text class="comment-time">{{ comment.time }}</text>
            </view>
            <text class="comment-text">{{ comment.content }}</text>
            <view class="comment-actions">
              <button class="action-btn" @click="likeComment(index)">
                <uni-icons 
                  type="heart" 
                  size="16" 
                  :color="comment.liked ? '#ff4d4f' : '#888'"
                ></uni-icons>
                <text class="action-text" :class="{ liked: comment.liked }">{{ comment.likes }}</text>
              </button>
			  <button class="action-btn reply-btn" @click="onReply(comment)">
			    <uni-icons type="chat" size="16" color="#888"></uni-icons>
			    <text class="action-text">回复</text>
			  </button>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 加载更多 -->
      <view class="load-more" v-if="comments.length > 0">
        <button class="load-more-btn" @click="loadMoreComments">
          加载更多评论
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onLoad, onShow } from '@dcloudio/uni-app';
import { ref } from 'vue';
// 引入你写好的三个接口
import { GetAnimalDailyDetail, addDailyComment, getDailyCommentsList } from '@/services/animalArchival/animalDaily.js'

// 动物详情数据
const animal = ref({});
const id = ref();

// 真实评论数据（初始化为空数组）
const comments = ref([]);

// 新评论内容
const newComment = ref('');

// 控制输入框焦点的开关
const isFocus = ref(false);

// 点击回复按钮触发的方法
const onReply = (comment) => {
  newComment.value = `回复 @${comment.username}：`;
  setTimeout(() => {
    isFocus.value = true;
  }, 100);
};

// 返回上一页
const onBack = () => {
  uni.navigateBack();
};

// 分享功能
const onShare = () => {
  uni.showToast({ title: '分享功能开发中', icon: 'none' });
};

// 核心修改 1：添加评论（真实请求后端）
const addComment = async () => {
  if (!newComment.value.trim()) {
    uni.showToast({ title: '请输入评论内容', icon: 'none' });
    return;
  }
  
  uni.showLoading({ title: '发送中...' });
  
  try {
    // 组装发给后端的数据
    const commentData = {
      dailyId: id.value,
      content: newComment.value,
      userId: 1001 // 假设当前登录用户的ID是 1001
    };
    
    // 调用 POST 接口保存到 MySQL
    const response = await addDailyComment(commentData);
    
    // 注意：你封装的 http 返回结果可能直接就是 data，或者是包含了 code 的对象
    // 统一做一个安全判断
    if (response.code === 200 || response.data?.code === 200 || response.message === '操作成功') {
      uni.hideLoading();
      uni.showToast({ title: '评论成功', icon: 'success' });
      
      // 成功后清空输入框
      newComment.value = '';
      
      // 评论成功后，重新拉取一次最新的评论列表！
      fetchComments(); 
    } else {
      throw new Error('评论失败');
    }
  } catch (error) {
    console.error('评论保存失败:', error);
    uni.hideLoading();
    uni.showToast({ title: '网络异常，评论失败', icon: 'none' });
  }
};

// 核心修改 2：获取真实评论列表 + 拼接假数据兜底展示
const fetchComments = async () => {
  if (!id.value) return;
  try {
    const response = await getDailyCommentsList(id.value);
    
    // 兼容你可能封装过的 axios/http 返回格式
    const resData = response.data || response;
    
    // 1. 先把数据库里的真实数据处理好
    let realComments = [];
    if (resData && Array.isArray(resData)) {
      realComments = resData.map(item => ({
        id: item.stringId || item.id,
        username: "用户" + (item.userId || '匿名'), 
        avatar: "https://picsum.photos/seed/user/40/40", 
        content: item.content,
        time: item.createTime ? item.createTime.replace('T', ' ') : '刚刚',
        likes: 0,
        liked: false
      }));
    }

    // 2. 核心：准备两条漂亮且标明“假数据”的固定评论
    const fakeComments = [
      {
        id: 'fake_1',
        username: "云吸宠达人 (假数据)",
        avatar: "https://picsum.photos/seed/fake1/40/40",
        content: "哇！这也太可爱了吧，心都要化了~ 感谢博主的分享！(此为排版展示假数据)",
        time: "2026-03-19 10:00:00",
        likes: 99,
        liked: false
      },
      {
        id: 'fake_2',
        username: "新乡动物保护 (假数据)",
        avatar: "https://picsum.photos/seed/fake2/40/40",
        content: "拍摄角度真好，请大家文明观赏，一起爱护小动物哦！(此为排版展示假数据)",
        time: "2026-03-19 10:30:00",
        likes: 66,
        liked: false
      }
    ];

    // 3. 把真数据和假数据拼接到一起赋值给页面
    // 这样无论是哪篇日常，都会在真实评论的下面跟着这两条假数据
    comments.value = [...realComments, ...fakeComments];

  } catch (error) {
    console.error('获取评论列表失败:', error);
  }
};

// 点赞评论 (目前保留本地假点赞)
const likeComment = (index) => {
  if (comments.value[index].liked) {
    comments.value[index].likes--;
  } else {
    comments.value[index].likes++;
  }
  comments.value[index].liked = !comments.value[index].liked;
};

// 页面加载获取ID
onLoad((options)=>{
  console.log('接收的参数：', options);
  id.value = options.id;
}) 

// 页面展示时获取详情和评论
onShow(()=>{
  getDailyDetail();
  fetchComments(); // 每次进入页面，都会拉取最新评论
})

// 获取日常详情
const getDailyDetail = async() => {
  try {
    const response = await GetAnimalDailyDetail(id.value)
    animal.value = response.data || response
  } catch (error) {
    console.error('处理数据失败:', error)
  } 
}

// 加载更多评论
const loadMoreComments = () => {
  uni.showToast({ title: '没有更多评论了', icon: 'none' });
};
</script>

<style scoped>
.animal-detail-container {
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 导航栏样式 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  background-color: transparent;
  padding: 8px;
  margin: 0;
}

.nav-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.right-actions {
  display: flex;
}

.action-btn {
  background-color: transparent;
  padding: 8px;
  margin-left: 12px;
}

/* 照片展示区 */
.photo-gallery {
  background-color: #000;
  position: relative;
}

.main-photo {
  width: 100%;
}

.photo-indicator {
  display: flex;
  justify-content: center;
  padding: 10px;
  gap: 6px;
}

.indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
}

.indicator-dot.active {
  background-color: #fff;
  width: 24px;
  border-radius: 4px;
}

/* 基本信息区 */
.basic-info {
  background-color: #fff;
  padding: 16px;
  margin-bottom: 10px;
}

.title-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.animal-name {
  font-size: 22px;
  font-weight: bold;
  color: #333;
}

.badges {
  display: flex;
  gap: 8px;
}

.badge {
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.wild-badge {
  background-color: #ff4d4f;
}

.show-badge {
  background-color: #52c41a;
}

.badge-text {
  color: #fff;
}

.animal-species {
  font-size: 16px;
  color: #666;
  margin-bottom: 16px;
  display: block;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-text {
  font-size: 13px;
  color: #888;
  margin: 4px 0;
  display: flex;
  align-items: center;
}

.stat-value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

/* 详细描述区 */
.description-section {
  background-color: #fff;
  padding: 16px;
  margin-bottom: 10px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.description-content {
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 8px;
  line-height: 1.6;
}

.description-text {
  font-size: 15px;
  color: #666;
  white-space: pre-line; /* 保留换行符 */
}

/* 评论区 */
.comments-section {
  background-color: #fff;
  padding: 16px;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sort-text {
  font-size: 14px;
  color: #1890ff;
  display: flex;
  align-items: center;
}

.sort-text::after {
  content: "";
  display: inline-block;
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid #1890ff;
  margin-left: 4px;
}

/* 评论输入框 */
.comment-input-box {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 24px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 10px;
}

.comment-input {
  flex: 1;
  background-color: transparent;
  border: none;
  outline: none;
  font-size: 14px;
  padding: 5px 0;
}

.send-btn {
  background-color: transparent;
  padding: 5px 10px;
  margin: 0;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.comment-username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
  display: block;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.comment-actions .action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: transparent;
  padding: 0;
  margin: 0;
  height: auto;
  line-height: normal;
}

.action-text {
  font-size: 12px;
  color: #888;
}

.action-text.liked {
  color: #ff4d4f;
}

/* 加载更多 */
.load-more {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

.load-more-btn {
  background-color: transparent;
  color: #1890ff;
  border: 1px solid #1890ff;
  border-radius: 20px;
  padding: 6px 20px;
  font-size: 14px;
}
</style>
