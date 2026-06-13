<template>
	<view class="audit-container">

		<view class="tabs">
			<view class="tab-item" :class="{ active: currentTab === 'todo' }" @click="currentTab = 'todo'">
				<text>待我审批</text>
				<view class="tab-line" v-if="currentTab === 'todo'"></view>
			</view>
			<view class="tab-item" :class="{ active: currentTab === 'processing' }" @click="currentTab = 'processing'">
				<text>审批中</text>
				<view class="tab-line" v-if="currentTab === 'processing'"></view>
			</view>
			<view class="tab-item" :class="{ active: currentTab === 'done' }" @click="currentTab = 'done'">
				<text>已结束</text>
				<view class="tab-line" v-if="currentTab === 'done'"></view>
			</view>
		</view>

		<scroll-view scroll-y class="list-area">
			<view v-if="filteredList.length === 0" class="empty-box">
				<image src="/static/images/tabbar/task.png" class="empty-icon"></image>
				<text>暂无相关审批记录</text>
			</view>

			<view class="card" v-for="item in filteredList" :key="item.id" @click="navToDetail(item.id)">
				<view class="card-top">
					<text class="time">到访时间：{{ item.visitDate }} {{ item.visitTime }}</text>
					<text class="status-tag" :class="'status-' + item.status">
						{{ getStatusText(item.status) }}
					</text>
				</view>

				<view class="card-content">
					<view class="row"><text class="label">访客姓名：</text><text class="value">{{ item.visitorName }}
							({{ item.phone }})</text></view>
					<view class="row"><text class="label">来访单位：</text><text class="value">{{ item.company }}</text>
					</view>
					<view class="row"><text class="label">被 访 人：</text><text class="value highlight">{{ item.hostName }}
							({{ item.hostDept }})</text></view>
					<view class="row"><text class="label">来访事由：</text><text class="value">{{ item.reason }}</text>
					</view>
				</view>

				<view class="card-bottom" v-if="canAudit(item)">
					<button class="btn btn-reject" @click.stop="doAudit(item.id, 'reject')">拒绝</button>
					<button class="btn btn-pass" @click.stop="doAudit(item.id, 'pass')">同意</button>
				</view>
			</view>
		</scroll-view>

	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onShow
	} from '@dcloudio/uni-app';

	const userRole = ref('');
	const currentHostName = '李总';

	onShow(() => {
		userRole.value = uni.getStorageSync('userRole') || 'host';
	});

	const currentTab = ref('todo');

	const allData = ref([{
			id: 1,
			visitorName: '张三',
			phone: '13800138001',
			company: '腾讯科技',
			hostName: '李总',
			hostDept: '技术部',
			reason: '项目洽谈',
			visitDate: '2023-11-20',
			visitTime: '14:00',
			status: 0
		},
		{
			id: 2,
			visitorName: '王五',
			phone: '13900139002',
			company: '阿里巴巴',
			hostName: '赵总',
			hostDept: '市场部',
			reason: '市场合作',
			visitDate: '2023-11-21',
			visitTime: '10:00',
			status: 0
		},
		{
			id: 3,
			visitorName: '李四',
			phone: '13700137003',
			company: '字节跳动',
			hostName: '李总',
			hostDept: '技术部',
			reason: '面试',
			visitDate: '2023-11-22',
			visitTime: '09:30',
			status: 1
		},
		{
			id: 4,
			visitorName: '赵六',
			phone: '13600136004',
			company: '个人',
			hostName: '李总',
			hostDept: '技术部',
			reason: '送文件',
			visitDate: '2023-11-18',
			visitTime: '16:00',
			status: 3
		},
		{
			id: 5,
			visitorName: '孙七',
			phone: '13500135005',
			company: '推销公司',
			hostName: '李总',
			hostDept: '技术部',
			reason: '推销产品',
			visitDate: '2023-11-19',
			visitTime: '11:00',
			status: 2
		}
	]);

	const getStatusText = (status) => {
		const map = {
			0: '待被访人审批',
			1: '待管理员审批',
			2: '被访人已拒绝',
			3: '审批通过 (待到访)',
			4: '管理员已拒绝'
		};
		return map[status] || '未知状态';
	};

	const canAudit = (item) => {
		if (userRole.value === 'host' && item.status === 0) return true;
		if (userRole.value === 'admin' && item.status === 1) return true;
		return false;
	};

	const filteredList = computed(() => {
		let list = allData.value;

		if (userRole.value === 'host') {
			list = list.filter(item => item.hostName === currentHostName);
		}

		return list.filter(item => {
			if (currentTab.value === 'todo') {
				return userRole.value === 'host' ? item.status === 0 : item.status === 1;
			} else if (currentTab.value === 'processing') {
				return userRole.value === 'host' ? item.status === 1 : item.status === 0;
			} else if (currentTab.value === 'done') {
				return [2, 3, 4].includes(item.status);
			}
			return true;
		});
	});

	const doAudit = (id, action) => {
		const actionText = action === 'pass' ? '同意' : '拒绝';
		uni.showModal({
			title: '审批确认',
			content: `确定要 ${actionText} 该访客的申请吗？`,
			success: (res) => {
				if (res.confirm) {
					const index = allData.value.findIndex(item => item.id === id);
					if (index > -1) {
						const item = allData.value[index];
						if (action === 'pass') {
							item.status = userRole.value === 'host' ? 1 : 3;
						} else {
							item.status = userRole.value === 'host' ? 2 : 4;
						}
						uni.showToast({
							title: '操作成功',
							icon: 'success'
						});
					}
				}
			}
		});
	};

	// 【修改点3】添加跳转到详情页的方法
	const navToDetail = (id) => {
		uni.navigateTo({
			url: `/pages/admin/detail/detail?id=${id}`
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

	.status-0 {
		background-color: #fff7e6;
		color: #fa8c16;
	}

	.status-1 {
		background-color: #e6f7ff;
		color: #1890ff;
	}

	.status-2,
	.status-4 {
		background-color: #fff1f0;
		color: #f5222d;
	}

	.status-3 {
		background-color: #f6ffed;
		color: #52c41a;
	}

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

	.card-content .highlight {
		color: #007aff;
		font-weight: bold;
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
</style>