<template>
	<view class="audit-container">
		<view class="tabs">
		    <view 
		        v-for="tab in tabList" 
		        :key="tab.id"
		        class="tab-item" 
		        :class="{ active: currentTab === tab.id }" 
		        @click="switchTab(tab.id)"
		    >
		        <text>{{ tab.name }}</text>
		        <view class="tab-line" v-if="currentTab === tab.id"></view>
		    </view>
		</view>

		<scroll-view scroll-y class="list-area">
			<view v-if="recordList.length === 0" class="empty-box">
				<image src="/static/images/tabbar/task.png" class="empty-icon"></image>
				<text>暂无相关审批记录</text>
			</view>

			<view class="card" v-for="item in recordList" :key="item.id" @click="navToDetail(item.id)">
				<view class="card-top">
					<text class="time">到访时间：{{ item.visitDate }} {{ item.visitTime }}</text>
					<text class="status-tag" :class="getStatusClass(item)">
						{{ getStatusText(item) }}
					</text>
				</view>

				<view class="card-content">
					<view class="row">
						<text class="label">访客姓名：</text>
						<text class="value">{{ item.applicantName || '未填写' }} ({{ item.applicantPhone || '无电话' }})</text>
					</view>
					<view class="row">
						<text class="label">来访单位：</text>
						<text class="value">{{ item.visitorCompany || '个人/无' }}</text>
					</view>
					<view class="row">
						<text class="label">来访事由：</text>
						<text class="value">{{ item.visitPurpose || '事由未填写' }}</text>
					</view>
				</view>

				<view class="card-bottom" v-if="
				    (userRole === 'host' && item.visitedPersonApprovalStatus === 0)
				    || (userRole === 'admin' && item.adminApprovalStatus === 0)
				">
				    <button class="btn btn-reject" @click.stop="doAudit(item.id, 2)">拒绝</button>
				    <button class="btn btn-pass" @click.stop="doAudit(item.id, 1)">同意</button>
				</view>
			</view>
            
			<view class="load-more" v-if="recordList.length > 0">
				{{ hasMore ? '加载中...' : '— 到底啦 —' }}
			</view>
		</scroll-view>
	</view>
</template>
<script setup>
import { ref, computed } from 'vue';
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { request } from '@/utils/request.js';

// ========== 1. 角色与Tab配置 ==========
const userRole = ref('');
onShow(() => {
    userRole.value = uni.getStorageSync('userRole') || 'host';
    resetAndFetch();
});

// 【核心调整1】统一Tab命名逻辑，被访人侧保留「我」的视角，管理员侧保持原有
const tabList = computed(() => {
    if (userRole.value === 'admin') {
        return [
            { id: 'todo', name: '待审批' },
            { id: 'passed', name: '已同意' },
            { id: 'rejected', name: '已拒绝' }
        ];
    }
    // 被访人侧Tab调整：待我审批 / 我已同意 / 已拒绝（和管理员逻辑对齐）
    return [
        { id: 'todo', name: '待我审批' },
        { id: 'passed', name: '我已同意' },
        { id: 'rejected', name: '已拒绝' }
    ];
});

const currentTab = ref('todo');
const recordList = ref([]);
const pageNum = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);

// ========== 2. 切换Tab ==========
const switchTab = (tab) => {
    if (currentTab.value === tab) return;
    currentTab.value = tab;
    resetAndFetch();
};

// 【核心调整2】重构被访人侧的查询参数，对应新Tab的状态逻辑
const buildQueryParams = () => {
    const params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value
    };

    if (userRole.value === 'admin') {
        // 管理员视角（保持原有逻辑）
        if (currentTab.value === 'todo') {
            params.visitedPersonApprovalStatus = 1;
            params.adminApprovalStatus = 0;
        } else if (currentTab.value === 'passed') {
            params.adminApprovalStatus = 1;
        } else if (currentTab.value === 'rejected') {
            params.applicationStatus = 2;
        }
    } else {
        // 被访人视角（新逻辑）
        if (currentTab.value === 'todo') {
            // 待我审批：被访人未审批
            params.visitedPersonApprovalStatus = 0;
        } else if (currentTab.value === 'passed') {
            // 我已同意：被访人已审批通过（不管管理员后续状态）
            params.visitedPersonApprovalStatus = 1;
        } else if (currentTab.value === 'rejected') {
			params.visitedPersonApprovalStatus = 2;
            // 已拒绝：被访人拒绝 OR 管理员拒绝（两种拒绝都展示）
            //params.rejectType = 'all'; // 后端需支持该参数，或直接传以下两个状态
            
        }
    }
    return params;
};

// ========== 3. 拉取列表（保持原有逻辑） ==========
const fetchAuditList = async (append = false) => {
    try {
        const url = userRole.value === 'admin'
            ? '/api/v1/visitor-applications/admin-approval'
            : '/api/v1/visitor-applications/audit';

        const res = await request({
            url: url,
            method: 'GET',
            data: buildQueryParams()
        });

        if (res) {
            const rows = res.data?.list || res.list || [];
            const total = res.data?.total || res.total || 0;
            
            if (append) {
                recordList.value = [...recordList.value, ...rows];
            } else {
                recordList.value = rows;
            }
            hasMore.value = recordList.value.length < total;
        }
    } catch (err) {
        console.error('获取审批列表失败:', err);
    } finally {
        uni.stopPullDownRefresh();
    }
};

// ========== 4. 重置并刷新（保持原有逻辑） ==========
const resetAndFetch = () => {
    pageNum.value = 1;
    hasMore.value = true;
    recordList.value = [];
    fetchAuditList(false);
};

