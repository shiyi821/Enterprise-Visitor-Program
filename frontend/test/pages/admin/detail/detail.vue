<template>
	<view class="detail-container" v-if="detailData">
		<view class="status-header" :class="'bg-status-' + detailData.status">
			<view class="status-title">{{ getStatusText(detailData.status) }}</view>
			<view class="status-sub">申请提交时间：{{ detailData.createTime || '2023-11-20 09:00:00' }}</view>
		</view>

		<view class="content-body">
			<view class="info-group">
				<view class="group-title">
					<view class="line"></view>访问信息
				</view>
				<view class="info-row"><text class="label">预约日期：</text><text
						class="value">{{ detailData.visitDate }}</text></view>
				<view class="info-row"><text class="label">预约时间：</text><text
						class="value">{{ detailData.visitTime }}</text></view>
				<view class="info-row"><text class="label">到访单位：</text><text
						class="value">{{ detailData.visitDepartment }}</text></view>
				<view class="info-row"><text class="label">被 访 人：</text><text
						class="value highlight">{{ detailData.hostName }}</text></view>
				<view class="info-row"><text class="label">来访事由：</text><text
						class="value">{{ detailData.reason }}</text></view>
			</view>

			<view class="info-group">
				<view class="group-title">
					<view class="line"></view>主申请人信息
				</view>
				<view class="info-row"><text class="label">访客姓名：</text><text
						class="value">{{ detailData.applicantName }}</text></view>
				<view class="info-row"><text class="label">联系电话：</text><text
						class="value">{{ detailData.applicantPhone }}</text></view>
				<view class="info-row"><text class="label">身份证号：</text><text
						class="value">{{ detailData.applicantIdCard }}</text></view>
				<view class="info-row"><text class="label">访客性别：</text><text
						class="value">{{ detailData.applicantGender }}</text></view>
				<view class="info-row"><text class="label">所属单位：</text><text
						class="value">{{ detailData.applicantCompany }}</text></view>
			</view>

			<view class="info-group" v-if="detailData.visitorCount > 1">
				<view class="group-title">
					<view class="line"></view>同行人员信息
				</view>
				<view class="info-row"><text class="label">总来访人数：</text><text
						class="value text-red">{{ detailData.visitorCount }} 人</text></view>
				<view class="companion-box">
					<text class="companion-label">同行人名单：</text>
					<view class="companion-list">
						<text class="companion-tag" v-for="(name, index) in getCompanions(detailData.companions)"
							:key="index">
							{{ name || '未填姓名' }}
						</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import {
		onLoad
	} from '@dcloudio/uni-app';

	const detailData = ref(null);

	onLoad((options) => {
		const id = options.id;
		if (id) {
			loadDetail(id);
		}
	});

	const getStatusText = (status) => {
		const map = {
			0: '待被访人审核',
			1: '待管理员审核',
			2: '被访人已拒绝',
			3: '审核通过 (待到访)',
			4: '已到访完成'
		};
		return map[status] || '未知状态';
	};

	// 解析同行人名单字符串变为数组
	const getCompanions = (str) => {
		if (!str) return [];
		return str.split(',');
	};

	// 模拟根据 ID 从后端获取详细数据
	const loadDetail = (id) => {
		uni.showLoading({
			title: '加载中...'
		});

		setTimeout(() => {
			// 这里模拟后端返回的完整详情数据，包含了 apply.vue 填的所有字段
			detailData.value = {
				id: id,
				status: 3, // 假设是已通过状态
				visitDate: '2023-11-20',
				visitTime: '14:00',
				visitDepartment: '技术研发中心',
				hostName: '李总',
				reason: '项目年度总结与明年规划洽谈，预计需要带团队进行2小时的会议。',
				applicantName: '张三',
				applicantPhone: '13800138001',
				applicantIdCard: '500101199001011234',
				applicantGender: '男',
				applicantCompany: '腾讯科技(深圳)有限公司',
				visitorCount: 4, // 总共4个人
				companions: '王大锤,赵小明,孙铁柱', // 3个同行人
				createTime: '2023-11-18 10:30:22'
			};
			uni.hideLoading();
		}, 400);
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
	}

	.bg-status-0,
	.bg-status-1 {
		background: linear-gradient(135deg, #faad14, #d48806);
	}

	.bg-status-2,
	.bg-status-4 {
		background: linear-gradient(135deg, #ff4d4f, #cf1322);
	}

	.bg-status-3 {
		background: linear-gradient(135deg, #52c41a, #389e0d);
	}

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
</style>