<template>
	<view class="container">
		<view class="user-info">
			<view class="avatar">👤</view>
			<view class="info">
				<text class="name">未登录</text>
				<text class="role">请点击登录查看个人资料</text>
			</view>
		</view>
		
		<view class="menu-list">
			<view class="menu-item">我的预约记录</view>
			<view class="menu-item">审批记录 (被访人)</view>
			<view class="menu-item">退出登录</view>
		</view>
	</view>
</template>

<script setup>
// 基础占位
</script>

<style lang="scss">
.container {
	background-color: #f8f8f8;
	min-height: 100vh;
}
.user-info {
	display: flex;
	align-items: center;
	padding: 30px 20px;
	background-color: #007AFF;
	color: white;
	.avatar {
		font-size: 50px;
		margin-right: 15px;
		background-color: #fff;
		border-radius: 50%;
		width: 70px;
		height: 70px;
		display: flex;
		justify-content: center;
		align-items: center;
	}
	.info {
		display: flex;
		flex-direction: column;
		.name { font-size: 20px; font-weight: bold; margin-bottom: 5px; }
		.role { font-size: 14px; opacity: 0.8; }
	}
}
.menu-list {
	margin-top: 20px;
	background-color: #fff;
	.menu-item {
		padding: 15px 20px;
		border-bottom: 1px solid #f0f0f0;
		font-size: 16px;
	}
}
</style>