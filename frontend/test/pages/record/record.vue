<template>
  <view class="container">
    <view class="tabs-main">
      <view 
        v-for="tab in mainTabs" 
        :key="tab.id" 
        class="tab-main-item"
        :class="{ active: currentMainTab === tab.id }"
        @click="switchMainTab(tab.id)"
      >
        <text class="tab-text">{{ tab.name }}</text>
        <view class="tab-line" v-if="currentMainTab === tab.id"></view>
      </view>
    </view>

    <view class="tabs-sub" v-if="currentMainTab === 'pending'">
      <view 
        v-for="subTab in subTabs" 
        :key="subTab.id" 
        class="tab-sub-item"
        :class="{ active: currentSubTab === subTab.id }"
        @click="switchSubTab(subTab.id)"
      >
        {{ subTab.name }}
      </view>
    </view>

    <scroll-view class="list-container">
      <view v-if="recordList.length > 0">
        <view v-for="item in recordList" :key="item.id" class="record-card" @click="navToDetail(item.id)">
          <view class="card-header">
            <view class="visitor-info">
              <text class="name">{{ item.applicantName || '未填写' }}</text>
              <text class="phone">{{ item.applicantPhone || '' }}</text>
            </view>
            <view class="status-tag" :class="getStatusClass(item)">
              {{ getStatusText(item) }}
            </view>
          </view>

          <view class="card-body">
            <view class="info-row">
              <text class="label">来访单位：</text>
              <text class="value">{{ item.visitorCompany || '无' }}</text>
            </view>
            <view class="info-row">
              <text class="label">来访时间：</text>
              <text class="value highlight">{{ formatDateTime(item.visitDate, item.visitTime) }}</text>
            </view>
            <view class="info-row">
              <text class="label">来访人数：</text>
              <text class="value">{{ item.visitorCount || 1 }} 人</text>
            </view>
            <view class="info-row">
              <text class="label">来访事由：</text>
              <text class="value">{{ item.visitPurpose || '事由未填写' }}</text>
            </view>
            <view class="info-row" v-if="item.deptName">
              <text class="label">到访部门：</text>
              <text class="value">{{ item.deptName }}</text>
            </view>
          </view>
        </view>

        <view class="load-more-text">
          {{ hasMore ? '正在加载更多数据...' : '— 已经到底啦 —' }}
        </view>
      </view>

      <view class="empty-wrapper" v-else>
        <image class="empty-img" src="/static/images/empty.png" mode="aspectFit" v-if="hasEmptyImg"></image>
        <text class="empty-text">暂无相关申请记录</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
// 💡 核心修复：引入你们项目自带的 request.js，它会自动带上 Token 并处理报错！
import { request } from '@/utils/request.js';

// 1. 页签数据定义
const mainTabs = [
  { id: 'pending', name: '待审批' },
  { id: 'approved', name: '待来访' },
  { id: 'visited', name: '已来访' },
  { id: 'rejected', name: '已拒绝' }
];

const subTabs = [
  { id: 'visited_person', name: '被访人审批' },
  { id: 'admin', name: '管理员审批' }
];

// 2. 状态变量
const currentMainTab = ref('pending');
const currentSubTab = ref('visited_person');
const recordList = ref([]);
const hasEmptyImg = ref(false); // 是否有默认空状态图，如果没有图设为 false 就只显示文字

// 分页与加载控制
const pageNum = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const isRefreshing = ref(false);

// 3. 构建请求后端的状态参数参数
const buildQueryParams = () => {
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value
  };

  // 根据当前选择的各种标签组合映射查询字段
  if (currentMainTab.value === 'pending') {
    params.applicationStatus = 0;
    if (currentSubTab.value === 'visited_person') {
      params.visitedPersonApprovalStatus = 0;
      params.adminApprovalStatus = 0;
    } else if (currentSubTab.value === 'admin') {
      params.visitedPersonApprovalStatus = 1;
      params.adminApprovalStatus = 0;
    }
  } else if (currentMainTab.value === 'approved') {
    params.visitedPersonApprovalStatus = 1;
    params.adminApprovalStatus = 1;
    params.applicationStatus = 0;
  } else if (currentMainTab.value === 'visited') {
    params.applicationStatus = 1;
  } else if (currentMainTab.value === 'rejected') {
    params.applicationStatus = 2;
  }

  return params;
};

// 4. 获取数据方法（已修复：使用项目自定义 request）
const fetchRecordList = async (append = false) => {
  try {
    const queryParams = buildQueryParams();
    
    // 发起正式的网络请求
    const res = await request({
      url: '/api/v1/visitor-applications', 
      method: 'GET',
      data: queryParams
    });

    // youlai-boot 封装的 request 成功后通常直接返回数据实体
    // 兼容处理：尝试获取 list 和 total
    if (res) {
      // 有些封装返回 res.data.list，有些直接返回 res.list
      const rows = res.data?.list || res.list || []; 
      const total = res.data?.total || res.total || 0;
      
      if (append) {
        recordList.value = [...recordList.value, ...rows];
      } else {
        recordList.value = rows;
      }
      
      // 判定是否还有更多分页数据
      hasMore.value = recordList.value.length < total;
    }
  } catch (error) {
    console.error('API请求错误:', error);
    // request.js 里通常已经有统一的报错 Toast，这里仅作降级处理
  } finally {
    if (isRefreshing.value) {
      uni.stopPullDownRefresh();
      isRefreshing.value = false;
    }
  }
};

