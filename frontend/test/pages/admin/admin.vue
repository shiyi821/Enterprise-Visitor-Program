<template>
	<view class="admin-container">

		<view class="header-bg">
			<view class="user-info">
				<image class="avatar" src="/static/images/my.png"></image>
				<view class="info-text">
					<text class="title">{{ roleName }}工作台</text>
					<text class="subtitle">欢迎回来{{ currentNickname || '系统运行良好' }}，一切运行良好</text>
				</view>
			</view>
		</view>

		<view class="data-card" v-if="userRole !== 'guard'">
			
			<view class="data-item" v-if="userRole === 'admin'">
				<text class="num text-blue">{{ stats.totalVisitorCount || 0 }}</text>
				<text class="label">累计到访</text>
			</view>
			
			<view class="data-item" v-if="userRole === 'host'">
				<text class="num text-blue">{{ stats.todayVisitorCount || 0 }}</text>
				<text class="label">今日到访</text>
			</view>
			
			<view class="data-item" v-if="userRole === 'admin'">
				<text class="num text-orange">{{ stats.adminPendingCount || 0 }}</text>
				<text class="label">待管理审核</text>
			</view>
			
			<view class="data-item">
				<text class="num text-pink">{{ stats.hostPendingCount || 0 }}</text>
				<text class="label">{{ userRole === 'host' ? '待我审核' : '待员工审核' }}</text>
			</view>
			
			<view class="data-item" v-if="userRole === 'admin'">
				<text class="num text-green">{{ stats.employeeCount || 0 }}</text>
				<text class="label">在职员工</text>
			</view>
		</view>

		<view class="menu-section" v-if="userRole === 'guard'">
			<view class="section-header">
				<text class="section-title">门岗工作区</text>
			</view>
			<view class="grid-box">
				<view class="grid-item" @click="navTo('/pages/admin/guard/scan')">
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
				
				<view class="grid-item" @click="navTo('/pages/admin/stats/index')">
					<view class="icon-wrap bg-yellow">
						<image class="icon" src="/static/images/tabbar/home.png"></image>
					</view>
					<text class="name">数据统计</text>
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
					<text class="name">通知发布</text>
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
	// 引入获取当前登录员工信息的API接口
	import { getCurrentUserInfo } from '@/api/employee.js';
	// 引入刚刚封装的管理台统计API
	import { getDashboardStats } from '@/api/admin.js';

	const userRole = ref('host');
	const currentNickname = ref(''); // 响应式变量，存储当前登录用户的昵称
	
	// 定义响应式的统计数据源
	const stats = ref({
		totalVisitorCount: 0,
		todayVisitorCount: 0,
		adminPendingCount: 0,
		hostPendingCount: 0,
		employeeCount: 0
	});

	// 每次打开此页面，读取判定好的角色，并向后端请求最新数据
	onShow(() => {
		userRole.value = uni.getStorageSync('userRole') || 'host';
		fetchCurrentUserInfo();
		// 如果不是门岗，则请求看板统计数据
		if (userRole.value !== 'guard') {
			fetchDashboardStats();
		}
	});

	// 调用异步接口获取当前人信息并绑定到工作台欢迎语
	const fetchCurrentUserInfo = async () => {
		try {
			const res = await getCurrentUserInfo();
			if (res.code === '00000' && res.data) {
				currentNickname.value = res.data.nickname;
			}
		} catch (error) {
			console.error('获取工作台动态用户信息异常:', error);
		}
	};

	// 拉取看板动态统计数据
	const fetchDashboardStats = async () => {
		try {
			const res = await getDashboardStats();
			if (res.code === '00000' && res.data) {
				stats.value = res.data;
			}
		} catch (error) {
			console.error('获取管理台统计数据异常:', error);
		}
	};

	const roleName = computed(() => {
		if (userRole.value === 'admin') return '管理员';
		if (userRole.value === 'host') return '员工';
		if (userRole.value === 'guard') return '门岗';
		return '工作台';
	});

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
					content: `访客码: ${res.result}\n是否确认放行？`
				});
			}
		});
	};
</script>

<style scoped>
	.admin-container {
		min-height: 100vh;
		background-color: #f5f7fa;
		padding-bottom: 40rpx;
	}

	.header-bg {
		background: linear-gradient(135deg, #007aff 0%, #005bb5 100%);
		padding: 60rpx 40rpx 100rpx 40rpx;
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
	
	/* 新增品红色，用于区分待被访人审核 */
	.text-pink {
		color: #ff2d55;
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