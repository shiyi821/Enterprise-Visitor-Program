<template>
	<view class="admin-container">

		<view class="debug-bar">
			<text class="debug-title">当前角色测试：</text>
			<button class="debug-btn" size="mini" @click="changeRole('admin')">管理员</button>
			<button class="debug-btn" size="mini" @click="changeRole('host')">被访人</button>
			<button class="debug-btn" size="mini" @click="changeRole('guard')">门岗安保</button>
		</view>

		<view class="header-bg">
			<view class="user-info">
				<image class="avatar" src="/static/images/my.png"></image>
				<view class="info-text">
					<text class="title">{{ roleName }}工作台</text>
					<text class="subtitle">欢迎回来，系统运行良好</text>
				</view>
			</view>
		</view>

		<view class="data-card" v-if="userRole !== 'guard'">
			<view class="data-item">
				<text class="num text-blue">12</text>
				<text class="label">今日访客</text>
			</view>
			<view class="data-item">
				<text class="num text-orange">5</text>
				<text class="label">待办审批</text>
			</view>
			<view class="data-item" v-if="userRole === 'admin'">
				<text class="num text-green">128</text>
				<text class="label">在职员工</text>
			</view>
		</view>

		<view class="menu-section" v-if="userRole === 'guard'">
			<view class="section-header">
				<text class="section-title">门岗工作区</text>
			</view>
			<view class="grid-box">
				<view class="grid-item" @click="handleScan">
					<view class="icon-wrap bg-blue">
						<image class="icon" src="/static/images/check.png"></image>
					</view>
					<text class="name" style="font-weight:bold; color:#007aff;">扫码核验</text>
				</view>
				<view class="grid-item" @click="navTo('/pages/admin/record/list')">
					<view class="icon-wrap bg-green">
						<image class="icon" src="/static/images/tabbar/enroll.png"></image>
					</view>
					<text class="name">今日到访</text>
				</view>
			</view>
		</view>

		<view class="menu-section" v-if="userRole === 'admin' || userRole === 'host'">
			<view class="section-header">
				<text class="section-title">业务管理</text>
			</view>
			<view class="grid-box">
				<view class="grid-item" @click="navTo('/pages/admin/audit/list')">
					<view class="icon-wrap bg-blue">
						<image class="icon" src="/static/images/tabbar/task.png"></image>
					</view>
					<text class="name">访客审批</text>
				</view>

				<view class="grid-item" v-if="userRole === 'admin'" @click="navTo('/pages/admin/record/list')">
					<view class="icon-wrap bg-green">
						<image class="icon" src="/static/images/tabbar/enroll.png"></image>
					</view>
					<text class="name">访客记录</text>
				</view>

				<view class="grid-item" v-if="userRole === 'host'" @click="navTo('/pages/admin/record/list')">
					<view class="icon-wrap bg-green">
						<image class="icon" src="/static/images/tabbar/enroll.png"></image>
					</view>
					<text class="name">被访记录</text>
				</view>

				<view class="grid-item" v-if="userRole === 'host'" @click="navToAssistApply">
					<view class="icon-wrap bg-orange">
						<image class="icon" src="/static/images/tabbar/add.png"></image>
					</view>
					<text class="name">辅助预约</text>
				</view>

				<view class="grid-item" v-if="userRole === 'admin'" @click="navTo('/pages/admin/holiday/index')">
					<view class="icon-wrap bg-pink">
						<image class="icon" src="/static/images/tabbar/album.png"></image>
					</view>
					<text class="name">节假日设置</text>
				</view>
			</view>
		</view>

		<view class="menu-section" v-if="userRole === 'admin'">
			<view class="section-header">
				<text class="section-title">系统与组织</text>
			</view>
			<view class="grid-box">
				<view class="grid-item" @click="navTo('/pages/admin/employee/list')">
					<view class="icon-wrap bg-purple">
						<image class="icon" src="/static/images/tabbar/my.png"></image>
					</view>
					<text class="name">员工管理</text>
				</view>
				<view class="grid-item" @click="navTo('/pages/admin/dept/list')">
					<view class="icon-wrap bg-cyan">
						<image class="icon" src="/static/images/tabbar/album.png"></image>
					</view>
					<text class="name">部门管理</text>
				</view>
				<view class="grid-item" @click="navTo('/pages/notice/notice')">
					<view class="icon-wrap bg-red">
						<image class="icon" src="/static/images/tabbar/news.png"></image>
					</view>
					<text class="name">通知管理</text>
				</view>
				<view class="grid-item" @click="navTo('/pages/admin/stats/index')">
					<view class="icon-wrap bg-yellow">
						<image class="icon" src="/static/images/tabbar/home.png"></image>
					</view>
					<text class="name">数据统计</text>
				</view>
			</view>
		</view>

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

	const userRole = ref(uni.getStorageSync('userRole') || 'host');

	onShow(() => {
		userRole.value = uni.getStorageSync('userRole') || 'host';
	});

	const roleName = computed(() => {
		if (userRole.value === 'admin') return '管理员';
		if (userRole.value === 'host') return '被访人';
		if (userRole.value === 'guard') return '门岗';
		return '';
	});

	const changeRole = (role) => {
		userRole.value = role;
		uni.setStorageSync('userRole', role);
		uni.showToast({
			title: `已切换为${roleName.value}`,
			icon: 'none'
		});
	};

	const navTo = (url) => {
		if (!url) {
			uni.showToast({
				title: '该功能开发中',
				icon: 'none'
			});
			return;
		}
		uni.navigateTo({
			url: url,
			fail: () => {
				uni.showToast({
					title: '页面尚未创建，开发中...',
					icon: 'none'
				});
			}
		});
	};

	const navToAssistApply = () => {
		uni.setStorageSync('isAssistMode', true);
		uni.switchTab({
			url: '/pages/apply/apply',
			fail: () => {
				uni.navigateTo({
					url: '/pages/apply/apply?mode=assist'
				});
			}
		});
	};

	const handleScan = () => {
		uni.scanCode({
			success: (res) => {
				uni.showModal({
					title: '扫码成功',
					content: `访客预约码: ${res.result}\n是否确认放行？`
				});
			}
		});
	};
