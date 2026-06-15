<template>
	<view class="detail-container" v-if="detail">
		<view class="status-header" :class="getBgClass(detail)">
			<view class="main-status">{{ getStatusText(detail) }}</view>
			<view class="sub-status">申请单号：{{ detail.id }}</view>
		</view>
<view class="info-card qr-card" v-show="detail.visitedPersonApprovalStatus === 1 && detail.adminApprovalStatus === 1 && detail.applicationStatus === 0">
            <view class="qr-title">入校/入厂核验码</view>
            <view class="qr-wrapper">
                <uqrcode ref="uQRCode" canvas-id="visitorQrCode" :value="String(detail.id)" :size="150" />
            </view>
            <view class="qr-tips">请在门岗处出示此二维码进行扫码核验</view>
        </view>
		<view class="info-card">
			<view class="card-title">基础信息</view>
			<view class="info-item">
				<text class="label">访客姓名：</text>
				<text class="value">{{ detail.applicantName }}</text>
			</view>
			<view class="info-item">
				<text class="label">联系电话：</text>
				<text class="value">{{ detail.applicantPhone }}</text>
			</view>
			<view class="info-item">
				<text class="label">来访单位：</text>
				<text class="value">{{ detail.visitorCompany || '个人' }}</text>
			</view>
			<view class="info-item">
				<text class="label">来访人数：</text>
				<text class="value">{{ detail.visitorCount }} 人</text>
			</view>
			<view class="info-item" v-if="detail.companionVisitors">
				<text class="label">同行人员：</text>
				<text class="value">{{ detail.companionVisitors }}</text>
			</view>
		</view>

		<view class="info-card">
			<view class="card-title">来访计划</view>
			<view class="info-item">
				<text class="label">到访部门：</text>
				<text class="value">{{ detail.deptName || '未指定' }}</text>
			</view>
			<view class="info-item">
				<text class="label">被访人员：</text>
				<text class="value">{{ detail.visitedPersonName || '未指定' }}</text>
			</view>
			<view class="info-item">
				<text class="label">预计到访：</text>
				<text class="value highlight">{{ detail.visitDate }} {{ detail.visitTime }}</text>
			</view>
			<view class="info-item block-item">
				<text class="label">来访事由：</text>
				<text class="value box-value">{{ detail.visitPurpose }}</text>
			</view>
		</view>

		<view class="info-card timeline-card">
			<view class="card-title">进度与审批信息</view>
			
			<view class="timeline-item done">
				<view class="dot"></view>
				<view class="content">
					<view class="title">提交申请</view>
					<view class="time">{{ detail.createTime || '未知时间' }}</view>
				</view>
			</view>

			<view class="timeline-item" :class="getTimelineClass(detail.visitedPersonApprovalStatus)">
				<view class="dot"></view>
				<view class="content">
					<view class="title">
						被访人审批 
						<text class="tag" v-if="detail.visitedPersonApprovalStatus !== 0">
							({{ detail.visitedPersonApprovalStatus === 1 ? '已同意' : '已拒绝' }})
						</text>
					</view>
					<view class="desc" v-if="detail.visitedPersonName">审批人：{{ detail.visitedPersonName }}</view>
					<view class="time" v-if="detail.visitedApprovalTime">{{ detail.visitedApprovalTime }}</view>
				</view>
			</view>

			<view class="timeline-item" :class="getTimelineClass(detail.adminApprovalStatus)" v-if="detail.visitedPersonApprovalStatus === 1">
				<view class="dot"></view>
				<view class="content">
					<view class="title">
						管理员审批
						<text class="tag" v-if="detail.adminApprovalStatus !== 0">
							({{ detail.adminApprovalStatus === 1 ? '已同意' : '已拒绝' }})
						</text>
					</view>
					<view class="desc" v-if="detail.adminName">审批人：{{ detail.adminName }}</view>
					<view class="time" v-if="detail.adminApprovalTime">{{ detail.adminApprovalTime }}</view>
				</view>
			</view>

			<view class="timeline-item" :class="detail.applicationStatus === 1 ? 'done' : ''" v-if="detail.visitedPersonApprovalStatus === 1 && detail.adminApprovalStatus === 1">
				<view class="dot"></view>
				<view class="content">
					<view class="title">门岗扫码放行</view>
					<view class="desc" v-if="detail.guardName">核验门岗：{{ detail.guardName }}</view>
					<view class="time" v-if="detail.guardTime">{{ detail.guardTime }}</view>
				</view>
			</view>

		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '@/utils/request.js';

