<template>
	<view class="container">
		<view class="header-section">
			<view class="user-info" @click="handleUserClick">
				<view class="avatar-wrap">
					<image class="avatar-img"
						:src="isLoggedIn && userInfo.avatar ? userInfo.avatar : '/static/images/my.png'"
						mode="aspectFill"></image>
				</view>

				<view class="info">
					<template v-if="isLoggedIn">
						<text class="name">{{ userInfo.nickname || '未命名用户' }}</text>
						<text class="greeting">欢迎回来~~~</text>
					</template>
					<template v-else>
						<text class="name login-text">马上注册/登录</text>
					</template>
				</view>
			</view>
		</view>

		<view class="menu-card">
			<view class="menu-list">
				<view class="menu-item" @click="navigateTo('/pages/my/myinfo')">
					<view class="item-left">
						<text class="icon">📝</text>
						<text class="item-text">修改我的资料</text>
					</view>
					<text class="arrow">></text>
				</view>

				<view class="menu-item" @click="handleAdminClick">
					<view class="item-left">
						<text class="icon">⚙️</text>
						<text class="item-text">系统后台管理 <text class="sub-text">(点击使用)</text></text>
					</view>
					<text class="arrow">></text>
				</view>

				<view class="menu-item" @click="showAbout">
					<view class="item-left">
						<text class="icon">👥</text>
						<text class="item-text">关于我们</text>
					</view>
					<text class="arrow">></text>
				</view>
			</view>

			<button v-if="isLoggedIn" class="logout-btn" @click="handleLogout">退出登录</button>
		</view>
	</view>
</template>

			<button v-if="isLoggedIn" class="logout-btn" @click="handleLogout">退出登录</button>
		</view>
	</view>
</template>
<script setup>
	import {
		ref
	} from 'vue';
	import {
		onShow
	} from '@dcloudio/uni-app';
	import {
		request
	} from '@/utils/request.js'; // 引入搭档的统一请求工具

	// 登录状态和用户信息
	const isLoggedIn = ref(false);
	const userInfo = ref({
		nickname: '',
		avatar: '',
		role: 'admin' // 默认暂时给 admin 方便测试，真实情况由后端返回
	});

	// 核心：每次页面显示时，立刻去后端获取最新数据
	onShow(async () => {
		const token = uni.getStorageSync('token');
		if (token) {
			isLoggedIn.value = true;

			// 1. 先用本地缓存垫底（防止页面白屏闪烁）
			const localUserInfo = uni.getStorageSync('userInfo');
			if (localUserInfo && typeof localUserInfo === 'object') {
				userInfo.value = localUserInfo;
			}

			// 2. 向后端请求真实资料，覆盖掉过期的缓存
			try {
				const res = await request({
					url: '/api/v1/users/profile',
					method: 'GET'
				});
				if (res && res.data) {
					// 保留一下 role 以防后端没传导致无法进后台测试
					const currentRole = userInfo.value.role;
					userInfo.value = res.data;
					if (!userInfo.value.role) userInfo.value.role = currentRole;

					uni.setStorageSync('userInfo', userInfo.value);
				}
			} catch (err) {
				console.error('主页拉取用户资料失败:', err);
			}

			// 检查是否有新用户的弹窗标志位
			const showRegisterTip = uni.getStorageSync('is_new_user_flag');
			if (showRegisterTip) {
				uni.removeStorageSync('is_new_user_flag');
				uni.showModal({
					title: '完善资料提示',
					content: '检测到您是新注册用户，为了不影响您的访客申请审批，是否现在前往完善个人资料？',
					confirmColor: '#245381',
					success: (res) => {
						if (res.confirm) {
							uni.navigateTo({
								url: '/pages/my/myinfo'
							});
						}
					}
				});
			}
		} else {
			isLoggedIn.value = false;
			userInfo.value = {
				nickname: '',
				avatar: '',
				role: 'visitor'
			};
		}
	});

	const handleUserClick = () => {
		if (!isLoggedIn.value) {
			uni.navigateTo({
				url: '/pages/login/login'
			});
		}
	};

	const handleLogout = () => {
		uni.showModal({
			title: '提示',
			content: '确定要退出登录吗？',
			success: function(res) {
				if (res.confirm) {
					uni.removeStorageSync('token');
					uni.removeStorageSync('userInfo');
					uni.removeStorageSync('is_new_user_flag');

					isLoggedIn.value = false;
					uni.showToast({
						title: '已退出登录',
						icon: 'success'
					});
				}
			}
		});
	};

	// ======================== 跳转后台管理拦截逻辑 ========================
	const handleAdminClick = () => {
		if (!isLoggedIn.value) {
			return uni.showToast({
				title: '请先登录系统',
				icon: 'none'
			});
		}

		// 演示拦截逻辑：如果角色是 visitor 则拒绝
		if (userInfo.value.role === 'visitor') {
			return uni.showModal({
				title: '权限拒绝',
				content: '抱歉，系统后台管理仅对企业内部员工开放，普通访客无权访问。',
				showCancel: false,
				confirmColor: '#E74C3C'
			});
		}

		uni.navigateTo({
			url: '/pages/admin/admin'
		});
	};

	// 通用路由跳转拦截
	const navigateTo = (url) => {
		if (!isLoggedIn.value && url !== '/pages/admin/admin') {
			uni.showToast({
				title: '请先登录',
				icon: 'none'
			});
			return;
		}
		uni.navigateTo({
			url: url,
			fail: () => {
				uni.showToast({
					title: '页面暂未开发',
					icon: 'none'
				});
			}
		});
	};

	const showAbout = () => {
		uni.showModal({
			title: '关于我们',
			content: '这是一个企业级访客预约管理系统。',
			showCancel: false,
			confirmColor: '#245381'
		});
	};
