<template>
  <view class="info-container">
    <view class="info-group">
      <view class="info-item avatar-item" @click="changeAvatar">
        <text class="label">头像</text>
        <view class="right-content">
          <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
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

      <view class="info-item readonly">
        <text class="label">部门名称</text>
        <view class="right-content">
          <text class="value-text">{{ userInfo.deptName || '暂无部门' }}</text>
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

// 模拟从后端获取的数据，结构对应你提供的Java实体类
const userInfo = ref({
  username: 'ZhangSan',
  nickname: '张三',
  gender: 1, // 1-男 2-女 0-保密
  deptName: '技术研发部', // 原Java字段为Long，这里假设前端展示的是解析后的字符串
  avatar: '', 
  mobile: '13800138000',
  status: 1  // 1-正常 0-禁用
});

// 性别选择器逻辑
const genderArray = ['保密', '男', '女'];
const genderIndex = computed(() => {
  // 映射关系：0-保密(index 0), 1-男(index 1), 2-女(index 2)
  return userInfo.value.gender; 
});

const bindGenderChange = (e) => {
  userInfo.value.gender = parseInt(e.detail.value);
};

// 修改头像
const changeAvatar = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      userInfo.value.avatar = res.tempFilePaths[0];
      // 此处应调用上传图片的接口
    }
  });
};

// 提交保存
const saveProfile = () => {
  console.log('准备提交的数据:', userInfo.value);
  uni.showLoading({ title: '保存中...' });
  
  // 模拟请求延迟
  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    });
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  }, 800);
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
  background-color: #245381; /* 统一色号 */
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