const detail = ref(null);

onLoad((options) => {
	if (options.id) {
		fetchDetail(options.id);
	}
});

// 获取详情数据调用新写的接口
const fetchDetail = async (id) => {
	try {
		uni.showLoading({ title: '加载中...' });
		const res = await request({
			url: `/api/v1/visitor-applications/${id}/detail`,
			method: 'GET'
		});
		// youlai-boot 的 request 通常直接剥除 code 包装
		if (res && res.data) {
			detail.value = res.data;
		} else if (res && res.id) {
			// 兼容不同版本的 request 封装
			detail.value = res;
		}
		uni.hideLoading();
	} catch (error) {
		uni.hideLoading();
		console.error('获取详情失败:', error);
	}
};

// 各种辅助展示的方法
const getStatusText = (item) => {
	if (item.applicationStatus === 2 || item.visitedPersonApprovalStatus === 2 || item.adminApprovalStatus === 2) return '申请已拒绝';
	if (item.applicationStatus === 1) return '已来访 (流程结束)';
	if (item.visitedPersonApprovalStatus === 0) return '等待被访人审批';
	if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 0) return '等待管理员审批';
	if (item.visitedPersonApprovalStatus === 1 && item.adminApprovalStatus === 1) return '审批通过，等待来访';
	return '状态异常';
};

const getBgClass = (item) => {
	if (item.applicationStatus === 2 || item.visitedPersonApprovalStatus === 2 || item.adminApprovalStatus === 2) return 'bg-reject';
	if (item.applicationStatus === 1) return 'bg-done';
	return 'bg-processing';
};

const getTimelineClass = (status) => {
	if (status === 1) return 'done';
	if (status === 2) return 'reject';
	return 'pending'; // 0
};
</script>

<style scoped>
.detail-container {
	min-height: 100vh;
	background-color: #f5f6f8;
	padding-bottom: 40rpx;
}

.status-header {
	padding: 60rpx 40rpx;
	color: #fff;
}

.bg-processing { background: linear-gradient(135deg, #1890ff, #0050b3); }
.bg-done { background: linear-gradient(135deg, #52c41a, #237804); }
.bg-reject { background: linear-gradient(135deg, #f5222d, #a8071a); }

.main-status {
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.sub-status {
	font-size: 24rpx;
	opacity: 0.8;
}

.info-card {
	margin: 20rpx;
	margin-top: -20rpx;
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
	position: relative;
	z-index: 2;
}

.info-card + .info-card {
	margin-top: 20rpx;
}

.card-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	border-left: 8rpx solid #1890ff;
	padding-left: 16rpx;
	margin-bottom: 30rpx;
}

.info-item {
	display: flex;
	margin-bottom: 20rpx;
	font-size: 28rpx;
	line-height: 1.5;
}

.info-item .label {
	color: #888;
	width: 150rpx;
	flex-shrink: 0;
}

.info-item .value {
	color: #333;
	flex: 1;
}

.highlight {
	color: #1890ff !important;
	font-weight: bold;
}

.block-item {
	flex-direction: column;
}

.block-item .label {
	margin-bottom: 10rpx;
}

.box-value {
	background-color: #f9f9f9;
	padding: 16rpx;
	border-radius: 8rpx;
	color: #555;
}

/* 时间线样式 */
.timeline-card {
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

.timeline-item .title {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 6rpx;
}

.timeline-item .tag {
	font-size: 24rpx;
	font-weight: normal;
	margin-left: 10rpx;
}

.timeline-item .desc, .timeline-item .time {
	font-size: 24rpx;
	color: #888;
	margin-top: 4rpx;
}
.qr-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40rpx 0;
}
.qr-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
}
.qr-wrapper {
    width: 150px;
    height: 150px;
    background-color: #f5f5f5; /* 占位色，防止二维码还没生成时难看 */
    display: flex;
    justify-content: center;
    align-items: center;
}
.qr-tips {
    font-size: 24rpx;
    color: #ff9900;
    margin-top: 20rpx;
}
</style>