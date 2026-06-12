<template>
	<view class="container">
		<view class="header">
			<text class="title">欢迎使用企业访客系统</text>
		</view>
		
		<view class="nav-grid">
			<view class="nav-item" @click="goToApply">
				<text class="icon">📝</text>
				<text>我要预约</text>
			</view>
			<view class="nav-item">
				<text class="icon">🔍</text>
				<text>扫码核验</text>
			</view>
			<view class="nav-item">
				<text class="icon">📋</text>
				<text>访客记录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';

const goToApply = () => {
	uni.switchTab({
		url: '/pages/apply/apply'
	});
};
</script>

<style lang="scss">
.container {
	padding: 20px;
}
.header {
	margin-bottom: 30px;
	text-align: center;
	.title {
		font-size: 20px;
		font-weight: bold;
		color: #333;
	}
}
.nav-grid {
	display: flex;
	justify-content: space-around;
	.nav-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 15px;
		background-color: #f5f5f5;
		border-radius: 10px;
		width: 25%;
		.icon {
			font-size: 30px;
			margin-bottom: 10px;
		}
	}
}
</style>