</script>

<style scoped>
	/* 样式与原来完全一致，无需变动 */
	.admin-container {
		min-height: 100vh;
		background-color: #f5f7fa;
		padding-bottom: 40rpx;
	}

	.debug-bar {
		background-color: #fff3cd;
		padding: 10rpx 20rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.debug-title {
		font-size: 24rpx;
		color: #856404;
		font-weight: bold;
	}

	.debug-btn {
		margin: 0;
		font-size: 20rpx;
		padding: 0 16rpx;
		background-color: #ffc107;
		color: #333;
	}

	.header-bg {
		background: linear-gradient(135deg, #007aff 0%, #005bb5 100%);
		padding: 40rpx 40rpx 100rpx 40rpx;
		border-bottom-left-radius: 40rpx;
		border-bottom-right-radius: 40rpx;
	}

	.user-info {
		display: flex;
		align-items: center;
	}

	.avatar {
		width: 100rpx;
		height: 100rpx;
		border-radius: 50%;
		border: 4rpx solid rgba(255, 255, 255, 0.4);
		margin-right: 30rpx;
	}

	.info-text {
		display: flex;
		flex-direction: column;
	}

	.title {
		font-size: 36rpx;
		font-weight: bold;
		color: #ffffff;
		margin-bottom: 8rpx;
	}

	.subtitle {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.8);
	}

	.data-card {
		margin: -60rpx 30rpx 30rpx 30rpx;
		background-color: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx 20rpx;
		display: flex;
		justify-content: space-around;
		box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.05);
	}

	.data-item {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.num {
		font-size: 44rpx;
		font-weight: bold;
		margin-bottom: 12rpx;
	}

	.text-blue {
		color: #007aff;
	}

	.text-orange {
		color: #ff9500;
	}

	.text-green {
		color: #34c759;
	}

	.label {
		font-size: 26rpx;
		color: #666;
	}

	.menu-section {
		background-color: #ffffff;
		border-radius: 20rpx;
		margin: 0 30rpx 30rpx 30rpx;
		padding: 30rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.03);
	}

	.section-header {
		margin-bottom: 30rpx;
		border-left: 8rpx solid #007aff;
		padding-left: 16rpx;
	}

	.section-title {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
	}

	.grid-box {
		display: flex;
		flex-wrap: wrap;
	}

	.grid-item {
		width: 25%;
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 30rpx;
	}

	.icon-wrap {
		width: 80rpx;
		height: 80rpx;
		border-radius: 24rpx;
		display: flex;
		justify-content: center;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.bg-blue {
		background-color: rgba(0, 122, 255, 0.1);
	}

	.bg-green {
		background-color: rgba(52, 199, 89, 0.1);
	}

	.bg-orange {
		background-color: rgba(255, 149, 0, 0.1);
	}

	.bg-purple {
		background-color: rgba(175, 82, 222, 0.1);
	}

	.bg-cyan {
		background-color: rgba(50, 173, 230, 0.1);
	}

	.bg-red {
		background-color: rgba(255, 59, 48, 0.1);
	}

	.bg-yellow {
		background-color: rgba(255, 204, 0, 0.1);
	}

	.bg-pink {
		background-color: rgba(255, 45, 85, 0.1);
	}

	.icon {
		width: 44rpx;
		height: 44rpx;
	}

	.name {
		font-size: 24rpx;
		color: #555;
	}
</style>