<template>
	<view class="main">
		<view class="top">
			<image mode="widthFix" src="/static/images/home2.jpg" class="loading" />
			<view class="top-mask"></view>
		</view>

		<view class="box">
			<view class="inner">

				<view class="item" @click="goTo('/pages/notice/notice')">
					<view class="left icon-blue">
						<image mode="aspectFill" src="/static/images/home/news.png" />
					</view> 
					<view class="right">
						<view class="title">通知公告</view>
						<view class="desc">预约规则，来访须知及最新政策</view>
					</view>
					<view class="arrow-right">></view>
				</view>

				<view class="item" @click="goTo('/pages/apply/apply')">
					<view class="left icon-orange">
						<image mode="aspectFill" src="/static/images/home/add.png" />
					</view> 
					<view class="right">
						<view class="title">申请来访</view>
						<view class="desc">提交来访日期时间与个人资料</view>
					</view>
					<view class="arrow-right">></view>
				</view>

				<view class="item" @click="goTo('/pages/record/record')">
					<view class="left icon-green">
						<image mode="aspectFill" src="/static/images/home/list.png" />
					</view> 
					<view class="right">
						<view class="title">我的申请</view>
						<view class="desc">随时追踪申请记录和审批进度</view>
					</view>
					<view class="arrow-right">></view>
				</view>

				<view class="item" @click="goTo('/pages/my/my')">
					<view class="left icon-purple">
						<image mode="aspectFill" src="/static/images/home/my.png" />
					</view> 
					<view class="right">
						<view class="title">个人中心</view>
						<view class="desc">管理个人身份信息与账号变更</view>
					</view>
					<view class="arrow-right">></view>
				</view>

			</view>
		</view>
	</view>
</template>

<script setup>
// 点击卡片跳转到对应的 TabBar 页面
const goTo = (url) => {
	uni.switchTab({
		url: url
	});
};
</script>

<style scoped>
:global(page::-webkit-scrollbar), ::-webkit-scrollbar {
	display: none !important;
	width: 0 !important;
	height: 0 !important;
	color: transparent !important;
	background: transparent !important;
}

:global(page) {
	background-color: #F5F7FA;
}

.main {
	padding: 0;
	background-color: #F5F7FA;
	min-height: 100vh;
}

/* 顶部区域 */
.top {
	width: 100%;
	position: relative;
}

.top image {
	width: 100%;
	display: block; 
}


.top-mask {
	position: absolute;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 150rpx;
	background: linear-gradient(to bottom, rgba(245, 247, 250, 0), rgba(245, 247, 250, 1));
}

/* 菜单盒子 */
.box {
	width: 100%;
	padding: 50rpx 40rpx 20rpx;
	margin-top: -100rpx;
	position: relative;
	background-color: #F5F7FA;
	margin-bottom: 80rpx;
	box-sizing: border-box;
	z-index: 2;
}

.box .inner {
	width: 100%;
	display: flex;
	flex-direction: column;
}

/* 现代感高级悬浮卡片 */
.box .inner .item {
	width: 100%;
	margin-bottom: 35rpx;
	min-height: 200rpx;
	background-color: #ffffff;
	border-radius: 32rpx; 
	display: flex;
	align-items: center;
	padding: 0 40rpx;
	box-sizing: border-box;
	/* 轻盈弥散阴影 */
	box-shadow: 0 10rpx 30rpx rgba(149, 157, 165, 0.08); 
	transition: all 0.25s ease;
}

/* 按压回弹效果 */
.box .inner .item:active {
	transform: scale(0.97);
	box-shadow: 0 4rpx 10rpx rgba(149, 157, 165, 0.05);
}

/* 图标圆角矩形底座 */
.box .inner .item .left {
	width: 110rpx;
	height: 110rpx;
	border-radius: 28rpx; 
	display: flex;
	justify-content: center;
	align-items: center;
	box-shadow: inset 0 0 0 1px rgba(255,255,255,0.5); 
}

/* 四种不同色系的马卡龙渐变底色 */
.icon-blue   { background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%); }
.icon-orange { background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%); }
.icon-green  { background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%); }
.icon-purple { background: linear-gradient(135deg, #F3E5F5 0%, #E1BEE7 100%); }

.box .inner .item .left image {
	width: 60rpx;
	height: 60rpx;
}

/* 文字排版 */
.box .inner .item .right {
	flex: 1;
	display: flex;
	flex-direction: column;
	margin-left: 35rpx;
}

.box .inner .item .right .title {
	font-size: 34rpx; 
	font-weight: 600;
	color: #2C3E50;
	margin-bottom: 12rpx;
}

.box .inner .item .right .desc {
	font-size: 24rpx;
	color: #95A5A6;
	line-height: 1.4;
}

/* 右侧引导箭头 */
.arrow-right {
	font-size: 32rpx;
	color: #D1D8E0;
	font-weight: 300;
	margin-left: 10rpx;
}
</style>