// ========== 5. 审批操作（保持原有逻辑） ==========
const doAudit = (id, actionStatus) => {
    const actionText = actionStatus === 1 ? '同意' : '拒绝';
    uni.showModal({
        title: '审批确认',
        content: `确定要 ${actionText} 该访客的申请吗？`,
        confirmColor: actionStatus === 1 ? '#007aff' : '#f5222d',
        success: async (res) => {
            if (res.confirm) {
                try {
                    uni.showLoading({ title: '处理中...' });
                    
                    const url = userRole.value === 'admin'
                        ? `/api/v1/visitor-applications/${id}/admin-audit?action=${actionStatus}`
                        : `/api/v1/visitor-applications/${id}/audit?action=${actionStatus}`;

                    await request({ url, method: 'PUT' });
                    
                    uni.hideLoading();
                    uni.showToast({ title: '操作成功', icon: 'success', duration: 1500 });
                    setTimeout(() => resetAndFetch(), 500);
                } catch (error) {
                    uni.hideLoading();
                    uni.showToast({ title: '审批失败', icon: 'none' });
                    console.error('审批请求失败:', error);
                }
            }
        }
    });
};

// 【核心调整3】优化状态文本，清晰区分拒绝主体（被访人/管理员）
const getStatusText = (item) => {
    // 优先判断拒绝状态（最易混淆的场景）
    if (item.visitedPersonApprovalStatus === 2) return '我已拒绝'; // 被访人自己拒绝
    if (item.adminApprovalStatus === 2) return '管理员已拒绝'; // 管理员拒绝
    // 已完成（来访结束）
    if (item.applicationStatus === 1) return '已来访';
    // 待审批状态
    if (item.visitedPersonApprovalStatus === 0) return '待我审批';
    if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 0) return '待管理员审批';
    if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 1) return '待来访';
    return '未知状态';
};

// 【微调】状态样式类，拒绝状态统一用reject（区分来源靠文本，样式统一）
const getStatusClass = (item) => {
    if (item.visitedPersonApprovalStatus === 2 || item.adminApprovalStatus === 2) return 'status-reject';
    if (item.applicationStatus === 1) return 'status-done';
    if (item.visitedPersonApprovalStatus === 0 || item.adminApprovalStatus === 0) return 'status-todo';
    return 'status-processing';
};

// 下拉刷新、触底加载、跳转详情（保持原有逻辑）
onReachBottom(() => {
    if (!hasMore.value) return;
    pageNum.value++;
    fetchAuditList(true);
});

onPullDownRefresh(() => {
    resetAndFetch();
});

const navToDetail = (id) => {
    uni.navigateTo({
        url: `/pages/admin/detail/detail?id=${id}`,
        fail: () => uni.showToast({ title: '详情页暂未开发', icon: 'none' })
    });
};
</script>
<style scoped>
	.audit-container {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background-color: #f5f7fa;
	}

	.tabs {
		display: flex;
		background-color: #ffffff;
		padding: 0 20rpx;
		border-bottom: 1rpx solid #eeeeee;
	}

	.tab-item {
		flex: 1;
		text-align: center;
		font-size: 28rpx;
		color: #666;
		padding: 30rpx 0;
		position: relative;
	}

	.tab-item.active {
		color: #007aff;
		font-weight: bold;
	}

	.tab-line {
		position: absolute;
		bottom: 0;
		left: 50%;
		transform: translateX(-50%);
		width: 40rpx;
		height: 6rpx;
		background-color: #007aff;
		border-radius: 4rpx;
	}

	.list-area {
		flex: 1;
		padding: 20rpx;
		box-sizing: border-box;
	}

	.empty-box {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		margin-top: 150rpx;
		color: #999;
		font-size: 28rpx;
	}

	.empty-icon {
		width: 120rpx;
		height: 120rpx;
		margin-bottom: 20rpx;
		opacity: 0.3;
	}

	.card {
		background-color: #ffffff;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.03);
	}

	.card-top {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
		padding-bottom: 20rpx;
		border-bottom: 1rpx dashed #eee;
	}

	.time {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
	}

	.status-tag {
		font-size: 22rpx;
		padding: 4rpx 16rpx;
		border-radius: 8rpx;
		font-weight: bold;
	}

	.status-todo { background-color: #fff7e6; color: #fa8c16; }
	.status-processing { background-color: #e6f7ff; color: #1890ff; }
	.status-reject { background-color: #fff1f0; color: #f5222d; }
	.status-done { background-color: #f6ffed; color: #52c41a; }

	.card-content .row {
		display: flex;
		margin-bottom: 14rpx;
		font-size: 26rpx;
		line-height: 1.5;
	}

	.card-content .label {
		color: #999;
		width: 140rpx;
		flex-shrink: 0;
	}

	.card-content .value {
		color: #333;
		flex: 1;
	}

	.card-bottom {
		display: flex;
		justify-content: flex-end;
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f9f9f9;
	}

	.btn {
		margin: 0 0 0 20rpx;
		padding: 0 40rpx;
		height: 60rpx;
		line-height: 60rpx;
		font-size: 26rpx;
		border-radius: 30rpx;
	}

	.btn-reject {
		background-color: #f5f5f5;
		color: #666;
	}

	.btn-pass {
		background-color: #007aff;
		color: #fff;
	}

	button::after {
		border: none;
	}
	
	.load-more {
		text-align: center;
		padding: 20rpx;
		font-size: 24rpx;
		color: #999;
	}
</style>