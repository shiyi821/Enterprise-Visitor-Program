<template>
	<view class="main">
		<view class="top-bg">
			<view class="page-title">消息中心</view>
			<view class="page-desc">您的预约审批进度与系统通知</view>
		</view>

		<view class="content">
			<view class="empty-box" v-if="messageList.length === 0">
				<image class="empty-img" src="https://img.icons8.com/ios/100/A6B1BB/box-important--v1.png"></image>
				<view class="empty-text">暂无最新消息</view>
			</view>

			<view class="message-list" v-else>
				<view class="msg-card" v-for="item in messageList" :key="item.id" @click="handleMessageClick(item)">
					<view class="card-header">
						<view class="title-wrap">
							<image v-if="item.type === 'success'" class="icon-type" src="https://img.icons8.com/ios-filled/50/2ECC71/ok--v1.png" />
							<image v-else-if="item.type === 'reject'" class="icon-type" src="https://img.icons8.com/ios-filled/50/E74C3C/cancel.png" />
							<image v-else class="icon-type" src="https://img.icons8.com/ios-filled/50/3B5BDB/bell.png" />
							
							<text class="title" :class="{'is-read-title': item.isRead}">{{ item.title }}</text>
						</view>
						<view class="date">{{ item.time }}</view>
					</view>
					
					<view class="unread-dot" v-if="!item.isRead"></view>

					<view class="card-body" :class="{'is-read-body': item.isRead}">
						{{ item.content }}
					</view>

					<view class="card-footer">
						<text class="footer-text">查看详情</text>
						<text class="arrow">&gt;</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';

const messageList = ref([
	{
		id: 1,
		type: 'success',
		title: '访客预约审批通过',
		time: '刚刚',
		content: '您提交的拜访申请（被访人：李经理，技术部）已通过审批。请在到达门岗时出示您的专属通行二维码进行核验放行。',
		isRead: false
	},
	{
		id: 2,
		type: 'reject',
		title: '访客预约被驳回',
		time: '昨天 14:30',
		content: '抱歉，您提交的拜访申请被驳回。驳回原因：被访人由于紧急会议今日不在园区，请修改来访时间后重新提交申请。',
		isRead: false
	},
	{
		id: 3,
		type: 'info',
		title: '系统维护通知',
		time: '2026-06-10',
		content: '尊敬的用户，系统将于本周日凌晨2:00-4:00进行服务器升级，期间可能会出现预约数据延迟同步的情况，敬请谅解。',
		isRead: true
	}
]);

const handleMessageClick = (item) => {
	if (!item.isRead) {
		item.isRead = true;
	}
	
	uni.navigateTo({
		url: `/pages/notice/detail?id=${item.id}`
	});
};
</script>

<style scoped>
:global(page) {
	background-color: #F5F7FA;
}
:global(page::-webkit-scrollbar), ::-webkit-scrollbar {
	display: none !important;
	width: 0 !important;
	height: 0 !important;
	color: transparent !important;
	background: transparent !important;
}

.main {
	min-height: 100vh;
	background-color: #F5F7FA;
	padding-bottom: 60rpx;
}

.top-bg {
	width: 100%;
	height: 280rpx;
	background-color: #245381;
	padding: 60rpx 40rpx 0;
	box-sizing: border-box;
}

.page-title {
	font-size: 44rpx;
	font-weight: bold;
	color: #FFFFFF;
	margin-bottom: 15rpx;
}

.page-desc {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.8);
}

.content {
	padding: 0 30rpx;
	margin-top: -80rpx;
	position: relative;
	z-index: 2;
}

.empty-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 150rpx;
}
.empty-img {
	width: 120rpx;
	height: 120rpx;
	margin-bottom: 20rpx;
	opacity: 0.5;
}
.empty-text {
	font-size: 28rpx;
	color: #95A5A6;
}

.message-list {
	width: 100%;
}

.msg-card {
	background-color: #FFFFFF;
	border-radius: 24rpx;
	padding: 35rpx 30rpx 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 10rpx 30rpx rgba(149, 157, 165, 0.08);
	position: relative;
	transition: all 0.25s ease;
}

.msg-card:active {
	transform: scale(0.98);
	box-shadow: 0 4rpx 10rpx rgba(149, 157, 165, 0.05);
}

.unread-dot {
	position: absolute;
	top: 35rpx;
	right: 30rpx;
	width: 16rpx;
	height: 16rpx;
	background-color: #E74C3C;
	border-radius: 50%;
	box-shadow: 0 4rpx 8rpx rgba(231, 76, 60, 0.4);
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 20rpx;
}

.title-wrap {
	display: flex;
	align-items: center;
	flex: 1;
	padding-right: 40rpx; 
}

.icon-type {
	width: 36rpx;
	height: 36rpx;
	margin-right: 15rpx;
	flex-shrink: 0;
}

.title {
	font-size: 32rpx;
	font-weight: 600;
	color: #2C3E50;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	transition: color 0.3s;
}

.is-read-title {
	color: #7F8C8D;
	font-weight: 500;
}

.date {
	font-size: 24rpx;
	color: #A6B1BB;
	flex-shrink: 0;
	margin-top: 6rpx;
}

.card-body {
	font-size: 28rpx;
	color: #34495E;
	line-height: 1.6;
	margin-bottom: 25rpx;
	transition: color 0.3s;
}

.is-read-body {
	color: #95A5A6;
}

.card-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	border-top: 1px solid #F1F2F6;
	padding-top: 20rpx;
}

.footer-text {
	font-size: 26rpx;
	color: #3B5BDB;
	font-weight: 500;
}

.arrow {
	color: #3B5BDB;
	font-size: 28rpx;
	font-family: Consolas, monospace;
}
</style>