// 5. 切换主页签
const switchMainTab = (tabId) => {
  if (currentMainTab.value === tabId) return;
  currentMainTab.value = tabId;
  // 切回待审批时，默认重置到被访人审批子项
  if (tabId === 'pending') {
    currentSubTab.value = 'visited_person';
  }
  resetAndFetch();
};

// 6. 切换子页签
const switchSubTab = (subTabId) => {
  if (currentSubTab.value === subTabId) return;
  currentSubTab.value = subTabId;
  resetAndFetch();
};

// 重置并重新获取数据
const resetAndFetch = () => {
  pageNum.value = 1;
  hasMore.value = true;
  recordList.value = [];
  fetchRecordList();
};

// 7. 页面生命周期挂载及触底刷新控制
onMounted(() => {
  fetchRecordList();
});

onPullDownRefresh(() => {
  isRefreshing.value = true;
  pageNum.value = 1;
  hasMore.value = true;
  fetchRecordList(false);
});

onReachBottom(() => {
  if (!hasMore.value) return;
  pageNum.value++;
  fetchRecordList(true);
});

// 8. 辅助工具函数：格式化展现卡片状态文本和样式
const getStatusText = (item) => {
  if (item.applicationStatus === 1) return '已来访';
  if (item.applicationStatus === 2) return '已拒绝';
  
  if (item.visitedPersonApprovalStatus === 0) return '待被访人审批';
  if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 0) return '待管理员审批';
  if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 1) return '待来访';
  return '未知状态';
};

const getStatusClass = (item) => {
  if (item.applicationStatus === 1) return 'status-visited';
  if (item.applicationStatus === 2) return 'status-rejected';
  if (item.visitedPersonApprovalStatus === 0) return 'status-pending-user';
  if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 0) return 'status-pending-admin';
  return 'status-approved';
};

const formatDateTime = (date, time) => {
  if (!date) return '时间未定';
  // 拼接年月日和时间点
  return `${date} ${time || ''}`.trim();
};
// 跳转到详情页
const navToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/record/detail?id=${id}`, // 假设你的详情页建在这里
    fail: () => {
      uni.showToast({ title: '详情页路径不存在，请先创建', icon: 'none' });
    }
  });
};
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background-color: #f6f7f9;
  display: flex;
  flex-direction: column;
}

/* 一级大标题导航样式 */
.tabs-main {
  display: flex;
  background-color: #ffffff;
  padding: 0 10px;
  border-bottom: 1rpx solid #eeeeee;
  position: sticky;
  top: 0;
  z-index: 10;
  
  .tab-main-item {
    flex: 1;
    text-align: center;
    padding: 14px 0;
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .tab-text {
      font-size: 15px;
      color: #666666;
      font-weight: 500;
    }
    
    &.active {
      .tab-text {
        color: #1a1a1a;
        font-weight: bold;
        font-size: 16px;
      }
    }
    
    .tab-line {
      position: absolute;
      bottom: 0;
      width: 28px;
      height: 3px;
      background-color: #2979ff;
      border-radius: 2px;
    }
  }
}

/* 二级子标题导航样式 */
.tabs-sub {
  display: flex;
  justify-content: flex-start;
  background-color: #fafafa;
  padding: 10px 15px;
  gap: 12px;
  border-bottom: 1rpx solid #f0f0f0;

  .tab-sub-item {
    padding: 6px 16px;
    font-size: 13px;
    color: #666666;
    background-color: #ffffff;
    border-radius: 100px;
    border: 1rpx solid #e5e5e5;
    transition: all 0.2s ease;
    
    &.active {
      color: #2979ff;
      background-color: rgba(41, 121, 255, 0.1);
      border-color: #2979ff;
      font-weight: 500;
    }
  }
}

/* 列表滚动区域布局 */
.list-container {
  flex: 1;
  padding: 12px;
  box-sizing: border-box;
}

/* 数据记录卡片精美样式 */
.record-card {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1rpx solid #f5f5f5;
    margin-bottom: 12px;
    
    .visitor-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .name {
        font-size: 17px;
        font-weight: bold;
        color: #333333;
      }
      
      .phone {
        font-size: 14px;
        color: #888888;
      }
    }
  }
  
  .card-body {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .info-row {
      display: flex;
      font-size: 14px;
      line-height: 1.4;
      
      .label {
        color: #999999;
        width: 75px;
        flex-shrink: 0;
      }
      
      .value {
        color: #444444;
        flex: 1;
        word-break: break-all;
        
        &.highlight {
          color: #333333;
          font-weight: 500;
        }
      }
    }
  }
}

/* 状态动态背景与文字配色方案 */
.status-tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending-user {
  color: #ff9900;
  background-color: rgba(255, 153, 0, 0.1);
}

.status-pending-admin {
  color: #e54d42;
  background-color: rgba(229, 77, 66, 0.1);
}

.status-approved {
  color: #00c853;
  background-color: rgba(0, 200, 83, 0.1);
}

.status-visited {
  color: #2979ff;
  background-color: rgba(41, 121, 255, 0.1);
}

.status-rejected {
  color: #909399;
  background-color: rgba(144, 147, 153, 0.1);
}

/* 上拉底部加载状态提示样式 */
.load-more-text {
  text-align: center;
  font-size: 13px;
  color: #aaaaaa;
  padding: 15px 0 30px 0;
}

/* 空数据状态样式 */
.empty-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100px;
  
  .empty-img {
    width: 120px;
    height: 120px;
    margin-bottom: 16px;
  }
  
  .empty-text {
    font-size: 14px;
    color: #999999;
  }
}
</style>