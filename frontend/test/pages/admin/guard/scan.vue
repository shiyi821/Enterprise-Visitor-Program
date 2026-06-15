<template>
  <view class="container">
    <view class="scan-header">
      <image class="scan-icon" src="/static/images/scan-icon.png" mode="aspectFit"></image>
      <button class="btn-scan" @click="handleScan">点击扫码核验</button>
      <text class="tips">请扫描访客手机上的核验二维码</text>
    </view>

    <view class="result-area" v-if="visitorInfo">
      <view class="info-card">
        <view class="card-title">访客信息核对</view>
        <view class="info-item">
          <text class="label">访客姓名：</text>
          <text class="value highlight">{{ visitorInfo.applicantName }}</text>
        </view>
        <view class="info-item">
          <text class="label">联系电话：</text>
          <text class="value">{{ visitorInfo.applicantPhone }}</text>
        </view>
        <view class="info-item">
          <text class="label">来访单位：</text>
          <text class="value">{{ visitorInfo.visitorCompany || '个人' }}</text>
        </view>
        <view class="info-item">
          <text class="label">来访人数：</text>
          <text class="value">{{ visitorInfo.visitorCount }} 人</text>
        </view>
        <view class="info-item">
          <text class="label">被访人：</text>
          <text class="value">{{ visitorInfo.visitedPersonName }} ({{ visitorInfo.deptName }})</text>
        </view>
        
        <view class="status-warning" v-if="!canPass">
          <text class="error-text">⚠️ 该单据当前状态不可放行：{{ getStatusReason() }}</text>
        </view>
      </view>

      <button 
        class="btn-pass" 
        :class="{ 'disabled': !canPass }" 
        :disabled="!canPass"
        @click="confirmPass"
      >
        确认信息无误，放行
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { request } from '@/utils/request.js';

const visitorInfo = ref(null);

// 判断当前单据是否允许放行 (必须是待来访状态)
const canPass = computed(() => {
  if (!visitorInfo.value) return false;
  return visitorInfo.value.visitedPersonApprovalStatus === 1 && 
         visitorInfo.value.adminApprovalStatus === 1 && 
         visitorInfo.value.applicationStatus === 0;
});

// 解释为什么不能放行
const getStatusReason = () => {
  const info = visitorInfo.value;
  if (!info) return '';
  if (info.applicationStatus === 1) return '该访客已来访，请勿重复核验';
  if (info.applicationStatus === 2) return '该申请已被拒绝';
  if (info.visitedPersonApprovalStatus === 0 || info.adminApprovalStatus === 0) return '审批流程尚未全部完成';
  return '状态异常';
};

// 调起摄像头扫码
const handleScan = () => {
  uni.scanCode({
    onlyFromCamera: true, // 只允许拍照，不允许相册选图防止作弊
    success: (res) => {
      console.log('扫码结果：', res.result);
      const id = res.result; // 二维码里存的就是ID
      if (id) {
        fetchVisitorDetail(id);
      } else {
        uni.showToast({ title: '无法识别的二维码', icon: 'none' });
      }
    },
    fail: (err) => {
      console.error('扫码取消或失败', err);
    }
  });
};

// 根据扫出的ID查询详情
const fetchVisitorDetail = async (id) => {
  try {
    uni.showLoading({ title: '核验中...' });
    const res = await request({
      url: `/api/v1/visitor-applications/${id}/detail`,
      method: 'GET'
    });
    
    if (res && (res.data || res.id)) {
      visitorInfo.value = res.data || res;
      uni.showToast({ title: '获取成功', icon: 'success' });
    } else {
      uni.showToast({ title: '未找到该申请单', icon: 'error' });
      visitorInfo.value = null;
    }
  } catch (error) {
    uni.showToast({ title: '网络或服务异常', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
};

// 确认放行提交后端
const confirmPass = async () => {
  if (!canPass.value) return; //
  
  uni.showModal({
    title: '放行确认',
    content: `确认放行访客【${visitorInfo.value.applicantName}】吗？`, //
    success: async (res) => {
      if (res.confirm) { //
        try {
          uni.showLoading({ title: '处理中...' }); //
          await request({
            url: `/api/v1/visitor-applications/${visitorInfo.value.id}/pass`, //
            method: 'PUT' //
          });
          
          uni.showToast({ 
            title: '放行成功', 
            icon: 'success',
            duration: 2000
          });
          
          // ====== 核心改动：删掉原本的 fetchVisitorDetail 行 ======
          // 直接将数据清空，这样下面的信息核对卡片会立刻隐藏，页面干净地等待扫下一个人
          visitorInfo.value = null;
          
        } catch (error) {
          // request.js 里有报错提示
        } finally {
          uni.hideLoading(); //
        }
      }
    }
  });
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f6f8;
  padding: 30rpx;
}
.scan-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #fff;
  padding: 60rpx 0;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
}
.scan-icon {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 30rpx;
}
.btn-scan {
  background-color: #3B5BDB;
  color: #fff;
  width: 80%;
  border-radius: 100rpx;
  font-size: 32rpx;
}
.tips {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #888;
}
.result-area {
  margin-top: 30rpx;
}
.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}
.card-title {
  font-size: 30rpx;
  font-weight: bold;
  border-left: 8rpx solid #3B5BDB;
  padding-left: 16rpx;
  margin-bottom: 30rpx;
}
.info-item {
  display: flex;
  margin-bottom: 20rpx;
  font-size: 28rpx;
}
.info-item .label {
  color: #888;
  width: 150rpx;
}
.info-item .value {
  color: #333;
  flex: 1;
}
.highlight {
  font-size: 32rpx;
  font-weight: bold;
  color: #1890ff !important;
}
.status-warning {
  margin-top: 20rpx;
  padding: 16rpx;
  background-color: #fff1f0;
  border-radius: 8rpx;
}
.error-text {
  color: #f5222d;
  font-size: 26rpx;
}
.btn-pass {
  background-color: #52c41a;
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
}
.btn-pass.disabled {
  background-color: #ccc;
  color: #fff;
}
</style>