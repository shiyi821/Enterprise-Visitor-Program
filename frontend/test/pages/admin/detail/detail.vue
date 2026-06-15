<template>
	<view class="detail-container" v-if="detailData">
		<view class="status-header" :class="getHeaderClass()">
			<view class="status-title">{{ getStatusText() }}</view>
			<view class="status-sub">申请提交时间：{{ detailData.createTime || '未知时间' }}</view>
		</view>

		<view class="content-body">
			<view class="info-group">
				<view class="group-title">
					<view class="line"></view>访问计划
				</view>
				<view class="info-row"><text class="label">预约日期：</text><text class="value">{{ detailData.visitDate }}</text></view>
				<view class="info-row"><text class="label">预约时间：</text><text class="value highlight">{{ detailData.visitTime }}</text></view>
				<view class="info-row"><text class="label">到访单位：</text><text class="value">{{ detailData.deptName || '未指定' }}</text></view>
				<view class="info-row"><text class="label">被 访 人：</text><text class="value highlight">{{ detailData.visitedPersonName || '未指定' }}</text></view>
				<view class="info-row"><text class="label">来访事由：</text><text class="value">{{ detailData.visitPurpose || '未填写事由' }}</text></view>
			</view>

			<view class="info-group">
				<view class="group-title">
					<view class="line"></view>主申请人信息
				</view>
				<view class="info-row"><text class="label">访客姓名：</text><text class="value">{{ detailData.applicantName }}</text></view>
				<view class="info-row"><text class="label">联系电话：</text><text class="value">{{ detailData.applicantPhone }}</text></view>
				<view class="info-row"><text class="label">所属单位：</text><text class="value">{{ detailData.visitorCompany || '个人/无' }}</text></view>
			</view>

			<view class="info-group" v-if="detailData.visitorCount > 1">
				<view class="group-title">
					<view class="line"></view>同行人员信息
				</view>
				<view class="info-row"><text class="label">总来访人数：</text><text class="value text-red">{{ detailData.visitorCount }} 人</text></view>
				<view class="companion-box" v-if="detailData.companionVisitors">
					<text class="companion-label">同行人名单：</text>
					<view class="companion-list">
						<text class="companion-tag" v-for="(name, index) in getCompanions(detailData.companionVisitors)" :key="index">
							{{ name }}
						</text>
					</view>
				</view>
			</view>

			<view class="info-group timeline-group">
				<view class="group-title">
					<view class="line"></view>审批流转记录
				</view>

				<view class="timeline-item" :class="getTimelineClass(detailData.visitedPersonApprovalStatus)">
					<view class="dot"></view>
					<view class="content">
						<view class="t-title">
							被访人审批
							<text class="tag" v-if="detailData.visitedPersonApprovalStatus !== 0">
								({{ detailData.visitedPersonApprovalStatus === 1 ? '已同意' : '已拒绝' }})
							</text>
						</view>
						<view class="t-desc" v-if="detailData.visitedPersonName">操作人：{{ detailData.visitedPersonName }}</view>
						<view class="t-time" v-if="detailData.visitedApprovalTime">{{ detailData.visitedApprovalTime }}</view>
					</view>
				</view>

				<view class="timeline-item" :class="getTimelineClass(detailData.adminApprovalStatus)" v-if="detailData.visitedPersonApprovalStatus === 1">
					<view class="dot"></view>
					<view class="content">
						<view class="t-title">
							管理员审批
							<text class="tag" v-if="detailData.adminApprovalStatus !== 0">
								({{ detailData.adminApprovalStatus === 1 ? '已同意' : '已拒绝' }})
							</text>
						</view>
						<view class="t-desc" v-if="detailData.adminName">操作人：{{ detailData.adminName }}</view>
						<view class="t-time" v-if="detailData.adminApprovalTime">{{ detailData.adminApprovalTime }}</view>
					</view>
				</view>

				<view class="timeline-item" :class="detailData.applicationStatus === 1 ? 'done' : ''" v-if="detailData.adminApprovalStatus === 1">
					<view class="dot"></view>
					<view class="content">
						<view class="t-title">门岗核验放行</view>
						<view class="t-desc" v-if="detailData.guardName">核验人：{{ detailData.guardName }}</view>
						<view class="t-time" v-if="detailData.guardTime">{{ detailData.guardTime }}</view>
					</view>
				</view>

			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '@/utils/request.js';

const detailData = ref(null);

onLoad((options) => {
	const id = options.id;
	if (id) {
		loadDetail(id);
	}
});

// 解析同行人名单字符串变为数组
const getCompanions = (str) => {
	if (!str) return [];
	return str.split(',').filter(name => name.trim() !== '');
};

// 真正去后端调用详情接口
const loadDetail = async (id) => {
	try {
		uni.showLoading({ title: '加载中...' });
		const res = await request({
			url: `/api/v1/visitor-applications/${id}/detail`,
			method: 'GET'
		});
		
		if (res && res.data) {
			detailData.value = res.data;
		} else if (res && res.id) {
			detailData.value = res;
		}
	} catch (error) {
		console.error('获取详情失败:', error);
		uni.showToast({ title: '获取详情失败', icon: 'none' });
	} finally {
		uni.hideLoading();
	}
};