</script>

<style lang="scss" scoped>
	:global(page::-webkit-scrollbar),
	::-webkit-scrollbar {
		display: none !important;
		width: 0 !important;
		height: 0 !important;
		color: transparent !important;
		background: transparent !important;
	}

	:global(page) {
		background-color: #f5f6f8;
	}

	.container {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}

	.header-section {
		background-color: #245381;
		padding: 60px 20px 40px 20px;
		color: white;
	}

	.user-info {
		display: flex;
		align-items: center;
	}

	.avatar-wrap {
		width: 64px;
		height: 64px;
		border-radius: 50%;
		background-color: #fff;
		margin-right: 15px;
		display: flex;
		justify-content: center;
		align-items: center;
		overflow: hidden;
		border: 2px solid rgba(255, 255, 255, 0.3);
	}

	.avatar-img {
		width: 100%;
		height: 100%;
	}

	.info {
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.name {
		font-size: 20px;
		font-weight: bold;
		margin-bottom: 6px;
	}

	.login-text {
		font-size: 18px;
	}

	.greeting {
		font-size: 14px;
		opacity: 0.8;
	}

	.menu-card {
		flex: 1;
		background-color: #fff;
		border-top-left-radius: 24px;
		border-top-right-radius: 24px;
		margin-top: -20px;
		padding: 20px 15px;
		box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
	}

	.menu-list {
		margin-bottom: 30px;
	}

	.menu-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 18px 10px;
		border-bottom: 1px solid #f0f0f0;
	}

	.menu-item:active {
		background-color: #fafafa;
	}

	.item-left {
		display: flex;
		align-items: center;
	}

	.icon {
		margin-right: 12px;
		font-size: 18px;
	}

	.item-text {
		font-size: 16px;
		color: #333;
	}

	.sub-text {
		font-size: 12px;
		color: #999;
		margin-left: 5px;
	}

	.arrow {
		color: #ccc;
		font-size: 16px;
		font-family: consolas;
	}

	.logout-btn {
		margin: 0 20px;
		background-color: #245381;
		color: #fff;
		border-radius: 25px;
		font-size: 16px;
	}

	.logout-btn::after {
		border: none;
	}
</style>