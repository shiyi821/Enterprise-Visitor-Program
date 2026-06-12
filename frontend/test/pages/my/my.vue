<template>
  <view class="container">
    <view class="header-section">
      <view class="user-info" @click="handleUserClick">
        <view class="avatar-wrap">
          <image 
            class="avatar-img" 
            :src="isLoggedIn && userInfo.avatar ? userInfo.avatar : '/static/default-avatar.png'" 
            mode="aspectFill"
          ></image>
        </view>
        
        <view class="info">
          <template v-if="isLoggedIn">
            <text class="name">{{ userInfo.nickname || '未命名用户' }}</text>
            <text class="greeting">欢迎回来~~~</text>
          </template>
          <template v-else>
            <text class="name login-text">马上注册/登录</text>
          </template>
        </view>
      </view>
    </view>
    
    <view class="menu-card">
      <view class="menu-list">
        <view class="menu-item" @click="navigateTo('/pages/my/myinfo')">
          <view class="item-left">
            <text class="icon">📝</text>
            <text class="item-text">修改我的资料</text>
          </view>
          <text class="arrow">></text>
        </view>
        
        <view class="menu-item" @click="navigateTo('/pages/admin/admin')">
          <view class="item-left">
            <text class="icon">⚙️</text>
            <text class="item-text">系统后台管理 <text class="sub-text">(点击使用)</text></text>
          </view>
          <text class="arrow">></text>
        </view>
        
        <view class="menu-item" @click="showAbout">
          <view class="item-left">
            <text class="icon">👥</text>
            <text class="item-text">关于我们</text>
          </view>
          <text class="arrow">></text>
        </view>
      </view>

      <button v-if="isLoggedIn" class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';

// 模拟登录状态和用户信息
const isLoggedIn = ref(false); 
const userInfo = ref({
  nickname: '丽丽',
  avatar: '' // 留空则使用默认头像，可以填入网络图片地址测试
});

// 处理点击头像区域的逻辑
const handleUserClick = () => {
  if (!isLoggedIn.value) {
    // 模拟跳转登录页或直接登录操作
    uni.showToast({ title: '正在前往登录...', icon: 'none' });
    // 模拟登录成功
    setTimeout(() => {
      isLoggedIn.value = true;
    }, 1000);
  }
};

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: function (res) {
      if (res.confirm) {
        isLoggedIn.value = false;
      }
    }
  });
};

// 路由跳转
const navigateTo = (url) => {
  if (!isLoggedIn.value && url !== '/pages/admin/admin') {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  uni.navigateTo({
    url: url,
    fail: () => {
      uni.showToast({ title: '页面暂未开发: ' + url, icon: 'none' });
    }
  });
};

// 关于我们弹窗
const showAbout = () => {
  uni.showModal({
    title: '关于我们',
    content: '这是一个访客预约管理系统小程序。',
    showCancel: false,
    confirmColor: '#245381'
  });
};
</script>

<style lang="scss">
page {
  background-color: #f5f6f8;
}

.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部蓝色区域 */
.header-section {
  background-color: #245381; /* 统一色号 */
  padding: 60px 20px 40px 20px;
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  
  .avatar-wrap {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background-color: #fff;
    margin-right: 15px;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    border: 2px solid rgba(255, 255, 255, 0.3);
    
    .avatar-img {
      width: 100%;
      height: 100%;
    }
  }
  
  .info {
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    .name {
      font-size: 20px;
      font-weight: bold;
      margin-bottom: 6px;
    }
    
    .login-text {
      font-size: 18px;
    }
    
    .greeting {
      font-size: 14px;
      opacity: 0.8;
    }
  }
}

/* 底部白色卡片区域 */
.menu-card {
  flex: 1;
  background-color: #fff;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  margin-top: -20px; /* 向上偏移以覆盖蓝色背景产生弧度效果 */
  padding: 20px 15px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.menu-list {
  margin-bottom: 30px;
  
  .menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 18px 10px;
    border-bottom: 1px solid #f0f0f0;
    
    .item-left {
      display: flex;
      align-items: center;
      
      .icon {
        margin-right: 12px;
        font-size: 18px;
      }
      
      .item-text {
        font-size: 16px;
        color: #333;
        
        .sub-text {
          font-size: 12px;
          color: #999;
          margin-left: 5px;
        }
      }
    }
    
    .arrow {
      color: #ccc;
      font-size: 16px;
      font-family: consolas; /* 让箭头看起来更规则 */
    }
    
    &:active {
      background-color: #fafafa;
    }
  }
}

.logout-btn {
  margin: 0 20px;
  background-color: #245381; /* 统一色号 */
  color: #fff;
  border-radius: 25px;
  font-size: 16px;
  
  &::after {
    border: none;
  }
}
</style>