// 状态判定逻辑
const getStatusText = () => {
	const item = detailData.value;
	if (!item) return '';
	if (item.applicationStatus === 2 || item.visitedPersonApprovalStatus === 2 || item.adminApprovalStatus === 2) return '申请已拒绝';
	if (item.applicationStatus === 1) return '已来访 (流程结束)';
	if (item.visitedPersonApprovalStatus === 0) return '等待被访人审批';
	if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 0) return '等待管理员审批';
	if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 1) return '审批通过，等待来访';
	return '状态异常';
};

// 顶部背景色
const getHeaderClass = () => {
	const item = detailData.value;
	if (!item) return '';
	if (item.applicationStatus === 2 || item.visitedPersonApprovalStatus === 2 || item.adminApprovalStatus === 2) return 'bg-status-reject';
	if (item.applicationStatus === 1) return 'bg-status-done';
	if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 1) return 'bg-status-passed';
	return 'bg-status-todo';
};

// 时间线节点颜色
const getTimelineClass = (status) => {
	if (status === 1) return 'done';
	if (status === 2) return 'reject';
	return 'pending'; // 0
};
</script>

<style scoped>
.detail-container {
	min-height: 100vh;
	background-color: #f5f7fa;
	padding-bottom: 60rpx;
}

/* 顶部状态栏颜色 */
.status-header {
	padding: 50rpx 40rpx;
	color: #fff;
	transition: all 0.3s;
}

.bg-status-todo { background: linear-gradient(135deg, #1890ff, #0050b3); } /* 蓝色: 处理中 */
.bg-status-passed { background: linear-gradient(135deg, #faad14, #d48806); } /* 橙色: 待来访 */
.bg-status-done { background: linear-gradient(135deg, #52c41a, #389e0d); } /* 绿色: 已完成 */
.bg-status-reject { background: linear-gradient(135deg, #ff4d4f, #cf1322); } /* 红色: 拒绝 */

.status-title {
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.status-sub {
	font-size: 24rpx;
	opacity: 0.9;
}

/* 内容区 */
.content-body {
	margin-top: -20rpx;
	padding: 0 30rpx;
	position: relative;
	z-index: 2;
}

.info-group {
	background-color: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.03);
}

.group-title {
	display: flex;
	align-items: center;
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 30rpx;
	border-bottom: 1px solid #f5f5f5;
	padding-bottom: 20rpx;
}

.group-title .line {
	width: 8rpx;
	height: 30rpx;
	background-color: #007aff;
	border-radius: 4rpx;
	margin-right: 16rpx;
}

.info-row {
	display: flex;
	margin-bottom: 20rpx;
	font-size: 28rpx;
	line-height: 1.6;
}

.info-row .label {
	color: #888;
	width: 160rpx;
	flex-shrink: 0;
}

.info-row .value {
	color: #333;
	flex: 1;
	word-break: break-all;
}

.highlight {
	color: #007aff !important;
	font-weight: bold;
}

.text-red {
	color: #ff4d4f !important;
	font-weight: bold;
}

/* 同行人特殊样式 */
.companion-box {
	background-color: #f8f9fb;
	padding: 20rpx;
	border-radius: 12rpx;
	margin-top: 10rpx;
}

.companion-label {
	font-size: 26rpx;
	color: #666;
	margin-bottom: 16rpx;
	display: block;
}

.companion-list {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.companion-tag {
	background-color: #e6f7ff;
	color: #1890ff;
	font-size: 24rpx;
	padding: 8rpx 20rpx;
	border-radius: 30rpx;
	border: 1px solid #91d5ff;
}

/* 时间线样式 */
.timeline-group {
	padding-bottom: 10rpx;
}

.timeline-item {
	position: relative;
	padding-left: 40rpx;
	padding-bottom: 40rpx;
	border-left: 2rpx dashed #ddd;
	margin-left: 20rpx;
}

.timeline-item:last-child {
	border-left: 2rpx solid transparent;
}

.timeline-item .dot {
	position: absolute;
	left: -11rpx;
	top: 6rpx;
	width: 20rpx;
	height: 20rpx;
	border-radius: 50%;
	background-color: #ddd;
}

.timeline-item.done .dot { background-color: #52c41a; box-shadow: 0 0 0 6rpx rgba(82, 196, 26, 0.2); border-left: none;}
.timeline-item.done { border-left-color: #52c41a; border-left-style: solid; }
.timeline-item.reject .dot { background-color: #f5222d; box-shadow: 0 0 0 6rpx rgba(245, 34, 45, 0.2); }

.timeline-item .content {
	position: relative;
	top: -6rpx;
}

.t-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 6rpx;
}

.t-title .tag {
	font-size: 24rpx;
	font-weight: normal;
	margin-left: 10rpx;
}

.t-desc, .t-time {
	font-size: 24rpx;
	color: #888;
	margin-top: 6rpx;
}
</style>