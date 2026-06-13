<template>
  <view class="page-wrap">
    <!-- 头部说明+添加按钮区域 -->
    <view class="header-card">
      <view class="text-wrap">
        <text class="main-title">节假日/不可预约日期管理</text>
        <text class="desc">设置后，访客将无法选择这些日期进行预约申请</text>
      </view>
      <!-- 日期选择器绑定添加按钮 -->
      <picker mode="date" @change="handleAddHoliday">
        <view class="add-btn">+ 添加休息日</view>
      </picker>
    </view>

    <!-- 列表区域 -->
    <scroll-view scroll-y class="list-container">
      <!-- 空状态 -->
      <view v-if="sortedHolidays.length === 0" class="empty-box">
        <text class="empty-tip">暂无设置的休息日</text>
      </view>

      <!-- 节假日条目 -->
      <view
        v-for="(item, idx) in sortedHolidays"
        :key="idx"
        class="holiday-item"
      >
        <view class="left">
          <text class="calendar-icon">🗓️</text>
          <text class="date-val">{{ item }}</text>
          <text class="rest-tag">休息日</text>
        </view>
        <view class="remove-btn" @click="handleRemove(item)">移除</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

// 休息日数组
const holidays = ref([]);
const STORAGE_KEY = 'system_holidays';

// 页面加载读取缓存
onMounted(() => {
  const cacheData = uni.getStorageSync(STORAGE_KEY);
  if (Array.isArray(cacheData)) {
    holidays.value = cacheData;
  }
});

// 日期升序排序
const sortedHolidays = computed(() => {
  return [...holidays.value].sort((a, b) => new Date(a) - new Date(b));
});

// 保存本地缓存
const saveStorage = () => {
  uni.setStorageSync(STORAGE_KEY, holidays.value);
};

// 添加休息日
const handleAddHoliday = (e) => {
  const selectDate = e.detail.value;
  // 重复校验
  if (holidays.value.includes(selectDate)) {
    uni.showToast({ title: '该日期已添加', icon: 'none' });
    return;
  }
  holidays.value.push(selectDate);
  saveStorage();
  uni.showToast({ title: '添加成功', icon: 'success' });
};

// 删除休息日
const handleRemove = (dateStr) => {
  uni.showModal({
    title: '确认移除',
    content: `移除后访客可预约${dateStr}，确定操作？`,
    confirmText: '确认移除',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        holidays.value = holidays.value.filter(d => d !== dateStr);
        saveStorage();
        uni.showToast({ title: '已移除' });
      }
    }
  });
};
</script>

<style scoped>
page {
  background: #f5f7fa;
}
.page-wrap {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 头部卡片 */
.header-card {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 36rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
}
.text-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.main-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #222;
  margin-bottom: 10rpx;
}
.desc {
  font-size: 26rpx;
  color: #888;
  line-height: 1.5;
}
.add-btn {
  background: #ff4d4f;
  color: #fff;
  font-size: 28rpx;
  padding: 22rpx 32rpx;
  border-radius: 999rpx;
  white-space: nowrap;
}

/* 列表容器 */
.list-container {
  flex: 1;
  padding: 0 20rpx;
}

/* 空状态 */
.empty-box {
  margin-top: 200rpx;
  text-align: center;
}
.empty-tip {
  font-size: 28rpx;
  color: #aaa;
}

/* 列表项 */
.holiday-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 32rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.calendar-icon {
  font-size: 32rpx;
}
.date-val {
  font-size: 32rpx;
  color: #333;
}
.rest-tag {
  font-size: 22rpx;
  background: #fff1f0;
  color: #ff4d4f;
  border: 1rpx solid #ffa39e;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}
.remove-btn {
  font-size: 26rpx;
  color: #666;
  background: #f5f5f5;
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
}
.remove-btn:active {
  background: #e8e8e8;
}
</style>