<template>
  <view class="info-container">
    <view class="info-group">
      <view class="info-item avatar-item" @click="changeAvatar">
        <text class="label">头像</text>
        <view class="right-content">
          <image :key="userInfo.avatar" class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
          <text class="arrow">></text>
        </view>
      </view>

      <view class="info-item">
        <text class="label">用户姓名</text>
        <view class="right-content">
          <input class="input-text" v-model="userInfo.username" placeholder="请输入姓名" />
        </view>
      </view>

      <view class="info-item">
        <text class="label">昵称</text>
        <view class="right-content">
          <input class="input-text" v-model="userInfo.nickname" placeholder="请输入昵称" />
        </view>
      </view>

      <view class="info-item">
        <text class="label">性别</text>
        <view class="right-content">
          <picker @change="bindGenderChange" :value="genderIndex" :range="genderArray">
            <view class="picker-text">
              {{ genderArray[genderIndex] }}
            </view>
          </picker>
          <text class="arrow">></text>
        </view>
      </view>

      <view class="info-item">
        <text class="label">联系方式</text>
        <view class="right-content">
          <input class="input-text" type="number" v-model="userInfo.mobile" placeholder="请输入手机号" />
        </view>
      </view>

    <view class="info-item readonly" v-if="userInfo.deptName">
      <text class="label">部门名称</text>
      <view class="right-content">
        <text class="value-text">{{ userInfo.deptName }}</text>
      </view>
    </view>

      <view class="info-item readonly">
        <text class="label">账号状态</text>
        <view class="right-content">
          <text class="value-text" :class="{'status-normal': userInfo.status === 1, 'status-disabled': userInfo.status === 0}">
            {{ userInfo.status === 1 ? '正常' : '禁用' }}
          </text>
        </view>
      </view>
    </view>

    <button class="save-btn" @click="saveProfile">保存修改</button>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app'; 
import { request } from '@/utils/request.js'; 

// 初始化数据结构
const userInfo = ref({
  username: '',
  nickname: '',
  gender: 0, 
  deptName: '',
  avatar: '', 
  mobile: '',
  status: 1
});

// 性别选择器逻辑
const genderArray = ['保密', '男', '女'];
const genderIndex = computed(() => {
  return userInfo.value.gender || 0; 
});

const bindGenderChange = (e) => {
  userInfo.value.gender = parseInt(e.detail.value);
};

// 页面加载自动获取真实数据
onLoad(() => {
  fetchUserProfile();
});

const fetchUserProfile = async () => {
  try {
    const res = await request({
      url: '/api/v1/users/profile',
      method: 'GET'
    });
    if (res && res.data) {
		console.log('后端返回的个人资料真实数据：', res.data);
      userInfo.value = res.data;
	  if (userInfo.value.status === undefined || userInfo.value.status === null) {
	          userInfo.value.status = 1;
	        }
      // 💡 核心修复 2：防止后端万一返回的 avatar 字段类型异常（如 null 或 0 ），强行标准化为字符串
      if (typeof userInfo.value.avatar !== 'string') {
        userInfo.value.avatar = '';
      }
    }
  } catch (err) {
    console.error('拉取个人资料失败:', err);
  }
};

// 修改头像
const changeAvatar = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      if (res.tempFilePaths && res.tempFilePaths.length > 0) {
        // ✅ 正确做法：直接取数组的第一个元素 res.tempFilePaths
        userInfo.value.avatar = res.tempFilePaths;
      }
    },
    fail: (err) => {
      console.error('选择图片失败:', err);
    }
  });
};

// 提交修改到后端
const saveProfile = async () => {
  console.log('准备提交给后端的真实数据:', userInfo.value);
  uni.showLoading({ title: '保存中...' });
  
  try {
    const res = await request({
      url: '/api/v1/users/profile',
      method: 'PUT',
      data: userInfo.value 
    });
    
    uni.hideLoading();
	uni.setStorageSync('userInfo', userInfo.value);
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    });
    
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
    
  } catch (err) {
    uni.hideLoading();
    console.error('保存修改失败:', err);
  }
};
</script>

<style lang="scss">
page {
  background-color: #f5f6f8;
}

.info-container {
  padding: 15px;
}

.info-group {
  background-color: #fff;
  border-radius: 12px;
  padding: 0 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 54px;
  border-bottom: 1px solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.readonly {
    background-color: #fafafa;
    margin: 0 -15px;
    padding: 0 15px;
  }

  .label {
    font-size: 15px;
    color: #333;
    width: 80px;
  }

  .right-content {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    
    .input-text, .picker-text, .value-text {
      flex: 1;
      text-align: right;
      font-size: 15px;
      color: #666;
    }
    
    .value-text {
      color: #999;
    }
    
    .status-normal {
      color: #52c41a;
    }
    
    .status-disabled {
      color: #ff4d4f;
    }

    .arrow {
      color: #ccc;
      margin-left: 8px;
      font-family: consolas;
    }
  }
}

.avatar-item {
  padding: 10px 0;
  
  .avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background-color: #f0f0f0;
  }
}

.save-btn {
  margin-top: 40px;
  background-color: #245381; 
  color: #fff;
  border-radius: 25px;
  font-size: 16px;
  
  &::after {
    border: none;
  }
  
  &:active {
    opacity: 0.9;
  }
}
</style>